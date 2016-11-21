package com.dj.androidpowercontrol.entity;

/**
 * Created by Administrator
 * on 2016/8/19.
 */
public class userData {
    private String deviceid;
    private String totaltime;

    public void setDeviceId(String deviceid) {
        this.deviceid = deviceid;
    }

    public void setTotaltime(String totaltime) {
        this.totaltime = totaltime;
    }

    public String getDeviceId() {
        return deviceid;
    }

    public String getTotaltime() {
        return totaltime;
    }
}
