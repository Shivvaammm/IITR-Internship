package com.MQTT.backend.ClientSession;

import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;

public class ClientSessionManager {

    private final Map<WebSocket, ClientSession> sessions = new HashMap<>();

    public synchronized void registerClient(WebSocket conn) {
        sessions.put(conn, new ClientSession(conn));
        System.out.println("Registered client session: " + conn.getRemoteSocketAddress());
    }

    public synchronized void unregisterClient(WebSocket conn) {
        sessions.remove(conn);
        System.out.println("Unregistered client session: " + conn.getRemoteSocketAddress());
    }

    public synchronized ClientSession getSession(WebSocket conn) {
        return sessions.get(conn);
    }

    public synchronized boolean hasSession(WebSocket conn) {
        return sessions.containsKey(conn);
    }
}
