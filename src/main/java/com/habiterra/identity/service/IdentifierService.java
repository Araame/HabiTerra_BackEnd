package com.habiterra.identity.service;
import com.habiterra.identity.entity.IdentifierType;
import com.habiterra.identity.exception.AuthException;
import org.springframework.stereotype.Service;
import java.util.Locale;
@Service
public class IdentifierService {
    public record Identifier(String value, IdentifierType type) {}
    public Identifier normalize(String input) {
        if (input == null || input.isBlank() || input.length() > 150) throw invalid();
        String value = input.strip();
        if (value.contains("@")) {
            value = value.toLowerCase(Locale.ROOT);
            if (value.length()>100 || !value.matches("[a-z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-z0-9](?:[a-z0-9-]*[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)+")) throw invalid();
            return new Identifier(value, IdentifierType.EMAIL);
        }
        value = value.replaceAll("[ ()-]", "");
        if (value.startsWith("00")) value = "+" + value.substring(2);
        // No implicit country: require international format.
        if (!value.matches("\\+[1-9][0-9]{7,14}")) throw invalid();
        return new Identifier(value, IdentifierType.TELEPHONE);
    }
    public String optional(String input, IdentifierType expected) {
        if (input == null || input.isBlank()) return null;
        Identifier id = normalize(input);
        if (id.type()!=expected) throw invalid();
        return id.value();
    }
    private AuthException invalid() { return new AuthException(400,"INVALID_IDENTIFIER","Email ou telephone international invalide"); }
}
