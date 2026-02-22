package com.MQTT.backend.ClientSession;

import org.java_websocket.WebSocket;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class ClientSession {
    private final WebSocket conn;
    private Set<String> subscribedTopics = new HashSet<>();

    // DH related
    private BigInteger publicKey;   // Client's public key from DH exchange
    private BigInteger sharedSecret; // Shared secret after DH key exchange

    public ClientSession(WebSocket conn) {
        this.conn = conn;
    }

    public WebSocket getConn() {
        return conn;
    }

    public Set<String> getSubscribedTopics() {
        return subscribedTopics;
    }

    public void subscribeTopic(String topic) {
        subscribedTopics.add(topic);
    }

    public void unsubscribeTopic(String topic) {
        subscribedTopics.remove(topic);
    }

    public void clearSubscriptions() {
        subscribedTopics.clear();
    }

    // DH key exchange setters/getters
    public void setPublicKey(BigInteger publicKey) {
        this.publicKey = publicKey;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public void setSharedSecret(BigInteger sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public BigInteger getSharedSecret() {
        return sharedSecret;
    }
}
