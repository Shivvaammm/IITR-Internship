package com.MQTT.backend.AlphaNumerics;

import java.io.*;

public class AlphanumericCeaserEnc {
 
 public static String encrypt(String input, int key) {
    StringBuilder result = new StringBuilder();
    int alphaKey = key % 26;
    int digitKey = key % 10;
    for (char c : input.toCharArray()) {
        if (c >= 'A' && c <= 'Z') {
            char shifted = (char) ((c - 'A' + alphaKey) % 26 + 'A');
            result.append(shifted);
        } else if (c >= 'a' && c <= 'z') {
            char shifted = (char) ((c - 'a' + alphaKey) % 26 + 'a');
            result.append(shifted);
        } else if (c >= '0' && c <= '9') {
            char shifted = (char) ((c - '0' + digitKey) % 10 + '0');
            result.append(shifted);
        } else {
            result.append(c);
        }
    }
    return result.toString();
}

public static String decrypt(String input, int key) {
    StringBuilder result = new StringBuilder();
    int alphaKey = key % 26;
    int digitKey = key % 10;
    for (char c : input.toCharArray()) {
        if (c >= 'A' && c <= 'Z') {
            char shifted = (char) ((c - 'A' - alphaKey + 26) % 26 + 'A');
            result.append(shifted);
        } else if (c >= 'a' && c <= 'z') {
            char shifted = (char) ((c - 'a' - alphaKey + 26) % 26 + 'a');
            result.append(shifted);
        } else if (c >= '0' && c <= '9') {
            char shifted = (char) ((c - '0' - digitKey + 10) % 10 + '0');
            result.append(shifted);
        } else {
            result.append(c);
        }
    }
    return result.toString();
}


public static void main(String[] args)  {
    var x=(encrypt("speed: 1405", 73));
    System.out.println(decrypt(x, 73));
}

   
}
