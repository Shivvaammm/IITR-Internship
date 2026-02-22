package com.MQTT.backend.RateMonitor;

import java.util.concurrent.CountDownLatch;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import com.MQTT.backend.Main;

public class MQTTExcelLoggerService implements Runnable {
    private final ExcelLogger excelLogger;
    private final CountDownLatch latch = new CountDownLatch(1);

    public MQTTExcelLoggerService(ExcelLogger excelLogger) {
        this.excelLogger = excelLogger;
    }

    @Override
    public void run() {
        try {
            String broker = Main.BROKER_ADDRESS; // Use your broker IP
            String clientId = "ExcelLoggerClient";

            IMqttClient client = new MqttClient(broker, clientId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

    // If using certificates, add this:
            // options.setSocketFactory(MySSLSocketFactory.getSocketFactory(...));

            client.connect(options);

            // listener
            client.subscribe("#", new ExcelMessageListener(excelLogger));

            System.out.println("Subscribed to all topics. Listening...");

            // Keep the thread alive
            latch.await();

        } catch (Exception e) {
            System.err.println("MQTTExcelLoggerService error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
