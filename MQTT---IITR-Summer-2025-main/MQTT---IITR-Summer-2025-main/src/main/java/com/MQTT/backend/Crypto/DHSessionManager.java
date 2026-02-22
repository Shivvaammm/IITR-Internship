package com.MQTT.backend.Crypto;

import com.MQTT.backend.AesImp.AESCBCEncryptorDecryptor;
import org.java_websocket.WebSocket;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DHSessionManager {

    private final BigInteger p;  // Large prime
    private final BigInteger g;  // Generator

    // Stores client → shared AES key
    private final Map<WebSocket, byte[]> sharedKeys = new HashMap<>();

    private final SecureRandom random = new SecureRandom();

    public DHSessionManager() {
        // You can generate or hardcode these safe prime and generator values
        // Here, example 2048-bit prime (RFC 3526 group 14)
        String hexP = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                "E485B576625E7EC6F44C42E9A63A3620FFFFFFFFFFFFFFFF";
        this.p = new BigInteger(hexP, 16);
        this.g = BigInteger.valueOf(2);  // generator

    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getG() {
        return g;
    }

    // Compute shared secret and store a 128-bit AES key derived from it
    public byte[] computeSharedSecret(BigInteger clientPubKey, BigInteger serverPrivate) throws Exception {
        // shared secret = clientPubKey^serverPrivate mod p
        BigInteger sharedSecret = clientPubKey.modPow(serverPrivate, getP());
        return toUnsignedBytes(sharedSecret);
    }

    // Retrieve stored AES key for a client
    public byte[] get128BitKey(WebSocket client) {
        return sharedKeys.get(client);
    }

    // Remove client session
    public void removeKey(WebSocket client) {
        sharedKeys.remove(client);
    }

    public static byte[] toUnsignedBytes(BigInteger bigInt) {
        byte[] bytes = bigInt.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0x00) {
            // Strip the leading 0x00 sign byte
            return Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return bytes;
    }
}
