package com.dj.androidpowercontrol_2_0_0.entity;

import java.io.Serializable;

/**
 * 保存地址，uuid，code返回信息
 */
public class PowerStateMsg implements Serializable{
    private int data;
    private boolean success;
    private String messages;

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }
}
