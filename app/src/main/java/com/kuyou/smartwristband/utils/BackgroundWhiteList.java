package com.kuyou.smartwristband.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

public class BackgroundWhiteList {
    private static final String TAG = "BackgroundWhiteList_123456";
    
    private static BackgroundWhiteList sMain;
    private Context mContext;

    private BackgroundWhiteList(Context context){
        mContext=context;
    }

    public static BackgroundWhiteList getInstance(Context context){
        if(null==sMain){
            sMain=new BackgroundWhiteList(context);
        }
        return sMain;
    }

    /**
     * action：申请加入后台白名单
     * return true 说明需要申请
     */
    public boolean open(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return false;
        }
        if(!isIgnoringBatteryOptimizations(mContext)){
            requestIgnoreBatteryOptimizations(mContext);
            //goSetting();
            return true;
        }
        return false;
    }

    private boolean isIgnoringBatteryOptimizations(Context context) {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return isIgnoring;
    }

    public void requestIgnoreBatteryOptimizations(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean showActivity(String packageName) {
        try {
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
            mContext.startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return false;
    }

    private boolean showActivity(String packageName, String activityDir) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, activityDir));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return false;
    }


    public void goSetting(){
        String brand="null";
        if(null!=Build.BRAND)
            brand= Build.BRAND.toLowerCase();
        switch (brand){
            case "huawei" :
            case "honor" :
                if(showActivity("com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"))
                    break;
                showActivity("com.huawei.systemmanager",
                        "com.huawei.systemmanager.optimize.bootstart.BootStartActivity");
                break;
            case "xiaomi" :
                showActivity("com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity");
                break;
            case "oppo" :
                if(showActivity("com.coloros.phonemanager"))
                    break;
                if(showActivity("com.oppo.safe"))
                    break;
                if(showActivity("com.coloros.oppoguardelf"))
                    break;
                showActivity("com.coloros.safecenter");
            case "vivo" :
                showActivity("com.iqoo.secure");
                break;
            case "meizu" :
                showActivity("com.meizu.safe");
                break;
            case "samsung" :
                if(showActivity("com.samsung.android.sm_cn"))
                    break;
                showActivity("com.samsung.android.sm");
                break;
            case "smartisan" :
                showActivity("com.smartisanos.security");
                break;
            default:
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

    }


}
