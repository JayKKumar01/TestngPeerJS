package com.github.jaykkumar01.testngpeerjs;

import android.content.Context;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JavaScriptInterface implements Data{
    Context context;
    AudioTrack audioTrack;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    ExecutorService executorService2 = Executors.newSingleThreadExecutor();

    List<ByteData> bytesList = new ArrayList<>();
    private boolean first;
    private int delay = 750;
    private int delayCount;
    private long sumDelay;


    public JavaScriptInterface(Context context,AudioTrack audioTrack){
        this.context = context;
        this.audioTrack = audioTrack;
    }

    @JavascriptInterface
    public void onClose(String id){
        PeerService.listener.onStop();
        Toast.makeText(context, "Closed: "+id, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void onConnected(){
        PeerService.listener.onConnected();
    }
    @JavascriptInterface
    public void showText(String txt){
        MainActivity.listener.onRead(txt);
    }


    @JavascriptInterface
    public void send(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private void saveBytesToFile(byte[] bytes, int read) {
        try {
            File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File filePath = new File(externalDir, "testing/received_audio.pcm");
            File destPath = new File(externalDir, "testing/received_audio.wav");
            FileOutputStream fos = new FileOutputStream(filePath, true); // Use "true" to append to the existing file
            fos.write(bytes, 0, read);
            fos.close();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PCM.rawToWave(filePath,destPath,SAMPLE_RATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @JavascriptInterface
    public void play(String id,byte[] bytes, int read, long millis) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long diff = System.currentTimeMillis() - millis;
                MainActivity.listener.onLoad("diff", (int) diff);

                if (++delayCount <= 10){
                    sumDelay += diff;

                    int avg = (int) (sumDelay/delayCount) + 200;
                    delay = avg/2;

//                    delay = (int) ((diff+0)/2);
//                    first = true;
                    MainActivity.listener.onSingleRead("Avg",avg);
                }

                int divider = (int) Math.max(1, diff / delay);
                audioTrack.write(bytes,0,read/divider);
                saveBytesToFile(bytes,read/divider);

//                if (divider == 1){
//                    audioTrack.write(bytes,0,read/divider);
//                    return;
//                }
//
//                int count = read / divider;
//
//                // Write the first byte
//                audioTrack.write(bytes, 0, 1);
//
//                // Calculate the step size to skip bytes evenly
//                int step = read / count;
//
//                // Start from the first byte after the initial one and skip bytes
//                for (int i = step; i < read; i += step) {
//                    audioTrack.write(bytes, i, 1);
//                }
            }
        });
    }


    private class ByteData {
        byte[] bytes;
        int read;
        long millis;
        boolean played;

        public ByteData() {
        }

        public boolean isPlayed() {
            return played;
        }

        public void setPlayed(boolean played) {
            this.played = played;
        }

        public ByteData(byte[] bytes) {
            this.bytes = bytes;
        }

        public ByteData(byte[] bytes, int read, long millis) {
            this.bytes = bytes;
            this.read = read;
            this.millis = millis;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public int getRead() {
            return read;
        }

        public void setRead(int read) {
            this.read = read;
        }

        public long getMillis() {
            return millis;
        }

        public void setMillis(long millis) {
            this.millis = millis;
        }
    }
}
