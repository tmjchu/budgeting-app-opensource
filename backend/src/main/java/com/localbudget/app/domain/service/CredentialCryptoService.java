package com.localbudget.app.domain.service;

import com.localbudget.app.domain.exception.CredentialOperationException;
import com.localbudget.app.domain.model.EncryptedCredentialEnvelope;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class CredentialCryptoService {

    static final int VERSION = 1;
    static final String KDF = "PBKDF2WithHmacSHA256";
    static final String CIPHER = "AES/GCM/NoPadding";
    static final int ITERATIONS = 210_000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final int IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;

    private final SecureRandom secureRandom;

    public CredentialCryptoService() {
        this(new SecureRandom());
    }

    CredentialCryptoService(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    public EncryptedCredentialEnvelope encrypt(byte[] plaintext, char[] password) {
        byte[] salt = randomBytes(SALT_BYTES);
        byte[] iv = randomBytes(IV_BYTES);
        try {
            SecretKey key = deriveKey(password, salt, ITERATIONS);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext);
            return new EncryptedCredentialEnvelope(
                    VERSION, KDF, ITERATIONS, CIPHER, encode(salt), encode(iv), encode(ciphertext));
        } catch (GeneralSecurityException exception) {
            throw new CredentialOperationException("Unable to encrypt credentials.", exception);
        }
    }

    public byte[] decrypt(EncryptedCredentialEnvelope envelope, char[] password) {
        validateEnvelope(envelope);
        try {
            byte[] salt = decode(envelope.salt());
            byte[] iv = decode(envelope.iv());
            byte[] ciphertext = decode(envelope.ciphertext());
            SecretKey key = deriveKey(password, salt, envelope.iterations());
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return cipher.doFinal(ciphertext);
        } catch (Exception exception) {
            throw new CredentialOperationException("Unable to unlock saved credentials.");
        }
    }

    private void validateEnvelope(EncryptedCredentialEnvelope envelope) {
        if (envelope == null
                || envelope.version() != VERSION
                || !KDF.equals(envelope.kdf())
                || envelope.iterations() < ITERATIONS
                || !CIPHER.equals(envelope.cipher())
                || envelope.salt() == null
                || envelope.iv() == null
                || envelope.ciphertext() == null) {
            throw new CredentialOperationException("Saved credential metadata is invalid.");
        }
    }

    private SecretKey deriveKey(char[] password, byte[] salt, int iterations)
            throws GeneralSecurityException {
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        byte[] key = null;
        try {
            key = SecretKeyFactory.getInstance(KDF).generateSecret(keySpec).getEncoded();
            return new SecretKeySpec(key, "AES");
        } finally {
            keySpec.clearPassword();
            if (key != null) {
                Arrays.fill(key, (byte) 0);
            }
        }
    }

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    private String encode(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    private byte[] decode(String value) {
        return Base64.getDecoder().decode(value.getBytes(StandardCharsets.US_ASCII));
    }
}
