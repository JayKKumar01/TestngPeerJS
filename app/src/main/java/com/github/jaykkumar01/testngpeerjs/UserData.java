package com.github.jaykkumar01.testngpeerjs;

import java.io.Serializable;

public class UserData implements Serializable{
    String user;
    String otherUser;

    public UserData() {
    }

    public UserData(String user, String otherUser) {
        this.user = user;
        this.otherUser = otherUser;
    }

    public UserData(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(String otherUser) {
        this.otherUser = otherUser;
    }
}
