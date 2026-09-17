package com.habiterra.application.dto;

import com.habiterra.application.entity.StatutCandidature;
import java.time.LocalDateTime;
// Application response model
public record ApplicationResponse(Long id, StatutCandidature status, LocalDateTime applicationDate,
        PropertySummaryResponse property, TenantSummaryResponse tenant) {}
