package com.github.jaykkumar01.testngpeerjs;

import java.io.File;

public interface PeerListener {
    void onJoin();
    void onSend(String msg);
    void onSend(File file);
    void onStop();

    void onUpload(byte[] buffer);

    void startUploading();
    void stopUploading();

    void onConnected();
}
