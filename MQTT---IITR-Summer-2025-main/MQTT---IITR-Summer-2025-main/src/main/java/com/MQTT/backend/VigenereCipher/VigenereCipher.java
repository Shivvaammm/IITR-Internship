package com.MQTT.backend.VigenereCipher;


public class VigenereCipher {

    public static String encrypt(String plaintext, String key) {
        StringBuilder ciphertext = new StringBuilder();
        plaintext = plaintext.toUpperCase();
        key = key.toUpperCase().replaceAll("[^A-Z]", "");

        int keyIndex = 0;
        for (char c : plaintext.toCharArray()) {
            if (Character.isLetter(c)) {
                int shift = key.charAt(keyIndex % key.length()) - 'A';
                char encrypted = (char) ((c - 'A' + shift) % 26 + 'A');
                ciphertext.append(encrypted);
                keyIndex++;
            } else {
                ciphertext.append(c);
            }
        }
        return ciphertext.toString();
    }

    public static String decrypt(String ciphertext, String key) {
        StringBuilder plaintext = new StringBuilder();
        ciphertext = ciphertext.toUpperCase();
        key = key.toUpperCase().replaceAll("[^A-Z]", "");

        int keyIndex = 0;
        for (char c : ciphertext.toCharArray()) {
            if (Character.isLetter(c)) {
                int shift = key.charAt(keyIndex % key.length()) - 'A';
                char decrypted = (char) ((c - 'A' - shift + 26) % 26 + 'A');
                plaintext.append(decrypted);
                keyIndex++;
            } else {
                plaintext.append(c);
            }
        }
        return plaintext.toString();
    }
}
