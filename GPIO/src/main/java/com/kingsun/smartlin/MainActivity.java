package com.kingsun.smartlin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends Activity {
    hardware hw = new hardware();
    private Button low;
    private Button high;
    private Switch onoff;
    String str;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hw.openGpioDev();
        setContentView(R.layout.activity_main);
        low = (Button) findViewById(R.id.button);
        high = (Button) findViewById(R.id.button2);
        onoff = (Switch) findViewById(R.id.switch1);

        low.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                hw.setGpioState(237, 0);  //灯亮
//                Log.i("tag", "------------------------    low     " + rk3288.RK30_PIN7_PB5);
                hw.setGpioState(6, 0);  //灯亮
                hw.setGpioState(5, 0); //休眠
                Intent intent = new Intent();
                intent.setAction("com.kingsun.action.shutdown");
                sendBroadcast(intent);

//                Class<?> c = PowerManager.class;
//                PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//                Method method = null;
//                try {
//                    method = c.getDeclaredMethod("goToSleep", long.class);
//                } catch (NoSuchMethodException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    method.invoke(powerManager, SystemClock.uptimeMillis());
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }


                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hw.setGpioState(116, 0);//唤醒

                        hw.setGpioState(6, 1);  //灯亮
//                        try {
//                            Runtime.getRuntime().exec("su -c reboot");
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

//                        Intent intent = new Intent();
//                        intent.setAction("touch.action.down");
//                        sendBroadcast(intent);
//
//                        Intent intent2 = new Intent();
//                        intent2.setAction("touch.action.up");
//                        sendBroadcast(intent2);
                        low.setText("xxxxx");
                    }
                }, 3000);
            }
        });

        high.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
//                hw.setGpioState(237, 1);  //GPIO2控制
                Log.i("tag", "------------------------   high");
//                hw.setGpioState(6, 1); //灯灭
//                hw.setGpioState(5, 1); //唤醒
            }
        });


    }

//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        System.out.println("------------" + keyCode);
//        if ((keyCode == 230 || keyCode == 237)) {//249-230KINGSUM1   250-231 KINGSUM2   251-232 KINGSUM3   252 -233KINGSUM4
//            onoff.setChecked(false);
//            hw.setGpioState(rk3288.RK30_PIN0_PA6, 0);
//        }
//        return super.onKeyUp(keyCode, event);
//    }
//
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        // System.out.println("========="+keyCode);
//        if ((keyCode == 230 || keyCode == 237)) {
//            onoff.setChecked(true);
//            hw.setGpioState(rk3288.RK30_PIN0_PA6, 0);
//        }
//        return super.onKeyDown(keyCode, event);
//    }

}

