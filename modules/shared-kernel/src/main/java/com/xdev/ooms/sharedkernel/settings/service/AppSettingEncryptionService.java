package com.xdev.ooms.sharedkernel.settings.service;

import com.xdev.ooms.sharedkernel.utils.OOSMLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AppSettingEncryptionService {

    private static final String PREFIX = "v1:";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int NONCE_LENGTH = 12;

    private final byte[] masterKey;
    private final boolean usingDedicatedKey;

    public AppSettingEncryptionService(
            @Value("${app.settings.encryption-key:}") String encryptionKey,
            @Value("${APP_SETTINGS_ENCRYPTION_KEY:}") String dedicatedEnvKey
    ) {
        if (!StringUtils.hasText(encryptionKey)) {
            throw new IllegalStateException("app.settings.encryption-key must be configured");
        }
        this.masterKey = deriveKey(encryptionKey);
        this.usingDedicatedKey = StringUtils.hasText(dedicatedEnvKey);
        if (!usingDedicatedKey) {
            OOSMLogger.log(this.getClass(), OOSMLogger.LogLevel.WARN,
                    "APP_SETTINGS_ENCRYPTION_KEY is not set; using app.settings.encryption-key fallback (set APP_SETTINGS_ENCRYPTION_KEY in production)");
        }
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean isUsingDedicatedKey() {
        return usingDedicatedKey;
    }

    public String encrypt(String settingKey, String plaintext) {
        try {
            byte[] nonce = new byte[NONCE_LENGTH];
            new SecureRandom().nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(masterKey, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
            cipher.updateAAD(settingKey.getBytes(StandardCharsets.UTF_8));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return PREFIX + Base64.getEncoder().encodeToString(nonce) + ":" + Base64.getEncoder().encodeToString(ciphertext);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to encrypt setting value", ex);
        }
    }

    public String decrypt(String settingKey, String encryptedValue) {
        if (!StringUtils.hasText(encryptedValue) || !encryptedValue.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Invalid encrypted setting payload");
        }
        try {
            String payload = encryptedValue.substring(PREFIX.length());
            int separator = payload.indexOf(':');
            if (separator < 0) {
                throw new IllegalArgumentException("Invalid encrypted setting payload");
            }
            byte[] nonce = Base64.getDecoder().decode(payload.substring(0, separator));
            byte[] ciphertext = Base64.getDecoder().decode(payload.substring(separator + 1));

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(masterKey, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH, nonce));
            cipher.updateAAD(settingKey.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to decrypt setting value", ex);
        }
    }

    private static byte[] deriveKey(String encryptionKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to derive encryption key", ex);
        }
    }
}
