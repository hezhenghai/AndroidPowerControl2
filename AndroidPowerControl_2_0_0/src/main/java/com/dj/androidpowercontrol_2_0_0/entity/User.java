package com.dj.androidpowercontrol_2_0_0.entity;

/**
 * Created by Administrator
 * on 2016/8/19.
 */
public class User {
    private String messages;
    private boolean success;
    private userData data;

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public userData getData() {
        return data;
    }

    public void setData(userData data) {
        this.data = data;
    }
}
