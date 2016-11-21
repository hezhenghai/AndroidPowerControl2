package dmoe.cn.nettyclient;

import android.app.Application;
import android.content.Context;

import org.netty.PushClient;

/**
 * Created by Administrator on 2016/8/4.
 */
public class MainApplication  extends Application {

    private static MainApplication mainApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;
        PushClient.create();

    }


    public static Context getAppContext() {
        return mainApplication;
    }
}
