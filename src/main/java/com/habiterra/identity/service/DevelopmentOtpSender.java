package com.habiterra.identity.service;
import com.habiterra.identity.entity.IdentifierType;
import org.slf4j.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
@Component
@ConditionalOnProperty(name="auth.otp.dev-mode", havingValue="true")
public class DevelopmentOtpSender implements OtpSender {
    private static final Logger log=LoggerFactory.getLogger(DevelopmentOtpSender.class);
    public void send(IdentifierType type,String identifier,String code) {
        log.info("[OTP DEV] Identifier: {} | Code: {}",identifier,code);
    }
}
