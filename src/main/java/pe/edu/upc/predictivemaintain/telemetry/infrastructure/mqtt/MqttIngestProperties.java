package pe.edu.upc.predictivemaintain.telemetry.infrastructure.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SP-01 spike. Settings of the MQTT bridge. Disabled by default so nothing changes for the rest of the team.
 *
 * @param brokerUrl      tcp://host:1883 (plain, local only) or ssl://host:8883 (TLS)
 * @param topicFilter    what to subscribe to; the sensor id is the second level: sensors/{sensorId}/readings
 * @param forwardBaseUrl base URL of the REST API that receives the readings
 * @param defaultDeviceKey X-Device-Key to use when the message does not carry its own (demo only)
 */
@ConfigurationProperties(prefix = "app.mqtt")
public record MqttIngestProperties(
        boolean enabled,
        String brokerUrl,
        String clientId,
        String username,
        String password,
        String topicFilter,
        int qos,
        String forwardBaseUrl,
        String defaultDeviceKey) {
}
