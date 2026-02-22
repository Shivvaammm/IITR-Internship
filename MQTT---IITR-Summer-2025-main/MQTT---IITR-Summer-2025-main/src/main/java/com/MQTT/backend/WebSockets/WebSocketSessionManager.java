package com.MQTT.backend.WebSockets;

import com.MQTT.backend.Subscriber.MqttService;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.java_websocket.WebSocket;

import java.util.*;

public class WebSocketSessionManager {

    private final Set<WebSocket> allClients = new HashSet<>();
    private final Map<String, Set<WebSocket>> topicSubscribers = new HashMap<>();
    private final Map<WebSocket, Set<String>> clientTopics = new HashMap<>();
    private final Map<WebSocket, byte[]> clientAESKeys = new HashMap<>();
    private final Map<WebSocket, byte[]> clientIVs = new HashMap<>();


    // --------------------- Client Registration ---------------------

    public synchronized void registerClient(WebSocket conn) {
        allClients.add(conn);
        clientTopics.putIfAbsent(conn, new HashSet<>());
        System.out.println("✅ Registered client: " + conn.getRemoteSocketAddress());
    }

    public synchronized void unregisterClient(WebSocket conn, MqttService mqttService) {
        allClients.remove(conn);
        Set<String> topics = new HashSet<>(clientTopics.getOrDefault(conn, Collections.emptySet()));

        for (String topic : topics) {
            Set<WebSocket> subscribers = topicSubscribers.getOrDefault(topic, new HashSet<>());
            subscribers.remove(conn);
            if (subscribers.isEmpty()) {
                topicSubscribers.remove(topic);
                try {
                    mqttService.unsubscribe(topic);
                    System.out.println("🛑 MQTT unsubscribed from topic (after disconnect): " + topic);
                } catch (MqttException e) {
                    System.err.println("⚠️ Error unsubscribing from topic " + topic + ": " + e.getMessage());
                }
            }
        }

        clientTopics.remove(conn);
        clientAESKeys.remove(conn);
        clientIVs.remove(conn);

        System.out.println("❌ Unregistered client: " + conn.getRemoteSocketAddress());
    }

    // --------------------- Topic Subscriptions ---------------------

    public synchronized void subscribeClientToTopic(WebSocket conn, String topic) {
        topicSubscribers.computeIfAbsent(topic, k -> new HashSet<>()).add(conn);
        clientTopics.computeIfAbsent(conn, k -> new HashSet<>()).add(topic);
        System.out.println("🔗 Client subscribed to topic [" + topic + "]: " + conn.getRemoteSocketAddress());
    }

    public synchronized void unsubscribeClientFromTopic(WebSocket conn, String topic, MqttService mqttService) {
        Set<WebSocket> subscribers = topicSubscribers.get(topic);
        if (subscribers != null) {
            subscribers.remove(conn);
            if (subscribers.isEmpty()) {
                topicSubscribers.remove(topic);
                try {
                    mqttService.unsubscribe(topic);
                    System.out.println("🛑 MQTT unsubscribed from topic: " + topic);
                } catch (Exception e) {
                    System.err.println("⚠️ Failed to unsubscribe from topic " + topic + ": " + e.getMessage());
                }
            }
        }

        Set<String> topics = clientTopics.get(conn);
        if (topics != null) {
            topics.remove(topic);
            if (topics.isEmpty()) {
                clientTopics.remove(conn);
            }
        }

        System.out.println("🔌 Client unsubscribed from topic [" + topic + "]: " + conn.getRemoteSocketAddress());
    }

    public synchronized List<WebSocket> getClientsSubscribedTo(String topic) {
        return new ArrayList<>(topicSubscribers.getOrDefault(topic, Collections.emptySet()));
    }

    // --------------------- AES Key and IV Management ---------------------

    public synchronized void setAESKey(WebSocket conn, byte[] key) {
        clientAESKeys.put(conn, key);
        System.out.println("🔐 AES key set for client: " + conn.getRemoteSocketAddress());
    }

    public synchronized byte[] getAESKey(WebSocket conn) {
        return clientAESKeys.get(conn);
    }

    public synchronized void setIV(WebSocket conn, byte[] iv) {
        clientIVs.put(conn, iv);
        System.out.println("🧩 IV set for client: " + conn.getRemoteSocketAddress());
    }

    public synchronized byte[] getIV(WebSocket conn) {
        return clientIVs.get(conn);
    }
}
