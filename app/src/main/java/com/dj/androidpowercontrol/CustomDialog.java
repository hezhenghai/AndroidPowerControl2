package com.dj.androidpowercontrol;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 自定义弹框
 */
public class CustomDialog extends Dialog {
    public CustomDialog(Context context) {
        super(context);
    }

    public CustomDialog(Context context, int themeResId) {
        super(context, themeResId);
    }


    public static class Builder {
        private Context context;
        TextView tv_title;
        ImageButton ib_change_pw;
        EditText et_input;
        ImageButton ib_ok;

        public Builder(Context context) {
            this.context = context;
        }

        public CustomDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CustomDialog dialog = new CustomDialog(context, R.style.Dialog);
            View layout = inflater.inflate(R.layout.setting_dialog, null);
            dialog.addContentView(layout, new ActionBar.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
            dialog.setContentView(layout);
            tv_title = (TextView) layout.findViewById(R.id.tv_title);
            ib_change_pw = (ImageButton) layout.findViewById(R.id.ib_change_pw);
            et_input = (EditText) layout.findViewById(R.id.et_input);
            ib_ok = (ImageButton) layout.findViewById(R.id.ib_ok);
            return dialog;
        }
    }

}
