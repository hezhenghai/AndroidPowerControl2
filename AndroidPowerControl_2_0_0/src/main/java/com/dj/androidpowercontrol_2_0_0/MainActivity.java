package com.dj.androidpowercontrol_2_0_0;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.dj.androidpowercontrol_2_0_0.entity.MyConfig;
import com.dj.androidpowercontrol_2_0_0.entity.PowerStateMsg;
import com.dj.androidpowercontrol_2_0_0.entity.User;
import com.dj.androidpowercontrol_2_0_0.entity.userData;
import com.dj.androidpowercontrol_2_0_0.util.Util;
import com.dj.androidpowercontrol_2_0_0.view.BindPidDialog;
import com.dj.androidpowercontrol_2_0_0.view.CustomDialog;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tencent.bugly.beta.Beta;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import GPIO.hardware;
import GPIO.rk3288;
import okhttp3.Call;

public class MainActivity extends Activity {

    /**
     * 地址信息
     */
    private String longitude;
    private String latitude;
    private String address;


    /**
     * 从服务器获取的总时间
     */
    private long tt = 0;

    /**
     * 网络状态监听器
     */
    private NetWorkChangeReceiver mReceiver;

    /**
     * 网络设置提示框
     */
    private CustomDialog.Builder builder;
    private CustomDialog dialog;

    /**
     * 多长时间请求一次，获取状态
     */
    private static final int INTERVAL = 10;
    private int intervalTime = 0;

    /**
     * cupid
     */
    private String cpuid;

    /**
     * 定位服务
     */
    LocationClient mLocationClient;

    /**
     * wifi设置控件
     */
    private ImageButton ib_wifi;

    /**
     * wifi强弱显示
     */
    private ImageView iv_wifi;

    /**
     * 剩余时间、总天数，时间、当前用时、cpuId显示控件
     */
    private TextView tv_remaining_time, tv_total_day, tv_total, tv_now, tv_cupId;

    /**
     * 工作指示灯
     */
    private ImageView iv_stop, iv_working;

    /**
     * 通电状态，初始状态为不工作状态
     */
    private boolean isWorking = false;

    /**
     * GPIO
     */
    hardware hw = new hardware();

    /**
     * 使用总时间 (秒钟)
     */
    private long mTotalTime = 0L;

    /**
     * 用于计时的变量 (秒钟)
     */
    private long mTime = 0L;

    /**
     * 用于定时检测wifi信号的Time间隔
     */
    private static final int WIFITIME_INTERVAL = 10;
    private int wifiTime;

    /**
     * wifi信号检测
     */
    //Wifi管理器
    private WifiManager wifiManager = null;
    //获得的Wifi信息
    private WifiInfo wifiInfo = null;
    //信号强度值
    private int level;

    /**
     * 用于计时的Timer
     */
    private Timer mTimer;

