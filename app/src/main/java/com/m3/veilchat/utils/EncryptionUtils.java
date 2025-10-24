package com.m3.veilchat.utils;

import android.util.Base64;
import android.util.Log;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class EncryptionUtils {
    private static final String TAG = "EncryptionUtils";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;

    // Emoji cipher mapping for StealthTalk feature
    private static final String[] EMOJIS = {
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰",
            "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩", "🥳", "😏",
            "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠",
            "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗", "🤔", "🤭", "🤫", "🤥"
    };

    private static Map<String, String> emojiMap = new HashMap<>();
    private static Map<String, String> reverseEmojiMap = new HashMap<>();

    static {
        // Initialize emoji mapping for cipher messages
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        for (int i = 0; i < characters.length(); i++) {
            if (i < EMOJIS.length) {
                emojiMap.put(String.valueOf(characters.charAt(i)), EMOJIS[i]);
                reverseEmojiMap.put(EMOJIS[i], String.valueOf(characters.charAt(i)));
            }
        }
    }

    // Generate AES secret key
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE, new SecureRandom());
        return keyGenerator.generateKey();
    }

    // Convert secret key to string for storage
    public static String keyToString(SecretKey secretKey) {
        return Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
    }

    // Convert string back to secret key
    public static SecretKey stringToKey(String keyString) {
        byte[] keyBytes = Base64.decode(keyString, Base64.DEFAULT);
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, ALGORITHM);
    }

    // Encrypt text with secret key
    public static String encrypt(String plainText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        // Generate random IV
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));

        // Combine IV and encrypted data
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.encodeToString(combined, Base64.DEFAULT);
    }

    // Decrypt text with secret key
    public static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        byte[] combined = Base64.decode(encryptedText, Base64.DEFAULT);

        // Extract IV and encrypted data
        byte[] iv = new byte[16];
        byte[] encrypted = new byte[combined.length - 16];
        System.arraycopy(combined, 0, iv, 0, 16);
        System.arraycopy(combined, 16, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, "UTF-8");
    }

    // Generate cipher key for StealthTalk (emoji-based)
    public static String generateCipherKey() {
        SecureRandom random = new SecureRandom();
        StringBuilder key = new StringBuilder();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < 16; i++) {
            key.append(characters.charAt(random.nextInt(characters.length())));
        }

        return key.toString();
    }

    // Encrypt message with emoji cipher (StealthTalk)
    public static String encryptWithEmojiCipher(String plainText, String cipherKey) {
        try {
            // First encrypt with AES
            SecretKey secretKey = deriveKeyFromCipherKey(cipherKey);
            String encrypted = encrypt(plainText, secretKey);

            // Then convert to emoji representation
            return textToEmoji(encrypted);
        } catch (Exception e) {
            Log.e(TAG, "Emoji encryption failed", e);
            return plainText; // Fallback to plain text
        }
    }

    // Decrypt message with emoji cipher (StealthTalk)
    public static String decryptWithEmojiCipher(String emojiText, String cipherKey) {
        try {
            // Convert from emoji back to base64
            String base64Text = emojiToText(emojiText);

            // Then decrypt with AES
            SecretKey secretKey = deriveKeyFromCipherKey(cipherKey);
            return decrypt(base64Text, secretKey);
        } catch (Exception e) {
            Log.e(TAG, "Emoji decryption failed", e);
            return emojiText; // Fallback to show encrypted text
        }
    }

    // Derive AES key from cipher key
    private static SecretKey deriveKeyFromCipherKey(String cipherKey) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(cipherKey.getBytes("UTF-8"));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    // Convert text to emoji representation
    private static String textToEmoji(String text) {
        StringBuilder emojiBuilder = new StringBuilder();
        for (char c : text.toCharArray()) {
            String charStr = String.valueOf(c);
            if (emojiMap.containsKey(charStr)) {
                emojiBuilder.append(emojiMap.get(charStr));
            } else {
                emojiBuilder.append(c); // Keep unknown characters as-is
            }
        }
        return emojiBuilder.toString();
    }

    // Convert emoji back to text
    private static String emojiToText(String emojiText) {
        StringBuilder textBuilder = new StringBuilder();
        // Since emojis might be multiple characters, we need to parse carefully
        // This is a simplified version - in production you'd need more robust parsing
        for (int i = 0; i < emojiText.length(); i++) {
            // Check for 2-character emojis first
            if (i + 1 < emojiText.length()) {
                String possibleEmoji = emojiText.substring(i, i + 2);
                if (reverseEmojiMap.containsKey(possibleEmoji)) {
                    textBuilder.append(reverseEmojiMap.get(possibleEmoji));
                    i++; // Skip next character
                    continue;
                }
            }

            // Check single character (though most emojis are 2 chars)
            String singleChar = emojiText.substring(i, i + 1);
            if (reverseEmojiMap.containsKey(singleChar)) {
                textBuilder.append(reverseEmojiMap.get(singleChar));
            } else {
                textBuilder.append(singleChar);
            }
        }
        return textBuilder.toString();
    }

    // Hash password for storage
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Password hashing failed", e);
            return null;
        }
    }

    // Validate password against hash

    public static boolean validatePassword(String password, String storedHash) {
        try {
            String newHash = hashPassword(password);
            return newHash != null && newHash.equals(storedHash);
        } catch (Exception e) {
            Log.e(TAG, "Password validation failed", e);
            return false;
        }
    }

    // Add this method for secret inbox password validation
    public static boolean validateSecretInboxPassword(String password, String storedHash) {
        return validatePassword(password, storedHash);
    }

    // Add method to clear specific keys if needed
    public static void clearKey(String keyIdentifier) {
        // Implementation for key clearing would go here
        // This might involve secure deletion from keystore
    }
}