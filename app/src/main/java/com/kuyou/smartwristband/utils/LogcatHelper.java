package com.kuyou.smartwristband.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import com.kuyou.smartwristband.BuildConfig;

public class LogcatHelper {
    private static final String TAG = "LogcatHelper_123456";
    private static final String FILE_NAME_BASE_DEF = "BDMsg";
    private static final String FILE_NAME_END = ".log";
    private static final String DIR_PATH_KU_LOG = "KuYou";

    private static LogcatHelper INSTANCE = null;
    private static String DIR_PATH_LOGCAT = null;

    private LogDumper mLogDumper = null;
    private Context mContext;
    private int mPId;

    public void init(Context context,String appName) {
        if(null!=DIR_PATH_LOGCAT){
            Log.w(TAG,"init cancel");
            return;
        }
        mContext=context;
        appName=appName.replaceAll("\\s*", "");

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            DIR_PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {//本应用目录
            DIR_PATH_LOGCAT = context.getFilesDir().getAbsolutePath();
        }
        DIR_PATH_LOGCAT=new StringBuilder(DIR_PATH_LOGCAT)
                .append("/").append(DIR_PATH_KU_LOG)
                .append("/").append(appName)
                .append("/").append("Log")
                .toString();
        Log.w(TAG,"DIR_PATH_LOGCAT="+DIR_PATH_LOGCAT);

        File file = new File(DIR_PATH_LOGCAT);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file.exists()) {
            Log.e(TAG,new StringBuilder("init>").append(DIR_PATH_LOGCAT).append(" create fail").toString());
        }
    }

    public static LogcatHelper getInstance(Context context,String appName) {
        if (INSTANCE == null) {
            INSTANCE = new LogcatHelper(context,appName);
        }
        return INSTANCE;
    }

    private LogcatHelper(Context context,String appName) {
        init(context,appName);
        mPId = android.os.Process.myPid();
    }

    public void start() {
        //@{ added by wgx Usefulness:非调试版本
        if(BuildConfig.DEBUG //debug版本不运行
            ||(!BuildConfig.DEBUG && Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) <= 0)){ //release版本未开usb调试不运行
            Log.d(TAG,"start > cancel");
            return;
        }
        Log.d(TAG,"start");
        //}@ end wgx

        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPId));
        }
        mLogDumper.start();
    }

    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    private static class LogDumper extends Thread {

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPID;
        private File logFile = null;
        private FileOutputStream out = null;
        private int autoCreateOldFileCount=-1;

        public LogDumper(String pid) {
            autoCreateOldFileCount=0;
            mPID = pid;

            /**
             * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
             * 显示当前mPID程序的 E和W等级的日志.
             */
            // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
            // cmds = "logcat | grep \"(" + mPID + ")\"";//打印所有日志信息
            // cmds = "logcat -s way";//打印标签过滤信息
            cmds = "logcat | grep \"(" + mPID + ")\"";
        }

        public void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {
            try {
                while (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        &&!PermissionUtil.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    try{
                        sleep(2000);
                    }catch (Exception e){}
                }
                try {
                    logFile = new File(new StringBuilder(DIR_PATH_LOGCAT)
                            .append(File.separator)
                            .append(CommonUtil.formatLocalTimeByMilSecond(System.currentTimeMillis(),"yyyyMMdd_HHmmss")).append(FILE_NAME_END)
                            .toString());
                    if (!logFile.exists()) {
                        logFile.createNewFile();
                    }
                    out = new FileOutputStream(logFile);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                } catch (IOException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }

                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
                String line = null;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if(!logFile.exists()
                        && autoCreateOldFileCount<5){
                        logFile.createNewFile();
                        out = new FileOutputStream(logFile);
                        autoCreateOldFileCount+=1;
                    }
                    if (out != null && line.contains(mPID)) {
                        out.write((line + "\n").getBytes());
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                    out = null;
                }
            }
        }
    }
}