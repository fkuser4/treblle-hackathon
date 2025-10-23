package com.hackathon.sdk.annotation;

import com.hackathon.sdk.config.MonitoringAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable automatic API monitoring for the application.
 * Add this annotation to your main Spring Boot application class.
 *
 * Example:
 * <pre>
 * {@code
 * @SpringBootApplication
 * @EnableMonitoring
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MonitoringAutoConfiguration.class)
public @interface EnableMonitoring {
}