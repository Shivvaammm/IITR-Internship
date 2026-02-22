package com.MQTT.backend.CeaserCipher;

import java.io.*;

public class BasicEnc {
    public static String shiftCipherEncrypt(String message, int key){
        StringBuffer sBuffer=new StringBuffer();
        for(char c:message.toLowerCase().toCharArray()){
            if(c>='0' && c<='9'){
                sBuffer.append((char) ((c - 'a' + key ) % 26 + 'a'));
            }
            else{
                sBuffer.append(' ');
            }
        }
        return sBuffer.toString();
    }
    public static String shiftCipherDecrypt(String message, int key){
        StringBuffer sBuffer=new StringBuffer();
        for(char c:message.toLowerCase().toCharArray()){
            if(c>='a' && c<='z'){
                sBuffer.append((char) ((c - 'a' - key + 26) % 26 + 'a'));
            }
            else{
                sBuffer.append(' ');
            }
        }
        return sBuffer.toString();
    }

    // Encrypt method
    public static String encryptFloat(String number, int shift) {
        StringBuilder encrypted = new StringBuilder();

        for (int i = 0; i < number.length(); i++) {
            char ch = number.charAt(i);
            if (ch == '.') {
                encrypted.append('$'); // Replace decimal with backslash
            } else {
                int digit = Integer.parseInt(ch+"");
                int shifted = (digit + shift) % 10;
                encrypted.append(shifted);
            }
        }

        return encrypted.toString();
    }

    // Decrypt method
    public static String decryptFloat(String encrypted, int shift) {
        StringBuilder decrypted = new StringBuilder();

        for (int i = 0; i < encrypted.length(); i++) {
            char ch = encrypted.charAt(i);
            if (ch == '$') {
                decrypted.append('.');
            } else {
                int digit = Integer.parseInt(ch+"");
                int original = ((digit - shift) % 10 + 10) % 10;;
                decrypted.append(original);
            }
        }

        return decrypted.toString();

    }
}




