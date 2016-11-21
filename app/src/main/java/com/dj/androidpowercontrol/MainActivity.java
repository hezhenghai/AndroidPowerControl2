package com.dj.androidpowercontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.SoundPool;
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

import GPIO.hardware;
import GPIO.rk3288;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.netty.PushClient;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;

public class MainActivity extends Activity {

    public static MainActivity instance = null;

    private static final String TAG = "MainActivity";

    /**
     * 通电状态，初始状态为不工作状态
     */
    private boolean isWorking = false;

    /**
     * GPIO
     */
    hardware hw = new hardware();

    /**
     * url
     */
    private String Url = MyConfig.URL + "Power/getTotalTime";

    /**
     * update time url
     */
    private String updateUrl = MyConfig.URL + "Power/updateTotalTime";

    /**
     * 绑定设备
     */
    private String bindUrl = MyConfig.URL + "Power/bindDevice";

    /**
     * 设置声音为最大值
     */
    private AudioManager mAudioManager;
    private int maxVolume;

    /**
     * 播放按键音
     */
    private SoundPool sp;//声明一个SoundPool
    private int music;//定义一个整型用load（）；来设置suondID

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

            if (msg.what == 0x123) {
                mTime++;
                mTotalTime++;
                saveTotalTiem();
                updateTimeDisplay();
            }
            if (msg.what == 0x456) {
                indexLight++;
                if (indexLight >= 5) {
                    indexLight = 0;
                    iv_working.setImageResource(R.drawable.button_02);
                } else {
                    iv_working.setImageResource(R.drawable.working);
                }
            }
            if (msg.what == 0x111) {
                openPower();
            }
            if (msg.what == 0x000) {
                closePower();
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

        //设置监听事件
        setListeners();

        instance = this;

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

        //长连接开始
        PushClient.start();

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
                builder.ib_change_pw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final CustomDialog2.Builder builder2 = new CustomDialog2.Builder(MainActivity.this);
                        final CustomDialog2 dialog2 = builder2.create();
                        //设置edittext点击 hint 消失
                        builder2.et_old_pw.setOnFocusChangeListener(mOnFocusChangeListener);
                        builder2.et_new_pw.setOnFocusChangeListener(mOnFocusChangeListener);
                        builder2.et_confirm_pw.setOnFocusChangeListener(mOnFocusChangeListener);
                        //点击更改密码
                        builder2.ib_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String s = builder2.et_old_pw.getText().toString().trim();
                                //验证密码是否正确
                                isPasswordTrue(s);

                            }
                        });
                        dialog2.setCanceledOnTouchOutside(true);// 点击外部区域关闭
                        dialog2.show();
                        //设置屏幕变暗
                        setScreenBgDarken();
                        dialog.dismiss();
                    }
                });
                builder.ib_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String s = builder.et_input.getText().toString().trim();
//                        //非空验证
//                        if (TextUtils.isEmpty(builder.et_input.getText())) {
//                            //获取焦点，显示信息
//                            builder.et_input.requestFocus();
//                            builder.et_input.setError(getResources().getString(R.string.password_can_not_be_empty));
//                            return;
//                        }
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
        boolean b = p.equals(password);
        return b;
    }


    /**
     * 通过蓝牙地址，确定设备id（将蓝牙id作为设备唯一识别号）
     */
    private void myUUID() {
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        uuid = mAdapter.getAddress();
        tv_uuid.setText("ID\n" + uuid);
    }

    /**
     * 发送网络请求，绑定设备
     */
    private void bindDevice() {
        OkHttpUtils
                .get()
                .url(bindUrl)
                .addParams("uuid", uuid)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, final Exception e, int i) {
                        Util.showLog(TAG, String.valueOf(e));
                        //登录失败
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Util.showToast(MainActivity.this, e.toString());
                            }
                        });
                    }

                    @Override
                    public void onResponse(final String s, int i) {
                        Util.showLog(TAG, s);
                        //登录成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Util.showToast(MainActivity.this, s);
                            }
                        });
                    }
                });
    }

    /**
     * 发送网络请求，更新时间
     */
    private void updateTime() {
        OkHttpUtils
                .post()
                .url(updateUrl)
                .addParams("totaltime", String.valueOf(mTotalTime))
                .addParams("deviceId", uuid)
                .build()
                .connTimeOut(10000)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, final Exception e, int i) {
                        Util.showLog(TAG, String.valueOf(e));
                        //登录失败
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Util.showToast(MainActivity.this, e.toString());
                            }
                        });
                    }

                    @Override
                    public void onResponse(final String s, int i) {
                        Util.showLog(TAG, s);
                        //登录成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Util.showToast(MainActivity.this, s);
                            }
                        });
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
        hw.setGpioState(rk3288.MY_GPIO2, 1);  //开
        hw.setGpioState(rk3288.RK30_PIN0_PA6, 1);  //开
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
        hw.setGpioState(rk3288.MY_GPIO2, 0);  //关
        hw.setGpioState(rk3288.RK30_PIN0_PA6, 0);  //关
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
     * 释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        hw.closeGpioDev();
    }
}
