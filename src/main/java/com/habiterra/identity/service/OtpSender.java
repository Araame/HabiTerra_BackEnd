package com.habiterra.identity.service;
import com.habiterra.identity.entity.IdentifierType;
public interface OtpSender { void send(IdentifierType type, String identifier, String code); }
