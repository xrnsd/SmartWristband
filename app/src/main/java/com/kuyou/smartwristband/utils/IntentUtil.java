package com.kuyou.smartwristband.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * 页面跳转intent工具类
 * Created by wzl on 2018/6/17.
 */

public class IntentUtil {

    /**
     * 跳转到指定页面
     * author wzl
     * date 2018/6/17 下午5:31
     * @param c
     *         上下文对象
     * @param cls
     *         指定跳转的类
     */
    public static void goToActivity(Context c, Class cls) {
        Intent intent = new Intent();
        intent.setClass(c, cls);
        c.startActivity(intent);
    }

    /**
     * 携带bundle数据跳转到指定页面
     * author wzl
     * date 2018/6/17 下午5:33
     */
    public static void goToActivity(Context c, Class cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(c, cls);
        intent.putExtras(bundle);
        c.startActivity(intent);
    }

    /**
     * 跳转指定页面并返回
     * author wzl
     * date 2018/6/17 下午5:35
     * @param requstCode
     *         请求码
     */
    public static void goToActivityForResult(Context c, Class cls, int requstCode) {
        Intent intent = new Intent();
        intent.setClass(c, cls);
        ((Activity) c).startActivityForResult(intent, requstCode);
    }

    /**
     * 携带bundle数据跳转指定页面并返回
     */
    public static void goToActivityForResult(Context c, Class cls, Bundle bundle, int requstCode) {
        Intent intent = new Intent();
        intent.setClass(c, cls);
        intent.putExtras(bundle);
        ((Activity) c).startActivityForResult(intent, requstCode);
    }

    /**
     * 跳转到指定页面并关闭当前页面
     * author wzl
     * date 2018/6/17 下午5:59
     */
    public static void goToActivityAndFinish(Context c, Class cls) {
        Intent intent = new Intent();
        intent.setClass(c, cls);
        c.startActivity(intent);
        ((Activity) c).finish();
    }

    /**
     * 跳转到指定页面并关闭当前页面
     * author wzl
     * date 2018/6/17 下午5:59
     */
    public static void goToActivityAndFinish(Context c, Class cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(c, cls);
        intent.putExtras(bundle);
        c.startActivity(intent);
        ((Activity) c).finish();
    }

    /**
     * 跳转到指定页面
     * author wzl
     * date 2018/6/17 下午6:02
     */
    public static void goToActivityAndFinishTop(Context c, Class cls) {
        Intent intent = new Intent();
        intent.setClass(c, cls);
        // FLAG_ACTIVITY_CLEAR_TOP 销毁目标Activity和它之上的所有Activity，重新创建目标Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(intent);
        ((Activity) c).finish();
    }

    /**
     * 跳转到指定页面
     * author wzl
     * date 2018/6/17 下午6:02
     */
    public static void goToActivityAndFinishTop(Context c, Class cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(c, cls);
        intent.putExtras(bundle);
        // FLAG_ACTIVITY_CLEAR_TOP 销毁目标Activity和它之上的所有Activity，重新创建目标Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(intent);
        ((Activity) c).finish();
    }

    /**
     * 跳转到指定页面
     * author wzl
     * date 2018/6/17 下午6:02
     */
    public static void goToActivityAndFinishSingleTop(Context c, Class clz, Bundle bundle) {
        Intent intent = new Intent(c, clz);
        intent.putExtras(bundle);
        //当该activity处于task栈顶时，可以复用，直接onNewIntent
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Activity a = (Activity) c;
        a.startActivity(intent);
        a.finish();
    }

    /**
     * 跳转到指定页面
     * author wzl
     * date 2018/6/17 下午6:02
     */
    public static void goToActivityAndFinishSingleTop(Context c, Class clz) {
        Intent intent = new Intent(c, clz);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Activity a = (Activity) c;
        a.startActivity(intent);
        a.finish();
    }


}
