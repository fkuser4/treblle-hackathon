package com.hackathon.sdk.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoringAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(MonitoringAutoConfiguration.class)
            .withPropertyValues(
                    "monitoring.enabled=true",
                    "monitoring.api-key=test-key",
                    "monitoring.project-id=test-project"
            );

    @Test
    void whenMonitoringEnabled_shouldCreateBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RestTemplate.class);
            assertThat(context).hasSingleBean(FilterRegistrationBean.class);
        });
    }

    @Test
    void whenMonitoringDisabled_shouldNotCreateBeans() {
        new ApplicationContextRunner()
                .withUserConfiguration(MonitoringAutoConfiguration.class)
                .withPropertyValues("monitoring.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(FilterRegistrationBean.class);
                });
    }

    @Test
    void shouldConfigureRestTemplateWithTimeouts() {
        contextRunner.run(context -> {
            RestTemplate restTemplate = context.getBean(RestTemplate.class);
            assertThat(restTemplate).isNotNull();
        });
    }
}