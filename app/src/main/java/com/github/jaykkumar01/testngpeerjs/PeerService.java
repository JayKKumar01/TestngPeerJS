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
import android.media.MediaPlayer;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeerService extends Service implements Data, PeerListener {
    WebView webView;

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    public static PeerListener listener;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;

    public static boolean isRecording = false;
    private NotificationManager notificationManager;
    Handler handler = new Handler();
    private UserData userData;
    private long time;
    private int count;
    private long max;
    FileOutputStream outputStream;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        listener = this;
        createNotification();
        userData = (UserData) intent.getSerializableExtra(getString(R.string.userdata));

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


        setupWebView();


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Buffer Size: "+ BUFFER_SIZE_IN_BYTES + " bytes", Toast.LENGTH_SHORT).show();

        }


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
        webView.addJavascriptInterface(new JavaScriptInterface(this,audioTrack), "Android");

        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        String path = "file:android_asset/call.html";
        webView.loadUrl(path);


    }

    public void callJavaScript(String func) {
        webView.evaluateJavascript(func, null);
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                webView.evaluateJavascript(func, null);
//            }
//        });

    }


    private void startRecording() {
        isRecording = true;
        audioRecord.startRecording();

        executorService.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File filePath = new File(externalDir, "testing/"+System.currentTimeMillis()+".pcm");
                    outputStream = new FileOutputStream(filePath);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                byte[] buffer = new byte[BUFFER_SIZE_IN_BYTES];
                while (isRecording) {
                    count++;
                    boolean isOneSecond = false;
                    if (System.currentTimeMillis()-time >= 1000){
                        time = System.currentTimeMillis();
                        isOneSecond = true;
                    }

                    long millis = System.currentTimeMillis();
                    int read = audioRecord.read(buffer, 0, BUFFER_SIZE_IN_BYTES);

                    if (read != AudioRecord.ERROR_INVALID_OPERATION) {
                        try {
                            outputStream.write(buffer, 0, read);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }




                    String str = objToString(Arrays.toString(buffer),read,millis);

                    if (isOneSecond){
                        MainActivity.listener.onRead(count+" times,"+str);
                        count = 0;
                    }


//                    MainActivity.listener.onLoad((read == BUFFER_SIZE_IN_BYTES)+"",read);
                    if (!Base.isNetworkAvailable(PeerService.this)){
                        MainActivity.listener.onNetwork("Network: "+false +" "+System.currentTimeMillis());
                        continue;
                    }else{
                        MainActivity.listener.onNetwork("Network: "+true +" "+System.currentTimeMillis());
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callJavaScript("javascript:sendFile("+ str +")");
                        }
                    });




//                    audioTrack.write(buffer, 0, read);
                }
            }
        });
    }

    private String objToString(Object... items) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            str.append(items[i]);
            if (i < items.length - 1) {
                str.append(",");
            }
        }
        return str.toString();
    }



    private void stopRecording() {
        isRecording = false;
        audioRecord.stop();
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        audioTrack.stop();
    }

    @Override
    public void onJoin() {
        callJavaScript("javascript:connect(\""+ userData.getOtherUser() +"\")");
    }

    @Override
    public void onSend(String msg) {
        callJavaScript("javascript:send(\""+ msg +"\")");
    }

    @Override
    public void onSend(File file) {
//        byte[] bytes = Base.convertToBytes(file);
//        callJavaScript("javascript:sendFile("+ Arrays.toString(bytes) +")");
//        try {
//            FileInputStream inputStream = new FileInputStream(file);
//            byte[] buffer = new byte[16 * 1024];
//
//            while ((inputStream.read(buffer)) > 0) {
//                callJavaScript("javascript:sendFile("+ Arrays.toString(buffer) +")");
//                count++;
//            }
//            inputStream.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }


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
        if (audioTrack != null){
            audioTrack.stop();
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

    @Override
    public void onConnected() {
        startRecording();
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














    private void playVoicePlayer(File audioFile) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Prepare the MediaPlayer


        // Start playing the audio
        mediaPlayer.start();
    }





    public PeerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}