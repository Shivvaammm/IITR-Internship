package com.MQTT.backend;

import com.MQTT.backend.Crypto.DHSessionManager;
import com.MQTT.backend.RateMonitor.ExcelLogger;
import com.MQTT.backend.RateMonitor.MQTTExcelLoggerService;
import com.MQTT.backend.Subscriber.MqttService;
import com.MQTT.backend.Utils.ArgsHolder;
import com.MQTT.backend.WebSockets.MyWebSocketServer;
import com.MQTT.backend.WebSockets.WebSocketSessionManager;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;


public class Main {

    //Generating one key for ceaser cipher enc at time of boot up, reuses this key throughout server lifetime for all connections 
    //todo : generate and map new key for every new connection
    // <---only for ceaser cipher--->
    public static  int key=ThreadLocalRandom.current().nextInt(1, 100);
      public static final String BROKER_ADDRESS= "tcp://192.168.40.62:1883";

    public static void main(String[] args) throws Exception {
        try {
         
    //<-------use terminal arguments for user name paswd auth ------------>
                   //currently TLS certificate are used w/o  id passwd auth
            //   if(args.length==0 || args.length>2){
            //   System.out.println("missing auth fields");
            //   return;
            //    }
        ExcelLogger logger = new ExcelLogger(); // Initializes or creates the Excel file
        Thread mqttLoggerThread = new Thread(new MQTTExcelLoggerService(logger));
        mqttLoggerThread.setDaemon(true); // Optional: exits with main program
        mqttLoggerThread.start();

            ArgsHolder.argsHolder=args;
            WebSocketSessionManager wsSessionManager = new WebSocketSessionManager();
            DHSessionManager dhSessionManager = new DHSessionManager();
            // 2. MQTT broker connection info
            String broker = BROKER_ADDRESS;
            String clientId = "JavaBackendClient";
            String username = "";  // or null
            String password = "";  // or null
          
            MqttService mqttService = new MqttService(broker, clientId, username, password, wsSessionManager);


            // 3. Initialize and start WebSocket server on port 8887
            MyWebSocketServer wsServer = new MyWebSocketServer(8887, wsSessionManager, dhSessionManager);

            // 4. Pass mqttService to wsServer so it can subscribe on client requests
            wsServer.setMqttService(mqttService);

            // 5. Start WebSocket server
            wsServer.setReuseAddr(true);
            wsServer.start();

            // . Keep main thread alive
            while (true) {
                Thread.sleep(1000);
            }

        } catch (MqttException e) {
            System.err.println("MQTT initialization failed:");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted:");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Log file error");
            e.printStackTrace();
        }
    }
}
