package com.kuyou.smartwristband.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.kuyou.smartwristband.R;
import com.kuyou.smartwristband.utils.LogcatHelper;
import com.tencent.mmkv.MMKV;

public class BaseAppcation extends Application {

    private static BaseAppcation app;
    private int count = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        String rootDir = MMKV.initialize(this);
        LogcatHelper.getInstance(this, getString(R.string.app_name)).start();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                count++;
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                if (count > 0) {
                    count--;
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    public static BaseAppcation getInstance() {
        return app;
    }

    /**
     * 判断app是否在后台
     *
     * @return
     */
    public boolean isBackground() {
        return count <= 0;
    }
}
