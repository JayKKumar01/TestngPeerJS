package com.github.jaykkumar01.testngpeerjs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.file.Files;

public class MainActivity extends AppCompatActivity implements Listener{
    private final String ID = "JayKKumar01";
    EditText editText;

    private static final int NOTIFICATION_PERMISSION_CODE = 112;
    private static final String NOTIFICATION_PERMISSION_CODE_STR = "112";

    String[] permissions = {Manifest.permission.RECORD_AUDIO};
    public static Listener listener;
    AppCompatButton startRecord,stopRecord;
    private int countSum;
    private int count;
    private long maxVal;
    private long sum;
    private boolean edited;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        editText = findViewById(R.id.editText);

        startRecord = findViewById(R.id.btnStartRecording);
        stopRecord = findViewById(R.id.btnStopRecording);
        listener = this;


    }


    public void send(View view) {
        String text = editText.getText().toString();
        if (text.isEmpty()){
            return;
        }

        PeerService.listener.onSend(text);

        editText.setText("");
    }


    public void sendPeer(View view) {
        run(R.string.admin,view);

    }
    public void receivePeer(View view) {
        run(R.string.user,view);
    }

    private void run(int val,View view) {
        if (!isPermission()){
            return;
        }

        String user = getString(val);
        String otherUser = user.equals(getString(R.string.admin)) ? getString(R.string.user) : getString(R.string.admin);

        UserData userData = new UserData(ID+"-"+getString(val),ID+"-"+ otherUser);
        Intent serviceIntent = new Intent(this, PeerService.class);
        serviceIntent.putExtra(getString(R.string.userdata),userData);
        startService(serviceIntent);
        view.setVisibility(View.GONE);
    }



    private boolean isPermission() {
        if(!isGranted()){
            askPermission();
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !shouldShowRequestPermissionRationale(NOTIFICATION_PERMISSION_CODE_STR)){
            int notificationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS);
            if (notificationPermission != PackageManager.PERMISSION_GRANTED){
                getNotificationPermission();
                return false;
            }



        }
        return true;
    }

    private void getNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
        }
    }

    void askPermission(){
        int reqCode = 1;
        ActivityCompat.requestPermissions(this,permissions, reqCode);
    }
    private boolean isGranted(){
        for(String permission: permissions){
            if(ActivityCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }


    public void joinPeer(View view) {
        PeerService.listener.onJoin();
        view.setVisibility(View.GONE);
    }

    public void sendFile(View view) {
        ((AppCompatButton)view).setText("File Sent");

        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(externalDir, "testing/1.mp4");

        PeerService.listener.onSend(file);

//        byte[] bytes = Base.convertToBytes(file);
//
//
//        file = new File(externalDir,"testing/2.wav");
//        boolean done = Base.writeToFile(bytes,file);
//        Toast.makeText(this, ""+done, Toast.LENGTH_SHORT).show();









//        String data = Base.convertFileToBase64(file);
//        Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
//
//        file = new File(externalDir, "text1.txt");
//        boolean done = Base.convertBase64ToFile(data,file);
//        Toast.makeText(this, "File saved? :"+ done, Toast.LENGTH_SHORT).show();
////        PeerService.listener.onSend(file);

    }

    @Override
    public void onLoad(String str,int val) {
        AppCompatButton button = findViewById(R.id.btnSendFile);
        setBtnText(button,str+": "+val);

    }

    @Override
    public void onRead(String read) {
        
        EditText editText = findViewById(R.id.editText);
//        if (edited){
//            return;
//        }else{
//            edited = true;
//        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editText.setText(read);
            }
        });

//        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        File file = new File(externalDir, "testing/1.txt");
//        read += "\n";
//
//        try {
//            FileOutputStream outputStream = new FileOutputStream(file,true);
//            outputStream.write(read.getBytes());
//            outputStream.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }


//        AppCompatButton button = findViewById(R.id.btnSendFile);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                button.setText(++count +"");
//            }
//        });
    }

    @Override
    public void onConvert(long time, String str) {
        AppCompatButton buttonVal = findViewById(R.id.btnVal);
        AppCompatButton buttonMaxVal = findViewById(R.id.btnMaxVal);
        if (time > maxVal){
            setBtnText(buttonMaxVal,"Max: "+time);
            maxVal = time;
        }
        sum += time;

        if (++countSum == 10){
            setBtnText(buttonVal,"Val: "+(sum/10));
            countSum = 0;
            sum = 0;
        }



    }

    @Override
    public void onSingleRead(String str, int val) {
        AppCompatButton btn = findViewById(R.id.sendBtn);
        setBtnText(btn,str+": "+val);
    }

    @Override
    public void onNetwork(String str) {
        AppCompatButton btn = findViewById(R.id.btnStop);
        setBtnText(btn,str);
    }

    private void setBtnText(AppCompatButton button, String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setText(str);
            }
        });
    }

    public void stopService(View view) {
        PeerService.listener.onStop();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void startRecording(View view) {
        PeerService.listener.startUploading();
        stopRecord.setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
    }

    public void stopRecording(View view) {
        PeerService.listener.stopUploading();
        startRecord.setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
    }

    public void record(View view) {
        Intent intent = new Intent(this,RecordingActivity.class);
        startActivity(intent);
    }
}