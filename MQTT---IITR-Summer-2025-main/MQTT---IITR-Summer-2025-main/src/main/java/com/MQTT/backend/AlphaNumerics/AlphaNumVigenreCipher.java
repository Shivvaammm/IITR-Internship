package com.MQTT.backend.AlphaNumerics;

public class AlphaNumVigenreCipher {

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.";

    private static int getCharIndex(char c) {
        return CHARSET.indexOf(c);
    }

    public static String encrypt(String plaintext, String key) {
        StringBuilder ciphertext = new StringBuilder();
        plaintext = plaintext.replaceAll("[^A-Za-z0-9.]", "");
        key = key.replaceAll("[^A-Za-z0-9.]", "");

        int keyIndex = 0;
        for (char c : plaintext.toCharArray()) {
            int pIndex = getCharIndex(c);
            if (pIndex != -1) {
                int kIndex = getCharIndex(key.charAt(keyIndex % key.length()));
                int encryptedIndex = (pIndex + kIndex) % CHARSET.length();
                ciphertext.append(CHARSET.charAt(encryptedIndex));
                keyIndex++;
            } else {
                ciphertext.append(c);
            }
        }
        return ciphertext.toString();
    }

    public static String decrypt(String ciphertext, String key) {
        StringBuilder plaintext = new StringBuilder();
        ciphertext = ciphertext.replaceAll("[^A-Za-z0-9.]", "");
        key = key.replaceAll("[^A-Za-z0-9.]", "");

        int keyIndex = 0;
        for (char c : ciphertext.toCharArray()) {
            int cIndex = getCharIndex(c);
            if (cIndex != -1) {
                int kIndex = getCharIndex(key.charAt(keyIndex % key.length()));
                int decryptedIndex = (cIndex - kIndex + CHARSET.length()) % CHARSET.length();
                plaintext.append(CHARSET.charAt(decryptedIndex));
                keyIndex++;
            } else {
                plaintext.append(c);
            }
        }
        return plaintext.toString();
    }

}