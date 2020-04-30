package com.kuyou.smartwristband.base;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.Nullable;

import com.kuyou.smartwristband.utils.CommonUtil;
import com.kuyou.smartwristband.utils.PermissionUtil;
import com.kuyou.smartwristband.R;
import com.kuyou.smartwristband.views.RecyclerRefreshLayout;
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import butterknife.ButterKnife;

public abstract class BaseActivity extends BasePermissionsActivity implements RecyclerRefreshLayout.SuperRefreshLayoutListener {

    protected final String TAG = this.getClass().getSimpleName();
    protected RecyclerRefreshLayout mContentViewGrop;
    protected View mRoodView;
    protected ImageView mIvWrisbandStatus;
    protected TextView titleName;
    protected RelativeLayout titleBg;
    protected TextView rightText;
    protected RelativeLayout bar_bg;
    protected RelativeLayout layoutTitle;
    protected ImageView rightImg;
    private Bundle bundle;
    protected BaseAppcation mBaseAppcation;
    private PermissionUtil mPermissionUtil = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O
                && isTranslucentOrFloating()) {
            boolean result = fixOrientation();
        }
        super.onCreate(savedInstanceState);
        bundle = savedInstanceState;
        mBaseAppcation = (BaseAppcation) getApplication();
        registerBleStatusReceiver();
        initActivity();
    }

    private boolean fixOrientation() {
        try {
            Field field = Activity.class.getDeclaredField("mActivityInfo");
            field.setAccessible(true);
            ActivityInfo o = (ActivityInfo) field.get(this);
            o.screenOrientation = -1;
            field.setAccessible(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isTranslucentOrFloating() {
        boolean isTranslucentOrFloating = false;
        try {
            int[] styleableRes = (int[]) Class.forName("com.android.internal.R$styleable").getField("Window").get(null);
            final TypedArray ta = obtainStyledAttributes(styleableRes);
            Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
            m.setAccessible(true);
            isTranslucentOrFloating = (boolean) m.invoke(null, ta);
            m.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isTranslucentOrFloating;
    }

    private void initActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.light_blue));
        }
        setContentView(R.layout.layout_base_title);
        mIvWrisbandStatus = (ImageView) findViewById(R.id.iv_wrisband_status);
        titleName = (TextView) findViewById(R.id.base_title_name);
        titleName.setText(getString(R.string.app_name));
        rightText = (TextView) findViewById(R.id.base_right_text);
        titleBg = (RelativeLayout) findViewById(R.id.base_title_bg);
        mContentViewGrop = (RecyclerRefreshLayout) findViewById(R.id.base_content);
        layoutTitle = (RelativeLayout) findViewById(R.id.layoutTitle);
        bar_bg = (RelativeLayout) findViewById(R.id.bar_bg);
        rightImg = (ImageView) findViewById(R.id.rightImg);
        onViewCreate();

        mContentViewGrop.setSuperRefreshLayoutListener(this);
        mContentViewGrop.setColorSchemeColors(Color.RED, Color.GREEN, Color.CYAN);
        mContentViewGrop.setCanLoadMore(false);
        initViews();
    }

    protected void onViewCreate() {
        mRoodView = LayoutInflater.from(this).inflate(getContentView(), null);
        ButterKnife.bind(this, mRoodView);
        mContentViewGrop.addView(mRoodView);
    }

    /**
     * 得到Activity布局ID
     *
     * @return
     */
    protected abstract int getContentView();

    /**
     * 初始化控件
     */
    protected void initViews() {

    }

    protected void showToast(final String content) {
        if (null == content || content.length() <= 0)
            return;
        runOnUiThread(() -> ToastUtil.showToast(this, content));
    }

    //@{ added by wgx Usefulness:
    private BroadcastReceiver mBluetoothStatusReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent
                    && null != intent.getAction()
                    && intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
//                        case BluetoothAdapter.STATE_TURNING_ON:
//                            break;
//                        case BluetoothAdapter.STATE_TURNING_OFF:
//                            break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "STATE_ON");
                        onBluetoothOn();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "STATE_OFF");
                        onBluetoothOff();
                        break;
                    default:
                        break;
                }
            }
        }
    };

    private void registerBleStatusReceiver() {
        registerReceiver(mBluetoothStatusReceive, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    protected boolean isBackground() {
        return mBaseAppcation.isBackground();
    }

    protected RecyclerRefreshLayout getRecyclerRefreshLayout() {
        return mContentViewGrop;
    }


    @Override
    protected void onPause() {
        super.onPause();
        mContentViewGrop.onComplete();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mContentViewGrop.setRefreshing(false);
        mContentViewGrop.onComplete();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mBluetoothStatusReceive);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }


    /**
     * action: 蓝牙打开了
     *
     */
    protected void onBluetoothOn() {

    }

    /**
     * action: 蓝牙关闭了
     *
     */
    protected void onBluetoothOff() {
        CommonUtil.isOpenBle(this);
    }

    @Override
    public void onRefreshing() {

    }

    @Override
    public void onLoadMore() {

    }

    //}@ end wgx

}
