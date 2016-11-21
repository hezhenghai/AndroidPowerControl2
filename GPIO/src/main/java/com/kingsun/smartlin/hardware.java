package com.kingsun.smartlin;

/**
 * Created by BIN on 2016/3/25.
 */
public class hardware {

    static {
        // The runtime will add "lib" on the front and ".o" on the end of
        // the name supplied to loadLibrary.
        System.loadLibrary("kingsun");
    }
    public native int openGpioDev();
    public native int closeGpioDev();
    public native String readGpioDev();
    public native int writeGpioDev();
    public native int getGpio(int num);
    public native int releaseGpio(int num);
    public native int setGpioState(int num,int state);

}