    /**
     * 用于计时的Handler
     */
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 计时
            if (msg.what == 0x123) {
                mTime++;
                mTotalTime++;
                // 保存时间到本地
                saveTotalTiem();
                // 更新时间显示
                updateTimeDisplay();
                // 设置一定时间后请求一次状态
                intervalTime++;
                if (intervalTime > INTERVAL) {
                    intervalTime = 0;
                    if (tt != 0) {
                        updateTime();
                    }
                    getPowerState();
                }
                //设定一定时间检查一下wifi信号
                wifiTime++;
                if (wifiTime > WIFITIME_INTERVAL) {
                    wifiTime = 0;
                    wifiInfo = wifiManager.getConnectionInfo();
                    level = wifiInfo.getRssi();
                    Util.showLog("TAG", "wifi-------------- " + level);
                    //根据获得的信号强度发送信息
                    if (level <= 0 && level >= -50) {
                        iv_wifi.setImageResource(R.drawable.icon_wifi_strong);
                    } else if (level < -50 && level >= -90) {
                        iv_wifi.setImageResource(R.drawable.icon_wifi_mediu);
                    } else if (level < -90 && level > -200) {
                        iv_wifi.setImageResource(R.drawable.icon_wifi_weak);
                    } else {
                        iv_wifi.setImageResource(R.drawable.icon_wifi_no);
                    }
                }

            }
            // 网络断开
            if (msg.what == 0x444) {
                Util.showLog("TAG", "4444444444444444444");
                iv_wifi.setImageResource(R.drawable.icon_wifi_no);
                showWifiSetDialog();
            }
            //网络连接成功
            if (msg.what == 0x555) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                Util.showLog("TAG", "55555555555555555555");
                iv_wifi.setImageResource(R.drawable.icon_wifi_strong);
                //定位,发送消息给服务器
                myLocation();
                // 检查升级
                Beta.checkUpgrade(false, false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* 隐藏标题栏 */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /* 隐藏状态栏 */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /* 设定屏幕显示为横屏 */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);
        /**
         * 打开gpio
         */
        hw.openGpioDev();
        // 获得WifiManager
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        // 初始化控件
        setViews();
        // 设置监听
        setListeners();
        // 初始化状态
        setInit();
        // 从本地获取使用总时间,若没有则为0
        getAndSetTotalTime();
        // 开始计时
        startTimer();
        // 注册网络监听器
        mReceiver = new NetWorkChangeReceiver();
        registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }


    /**
     * 初始化控件
     */
    private void setViews() {
        ib_wifi = (ImageButton) findViewById(R.id.ib_wifi);
        iv_wifi = (ImageView) findViewById(R.id.iv_wifi);
        tv_remaining_time = (TextView) findViewById(R.id.tv_remaining_time);
        tv_total_day = (TextView) findViewById(R.id.tv_total_day);
        tv_total = (TextView) findViewById(R.id.tv_total);
        tv_now = (TextView) findViewById(R.id.tv_now);
        tv_cupId = (TextView) findViewById(R.id.tv_cupId);
        iv_stop = (ImageView) findViewById(R.id.iv_stop);
        iv_working = (ImageView) findViewById(R.id.iv_working);
    }


    /**
     * 设置监听
     */
    private void setListeners() {
        ib_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);//系统设置界面
                startActivity(intent);
            }
        });
        tv_cupId.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final BindPidDialog.Builder builder = new BindPidDialog.Builder(MainActivity.this);
                final BindPidDialog dialog = builder.create();
                builder.tv_bind.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            saveDeviceId(builder.et_bind_id.getText().toString());
                            Util.showLog("TAG", builder.et_bind_id.getText().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // 绑定设备
                        bindDevice();
                        dialog.dismiss();
                    }
                });
                dialog.setCanceledOnTouchOutside(false);// 点击外部区域不关闭
                dialog.show();
                return false;
            }
        });

    }

    /**
     * 初始化状态
     */
    private void setInit() {
        closePower();
        cpuid = Util.getCpuId();
        tv_cupId.setText(cpuid);
    }

    /**
     * 开始计时
     */
    private void startTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(0x123);
            }
        }, 1000, 1000);
    }

    /**
     * 更新时间显示
     */
    private void updateTimeDisplay() {
        tv_now.setText(Util.makeMS2Format(mTime));
        long ddd = mTotalTime / (60 * 60 * 24);
        long ttt = mTotalTime % (60 * 60 * 24);
        tv_total_day.setText("" + ddd);
        tv_total.setText(Util.makeMS2Format(ttt));
    }


