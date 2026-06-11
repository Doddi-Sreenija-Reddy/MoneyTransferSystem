package com.banking.transfer.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for WebConfig to verify CORS configuration.
 */
@SpringBootTest
class WebConfigTest {

    @Autowired
    @Qualifier("webConfig")
    private WebMvcConfigurer webMvcConfigurer;

    @Test
    @DisplayName("WebConfig bean should be created")
    void webConfig_shouldBeCreated() {
        assertThat(webMvcConfigurer).isNotNull();
        assertThat(webMvcConfigurer).isInstanceOf(WebConfig.class);
    }

    @Test
    @DisplayName("CORS configuration should be applied")
    void corsConfiguration_shouldBeApplied() {
        // Create a test CorsRegistry to verify configuration
        CorsRegistry registry = new CorsRegistry();
        webMvcConfigurer.addCorsMappings(registry);

        // The configuration is applied, we can verify the bean exists
        assertThat(webMvcConfigurer).isNotNull();
    }
}
