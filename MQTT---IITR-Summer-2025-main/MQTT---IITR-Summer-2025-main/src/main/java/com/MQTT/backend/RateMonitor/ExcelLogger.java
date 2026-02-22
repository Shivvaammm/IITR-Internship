package com.MQTT.backend.RateMonitor;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelLogger {

    private static final String FILE_PATH = "mqtt_message_log.xlsx";
    private Workbook workbook;
    private Sheet sheet;
    private int rowCount;

    public ExcelLogger() {
        try {
            File file = new File(FILE_PATH);

            // Check for corrupt (empty) file and delete it
            if (file.exists() && file.length() == 0) {
                System.out.println("Detected empty Excel file. Deleting and recreating...");
                boolean deleted = file.delete();
                if (!deleted) {
                    System.err.println("Failed to delete corrupt Excel file. Logger may not work.");
                }
            }

            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    workbook = new XSSFWorkbook(fis);
                    sheet = workbook.getSheetAt(0);
                    rowCount = sheet.getLastRowNum() + 1;
                }
            } else {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("MQTT Logs");

                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Timestamp");
                header.createCell(1).setCellValue("Topic");
                header.createCell(2).setCellValue("Payload");
                rowCount = 1;
                saveToFile(); // create the file immediately with headers
            }

        } catch (IOException e) {
            System.err.println("Failed to initialize ExcelLogger: " + e.getMessage());
        }
    }

    public synchronized void log(String topic, String payload) {
        try {
            if (workbook == null || sheet == null) return;

            Row row = sheet.createRow(rowCount++);
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            row.createCell(0).setCellValue(timestamp);
            row.createCell(1).setCellValue(topic);
            row.createCell(2).setCellValue(payload);
            saveToFile();
        } catch (Exception e) {
            System.err.println("Error writing log entry: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
            workbook.write(fos);
        } catch (IOException e) {
            System.err.println("Error saving Excel file: " + e.getMessage());
        }
    }
}
