package com.localbudget.app.domain.model;

public record EncryptedCredentialEnvelope(
        int version,
        String kdf,
        int iterations,
        String cipher,
        String salt,
        String iv,
        String ciphertext) {}
