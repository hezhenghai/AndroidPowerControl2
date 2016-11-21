package com.dj.androidpowercontrol.entity;

/**
 * 配置文件
 */
public class MyConfig {

    /**
     * 设备型号 （安卓电源控制）
     */
    public static final String CODE = "DJ-DYKZ-01";

    /**
     * 网络接口
     */
    public static final String URL = "http://120.24.251.29";
    public static final String IP = "120.24.251.29";
    public static final int PORT = 8789;

    /**
     * update time url
     */
    public static final String updateUrl = MyConfig.URL + "/Power/updateTotalTime";

    /**
     * 上传地址
     */
    public static final String locationUrl = MyConfig.URL + "/Code/saveCode";

    /**
     * 绑定设备
     */
    public static final String bindUrl = MyConfig.URL + "/Power/bindDevice";

    /**
     * 初始密码
     */
    public static final String PASSWORD = "password";
    public static final String FIRST_PASSWORD = "123456";

    /**
     * 系统运行时间保存
     */
    public static final String TOTAL_TIME = "total_time";


    /**
     * 系统设备ID
     */
    public static final String DEVICE_ID = "device_id";

}
