package com.kuyou.smartwristband.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;


public class CnWinUtil {
    /**
     * 得到版本号
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = packInfo.versionName;
        Log.e("TAG",packInfo.versionName);
        return version;
    }

    /**
     * 得到版本号
     *
     * @param context
     * @return
     */
    public static long getVersionCode(Context context) {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        long versionCode = packInfo.versionCode;
        Log.e("TAG",packInfo.versionName);
        return versionCode;
    }

    /**
     * 得到泛型
     *
     * @param o
     * @param i
     * @param <T>
     * @return
     */
    public static <T> T getT(Object o, int i) {
        // 获取当前运行类泛型父类类型，即为参数化类型，有所有类型公用的高级接口Type接收!
        Type type = o.getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) type;
        Type[] ts = pt.getActualTypeArguments();
        Class<T> clazz = (Class<T>) ts[i];
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过类名得到
     *
     * @param className
     * @return
     */
    public static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    /**
     * 根据字符串与字符串集合排序 返回位置
     *
     * @param str
     * @param list
     * @return
     */
    public static int matchingList(String str, List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (str.equals(list.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 得到当前的日期时间(Y - M - D H:M:S)
     *
     * @param date
     * @return
     */
    public static String getYMDHMSDate(long date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-M-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    /**
     * 得到当前的时间(H:M:S)
     *
     * @param date
     * @return
     */
    public static String getHMSTimes(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return simpleDateFormat.format(date);
    }
}
