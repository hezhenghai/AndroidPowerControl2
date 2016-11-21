package com.dj.androidpowercontrol;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 自定义弹框 2 (更改密码)
 */
public class CustomDialog2 extends Dialog {
    public CustomDialog2(Context context) {
        super(context);
    }

    public CustomDialog2(Context context, int themeResId) {
        super(context, themeResId);
    }


    public static class Builder {
        private Context context;
        EditText et_old_pw;
        EditText et_new_pw;
        EditText et_confirm_pw;
        ImageButton ib_ok;

        public Builder(Context context) {
            this.context = context;
        }

        public CustomDialog2 create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CustomDialog2 dialog = new CustomDialog2(context, R.style.Dialog);
            View layout = inflater.inflate(R.layout.setting_dialog2, null);
            dialog.addContentView(layout, new ActionBar.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
            dialog.setContentView(layout);
            et_old_pw = (EditText) layout.findViewById(R.id.et_old_pw);
            et_new_pw = (EditText) layout.findViewById(R.id.et_new_pw);
            et_confirm_pw = (EditText) layout.findViewById(R.id.et_confirm_pw);
            ib_ok = (ImageButton) layout.findViewById(R.id.ib_ok);
            return dialog;
        }
    }

}
