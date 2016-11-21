package com.dj.androidpowercontrol;

import android.app.Application;
import android.content.Context;

import org.netty.PushClient;


/**
 * Created by Administrator
 * on 2016/8/10.
 */
public class MyApplication extends Application {
    private static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        PushClient.create();

    }


    public static Context getAppContext() {
        return myApplication;
    }
}
