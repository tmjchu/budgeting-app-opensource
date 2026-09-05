package com.localbudget.app.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.localbudget.app.domain.exception.CredentialOperationException;
import com.localbudget.app.domain.model.EncryptedCredentialEnvelope;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CredentialCryptoServiceTest {

    private final CredentialCryptoService cryptoService = new CredentialCryptoService();

    @Test
    void encryptsAndDecryptsCredentialPayload() {
        byte[] plaintext = "client-id:secret-value".getBytes(StandardCharsets.UTF_8);

        EncryptedCredentialEnvelope envelope =
                cryptoService.encrypt(plaintext, "a-secure-password".toCharArray());

        assertThat(envelope.ciphertext())
                .doesNotContain("client-id")
                .doesNotContain("secret-value");
        assertThat(cryptoService.decrypt(envelope, "a-secure-password".toCharArray()))
                .isEqualTo(plaintext);
    }

    @Test
    void rejectsWrongPasswordAndTamperedCiphertext() {
        EncryptedCredentialEnvelope envelope =
                cryptoService.encrypt(
                        "sensitive".getBytes(StandardCharsets.UTF_8),
                        "a-secure-password".toCharArray());

        assertThatThrownBy(
                        () -> cryptoService.decrypt(envelope, "the-wrong-password".toCharArray()))
                .isInstanceOf(CredentialOperationException.class)
                .hasMessage("Unable to unlock saved credentials.");

        EncryptedCredentialEnvelope tampered =
                new EncryptedCredentialEnvelope(
                        envelope.version(),
                        envelope.kdf(),
                        envelope.iterations(),
                        envelope.cipher(),
                        envelope.salt(),
                        envelope.iv(),
                        envelope.ciphertext().substring(0, envelope.ciphertext().length() - 2)
                                + "AA");
        assertThatThrownBy(() -> cryptoService.decrypt(tampered, "a-secure-password".toCharArray()))
                .isInstanceOf(CredentialOperationException.class);
    }

    @Test
    void rejectsUnsupportedMetadata() {
        EncryptedCredentialEnvelope invalid =
                new EncryptedCredentialEnvelope(99, "unknown", 1, "unknown", "", "", "");

        assertThatThrownBy(() -> cryptoService.decrypt(invalid, "a-secure-password".toCharArray()))
                .isInstanceOf(CredentialOperationException.class)
                .hasMessage("Saved credential metadata is invalid.");
    }
}
