package com.habiterra.identity.service;
import com.habiterra.identity.exception.AuthException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailOtpSender {
    private final ObjectProvider<JavaMailSender> mail;
    private final String from;

    // Sending mail
    public EmailOtpSender(ObjectProvider<JavaMailSender> mail, @Value("${auth.mail.from:}") String from) {
        this.mail = mail;
        this.from = from;
    }

    // Handles sending mail
    public void send(String identifier, String code) {
        JavaMailSender sender = mail.getIfAvailable();

        if (sender == null || from.isBlank()) throw unavailable();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(identifier);
            message.setSubject("Votre code de vérification Habiterra");
            message.setText("""
                    Bonjour,

                    Votre code de vérification Habiterra est : %s

                    Ce code expire dans 5 minutes. Ne partagez ce code avec personne.
                    Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.

                    L'équipe Habiterra
                    """.formatted(code));
            sender.send(message);
        } catch (MailException e) {
            throw unavailable();
        }
    }

    private AuthException unavailable() {
        return new AuthException(503, "OTP_DELIVERY_UNAVAILABLE",
                "Envoi du code indisponible. Veuillez réessayer plus tard.");
    }
}
