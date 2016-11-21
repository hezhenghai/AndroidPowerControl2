package com.dj.androidpowercontrol.util;

import java.security.MessageDigest;

/**
 * MD5加密  utf-8  32位  大写输出
 */
public class MD5 {
    public static String getMD5(String s) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(s.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] bytes = md5.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            if (Integer.toHexString(0xFF & bytes[i]).length() == 1)
                sb.append("0").append(Integer.toHexString(0xFF & bytes[i]));
            else
                sb.append(Integer.toHexString(0xFF & bytes[i]));
        }
        return sb.toString().toUpperCase();
    }
}
