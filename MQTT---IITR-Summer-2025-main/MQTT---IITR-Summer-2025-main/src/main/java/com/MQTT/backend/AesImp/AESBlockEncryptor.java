package com.MQTT.backend.AesImp;


public class AESBlockEncryptor {
    private static final int Nr = 10;

    public static byte[] encryptBlock(byte[] input, byte[] key) {
        byte[][] state = new byte[4][4];
        byte[][] roundKeys = keyExpansion(key);

        for (int i = 0; i < 16; i++) {
            state[i % 4][i / 4] = input[i];
        }

        addRoundKey(state, roundKeys, 0);

        for (int round = 1; round < Nr; round++) {
            subBytes(state);
            shiftRows(state);
            mixColumns(state);
            addRoundKey(state, roundKeys, round);
        }

        subBytes(state);
        shiftRows(state);
        addRoundKey(state, roundKeys, Nr);

        byte[] output = new byte[16];
        for (int i = 0; i < 16; i++) {
            output[i] = state[i % 4][i / 4];
        }

        return output;
    }

    // ========== AES Transformation Steps ==========

    private static void subBytes(byte[][] state) {
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                state[i][j] = (byte) AesUtils.SBOX[state[i][j] & 0xFF];
               
            }
        }
    }

    private static void shiftRows(byte[][] state) {
        byte temp;
        // Row 1
        temp = state[1][0];
        state[1][0] = state[1][1];
        state[1][1] = state[1][2];
        state[1][2] = state[1][3];
        state[1][3] = temp;

        // Row 2
        byte t0 = state[2][0], t1 = state[2][1];
        state[2][0] = state[2][2];
        state[2][1] = state[2][3];
        state[2][2] = t0;
        state[2][3] = t1;

        // Row 3
        temp = state[3][3];
        state[3][3] = state[3][2];
        state[3][2] = state[3][1];
        state[3][1] = state[3][0];
        state[3][0] = temp;
    }

    private static void mixColumns(byte[][] state) {
        // Galois field multiply (simplified version)
        for (int c = 0; c < 4; c++) {
            byte a0 = state[0][c], a1 = state[1][c], a2 = state[2][c], a3 = state[3][c];
            state[0][c] = (byte)(mul(2,a0)^mul(3,a1)^a2^a3);
            state[1][c] = (byte)(a0^mul(2,a1)^mul(3,a2)^a3);
            state[2][c] = (byte)(a0^a1^mul(2,a2)^mul(3,a3));
            state[3][c] = (byte)(mul(3,a0)^a1^a2^mul(2,a3));
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

    public static byte[][] keyExpansion(byte[] key) {
        byte[][] w = new byte[44][4];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                w[i][j] = key[4 * i + j];

        for (int i = 4; i < 44; i++) {
            byte[] temp = w[i - 1].clone();
            if (i % 4 == 0) {
                temp = subWord(rotWord(temp));
                temp[0] ^= (byte)(RCON[i / 4]);
            }
            for (int j = 0; j < 4; j++) {
                w[i][j] = (byte)(w[i - 4][j] ^ temp[j]);
            }
        }
        return w;
    }

    private static byte[] rotWord(byte[] word) {
        return new byte[]{ word[1], word[2], word[3], word[0] };
    }

    private static byte[] subWord(byte[] word) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++)
            result[i] = (byte) AesUtils.SBOX[word[i] & 0xFF];
        return result;
    }

    private static final int[] RCON = {
        0x00, 0x01, 0x02, 0x04,
        0x08, 0x10, 0x20, 0x40,
        0x80, 0x1B, 0x36
    };
}
