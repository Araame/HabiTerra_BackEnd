package com.habiterra.identity.service;
import com.habiterra.identity.entity.IdentifierType;
import org.slf4j.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
@Component
@Profile("dev & !prod")
public class DevelopmentOtpSender implements OtpSender {
    private static final Logger log=LoggerFactory.getLogger(DevelopmentOtpSender.class);
    public void send(IdentifierType type,String identifier,String code) {
        log.info("DEV ONLY OTP {} {} : {}",type,identifier,code);
    }
}
