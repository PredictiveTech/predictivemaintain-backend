package pe.edu.upc.predictivemaintain.notifications.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Turns on @Async (notifications run outside the request) and @Scheduled (the daily expiry notice).
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncSchedulingConfiguration {
}