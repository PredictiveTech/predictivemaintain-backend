package pe.edu.upc.predictivemaintain.telemetry.infrastructure.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * SP-01 spike. Subscribes to an MQTT topic and forwards every reading to the existing
 * POST /api/v1/sensors/readings endpoint, so validation, thresholds and alerts stay in one place.
 *
 * Topic:   sensors/{sensorId}/readings
 * Payload: {"value": 71.5, "unit": "C", "measuredAt": "2026-11-04T12:00:00Z",
 *           "sourceKey": "optional", "deviceKey": "optional"}
 */
@Component
@EnableConfigurationProperties(MqttIngestProperties.class)
@ConditionalOnProperty(prefix = "app.mqtt", name = "enabled", havingValue = "true")
public class MqttReadingBridge implements MqttCallbackExtended {

    private static final Logger log = LoggerFactory.getLogger(MqttReadingBridge.class);

    private final MqttIngestProperties props;
    private final ObjectMapper mapper;
    private final RestClient http;
    private MqttClient client;

    public MqttReadingBridge(MqttIngestProperties props, ObjectMapper mapper, RestClient.Builder builder) {
        this.props = props;
        this.mapper = mapper;
        this.http = builder.baseUrl(props.forwardBaseUrl()).build();
    }

    @PostConstruct
    void start() throws MqttException {
        client = new MqttClient(props.brokerUrl(), props.clientId(), new MemoryPersistence());
        client.setCallback(this);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        // Persistent session: the broker keeps QoS 1 messages while the backend is down
        options.setCleanSession(false);
        if (props.username() != null && !props.username().isBlank()) {
            options.setUserName(props.username());
            options.setPassword(props.password().toCharArray());
        }
        // With an ssl:// URL Paho uses the JVM trust store, which already trusts public CAs (HiveMQ/EMQX Cloud).
        // A broker with a private CA needs options.setSocketFactory(...) (see the spike doc).
        try {
            client.connect(options);
        } catch (MqttException e) {
            // The spike must not stop the API from booting if the broker is down
            log.error("MQTT: cannot connect to {}: {}", props.brokerUrl(), e.getMessage());
        }
    }

    @PreDestroy
    void stop() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
        if (client != null) {
            client.close();
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverUri) {
        // Subscribe on every (re)connection: it is what makes automatic reconnect useful
        try {
            client.subscribe(props.topicFilter(), props.qos());
            log.info("MQTT: connected to {} (reconnect={}), subscribed to {}", serverUri, reconnect, props.topicFilter());
        } catch (MqttException e) {
            log.error("MQTT: subscribe failed: {}", e.getMessage());
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // Never throw from here: Paho would drop the connection because of one bad message
        try {
            forward(topic, new String(message.getPayload(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.warn("MQTT: message on {} discarded: {}", topic, e.getMessage());
        }
    }

    private void forward(String topic, String payload) throws Exception {
        String[] levels = topic.split("/");
        if (levels.length != 3 || !"readings".equals(levels[2])) {
            throw new IllegalArgumentException("unexpected topic, expected sensors/{sensorId}/readings");
        }
        UUID sensorId = UUID.fromString(levels[1]);

        JsonNode in = mapper.readTree(payload);
        String measuredAt = in.path("measuredAt").asText(null);
        String sourceKey = in.hasNonNull("sourceKey") ? in.get("sourceKey").asText()
                : "mqtt-" + sensorId + "-" + measuredAt;
        String deviceKey = in.hasNonNull("deviceKey") ? in.get("deviceKey").asText() : props.defaultDeviceKey();

        ObjectNode body = mapper.createObjectNode();
        body.put("sensorId", sensorId.toString());
        body.put("sourceKey", sourceKey);
        body.set("value", in.get("value"));
        body.set("unit", in.get("unit"));
        body.put("measuredAt", measuredAt);

        try {
            var response = http.post().uri("/api/v1/sensors/readings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Device-Key", deviceKey)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);
            log.info("MQTT: reading of sensor {} forwarded -> HTTP {}", sensorId, response.getStatusCode().value());
        } catch (RestClientResponseException e) {
            // 4xx means the data is wrong: retrying would never fix it, so it is only logged
            log.warn("MQTT: API rejected reading of sensor {} -> HTTP {} {}", sensorId, e.getStatusCode().value(),
                    e.getResponseBodyAsString());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT: connection lost ({}), Paho will try to reconnect", cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // We only subscribe, nothing to do
    }
}
