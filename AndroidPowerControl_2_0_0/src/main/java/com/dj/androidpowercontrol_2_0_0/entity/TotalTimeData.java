package com.dj.androidpowercontrol_2_0_0.entity;

import java.io.Serializable;

/**
 * Created by Administrator
 * on 2016/12/7.
 */

public class TotalTimeData implements Serializable {

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
