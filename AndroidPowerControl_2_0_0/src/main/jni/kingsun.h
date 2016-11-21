//
// Created by BIN on 2016/3/25.
//

#ifndef SMARTLIN_KINGSUN_H
#define SMARTLIN_KINGSUN_H

#include <stdio.h>
#include <math.h>
#include <malloc.h>
#include <sys/ioctl.h>
#include <fcntl.h>

#include <android/log.h>

#define CMD_GET_GPIO 0
#define CMD_SET_GPIO 1
#define CMD_RELEASE_GPIO 5

struct UserData{
    int gpio;
    int state;
};

int fd=-1;

#endif //SMARTLIN_KINGSUN_H
