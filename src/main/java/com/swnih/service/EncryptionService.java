package com.swnih.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for encrypting and decrypting OAuth tokens using AES-256 encryption.
 * Implements secure token storage as required by the system specifications.
 */
@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int KEY_LENGTH = 256;

    @Value("${encryption.secret-key:your-encryption-secret-key-32-chars}")
    private String secretKeyString;

    /**
     * Encrypt a plain text string using AES-256 encryption.
     * 
     * @param plainText the text to encrypt
     * @return Base64 encoded encrypted string
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(String plainText) {
        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypt an encrypted string using AES-256 decryption.
     * 
     * @param encryptedText Base64 encoded encrypted string
     * @return decrypted plain text
     * @throws RuntimeException if decryption fails
     */
    public String decrypt(String encryptedText) {
        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    /**
     * Generate a new AES-256 secret key.
     * 
     * @return Base64 encoded secret key
     */
    public String generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_LENGTH, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate secret key", e);
        }
    }

    /**
     * Get the secret key for encryption/decryption.
     * 
     * @return SecretKey instance
     */
    private SecretKey getSecretKey() {
        try {
            // Ensure the key is exactly 32 bytes for AES-256
            String normalizedKey = normalizeKey(secretKeyString);
            byte[] keyBytes = normalizedKey.getBytes(StandardCharsets.UTF_8);
            
            return new SecretKeySpec(keyBytes, ALGORITHM);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create secret key", e);
        }
    }

    /**
     * Normalize the secret key to exactly 32 bytes for AES-256.
     * 
     * @param key the input key string
     * @return normalized 32-byte key string
     */
    private String normalizeKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Secret key cannot be null or empty");
        }
        
        // If key is shorter than 32 bytes, pad with zeros
        if (key.length() < 32) {
            StringBuilder sb = new StringBuilder(key);
            while (sb.length() < 32) {
                sb.append('0');
            }
            return sb.toString();
        }
        
        // If key is longer than 32 bytes, truncate
        if (key.length() > 32) {
            return key.substring(0, 32);
        }
        
        return key;
    }

    /**
     * Validate that the encryption service is properly configured.
     * 
     * @return true if encryption service is working correctly
     */
    public boolean validateConfiguration() {
        try {
            String testData = "test-encryption-data";
            String encrypted = encrypt(testData);
            String decrypted = decrypt(encrypted);
            
            return testData.equals(decrypted);
            
        } catch (Exception e) {
            return false;
        }
    }
}