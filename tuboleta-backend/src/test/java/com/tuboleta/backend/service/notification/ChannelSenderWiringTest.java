package com.tuboleta.backend.service.notification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Verifica que exactamente un {@link ChannelSender} EMAIL se active según
 * {@code notifications.mailgun.api-key}, sin depender de
 * {@code ConditionalOnMissingBean} (orden de escaneo no garantizado para
 * {@code @Component}s planos) — ver javadoc de {@link LoggingEmailSender}.
 */
class ChannelSenderWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Configuration
    @AutoConfigurationPackage
    @ComponentScan(basePackageClasses = ChannelSender.class)
    static class TestConfig {
    }

    @Test
    void withoutApiKey_onlyLoggingEmailSenderIsActive() {
        contextRunner
                .withPropertyValues("notifications.mailgun.api-key=", "notifications.email.from=noreply@test.local")
                .run(context -> {
                    assertThat(context).hasSingleBean(ChannelSender.class);
                    assertThat(context).hasSingleBean(LoggingEmailSender.class);
                    assertThat(context).doesNotHaveBean(MailgunEmailSender.class);
                });
    }

    @Test
    void withApiKey_onlyMailgunEmailSenderIsActive() {
        contextRunner
                .withPropertyValues("notifications.mailgun.api-key=key-fake-for-test",
                        "notifications.mailgun.sender-url=https://api.mailgun.net/v3/test.local/messages",
                        "notifications.mailgun.from=monitoreo@test.local",
                        "notifications.email.from=noreply@test.local")
                .run(context -> {
                    assertThat(context).hasSingleBean(ChannelSender.class);
                    assertThat(context).hasSingleBean(MailgunEmailSender.class);
                    assertThat(context).doesNotHaveBean(LoggingEmailSender.class);
                });
    }
}
