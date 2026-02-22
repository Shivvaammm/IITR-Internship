package com.MQTT.backend.AlphaNumerics;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class AlphaNumPlayFair{

    private char[][] matrix = new char[6][6];
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public AlphaNumPlayFair(String key) {
        generateMatrix(key);
    }

    private void generateMatrix(String key) {
        Set<Character> used = new HashSet<>();
        key = key.toUpperCase().replaceAll("[^A-Z0-9]", "");

        StringBuilder matrixKey = new StringBuilder();

        for (char c : key.toCharArray()) {
            if (!used.contains(c)) {
                matrixKey.append(c);
                used.add(c);
            }
        }

        for (char c : CHARSET.toCharArray()) {
            if (!used.contains(c)) {
                matrixKey.append(c);
                used.add(c);
            }
        }

        for (int i = 0; i < 36; i++) {
            matrix[i / 6][i % 6] = matrixKey.charAt(i);
        }
    }

    private String prepareText(String text) {
        text = text.toUpperCase().replaceAll("[^A-Z0-9]", "");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i += 2) {
            char a = text.charAt(i);
            char b = (i + 1 < text.length()) ? text.charAt(i + 1) : 'X';

            if (a == b) {
                b = 'X';
                i--;
            }
            result.append(a).append(b);
        }

        return result.toString();
    }

    private int[] findPosition(char c) {
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++)
                if (matrix[i][j] == c)
                    return new int[]{i, j};
        return null;
    }

    public String encrypt(String plaintext) {
        String text = prepareText(plaintext);
        StringBuilder encrypted = new StringBuilder();

        for (int i = 0; i < text.length(); i += 2) {
            char a = text.charAt(i);
            char b = text.charAt(i + 1);

            int[] posA = findPosition(a);
            int[] posB = findPosition(b);

            if (posA[0] == posB[0]) {
                encrypted.append(matrix[posA[0]][(posA[1] + 1) % 6]);
                encrypted.append(matrix[posB[0]][(posB[1] + 1) % 6]);
            } else if (posA[1] == posB[1]) {
                encrypted.append(matrix[(posA[0] + 1) % 6][posA[1]]);
                encrypted.append(matrix[(posB[0] + 1) % 6][posB[1]]);
            } else {
                encrypted.append(matrix[posA[0]][posB[1]]);
                encrypted.append(matrix[posB[0]][posA[1]]);
            }
        }

        return encrypted.toString();
    }

    private String removePadding(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char curr = text.charAt(i);
            // Remove 'X' between duplicate letters (except at the end)
            if (i > 0 && i < text.length() - 1 && curr == 'X' && text.charAt(i - 1) == text.charAt(i + 1)) {
                continue;
            }
            result.append(curr);
        }
        // Remove trailing 'X' if it was added as padding
        if (result.length() > 0 && result.charAt(result.length() - 1) == 'X') {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

    public String decrypt(String ciphertext) {
        StringBuilder decrypted = new StringBuilder();

        for (int i = 0; i < ciphertext.length(); i += 2) {
            char a = ciphertext.charAt(i);
            char b = ciphertext.charAt(i + 1);

            int[] posA = findPosition(a);
            int[] posB = findPosition(b);

            if (posA[0] == posB[0]) {
                decrypted.append(matrix[posA[0]][(posA[1] + 5) % 6]);
                decrypted.append(matrix[posB[0]][(posB[1] + 5) % 6]);
            } else if (posA[1] == posB[1]) {
                decrypted.append(matrix[(posA[0] + 5) % 6][posA[1]]);
                decrypted.append(matrix[(posB[0] + 5) % 6][posB[1]]);
            } else {
                decrypted.append(matrix[posA[0]][posB[1]]);
                decrypted.append(matrix[posB[0]][posA[1]]);
            }
        }

        return removePadding(decrypted.toString());
    }

    public static String generateRandomAlphanumericString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }
}
