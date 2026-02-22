package com.MQTT.backend.Subscriber;

import com.MQTT.backend.TranspositionCipher.TranspositionCipher;
import com.MQTT.backend.VigenereCipher.VigenereCipher;
import  com.MQTT.backend.WebSockets.*;
import com.MQTT.backend.Main;
import com.MQTT.backend.AesImp.AESCBCEncryptorDecryptor;
import com.MQTT.backend.AlphaNumerics.AlphaNumTranspositionCipher;
import com.MQTT.backend.AlphaNumerics.AlphaNumVigenreCipher;
import com.MQTT.backend.AlphaNumerics.AlphanumericCeaserEnc;
// import com.MQTT.backend.RateMonitor.ExcelLogger;
import com.MQTT.backend.CeaserCipher.BasicEnc;

import org.eclipse.paho.client.mqttv3.*;
import org.java_websocket.WebSocket;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.cert.X509Certificate;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;



public class MqttService {
    private final int brokerKey=73;  //if data is encrypted with caeser cipher on the esp32 side
    private final MqttClient client;
    private final WebSocketSessionManager sessionManager;
    // private final ExcelLogger excelLogger = new ExcelLogger();
    // Map topic -> isSubscribed (to avoid resubscribing multiple times)
    private final Map<String, Boolean> subscribedTopics = new HashMap<>();

    public MqttService(String broker, String clientId, String username, String password, WebSocketSessionManager sessionManager) throws Exception{
        this.client = new MqttClient(broker, clientId);
        this.sessionManager = sessionManager;

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
    System.setProperty("javax.net.ssl.trustStore", "src/main/java/com/MQTT/backend/certs/truststore.jks");
    System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        if (username != null && password != null) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.err.println("⚠️ MQTT connection lost");
            }


            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String encPayload = new String(message.getPayload(), StandardCharsets.UTF_8);
                // String encPayload= AlphanumericCeaserEnc.decrypt(encPayload,brokerKey);
                System.out.println(encPayload);
                System.out.println("📥 MQTT message arrived on topic [" + topic + "]: " + encPayload);

                // Broadcast encrypted message to all clients subscribed to this topic
                for (WebSocket conn : sessionManager.getClientsSubscribedTo(topic)) {
                    // <-- Added check to skip disconnected clients
                    if (conn.isClosed() || conn.isClosing()) {
                        System.out.println("Skipping closed/disconnected client: " + conn.getRemoteSocketAddress());
                        continue;
                    }
                    if(!MyWebSocketServer.clientEnc.containsKey(conn)){
                        conn.send("error no encryption type selected");
                        return;
                    }
                    try {
                        switch (MyWebSocketServer.clientEnc.get(conn)) {
                            case 2 -> {
                                byte[] aesKey = sessionManager.getAESKey(conn);
                                byte[] iv = sessionManager.getIV(conn);
                                if (aesKey == null || iv == null) {
                                    System.out.println("❌ No AES key/IV for client, skipping encryption");
                                    continue;
                                }

                                byte[] encrypted = AESCBCEncryptorDecryptor.encryptCBC(encPayload.getBytes(StandardCharsets.UTF_8), aesKey, iv);
                                String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);

                                // Send encrypted message to client
                                conn.send("data: " + topic + " " + encryptedBase64);
                            }
                            case 1 -> {
                                byte[] encrypted = AlphanumericCeaserEnc.encrypt(encPayload, Main.key).getBytes();
                                String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
                                conn.send("data: " + topic + " " + encryptedBase64);
                            }
                            case 5 ->{
                                var pcf= MyWebSocketServer.clientToPfcMatrix.get(conn);
                                if(pcf!=null) {
                                    byte[] encrypted = pcf.encrypt(encPayload).getBytes();
                                    String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
                                    conn.send("data: " + topic + " " + encryptedBase64);
                                    System.out.println(encryptedBase64);

                                }
                                else{
                                    conn.send("Something wrong in encryption using PFC");
                                }
                            }case 4 ->{
                                    var key=MyWebSocketServer.clientToTPCKey.get(conn);
                                if(key!=null) {
                                    byte[] encrypted = TranspositionCipher.encrypt(encPayload,key.toUpperCase().strip()).getBytes();
                                    String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
                                    conn.send("data: " + topic + " " + encryptedBase64);
                                    System.out.println(encryptedBase64);

                                }
                                else{
                                    conn.send("Something wrong in encryption using PFC");
                                }
                            }case 3 ->{
                                    var key=MyWebSocketServer.clientToVignereKey.get(conn);
                                if(key!=null) {
                                    byte[] encrypted = VigenereCipher.encrypt(encPayload,key.toUpperCase().strip()).getBytes();
                                    String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
                                    conn.send("data: " + topic + " " + encryptedBase64);
                                    System.out.println(encryptedBase64);

                                }
                                else{
                                    conn.send("Something wrong in encryption using Vingenre Cipher");
                                }
                            }
                            case 8-> {
                                   var pcfa= MyWebSocketServer.clientToPfcAMatrix.get(conn);
                                if(pcfa!=null) {
                                    byte[] encrypted = pcfa.encrypt(encPayload).getBytes();
                                    String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
                                    conn.send("data: " + topic + " " + encryptedBase64);
                                    System.out.println(encryptedBase64);
                                }
                            }
                            case 6->{
                                       var key=MyWebSocketServer.clientToVignereKey.get(conn);
                                if(key!=null) {
                                    byte[] encrypted = AlphaNumVigenreCipher.encrypt(encPayload,key.toUpperCase().strip()).getBytes();
                                    String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
                                    conn.send("data: " + topic + " " + encryptedBase64);
                                    System.out.println(encryptedBase64);

                                }
                                else{
                                    conn.send("Something wrong in encryption using Vingenre Cipher");
                                }
                            }
                            case 7->{
                                 var key=MyWebSocketServer.clientToTPCKey.get(conn);
                                if(key!=null) {
                                    byte[] encrypted = AlphaNumTranspositionCipher.encrypt(encPayload,key.toUpperCase().strip()).getBytes();
                                    String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
                                    conn.send("data: " + topic + " " + encryptedBase64);
                                    System.out.println(encryptedBase64);
                                }
                            }
                        }

                    } catch (Exception e) {
                        System.err.println("Error encrypting/sending MQTT message to client: " + e.getMessage());
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("Delivery not complete");
            }
        });
       

        client.connect(options);
        System.out.println("✅ MQTT connected to broker: " + broker);
    }

    public void unsubscribe(String topic) throws MqttException {
        client.unsubscribe(topic);
        subscribedTopics.remove(topic);
    }

    // Subscribe client to topic, store AES key + IV for that client
    public synchronized void subscribe(String topic, WebSocket clientConn) throws MqttException {
        // Subscribe once to the MQTT topic (if not already subscribed)
        if (!subscribedTopics.containsKey(topic)) {
            client.subscribe(topic);
            subscribedTopics.put(topic, true);
            System.out.println("Subscribed to MQTT topic: " + topic);
        }

        // Add client subscription in WebSocketSessionManager
        sessionManager.subscribeClientToTopic(clientConn, topic);

    }

 // Create SSL socket factory with CA cert
    private static SSLSocketFactory getSocketFactory(String caCrtFile) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        FileInputStream fis = new FileInputStream(caCrtFile);
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(fis);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("caCert", caCert);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), new SecureRandom());
        return context.getSocketFactory();
    }
}
