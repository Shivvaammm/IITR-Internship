package com.MQTT.backend.TranspositionCipher;

import java.util.Base64;
import java.util.Comparator;

public class TranspositionCipher {

    public static String encrypt(String plaintext, String key) {
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
                    grid[r][c] = 'X'; // padding
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
        Integer[] indices = new Integer[len];
        for (int i = 0; i < len; i++) {
            indices[i] = i;
        }
        java.util.Arrays.sort(indices, Comparator.comparingInt(key::charAt));

        int[] order = new int[len];
        for (int i = 0; i < len; i++) {
            order[i] = indices[i];
        }

        return order;
    }
    public static String decrypt(String ciphertext, String key) {
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

        // Read the grid row-wise to get plaintext
        StringBuilder plaintext = new StringBuilder();
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                plaintext.append(grid[r][c]);
            }
        }

        return plaintext.toString();
    }

    public static void main(String[] args) {
            var a= Base64.getDecoder().decode("QlcgRyBBT09DWVRRSw==");
        System.out.println(decrypt(new String(a),"tKGokkk"));
    }
}
