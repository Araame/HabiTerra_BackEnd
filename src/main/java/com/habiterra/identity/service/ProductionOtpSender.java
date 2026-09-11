package com.habiterra.identity.service;
import com.habiterra.identity.entity.IdentifierType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
@Component
@ConditionalOnProperty(name="auth.otp.dev-mode", havingValue="false", matchIfMissing=true)
public class ProductionOtpSender implements OtpSender {
    private final EmailOtpSender email; private final SmsOtpSender sms;
    public ProductionOtpSender(EmailOtpSender email,SmsOtpSender sms){this.email=email;this.sms=sms;}
    public void send(IdentifierType type,String identifier,String code){
        if(type==IdentifierType.EMAIL)email.send(identifier,code);else sms.send(identifier,code);
    }
}
