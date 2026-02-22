package com.MQTT.backend.WebSockets;

import com.MQTT.backend.Crypto.DHSessionManager;
import com.MQTT.backend.Main;
import com.MQTT.backend.AlphaNumerics.AlphaNumPlayFair;
import com.MQTT.backend.PlayFairCipher.PlayFairCipher;
import com.MQTT.backend.Subscriber.MqttService;
import com.MQTT.backend.TranspositionCipher.TranspositionCipher;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class MyWebSocketServer extends WebSocketServer {

    private final WebSocketSessionManager sessionManager;
    private final DHSessionManager dhManager;
    private MqttService mqttService;
    // Diffie-Hellman parameters (prime p and generator g)
    private final BigInteger p ;
    private final BigInteger g ;

    // Store server private keys per client connection
    public static Map<WebSocket, BigInteger> serverPrivates = new java.util.HashMap<>();

    public static Map<WebSocket, Integer> clientEnc=new HashMap<>();
    public static Map<WebSocket, PlayFairCipher> clientToPfcMatrix=new HashMap<>();
    public static Map<WebSocket, AlphaNumPlayFair> clientToPfcAMatrix=new HashMap<>();
    public static Map<WebSocket,String> clientToTPCKey=new HashMap<>();
    public static Map<WebSocket,String> clientToVignereKey=new HashMap<>();

    public MyWebSocketServer(int port, WebSocketSessionManager sessionManager, DHSessionManager dhManager) {
        super(new InetSocketAddress(port));
        this.sessionManager = sessionManager;
        this.dhManager = dhManager;
        this.p = dhManager.getP();
        this.g = dhManager.getG();
    }

    public void setMqttService(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        sessionManager.registerClient(conn);
        System.out.println("Client connected: " + conn.getRemoteSocketAddress());


        conn.send("🔐 Send your DH public key with prefix 'start-dh:'");

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        sessionManager.unregisterClient(conn,mqttService);
        dhManager.removeKey(conn);
        serverPrivates.remove(conn);
        clientEnc.remove(conn);
        serverPrivates.remove(conn);
        clientToPfcMatrix.remove(conn);
        clientToTPCKey.remove(conn);
        clientToVignereKey.remove(conn);
        clientToPfcAMatrix.remove(conn);
        System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            if(message.startsWith("type:")){
                    Integer encType=Integer.parseInt(message.substring("type:".length()).strip());
                    clientEnc.put(conn,encType);
                    switch ( encType){
                        case 2 -> {       conn.send("p:"+dhManager.getP().toString());
                            conn.send("g:"+dhManager.getG().toString());
                        }
                        case 1->   conn.send("shift:"+ Main.key); //caser cipher
                        case 5-> {
                            var key=PlayFairCipher.generateRandomAlphabetString(8);
                            conn.send("PFCKey:"+key);
                            clientToPfcMatrix.put(conn,new PlayFairCipher(key));
                        }
                        case 4->{
                            var key=PlayFairCipher.generateRandomAlphabetString(8);
                            conn.send("TCKey:"+key.toUpperCase());
                            clientToTPCKey.put(conn,key);
                        }
                        case 3->{
                            var key=PlayFairCipher.generateRandomAlphabetString(8);
                            conn.send("VCKey:"+key.toUpperCase());
                            clientToVignereKey.put(conn,key);
                        }
                       case 8->{
                         var key=PlayFairCipher.generateRandomAlphabetString(8);
                            conn.send("PFCAKey:"+key.toUpperCase());
                            clientToPfcAMatrix.put(conn,new AlphaNumPlayFair(key));
                       }
                        case 6->{
                            var key=PlayFairCipher.generateRandomAlphabetString(8);
                            conn.send("VCAKey:"+key.toUpperCase());
                            clientToVignereKey.put(conn,key);
                        }
                      case 7 ->{
                           var key=PlayFairCipher.generateRandomAlphabetString(8);
                            conn.send("TCAKey:"+key.toUpperCase());
                            clientToTPCKey.put(conn,key);
                        
                      }
                        default -> conn.send("SOMETHING WENT WRONG :(");
                    }
             }
             // this only executes for AES enc
            if (message.startsWith("start-dh:")) {
                if(clientEnc.get(conn)!=2)  {
                    conn.send( "cannot start key exchange wrong encryption type selected");
                    return ;}
                // 1. Client sends its DH public key
                BigInteger clientPubKey = new BigInteger(message.substring("start-dh:".length()));
                // 2. Server generates its own private key
                BigInteger serverPrivate = new BigInteger(256, new SecureRandom());

                // 3. Compute server's public key (g^priv mod p)
                BigInteger serverPublic = g.modPow(serverPrivate, p);
                // 4. Store server private key for this client
                serverPrivates.put(conn, serverPrivate);

                // 5. Compute shared secret: (clientPubKey ^ serverPrivate) mod p
                byte[] sharedSecret = dhManager.computeSharedSecret(clientPubKey,serverPrivate);

                //                byte[] secretBytes = Arrays.copyOfRange(sharedBytes,2,sharedBytes.length);

                // Example: Use first 16 bytes as AES key and next 16 as IV (padding if necessary)
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                byte[] hash = sha256.digest(sharedSecret);
// First 16 bytes = AES key
                byte[] aesKey = Arrays.copyOfRange(hash, 0, 16);


// Next 16 bytes = IV
                byte[] iv = Arrays.copyOfRange(hash, 16, 32);


                // 7. Store AES key and IV in session manager for this client
                sessionManager.setAESKey(conn, aesKey);
                sessionManager.setIV(conn, iv);


                // 8. Send server's public key back to client

                conn.send("dh-server-pub:" + serverPublic.toString());


                System.out.println("DH key exchange completed with client: " + conn.getRemoteSocketAddress());
                return;
            }
            if (message.startsWith("unsubscribe:")) {
                String topic = message.substring("unsubscribe:".length()).trim();
                sessionManager.unsubscribeClientFromTopic(conn, topic, mqttService);
                return;
            }

            if (message.startsWith("subscribe:")) {
                // Client wants to subscribe to a topic
                String topic = message.substring("subscribe:".length()).trim();
                String[] params=topic.split(" ");
//                if(!params[1].equals( ArgsHolder.argsHolder[0] )|| !params[2].equals(ArgsHolder.argsHolder[1])){
//                    conn.send("data:Auth failed cannot subscribe");
//                    return;
//                }
                switch (clientEnc.get(conn)) {
                   case 1->{

                       mqttService.subscribe(params[0], conn);

                   }
                    case 2 -> {
                        // Retrieve AES key and IV for this client
                        byte[] aesKey = sessionManager.getAESKey(conn);
                        byte[] iv = sessionManager.getIV(conn);

                        if (aesKey == null || iv == null) {
                            conn.send("error:DH key exchange incomplete, cannot subscribe");
                            return;
                        }

                        // Subscribe client to MQTT topic with encryption keys
                        mqttService.subscribe(params[0], conn);

                    }
                    case 4 ->{
                       if(clientEnc.containsKey(conn)){
                           mqttService.subscribe(params[0],conn);
                       }
                       else {
                           conn.send("failed to subscribe , no key found for Transposition Cipher");
                       }
                    }
                    case 5 ->{
                                    if(clientToPfcMatrix.containsKey(conn)){
                                        mqttService.subscribe(params[0],conn);
                                    }
                                    else {
                                        conn.send("failed to subscribe , no key found");
                                    }

                    }case 3 ->{
                                    if(clientToVignereKey.containsKey(conn)){
                                        mqttService.subscribe(params[0],conn);
                                    }
                                    else {
                                        conn.send("failed to subscribe , no key found");
                                    }
                    
                    }
                    case 8 ->{
                         if(clientToPfcAMatrix.containsKey(conn)){
                                        mqttService.subscribe(params[0],conn);
                                    }
                                    else {
                                        conn.send("failed to subscribe , no key found");
                                    }
                    }
                    case 6->{
                          if(clientToVignereKey.containsKey(conn)){
                                        mqttService.subscribe(params[0],conn);
                                    }
                                    else {
                                        conn.send("failed to subscribe , no key found");
                                    }
                    }
                    case 7->{
                             if(clientEnc.containsKey(conn)){
                           mqttService.subscribe(params[0],conn);
                       }
                       else {
                           conn.send("failed to subscribe , no key found for Transposition Cipher Advance");
                       }
                    }
                    default->
                        conn.send("error:Unsupported encryption type");


                }


            }

            // handle other message types (unsubscribe, ping, etc.) here

        } catch (Exception e) {
            e.printStackTrace();
            conn.send("error:" + e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
        if (conn != null && conn.isOpen()) {
            conn.send("error:" + ex.getMessage());
        }
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started on port " + getPort());
    }

}
