package com.dj.androidpowercontrol.entity;

import java.io.Serializable;

/**
 * 保存地址，uuid，code返回信息
 */
public class SaveMsg implements Serializable{
    private String data;
    private boolean success;
    private String messages;

    public String getData() {
        return data;
    }

    public void setData(String data) {
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
