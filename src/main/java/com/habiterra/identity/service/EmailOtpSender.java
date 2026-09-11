package com.habiterra.identity.service;
import com.habiterra.identity.exception.AuthException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
@Component
public class EmailOtpSender {
    private final ObjectProvider<JavaMailSender> mail;
    private final String from;
    public EmailOtpSender(ObjectProvider<JavaMailSender> mail,@Value("${auth.mail.from:}") String from) {this.mail=mail;this.from=from;}
    public void send(String identifier,String code) {
        JavaMailSender sender=mail.getIfAvailable();
        if(sender==null || from.isBlank()) throw new AuthException(503,"OTP_DELIVERY_UNAVAILABLE","Envoi email non configure");
        try {
            SimpleMailMessage message=new SimpleMailMessage();
            message.setFrom(from);message.setTo(identifier);message.setSubject("ImmoKer - Code de verification");
            message.setText("Votre code ImmoKer : "+code+". Valable 5 minutes. Ne le partagez pas.");
            sender.send(message);
        } catch(MailException e) {throw new AuthException(503,"OTP_DELIVERY_FAILED","Envoi du code impossible");}
    }
}
