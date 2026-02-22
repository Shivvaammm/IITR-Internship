package com.MQTT.backend.AesImp;

import java.util.Arrays;

public class AESBlockDecryptor {
    private static final int Nr = 10;

    public static byte[] decryptBlock(byte[] input, byte[] key) {
        byte[][] state = new byte[4][4];
        byte[][] roundKeys = AESBlockEncryptor.keyExpansion(key); // Use same key expansion

        for (int i = 0; i < 16; i++) {
            state[i % 4][i / 4] = input[i];
        }

        addRoundKey(state, roundKeys, Nr);

        for (int round = Nr - 1; round >= 1; round--) {
            invShiftRows(state);
            invSubBytes(state);
            addRoundKey(state, roundKeys, round);
            invMixColumns(state);
        }

        invShiftRows(state);
        invSubBytes(state);
        addRoundKey(state, roundKeys, 0);

        byte[] output = new byte[16];
        for (int i = 0; i < 16; i++) {
            output[i] = state[i % 4][i / 4];
        }

        return output;
    }

    private static void invSubBytes(byte[][] state) {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                state[i][j] = (byte) AesUtils.INV_SBOX[state[i][j] & 0xFF];
    }

    private static void invShiftRows(byte[][] state) {
        byte temp;
        // Row 1
        temp = state[1][3];
        state[1][3] = state[1][2];
        state[1][2] = state[1][1];
        state[1][1] = state[1][0];
        state[1][0] = temp;

        // Row 2
        byte t0 = state[2][0], t1 = state[2][1];
        state[2][0] = state[2][2];
        state[2][1] = state[2][3];
        state[2][2] = t0;
        state[2][3] = t1;

        // Row 3
        temp = state[3][0];
        state[3][0] = state[3][1];
        state[3][1] = state[3][2];
        state[3][2] = state[3][3];
        state[3][3] = temp;
    }

    private static void invMixColumns(byte[][] state) {
        for (int c = 0; c < 4; c++) {
            byte a0 = state[0][c], a1 = state[1][c], a2 = state[2][c], a3 = state[3][c];
            state[0][c] = (byte)(mul(0x0e,a0)^mul(0x0b,a1)^mul(0x0d,a2)^mul(0x09,a3));
            state[1][c] = (byte)(mul(0x09,a0)^mul(0x0e,a1)^mul(0x0b,a2)^mul(0x0d,a3));
            state[2][c] = (byte)(mul(0x0d,a0)^mul(0x09,a1)^mul(0x0e,a2)^mul(0x0b,a3));
            state[3][c] = (byte)(mul(0x0b,a0)^mul(0x0d,a1)^mul(0x09,a2)^mul(0x0e,a3));
        }
    }

    private static byte mul(int a, byte b) {
        byte result = 0;
        for (int i = 0; i < 8; i++) {
            if ((a & 1) != 0) result ^= b;
            boolean hiBitSet = (b & 0x80) != 0;
            b <<= 1;
            if (hiBitSet) b ^= 0x1b;
            a >>= 1;
        }
        return result;
    }

    private static void addRoundKey(byte[][] state, byte[][] w, int round) {
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                state[row][col] ^= w[round * 4 + col][row];
            }
        }
    }
    public static byte[] removePKCS7Padding(byte[] data) {
    int paddingLength = data[data.length - 1] & 0xFF; // Unsigned byte

    // Validate padding
    if (paddingLength < 1 || paddingLength > 16 || paddingLength > data.length) {
        throw new IllegalArgumentException("Invalid PKCS7 padding length: " + paddingLength);
    }

    // Optional: Validate that last N bytes == paddingLength
    for (int i = data.length - paddingLength; i < data.length; i++) {
        if ((data[i] & 0xFF) != paddingLength) {
            throw new IllegalArgumentException("Invalid PKCS7 padding bytes");
        }
    }

    return Arrays.copyOf(data, data.length - paddingLength);
}

}
