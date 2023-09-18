package com.github.jaykkumar01.testngpeerjs;

public interface Listener {
    void onLoad(String str,int val);

    void onRead(String read);

    void onConvert(long time, String str);

    void onSingleRead(String str, int val);

    void onNetwork(String str);
}
