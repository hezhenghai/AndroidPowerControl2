package com.dj.androidpowercontrol_2_0_0.view;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dj.androidpowercontrol_2_0_0.R;


/**
 * 自定义弹框,绑定设备
 */
public class BindPidDialog extends Dialog {

    public BindPidDialog(Context context) {
        super(context);
    }

    public BindPidDialog(Context context, int themeResId) {
        super(context, themeResId);
    }


    public static class Builder {
        public Context context;
        public ImageButton ib_delete;
        public EditText et_bind_id;
        public TextView tv_bind;

        public Builder(Context context) {
            this.context = context;
        }

        public BindPidDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final BindPidDialog dialog = new BindPidDialog(context, R.style.Dialog);
            View layout = inflater.inflate(R.layout.bind_pid_dialog, null);
            dialog.addContentView(layout, new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            dialog.setContentView(layout);
            ib_delete = (ImageButton) layout.findViewById(R.id.ib_delete);
            ib_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            et_bind_id = (EditText) layout.findViewById(R.id.et_bind_id);
            tv_bind = (TextView) layout.findViewById(R.id.tv_bind);
            return dialog;
        }
    }

}
