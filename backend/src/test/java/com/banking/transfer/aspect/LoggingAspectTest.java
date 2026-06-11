package com.banking.transfer.aspect;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that LoggingAspect logs entering, exiting, and exception messages
 * when invoking beans under com.banking.transfer.service.* (per pointcut).
 *
 * We provide a lightweight @Configuration that:
 * - registers the LoggingAspect
 * - registers a test service bean under the target package
 * - enables Spring AOP
 */
@ExtendWith(OutputCaptureExtension.class)
@SpringJUnitConfig(classes = LoggingAspectTest.TestConfig.class)
// Ensure AOP proxies are enabled in this lightweight context
@ImportAutoConfiguration(AopAutoConfiguration.class)
class LoggingAspectTest {

    private final com.banking.transfer.service.DemoTestService demoTestService;

    @Autowired
    LoggingAspectTest(com.banking.transfer.service.DemoTestService demoTestService) {
        this.demoTestService = demoTestService;
    }

    @Test
    @DisplayName("logs entering and exiting messages for successful service call")
    void logsOnSuccess(CapturedOutput output) {
        String result = demoTestService.hello("Alice");

        assertThat(result).isEqualTo("Hello, Alice");

        // Verify log patterns from the aspect
        assertThat(output)
                .asString()
                .contains("[AOP] Entering DemoTestService.hello")
                .contains("[AOP] Exiting DemoTestService.hello - Execution time:");
    }

    @Test
    @DisplayName("logs exception message and timing for failing service call")
    void logsOnException(CapturedOutput output) {
        try {
            demoTestService.fail();
        } catch (RuntimeException ignored) {
            // expected
        }

        assertThat(output)
                .asString()
                .contains("[AOP] Entering DemoTestService.fail")
                .contains("[AOP] Exception in DemoTestService.fail")
                .contains("after") // "after {n}ms"
                .contains("Boom!"); // exception message propagated by aspect log
    }

    /**
     * Minimal test configuration registering:
     * - the LoggingAspect under test
     * - a demo service in the com.banking.transfer.service package
     * so that the pointcut 'execution(* com.banking.transfer.service.*.*(..))'
     * matches and the aspect is applied.
     */
    @Configuration
    static class TestConfig {

        @Bean
        LoggingAspect loggingAspect() {
            return new LoggingAspect();
        }

        // Define the test service under the targeted package
        @Bean
        com.banking.transfer.service.DemoTestService demoTestService() {
            return new com.banking.transfer.service.DemoTestService();
        }
    }
}
