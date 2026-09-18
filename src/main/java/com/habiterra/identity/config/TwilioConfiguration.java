package com.habiterra.identity.config;

import com.twilio.http.TwilioRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Configuration(proxyBeanMethods = false)
public class TwilioConfiguration {
    @Bean
    @Conditional(CredentialsPresent.class)
    public TwilioRestClient twilioRestClient(
            @Value("${twilio.account-sid:}") String accountSid,
            @Value("${twilio.auth-token:}") String authToken) {
        return new TwilioRestClient.Builder(accountSid, authToken).build();
    }

    static class CredentialsPresent implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            var environment = context.getEnvironment();
            return !environment.getProperty("twilio.account-sid", "").isBlank()
                    && !environment.getProperty("twilio.auth-token", "").isBlank();
        }
    }
}
