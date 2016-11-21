package com.dj.androidpowercontrol_2_0_0.util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * 工具类
 */
public class Util {
    private static Toast toast;

    public static void showToast(Context context, String content) {
        if (toast == null) {
            toast = Toast.makeText(context,
                    content,
                    Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    public static void showLog(String context, String s) {
        Log.i(context, s);
    }


    /**
     * 获取cup_id
     *
     * @return
     */
    public static String getCpuId() {
        String str = "", strCPU = "", cpuId = "";
        try {
            //读取CPU信息
            Process pp = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            //查找CPU序列号
            for (int i = 1; i < 100; i++) {
                str = input.readLine();
                if (str != null) {
                    //查找到序列号所在行
                    if (str.indexOf("Serial") > -1) {
                        //提取序列号
                        strCPU = str.substring(str.indexOf(":") + 1, str.length());
                        //去空格
                        cpuId = strCPU.trim();
                        //Log.d("GetCpuId ","= "+cpuId);
                        break;
                    }
                } else {
                    //文件结尾
                    break;
                }
            }
        } catch (Exception ex) {
            //赋予默认值
            ex.printStackTrace();
        }
        return cpuId;
    }


    /**
     * 将秒值转成 HH:mm:ss 格式的字符串
     */
    public static String makeMS2Format(long mSecond) {
        long mMinute = mSecond / 60;
        long mHour = mMinute / 60;
        return String.format("%02d", mHour) + ":" + String.format("%02d", mMinute % 60) + ":" + String.format("%02d", mSecond % 60);
    }


}
