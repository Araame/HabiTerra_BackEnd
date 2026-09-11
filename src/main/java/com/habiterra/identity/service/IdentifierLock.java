package com.habiterra.identity.service;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
/** Transaction-scoped PostgreSQL lock, including when no OTP row exists yet. */
@Component
public class IdentifierLock {
    private final EntityManager entityManager;
    public IdentifierLock(EntityManager entityManager) { this.entityManager=entityManager; }
    public void acquire(String identifier) {
        entityManager.createNativeQuery("select 1 from pg_advisory_xact_lock(hashtextextended(:identifier, 0))")
            .setParameter("identifier", identifier).getSingleResult();
    }
}
