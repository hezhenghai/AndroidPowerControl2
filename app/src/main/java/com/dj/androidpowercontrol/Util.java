package com.dj.androidpowercontrol;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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

}
