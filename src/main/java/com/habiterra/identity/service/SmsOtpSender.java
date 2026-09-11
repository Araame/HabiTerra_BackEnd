package com.habiterra.identity.service;
import com.habiterra.identity.exception.AuthException;
import org.springframework.stereotype.Component;
/** Replace the implementation once the SMS provider and credentials are selected. */
@Component
public class SmsOtpSender {
    public void send(String identifier,String code) {
        throw new AuthException(503,"OTP_DELIVERY_UNAVAILABLE","Fournisseur SMS non configure");
    }
}
