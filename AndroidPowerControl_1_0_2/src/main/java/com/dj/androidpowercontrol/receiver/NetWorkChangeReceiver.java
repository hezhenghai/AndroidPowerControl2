package com.dj.androidpowercontrol.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.dj.androidpowercontrol.MainActivity;
import com.dj.androidpowercontrol.util.Util;

/**
 * 监听网络状态
 */
public class NetWorkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
            Util.showToast(context, "网络不可用");
            MainActivity.instance.mHandler.sendEmptyMessage(0x444);
        } else {
            Util.showToast(context, "网络已连接");
            MainActivity.instance.mHandler.sendEmptyMessage(0x555);
        }
    }
}

