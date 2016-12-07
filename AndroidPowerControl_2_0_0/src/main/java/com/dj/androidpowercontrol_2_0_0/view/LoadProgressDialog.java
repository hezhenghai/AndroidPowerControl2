package com.dj.androidpowercontrol_2_0_0.view;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.dj.androidpowercontrol_2_0_0.R;

/**
 * 进度对话框
 */
public class LoadProgressDialog extends Dialog {
    public LoadProgressDialog(Context context) {
        super(context);
    }

    public LoadProgressDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected LoadProgressDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static class Builder {
        public Context context;

        public ImageView iv_load_image;

        public Builder(Context context) {
            this.context = context;
        }

        public LoadProgressDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final LoadProgressDialog dialog = new LoadProgressDialog(context, R.style.LoadDialog);
            View layout = inflater.inflate(R.layout.dialog_load_progress, null);
            iv_load_image = (ImageView) layout.findViewById(R.id.iv_load_image);
            Animation myAnimation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            myAnimation.setRepeatCount(-1);//设置旋转重复次数，即转几圈
            myAnimation.setDuration(1000);//设置持续时间，注意这里是每一圈的持续时间，如果上面设置的圈数为3，持续时间设置1000，则图片一共旋转3秒钟
            myAnimation.setInterpolator(new LinearInterpolator());//设置动画匀速改变。相应的还有AccelerateInterpolator、DecelerateInterpolator、CycleInterpolator等
            iv_load_image.setAnimation(myAnimation);//设置imageview的动画，也可以myImageView.startAnimation(myAlphaAnimation)
            dialog.addContentView(layout, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            dialog.setContentView(layout);
            return dialog;
        }
    }
}
