package com.dj.androidpowercontrol_2_0_0.entity;

/**
 * 配置文件
 */
public class MyConfig {

    /**
     * 设备型号 （安卓电源控制）
     */
    public static final String CODE = "DTQ6-1";

    /**
     * 网络接口
     */
    public static final String URL = "http://120.24.251.29";

    /**
     * update time url
     */
    public static final String updateUrl = MyConfig.URL + "/Power/updateTotalTime";

    /**
     * 绑定设备
     */
    public static final String bindUrl = MyConfig.URL + "/Power/bindDevice";
    /**
     * 获取设备开关状态码
     */
    public static final String getStateUrl = MyConfig.URL + "/Power/getPowerState";

    /**
     * 系统运行时间保存
     */
    public static final String TOTAL_TIME = "total_time";

    /**
     * 系统设备ID
     */
    public static final String DEVICE_ID = "device_id";
    /**
     * power_ID
     */
    public static final String POWER_ID = "power_id";
    /**
     * pID
     */
    public static final String P_ID = "p_id";


}
