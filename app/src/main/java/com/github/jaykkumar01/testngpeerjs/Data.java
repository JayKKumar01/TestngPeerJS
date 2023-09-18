package com.github.jaykkumar01.testngpeerjs;

import android.media.AudioFormat;
import android.media.AudioRecord;

public interface Data {
    String CHANNEL_ID = "call_channel";
    String CHANNEL_NAME = "Call Notifications";
    int NOTIFICATION_ID = 1;
    int REQUEST_CODE_MUTE = 100;
    int REQUEST_CODE_HANGUP = 200;
    int REQUEST_CODE_DEAFEN = 300;
    int REQUEST_CODE_CONTENT = 0;
//    int SAMPLE_RATE = 44100;
    int SAMPLE_RATE = 22000;
    int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
//    int BUFFER_SIZE_IN_BYTES = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    int BUFFER_SIZE_IN_BYTES = 8 * 1024;
}
