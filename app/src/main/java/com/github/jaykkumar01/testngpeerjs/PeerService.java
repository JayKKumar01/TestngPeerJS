package com.github.jaykkumar01.testngpeerjs;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PeerService extends Service implements Data, PeerListener {
    private WebView webView;
    private UserData userData;
    public static PeerListener listener;
    int count = 0;

    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE_IN_BYTES = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
//    private static final int BUFFER_SIZE_IN_BYTES = 16 * 1024;

    private AudioRecord audioRecord;
    private AudioTrack audioTrack;

    private boolean isRecording = false;
    private NotificationManager notificationManager;
    Handler handler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        listener = this;
        createNotification();
        userData = (UserData) intent.getSerializableExtra(getString(R.string.userdata));
        setupWebView();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Buffer Size: "+ BUFFER_SIZE_IN_BYTES + " bytes", Toast.LENGTH_SHORT).show();

        }
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE_IN_BYTES);

        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AUDIO_FORMAT,
                BUFFER_SIZE_IN_BYTES,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
        handler = new Handler(Looper.getMainLooper());

        return START_STICKY;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                boolean x = !url.equals("file:///android_asset/call.html");
                if(x){
                    return;
                }
                callJavaScript("javascript:init(\""+ userData.getUser() +"\")");
            }

        });
        webView.addJavascriptInterface(new JavaScriptInterface(), "Android");

        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        String path = "file:android_asset/call.html";
        webView.loadUrl(path);


    }

    private void startRecording() {
        isRecording = true;
        audioRecord.startRecording();





//        audioTrack.play();
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[BUFFER_SIZE_IN_BYTES];
                int i = 0;
                while (isRecording) {
                    int read = audioRecord.read(buffer, 0, BUFFER_SIZE_IN_BYTES);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callJavaScript("javascript:sendFile("+Arrays.toString(buffer) +")");
                        }
                    });

//                    audioTrack.write(buffer, 0, read);
//                    MainActivity.listener.onRead(++i + ": "+read);
                }
            }
        }).start();
    }



    private void stopRecording() {
        isRecording = false;
        audioRecord.stop();
//        audioTrack.stop();
    }

    @Override
    public void onJoin() {
        callJavaScript("javascript:connect(\""+ userData.getOtherUser() +"\")");
//        startRecording();
    }

    @Override
    public void onSend(String msg) {
        callJavaScript("javascript:send(\""+ msg +"\")");
    }

    @Override
    public void onSend(File file) {
//        byte[] bytes = Base.convertToBytes(file);
//        callJavaScript("javascript:sendFile("+ Arrays.toString(bytes) +")");
        try {
            FileInputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[16 * 1024];

            while ((inputStream.read(buffer)) > 0) {
                callJavaScript("javascript:sendFile("+ Arrays.toString(buffer) +")");
                count++;
            }
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


//        callJavaScript("javascript:sendFile("+ Arrays.toString(bytes) +")");
    }

    @Override
    public void onStop() {
        if (webView != null) {
            webView.loadUrl("");
            Toast.makeText(this, "Disconnected!", Toast.LENGTH_SHORT).show();
        }
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
        if (isRecording){
            stopRecording();
        }
        stopSelf();
    }

    @Override
    public void onUpload(byte[] buffer) {
        callJavaScript("javascript:sendFile("+Arrays.toString(buffer) +")");
    }

    @Override
    public void startUploading() {
        if (!isRecording){
            startRecording();
        }
    }

    @Override
    public void stopUploading() {
        if (isRecording){
            stopRecording();
        }
    }

    public class JavaScriptInterface {

        @JavascriptInterface
        public void send(String msg) {
            Toast.makeText(PeerService.this, msg, Toast.LENGTH_SHORT).show();
        }
        @JavascriptInterface
        public void answer() {
            //startRecording();
        }
        @JavascriptInterface
        public void play(byte[] bytes) {
            audioTrack.write(bytes, 0, BUFFER_SIZE_IN_BYTES);
        }
        @JavascriptInterface
        public void receiveFile(byte[] bytes) {
            MainActivity.listener.onLoad(count);

            File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(externalDir, "testing/2.mp4");
            try {
                FileOutputStream outputStream = new FileOutputStream(file,true);
                outputStream.write(bytes);
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


//            boolean done = Base.writeToFile(bytes,file);


            if (--count < 2) {

                Toast.makeText(PeerService.this, "File saved!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void callJavaScript(String func) {
        webView.evaluateJavascript(func, null);
    }

    private void createNotification() {


        Intent callIntent = new Intent(this,MainActivity.class);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, callIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Voice Connected")
                .setContentText("Tap to manage the call")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(true);

        //notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the notification channel for Android Oreo and above
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("channelDescription");


                notificationManager.createNotificationChannel(channel);
            }
            startForeground(NOTIFICATION_ID, builder.build());
            notificationManager.notify(NOTIFICATION_ID, builder.build());



        }
    }



















    public PeerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}