package com.MQTT.backend.AlphaNumerics;

import java.util.Base64;

public class AlphaNumTranspositionCipher {
    private static String filterAlphanumeric(String input) {
        return input.replaceAll("[^A-Za-z0-9.]", "");
    }
    public static String encrypt(String plaintext, String key) {
        plaintext = filterAlphanumeric(plaintext);
        key = filterAlphanumeric(key);

        int[] keyOrder = getKeyOrder(key);
        int numCols = key.length();
        int numRows = (int) Math.ceil((double) plaintext.length() / numCols);

        char[][] grid = new char[numRows][numCols];

        int k = 0;
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                if (k < plaintext.length()) {
                    grid[r][c] = plaintext.charAt(k++);
                } else {
                    grid[r][c] = 'X'; // Padding
                }
            }
        }

        StringBuilder ciphertext = new StringBuilder();
        for (int colIndex : keyOrder) {
            for (int r = 0; r < numRows; r++) {
                ciphertext.append(grid[r][colIndex]);
            }
        }

        return ciphertext.toString();
    }

    private static int[] getKeyOrder(String key) {
        int len = key.length();
        Character[] keyChars = new Character[len];
        for (int i = 0; i < len; i++) keyChars[i] = key.charAt(i);

        Integer[] indices = new Integer[len];
        for (int i = 0; i < len; i++) indices[i] = i;

        java.util.Arrays.sort(indices, (i, j) -> {
            int cmp = Character.compare(keyChars[i], keyChars[j]);
            if (cmp != 0) return cmp;
            return Integer.compare(i, j); // Stable for duplicates
        });

        int[] order = new int[len];
        for (int i = 0; i < len; i++) order[i] = indices[i];
        return order;
    }

    public static String decrypt(String ciphertext, String key) {
        key = filterAlphanumeric(key);

        int[] keyOrder = getKeyOrder(key);
        int numCols = key.length();
        int numRows = (int) Math.ceil((double) ciphertext.length() / numCols);

        char[][] grid = new char[numRows][numCols];

        int k = 0;
        for (int colIndex : keyOrder) {
            for (int r = 0; r < numRows; r++) {
                if (k < ciphertext.length()) {
                    grid[r][colIndex] = ciphertext.charAt(k++);
                }
            }
        }

        StringBuilder plaintext = new StringBuilder();
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                plaintext.append(grid[r][c]);
            }
        }

        // Optionally trim padding:
        int lastRealChar = plaintext.length();
        while (lastRealChar > 0 && plaintext.charAt(lastRealChar - 1) == 'X') lastRealChar--;
        return plaintext.substring(0, lastRealChar);
    }
    public static void main(String[] args) {
        String base64Data = "LjdYODVYNjc=";
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        String decodedString = new String(decodedBytes);
        String decryptedText = decrypt(decodedString, "OKJPOQOK");
        System.out.println("Decrypted text: " + decryptedText);
    }
}