//    /**
//     * 保存pId到本地
//     */
//    private void savePId(String string) {
//        SharedPreferences share = getSharedPreferences(MyConfig.P_ID, MODE_PRIVATE);
//        SharedPreferences.Editor editor = share.edit();
//        editor.putString(MyConfig.P_ID, string);
//        editor.apply();
//    }
//
//    /**
//     * 获取pid
//     */
//    private String getPId() {
//        SharedPreferences share = getSharedPreferences(MyConfig.P_ID, MODE_PRIVATE);
//        return share.getString(MyConfig.P_ID, null);
//    }


    /**
     * 保存deviceId到本地，只用于第一次绑定到网络
     */
    private void saveDeviceId(String string) {
        SharedPreferences share = getSharedPreferences(MyConfig.DEVICE_ID, MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putString(MyConfig.DEVICE_ID, string);
        editor.apply();
    }

    /**
     * 获取deviceId
     */
    private String getDeviceId() {
        SharedPreferences share = getSharedPreferences(MyConfig.DEVICE_ID, MODE_PRIVATE);
        return share.getString(MyConfig.DEVICE_ID, null);
    }

//    /**
//     * 保存POWER_ID到本地
//     */
//    private void savePowerId(String string) {
//        SharedPreferences share = getSharedPreferences(MyConfig.POWER_ID, MODE_PRIVATE);
//        SharedPreferences.Editor editor = share.edit();
//        editor.putString(MyConfig.POWER_ID, string);
//        editor.apply();
//    }
//
//    /**
//     * 获取POWER_ID
//     */
//    private String getPowerId() {
//        SharedPreferences share = getSharedPreferences(MyConfig.POWER_ID, MODE_PRIVATE);
//        return share.getString(MyConfig.POWER_ID, null);
//    }


    /**
     * 保存总运行时间到本地
     */
    private void saveTotalTiem() {
        SharedPreferences share = getSharedPreferences(MyConfig.TOTAL_TIME, MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putString(MyConfig.TOTAL_TIME, String.valueOf(mTotalTime));
        editor.apply();
    }

    /**
     * 从本地获取总时间，若本地没有，则设为0
     */
    private void getAndSetTotalTime() {
        SharedPreferences share = getSharedPreferences(MyConfig.TOTAL_TIME, MODE_PRIVATE);
        String p = share.getString(MyConfig.TOTAL_TIME, null);
        if (p == null) {
            mTotalTime = 0;
        } else {
            mTotalTime = Long.parseLong(p);
        }
    }

    /**
     * 打开电源
     */
    public void openPower() {
        if (isWorking) {
            return;
        }
        iv_stop.setImageResource(R.drawable.status_stop_dis);
        iv_working.setImageResource(R.drawable.status_working);
        hw.setGpioState(rk3288.RK30_PIN7_PB5, 1);  //GPIO2 开
//        hw.setGpioState(rk3288.RK30_PIN0_PA6, 1);  //led灯 开
        isWorking = true;
    }

    /**
     * 关闭电源
     */
    public void closePower() {
        iv_stop.setImageResource(R.drawable.status_stop);
        iv_working.setImageResource(R.drawable.status_work);
        hw.setGpioState(rk3288.RK30_PIN7_PB5, 0);  //GPIO2 关
//        hw.setGpioState(rk3288.RK30_PIN0_PA6, 0);  //led灯 关
        isWorking = false;
    }


    /**
     * 定位
     */
    private synchronized void myLocation() {
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                longitude = String.valueOf(location.getLongitude());
                latitude = String.valueOf(location.getLatitude());
                address = location.getAddrStr();
                Util.showLog("TAG", "---------------" + longitude + "---------" + latitude + "---------" + address);
                if (longitude != null && latitude != null && address != null) {
                    bindDevice();
                    mLocationClient.stop();
                }
            }
        });
        initLocation();
        mLocationClient.start();
    }

    /**
     * 定位设置
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于10000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(false);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }


    /**
     * 发送网络请求，绑定设备
     */
    private synchronized void bindDevice() {
        Util.showLog("TAG", "uuid : " + getDeviceId() + "\nlongitude : " + longitude + "\nlatiude : " + latitude + "\ncode : " + MyConfig.CODE + "\npId : " + cpuid + "\naddress : " + address);
        OkHttpUtils
                .get()
                .url(MyConfig.bindUrl)
                .addParams("uuid", getDeviceId())
                .addParams("longitude", longitude)
                .addParams("latitude", latitude)
                .addParams("code", MyConfig.CODE)
                .addParams("pId", cpuid)
                .addParams("address", address)
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, final Exception e, int i) {
                        Util.showLog("TAG", "bindDevice------onError--- " + e);
                    }

                    @Override
                    public void onResponse(final String s, int i) {
                        Util.showLog("TAG", "bindDevice-----onResponse---- " + s);
                        //登录成功
                        try {
                            User user = new Gson().fromJson(s, User.class);
                            if (user.isSuccess()) {
                                userData data = user.getData();
                                if (data.getTotaltime() != null) {
                                    tt = Long.parseLong(data.getTotaltime());
                                    if (tt < mTotalTime) {
                                        updateTime();
                                    }
                                    if (tt > mTotalTime) {
                                        mTotalTime = tt + mTime;
                                    }
                                }
                            }
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                        }
                        getPowerState();
                    }
                });
    }


    /**
     * 发送网络请求，更新时间
     */
    private void updateTime() {
        Util.showLog("TAG", "updateTime------ " + "\ntotaltime : " + String.valueOf(mTotalTime) + "\npowerId : " + cpuid);
        OkHttpUtils
                .post()
                .url(MyConfig.updateUrl)
                .addParams("totaltime", String.valueOf(mTotalTime))
                .addParams("deviceId", getDeviceId())
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, final Exception e, int i) {
                        //更新失败
                        Util.showLog("TAG", "updateTime---onError--- " + e);
                    }

                    @Override
                    public void onResponse(final String s, int i) {
                        //更新成功
                        Util.showLog("TAG", "updateTime----onResponse-- " + s);
                    }
                });
    }

    /**
     * 获取开关状态码
     */
    private synchronized void getPowerState() {
        Util.showLog("TAG", "getPowerState------ " + "\npowerId : " + cpuid);
        OkHttpUtils
                .get()
                .url(MyConfig.getStateUrl)
                .addParams("powerId", cpuid)
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, final Exception e, int i) {
                        //获取失败
                        Util.showLog("TAG", "getPowerState-----onError---- " + e);
                    }

                    @Override
                    public void onResponse(final String s, int i) {
                        //获取成功
                        Util.showLog("TAG", "getPowerState---------onResponse----- " + s);
                        try {
                            PowerStateMsg psm = new Gson().fromJson(s, PowerStateMsg.class);
                            if (psm.isSuccess()) {
                                if (psm.getData() == 0) {
                                    closePower();
                                }
                                if (psm.getData() == 1) {
                                    openPower();
                                }
                            }
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 显示网络设置提示对话框
     */
    private synchronized void showWifiSetDialog() {
        if (dialog == null) {
            builder = new CustomDialog.Builder(MainActivity.this);
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);// 点击外部区域不关闭
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }


    /**
     * 监听网络状态
     */
    private class NetWorkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
                mHandler.sendEmptyMessage(0x444);
            } else {
                mHandler.sendEmptyMessage(0x555);
            }
        }
    }

    /**
     * 释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        hw.closeGpioDev();
    }

}
