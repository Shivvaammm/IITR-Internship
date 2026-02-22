package com.MQTT.backend.RateMonitor;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ExcelMessageListener implements IMqttMessageListener {

    private final ExcelLogger excelLogger;

    public ExcelMessageListener(ExcelLogger excelLogger) {
        this.excelLogger = excelLogger;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        System.out.println("[MQTT] Received → Topic: " + topic + ", Payload: " + payload);
        excelLogger.log(topic, payload);
    }
}
