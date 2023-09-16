package com.github.jaykkumar01.testngpeerjs;;

import android.util.Base64;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class Base {


    public static String convertFileToBase64(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            fileInputStream.close();

            byte[] bytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static boolean convertBase64ToFile(String base64String, File outputFile) {

        try {
            byte[] data = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                data = java.util.Base64.getDecoder().decode(base64String);
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(data);
                fos.flush();
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static byte[] convertToBytes(File file) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                return Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static boolean writeToFile(byte[] bytes, File file) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
