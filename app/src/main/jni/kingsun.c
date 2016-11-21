#include <jni.h>
#include "kingsun.h"

JNIEXPORT jint JNICALL
Java_GPIO_hardware_openGpioDev(JNIEnv *env, jobject instance) {

    // TODO
    jint ret=0;

    fd = open("/dev/kingsun", O_RDWR);
    if (fd < 0) {
        ret=-1;
    }
    return ret;

}

JNIEXPORT jint JNICALL
Java_GPIO_hardware_closeGpioDev(JNIEnv *env, jobject instance) {

    // TODO
    jint ret=0;

    ret = close(fd);
    if (fd < 0) {
        ret=-1;
    }
    return ret;
}

JNIEXPORT jint JNICALL
Java_GPIO_hardware_setGpioState(JNIEnv *env, jobject instance, jint num,
                                                jint state) {

    // TODO
    jint err=-1;
    struct UserData userdata;
    memset(&userdata,0x00, sizeof(userdata));
    userdata.gpio=num;
    userdata.state=state;

    err = ioctl(fd, CMD_SET_GPIO, &userdata);
    if(err<0){
        err=-1;
    }
    return err;
}

JNIEXPORT jint JNICALL
Java_GPIO_hardware_releaseGpio(JNIEnv *env, jobject instance, jint num) {

    // TODO
    jint ret=-1;
    struct UserData userdata;
    memset(&userdata,0x00, sizeof(userdata));
    userdata.gpio=num;
    userdata.state=0;
    ret = ioctl(fd, CMD_RELEASE_GPIO, &userdata);
    return ret;
}

JNICALL
Java_GPIO_hardware_getGpio(JNIEnv *env, jobject instance, jint num) {

    // TODO
    jint ret=-1;
    struct UserData userdata;
    memset(&userdata,0x00, sizeof(userdata));
    userdata.gpio=num;
    userdata.state=0;

    ret = ioctl(fd, CMD_GET_GPIO, &userdata);
    return ret;
}

JNICALL
Java_GPIO_hardware_writeGpioDev(JNIEnv *env, jobject instance) {

    // TODO

}

JNICALL
Java_GPIO_hardware_readGpioDev(JNIEnv *env, jobject instance) {

    // TODO
    jint ret=-1;
    unsigned char returnValue[20];
    ret = read(fd,returnValue, sizeof(returnValue));
    return (*env)->NewStringUTF(env, returnValue);
}