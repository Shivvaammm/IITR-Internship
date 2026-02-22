package com.MQTT.backend.AesImp;

import java.util.Arrays;

public class AESCBCEncryptorDecryptor {
    public static byte[] encryptCBC(byte[] plaintext, byte[] key, byte[] iv) {
        plaintext = AesUtils.pad(plaintext);
        byte[][] blocks = AesUtils.splitIntoBlocks(plaintext);
        byte[][] ciphertext = new byte[blocks.length][16];

        byte[] prev = iv.clone();

        for (int i = 0; i < blocks.length; i++) {
            byte[] xored = AesUtils.xorBlocks(blocks[i], prev);
            byte[] encrypted = AESBlockEncryptor.encryptBlock(xored, key);
            ciphertext[i] = encrypted;
            prev = encrypted;
        }
        return AesUtils.joinBlocks(ciphertext);
    }

    public static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
        sb.append(String.format("%02X", b));
    }
    return sb.toString();
}
public static byte[] decryptCBC(byte[] ciphertext, byte[] key, byte[] iv) {
        byte[] plaintext = new byte[ciphertext.length];
        byte[] prevBlock = Arrays.copyOf(iv, 16);

        for (int i = 0; i < ciphertext.length; i += 16) {
            byte[] block = Arrays.copyOfRange(ciphertext, i, i + 16);
            byte[] decrypted = AESBlockDecryptor.decryptBlock(block, key);
            byte[] original = AesUtils.xorBlocks(decrypted, prevBlock);
            System.arraycopy(original, 0, plaintext, i, 16);
            prevBlock = block;
        }

        return plaintext;
    }
}