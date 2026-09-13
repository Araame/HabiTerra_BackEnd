package com.habiterra.identity.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name="otp_verification")
public class OtpVerification {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String identifier;
    @Enumerated(EnumType.STRING)
    private IdentifierType identifierType;
    private String codeHash;
    private Instant expiresAt;
    private boolean verified;
    private int attempts;
    private int maxAttempts;
    private Instant createdAt;
    private Instant verifiedAt;
    private boolean invalidated;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id=id;
    }
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier=identifier;
    }
    public IdentifierType getIdentifierType() {
        return identifierType;
    }
    public void setIdentifierType(IdentifierType identifierType) {
        this.identifierType=identifierType;
    }
    public String getCodeHash() {
        return codeHash;
    }
    public void setCodeHash(String codeHash) {
        this.codeHash=codeHash;
    }
    public Instant getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt=expiresAt;
    }
    public boolean isVerified() {
        return verified;
    }
    public void setVerified(boolean verified) {
        this.verified=verified;
    }
    public int getAttempts() {
        return attempts;
    }
    public void setAttempts(int attempts) {
        this.attempts=attempts;
    }
    public int getMaxAttempts() {
        return maxAttempts;
    }
    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts=maxAttempts;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt=createdAt;
    }
    public Instant getVerifiedAt() {
        return verifiedAt;
    }
    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt=verifiedAt;
    }
    public boolean isInvalidated() {
        return invalidated;
    }
    public void setInvalidated(boolean invalidated) {
        this.invalidated=invalidated;
    }
}
