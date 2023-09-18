package com.github.jaykkumar01.testngpeerjs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class RecordingActivity extends AppCompatActivity {

    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private FileOutputStream audioOutputStream;
    private String audioFolderPath;
    private AudioTrack audioTrack;
    private String audioFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        Button recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording();
                    recordButton.setText("Stop Recording");
                } else {
                    stopRecording();
                    recordButton.setText("Start Recording");
                }
            }
        });

        audioFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio_segments/";
        File folder = new File(audioFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        recordButton.setEnabled(true);
    }

    private void startRecording() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
        );
        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AUDIO_FORMAT,
                BUFFER_SIZE,
                AudioTrack.MODE_STREAM);
        audioTrack.play();

        audioRecord.startRecording();
        isRecording = true;

        try {
            audioFilePath = audioFolderPath + System.currentTimeMillis() + ".pcm";

            audioOutputStream = new FileOutputStream(audioFilePath);
            startAudioBuffering(audioOutputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }

        if (audioOutputStream != null) {
            try {
                audioOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startAudioBuffering(final FileOutputStream outputStream) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (isRecording) {
                    int bytesRead = audioRecord.read(buffer, 0, BUFFER_SIZE);
                    audioTrack.write(buffer,0,bytesRead);
                    if (bytesRead != AudioRecord.ERROR_INVALID_OPERATION) {
                        try {
                            outputStream.write(buffer, 0, bytesRead);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    private void playRecordedAudio(String audioFilePath) {
        if (audioFilePath != null) {
            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AUDIO_FORMAT,
                    BUFFER_SIZE,
                    AudioTrack.MODE_STREAM
            );
            audioTrack.play();

            try {
                FileInputStream audioInputStream = new FileInputStream(audioFilePath);
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                    audioTrack.write(buffer, 0, bytesRead);
                }
                audioInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void play(View view) {
        playRecordedAudio(audioFilePath);
    }

    public void convert(View view) {
        String destPAth = audioFilePath.replace(".pcm",".wav");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PCM.rawToWave(new File(audioFilePath),new File(destPAth),SAMPLE_RATE);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show();
    }
}
