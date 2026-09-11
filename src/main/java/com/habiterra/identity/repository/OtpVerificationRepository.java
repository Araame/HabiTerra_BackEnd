package com.habiterra.identity.repository;
import com.habiterra.identity.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.Optional;
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findFirstByIdentifierAndIdentifierTypeOrderByIdDesc(String identifier, IdentifierType type);
    long countByIdentifierAndCreatedAtAfter(String identifier, Instant after);
}
