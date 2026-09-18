package com.habiterra.identity.service;

import com.habiterra.identity.exception.AuthException;
import com.twilio.exception.ApiException;
import com.twilio.exception.TwilioException;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Delivers the existing Habiterra OTP; never substitutes a Twilio trial template. */
@Component
public class SmsOtpSender {
    private static final Logger log = LoggerFactory.getLogger(SmsOtpSender.class);
    private final ObjectProvider<TwilioRestClient> clients;
    private final String from;

    public SmsOtpSender(ObjectProvider<TwilioRestClient> clients,
                        @Value("${twilio.from-number:}") String from) {
        this.clients = clients;
        this.from = from;
    }
// Twilio sending OTP
    public void send(String identifier, String code) {
        TwilioRestClient client = clients.getIfAvailable();
        if (client == null || from.isBlank()) throw unavailable();

        try {
            new MessageCreator(new PhoneNumber(identifier), new PhoneNumber(from),
                    "Habiterra : votre code de vérification est " + code + ".\n"
                            + "Il expire dans 5 minutes.\nNe partagez jamais ce code.")
                    .create(client);
        } catch (ApiException e) {
            // Only the numeric provider code: exception text can contain personal data.
            log.warn("SMS delivery rejected by Twilio (errorCode={})", e.getCode());
            throw unavailable();
        } catch (TwilioException e) {
            log.warn("SMS delivery unavailable (Twilio transport or SDK failure)");
            throw unavailable();
        }
    }

    private AuthException unavailable() {
        return new AuthException(503, "OTP_DELIVERY_UNAVAILABLE",
                "Envoi du code indisponible. Veuillez réessayer plus tard.");
    }
}
