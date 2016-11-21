package com.dj.androidpowercontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.dj.androidpowercontrol.entity.MyConfig;
import com.dj.androidpowercontrol.entity.SaveMsg;
import com.dj.androidpowercontrol.entity.User;
import com.dj.androidpowercontrol.entity.userData;
import com.dj.androidpowercontrol.receiver.NetWorkChangeReceiver;
import com.dj.androidpowercontrol.util.NetWorkUtils;
import com.dj.androidpowercontrol.util.Util;
import com.dj.androidpowercontrol.view.CustomDialog;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import GPIO.hardware;
import GPIO.rk3288;
import okhttp3.Call;


import org.netty.PushClient;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {
    public static MainActivity instance = null;

    private static final String TAG = "TAG";
    /**
     * 定位服务
     */
    LocationClient mLocationClient = null;
    /**
     * 地址
     */
    String address = "";


    /**
     * 通电状态，初始状态为不工作状态
     */
    private boolean isWorking = false;

    /**
     * GPIO
     */
    hardware hw = new hardware();

    /**
     * 设置声音为最大值
     */
    private AudioManager mAudioManager;
    private int maxVolume;

    /**
     * 播放按键音
     */
    private SoundPool sp;//声明一个SoundPool
    private int music;//定义一个整型用load（）；来设置soundID

    /**
     * 设置按钮
     */
    private ImageButton ib_setting;

    /**
     * text view 显示 蓝牙ID，使用总时间，本次开机时间，功率，     电压，    电流
     */
    private TextView tv_uuid, tv_time_total, tv_time_use, tv_power, tv_voltage, tv_current;

    /**
     * 工作状态显示
     */
    private ImageView iv_working, iv_stop;

    /**
     * uuid （蓝牙编号）
     */
    private String uuid;

    /**
     * 功率  （默认 30）
     * 电压  （默认 220）
     * 电流  （默认 5）
     */
    private int mPower = 30;
    private int mVoltage = 220;
    private int mCurrent = 5;

    /**
     * 网络状态监听广播
     */
    private NetWorkChangeReceiver myReceiver;

    /**
     * 使用总时间 (秒钟)
     */
    private long mTotalTime = 0L;

    /**
     * 用于计时的变量 (秒钟)
     */
    private long mTime = 0L;

    /**
     * 用于指示灯闪烁效果的Timer
     */
    private Timer mLightTimer;

    /**
     * 用于定义闪烁间隔
     */
    private int indexLight = 0;

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
                // 当时间达到1000小时时，重置
                if (mTotalTime >= 3600000) {
                    mTotalTime = 0;
                }
                // 保存时间到本地
                saveTotalTiem();
                // 更新时间显示
                updateTimeDisplay();
            }
            // 灯闪烁效果
            if (msg.what == 0x456) {
                indexLight++;
                if (indexLight >= 5) {
                    indexLight = 0;
                    iv_working.setImageResource(R.drawable.button_02);
                } else {
                    iv_working.setImageResource(R.drawable.working);
                }
            }
            // 远程控制打开电源
            if (msg.what == 0x111) {
                openPower();
            }
            // 远程控制关闭电源
            if (msg.what == 0x000) {
                closePower();
            }
            // 网络断开
            if (msg.what == 0x444) {
                closePower();
                //长连接断开
                PushClient.close();
            }
            //网络连接成功
            if (msg.what == 0x555) {
                //长连接开始
                PushClient.start();
                //绑定设备到服务器
                bindDevice();
                //上传总时间到网络
//                updateTime();
                //定位,发送消息给服务器
                myLocation();
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

        /**
         * 打开gpio
         */
        hw.openGpioDev();

        setContentView(R.layout.activity_main);

        //设置声音为最大
        setVolumeMax();

        sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        music = sp.load(this, R.raw.keytone, 1); //把你的声音素材放到res/raw里，第2个参数即为资源文件，第3个为音乐的优先级

        //初始化view
        setViews();

        instance = this;

        //设置监听事件
        setListeners();


        //获取id
        myUUID();

        //设置初始密码
        setPassword();

        //从本地获取总时间，若本地没有，则设为0
        getAndSetTotalTime();

        //显示时间
        updateTimeDisplay();

        //计时
        startTimer();

        //关闭电源
        closePower();

        //注册网络状态监听广播
        registerReceiver();

        //判断是否有网络
        if (NetWorkUtils.isNetworkConnected(this)) {
            //有
            //长连接开始
            PushClient.start();
            //绑定设备到服务器
//            bindDevice();
            //上传总时间到网络
//            updateTime();
            //定位,发送消息给服务器
//            myLocation();

        } else {
            //没有
//            Util.showToast(MainActivity.this, "网络不可用，请连接网络！");
        }
    }

    /**
     * 初始化view
     */
    private void setViews() {
        ib_setting = (ImageButton) findViewById(R.id.ib_setting);
        tv_uuid = (TextView) findViewById(R.id.tv_uuid);
        tv_time_total = (TextView) findViewById(R.id.tv_time_total);
        tv_time_use = (TextView) findViewById(R.id.tv_time_use);
        tv_power = (TextView) findViewById(R.id.tv_power);
        tv_voltage = (TextView) findViewById(R.id.tv_voltage);
        tv_current = (TextView) findViewById(R.id.tv_current);
        iv_working = (ImageView) findViewById(R.id.iv_working);
        iv_stop = (ImageView) findViewById(R.id.iv_stop);

        tv_power.setText(String.valueOf(mPower));
        tv_voltage.setText(String.valueOf(mVoltage));
        tv_current.setText(String.valueOf(mCurrent));

    }

    /**
     * 设置监听事件
     */
    private void setListeners() {
        ib_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //播放按键音
                sp.play(music, 1, 1, 0, 0, 1);
                final CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
                final CustomDialog dialog = builder.create();
                builder.tv_title.setText(getResources().getString(R.string.please_input_password));
                builder.ib_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String s = builder.et_input.getText().toString().trim();
                        if (isPasswordTrue(s)) {
                            builder.tv_title.setText(getResources().getString(R.string.please_input_password));
                            //跳转到系统设置界面
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);//wifi设置界面
                            startActivity(intent);
                            dialog.dismiss();
                        } else {
                            builder.tv_title.setText(getResources().getString(R.string.wrong_password));
                            builder.et_input.setText("");
                        }
                    }
                });
                dialog.setCanceledOnTouchOutside(true);// 点击外部区域关闭
                dialog.show();
                //设置屏幕变暗
                setScreenBgDarken();
            }
        });
    }

    /**
     * 判断密码是否正确
     */
    private boolean isPasswordTrue(String password) {
        SharedPreferences share = getSharedPreferences(MyConfig.PASSWORD, MODE_PRIVATE);
        String p = share.getString(MyConfig.PASSWORD, null);
        boolean b = false;
        if (p != null) {
            b = p.equals(password);
        }
        return b;
    }

    /**
     * 通过蓝牙地址，确定设备id（将蓝牙id作为设备唯一识别号）
     */
    private void myUUID() {
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        uuid = mAdapter.getAddress();
        tv_uuid.setText("ID" + "\n" + uuid);
    }

    /**
     * 发送网络请求，绑定设备
     */
    private void bindDevice() {
        Util.showLog(TAG,"--------------"+uuid);
        OkHttpUtils
                .get()
                .url(MyConfig.bindUrl)
                .addParams("uuid", uuid)
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, final Exception e, int i) {
                        //登录失败
                        Util.showLog(TAG, String.valueOf(e));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                Util.showToast(MainActivity.this, e.toString());
                            }
                        });
                    }

                    @Override
                    public void onResponse(final String s, int i) {
                        //登录成功
                        Util.showLog(TAG, s);
                        try {
                            User user = new Gson().fromJson(s, User.class);
                            if (user.isSuccess()) {
                                userData data = user.getData();
                                saveDeviceId(data.getDeviceId());
                                if (data.getTotaltime() != null) {
                                    long tt = Long.parseLong(data.getTotaltime());
                                    if (tt > mTotalTime) {
                                        mTotalTime = tt + mTime;
                                    } else {
                                        updateTime();
                                    }
                                }
                            }
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    /**
     * 发送网络请求，更新时间
     */
    private void updateTime() {
        SharedPreferences share = getSharedPreferences(MyConfig.DEVICE_ID, MODE_PRIVATE);
        String p = share.getString(MyConfig.DEVICE_ID, null);
        if (p == null) return;
        OkHttpUtils
                .post()
                .url(MyConfig.updateUrl)
                .addParams("totaltime", String.valueOf(mTotalTime))
                .addParams("deviceId", p)
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, final Exception e, int i) {
                        //登录失败
                        Util.showLog(TAG, String.valueOf(e));
                    }

                    @Override
                    public void onResponse(final String s, int i) {
                        //登录成功
                        Util.showLog(TAG, s);

                    }
                });
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
     * 开启闪烁效果
     */
    private void startLight() {
        if (mLightTimer != null) {
            mLightTimer.cancel();
            mLightTimer = null;
        }
        mLightTimer = new Timer();
        mLightTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(0x456);
            }
        }, 0, 300);
    }

    /**
     * 关闭闪烁效果
     */
    private void stopLight() {
        if (mLightTimer != null) {
            mLightTimer.cancel();
            mLightTimer = null;
        }
    }

    /**
     * 更新时间显示
     */
    private void updateTimeDisplay() {
        tv_time_use.setText(makeS2Format2(mTime));
        tv_time_total.setText(makeS2Format(mTotalTime));
    }

    /**
     * 将秒数值转成 HHH:mm:ss 格式的字符串
     */
    private String makeS2Format(long mSecond) {
        long mMinute = mSecond / 60L;
        long mHour = mMinute / 60L;
        return String.format("%03d", mHour) + ":" + String.format("%02d", mMinute % 60L) + ":" + String.format("%02d", mSecond % 60L);
    }

    /**
     * 将秒数值转成 HH:mm:ss 格式的字符串
     */
    private String makeS2Format2(long mSecond) {
        long mMinute = mSecond / 60L;
        long mHour = mMinute / 60L;
        return String.format("%02d", mHour) + ":" + String.format("%02d", mMinute % 60L) + ":" + String.format("%02d", mSecond % 60L);
    }

    /**
     * 打开电源
     */
    public void openPower() {
        if (isWorking) {
            return;
        }
        //开启闪烁效果
        startLight();
        iv_stop.setImageResource(R.drawable.button_02);
        hw.setGpioState(rk3288.RK30_PIN7_PB5, 1);  //GPIO2 开
//        hw.setGpioState(rk3288.RK30_PIN0_PA6, 1);  //led灯 开
        isWorking = true;
    }

    /**
     * 关闭电源
     */
    public void closePower() {
        //关闭闪烁效果
        stopLight();
        iv_working.setImageResource(R.drawable.button_02);
        iv_stop.setImageResource(R.drawable.stop);
        hw.setGpioState(rk3288.RK30_PIN7_PB5, 0);  //GPIO2 关
//        hw.setGpioState(rk3288.RK30_PIN0_PA6, 0);  //led灯 关
        isWorking = false;
    }

    /**
     * 设置屏幕背景变暗
     */
    private void setScreenBgDarken() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.4f;
        lp.dimAmount = 0.4f;
        getWindow().setAttributes(lp);
    }

    /**
     * 设置屏幕背景变亮
     */
    private void setScreenBgLight() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        lp.dimAmount = 1.0f;
        getWindow().setAttributes(lp);
    }

    /**
     * 设置edittext获取焦点，hint隐藏
     */
    private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            EditText textView = (EditText) v;
            String hint;
            if (hasFocus) {
                hint = textView.getHint().toString();
                textView.setTag(hint);
                textView.setHint("");
            } else {
                hint = textView.getTag().toString();
                textView.setHint(hint);
            }
        }
    };

    /**
     * 保存初始密码到本地,只用于第一次开机
     */
    private void setPassword() {
        SharedPreferences share = getSharedPreferences(MyConfig.PASSWORD, MODE_PRIVATE);
        String p = share.getString(MyConfig.PASSWORD, null);
        if (p == null) {
            SharedPreferences.Editor editor = share.edit();
            editor.putString(MyConfig.PASSWORD, MyConfig.FIRST_PASSWORD);
            editor.apply();
        }
    }

    /**
     * 保存deviceId到本地，只用于第一次绑定到网络
     */
    private void saveDeviceId(String string) {
        SharedPreferences share = getSharedPreferences(MyConfig.DEVICE_ID, MODE_PRIVATE);
        String p = share.getString(MyConfig.DEVICE_ID, null);
        if (p == null) {
            SharedPreferences.Editor editor = share.edit();
            editor.putString(MyConfig.DEVICE_ID, string);
            editor.apply();
        }
    }


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
     * 设置声音为最大
     */
    private void setVolumeMax() {
        //获取系统音量对象的实例
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //获取音量最大值
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        //设置系统音量为最大
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, maxVolume, 0);
    }

    /**
     * 定位
     */
    private void myLocation() {
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
//                StringBuffer sb = new StringBuffer(256);
//                sb.append("time : ");
//                sb.append(location.getTime());
//                sb.append("\nerror code : ");
//                sb.append(location.getLocType());
//                sb.append("\nlatitude : ");
//                sb.append(location.getLatitude());
//                sb.append("\nlontitude : ");
//                sb.append(location.getLongitude());
//                sb.append("\nradius : ");
//                sb.append(location.getRadius());
//                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
//                    sb.append("\nspeed : ");
//                    sb.append(location.getSpeed());// 单位：公里每小时
//                    sb.append("\nsatellite : ");
//                    sb.append(location.getSatelliteNumber());
//                    sb.append("\nheight : ");
//                    sb.append(location.getAltitude());// 单位：米
//                    sb.append("\ndirection : ");
//                    sb.append(location.getDirection());// 单位度
//                    sb.append("\naddr : ");
//                    sb.append(location.getAddrStr());
//                    sb.append("\ndescribe : ");
//                    sb.append("gps定位成功");
//
//                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
//                    sb.append("\naddr : ");
//                    sb.append(location.getAddrStr());
//                    //运营商信息
//                    sb.append("\noperationers : ");
//                    sb.append(location.getOperators());
//                    sb.append("\ndescribe : ");
//                    sb.append("网络定位成功");
//                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
//                    sb.append("\ndescribe : ");
//                    sb.append("离线定位成功，离线定位结果也是有效的");
//                } else if (location.getLocType() == BDLocation.TypeServerError) {
//                    sb.append("\ndescribe : ");
//                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
//                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
//                    sb.append("\ndescribe : ");
//                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
//                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
//                    sb.append("\ndescribe : ");
//                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
//                }
//                sb.append("\nlocationdescribe : ");
//                sb.append(location.getLocationDescribe());// 位置语义化信息
//                List<Poi> list = location.getPoiList();// POI数据
//                if (list != null) {
//                    sb.append("\npoilist size = : ");
//                    sb.append(list.size());
//                    for (Poi p : list) {
//                        sb.append("\npoi= : ");
//                        sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
//                    }
//                }
//                Util.showLog("BaiduLocationApiDem", "=================" + sb.toString());
                address = location.getAddrStr();
                if (!address.isEmpty()) {
                    Util.showLog(TAG, address);
                    //定位成功，上传数据到服务器
                    //这里用post请求，参数带汉字
                    OkHttpUtils
                            .post()
                            .url(MyConfig.locationUrl)
                            .addParams("code", MyConfig.CODE) //设备型号
                            .addParams("uuid", uuid) //设备唯一识别号（蓝牙id）
                            .addParams("address", address) //设备地址
                            .build()
                            .connTimeOut(10000)
                            .readTimeOut(10000)
                            .writeTimeOut(10000)
                            .execute(new StringCallback() {
                                @Override
                                public void onError(Call call, final Exception e, int i) {
                                    Util.showLog(TAG, String.valueOf(e));
                                    //登录失败
//                                            Util.showToast(LoginActivity.this,e.toString());

                                }

                                @Override
                                public void onResponse(final String s, int i) {
                                    Util.showLog(TAG, s);
                                    SaveMsg saveMsg = new Gson().fromJson(s, SaveMsg.class);
                                    if (saveMsg.isSuccess()) {
                                        //登录成功
                                    } else {
//                                        Util.showToast(MainActivity.this, saveMsg.getMessages());
                                    }
                                }
                            });
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
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }


    /**
     * 注册网络状态监听广播
     */
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        myReceiver = new NetWorkChangeReceiver();
        this.registerReceiver(myReceiver, filter);
    }

    /**
     * 注销网络状态监听广播
     */
    private void unregisterReceiver() {
        this.unregisterReceiver(myReceiver);
    }

    /**
     * 释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        PushClient.close();
        unregisterReceiver();
        hw.closeGpioDev();
    }
}
