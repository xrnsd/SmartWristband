package com.kuyou.smartwristband;

import android.content.DialogInterface;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kuyou.smartwristband.base.BaseActivity;
import com.kuyou.smartwristband.ui.ScanDeviceReadyActivity;
import com.kuyou.smartwristband.utils.BackgroundWhiteList;
import com.kuyou.smartwristband.utils.CommonUtil;
import com.kuyou.smartwristband.utils.IntentUtil;
import com.kuyou.smartwristband.wristband.WristbandDevice;
import com.kuyou.smartwristband.wristband.WristbandManager;

import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper;

import butterknife.BindView;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final String KEY_SYSTOLIC_PRESSURE = "value.SystolicPressure",
            KEY_DIASTOLIC_PRESSURE = "value.DiastolicPressure",
            KEY_STEPCOUNT = "value.StepCount",
            KEY_HEARTRATE = "value.HeartRate";

    @BindView(R.id.scanDevice)
    Button mBtnScanDevice;
    @BindView(R.id.tvContectDate)
    TextView mTvContectDate;
    @BindView(R.id.tv_device_info_name)
    TextView mTvDeviceInfoName;
    @BindView(R.id.tv_device_info_mac)
    TextView mTvDeviceInfoMac;
    @BindView(R.id.tv_device_info_battery)
    TextView mTvDeviceInfoBattery;
    @BindView(R.id.tv_step_count)
    TextView mTvStepCount;
    @BindView(R.id.tv_heart_rate)
    TextView mTvHeartRate;
    @BindView(R.id.tv_blood_pressure)
    TextView mTvBloodPressure;
    @BindView(R.id.ivState)
    ImageView mIvState;

    private WristbandManager mWristbandManager;
    private WristbandDevice mWristbandDevice;

    private Runnable mRunnableRefreshEnd = new Runnable() {
        @Override
        public void run() {
            getRecyclerRefreshLayout().setRefreshing(false);
        }
    };

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        super.initViews();
        //手环管理器初始化
        mWristbandManager = WristbandManager.getInstance(mBaseAppcation,
            new WristbandManager.IOnWristbandSyncListener() {
                @Override
                public void onSyncFinish() {
                    Log.d(TAG, "onSyncFinish");
                    runOnUiThread(() -> refreshViews());
                }

                @Override
                public void onSyncFail(String error) {
                    Log.d(TAG, new StringBuilder("onSyncFail>error=")
                            .append(error)
                            .toString());
                    showToast(error);
                    runOnUiThread(() -> refreshViews());
                }

                @Override
                public void onUploadFinish(okhttp3.Response result) {
                    if (null != result)
                        Log.d(TAG, "onUploadFinish>" + result.toString());
                }

                @Override
                public boolean onLocationChanged(final Location location) {
                    final boolean flagLocationLog = true;
                    if (null == location) {
                        return flagLocationLog;
                    }
                    return flagLocationLog;
                }
        });
        mWristbandDevice = mWristbandManager.getWristbandDevice();
        mTvDeviceInfoMac.setOnLongClickListener(mWristbandDevice);
    }

    @Override
    protected void onResume() {
        super.onResume();
        runOnUiThread(() -> refreshViews());
        mWristbandManager.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWristbandManager.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        mWristbandManager.onDestroy();
        super.onDestroy();
    }

    @Override
    protected String[] getPermissions() {
        BackgroundWhiteList.getInstance(MainActivity.this).open();
        return mWristbandManager.getPermissions();
    }

    @Override
    protected void onBluetoothOn() {
        super.onBluetoothOn();
        if (BleSdkWrapper.isBind()) {
            getRecyclerRefreshLayout().setRefreshing(true);
        } else {
            mBtnScanDevice.callOnClick();
        }
        mWristbandManager.onBluetoothOn();
        mWristbandManager.reStart(false);
    }

    @Override
    protected void onBluetoothOff() {
        super.onBluetoothOff();
        getRecyclerRefreshLayout().setRefreshing(false);
        mWristbandManager.onBluetoothOff();
    }

    @Override
    public void onRefreshing() {
        super.onRefreshing();
        if (CommonUtil.isOpenBle(MainActivity.this)) {
            runOnUiThread(() -> refreshViews());
            return;
        }
        Log.d(TAG, "onRefreshing");
        mWristbandManager.reStart(true);
    }

    /**
     * action: [解]绑定手环<br/>
     * remark: 蓝牙未开启时,会先请求开启
     */
    public void toScanBindDevice(View v) {
        if (BleSdkWrapper.isBind()) {
            CommonUtil.showSureDialog(MainActivity.this, getString(R.string.enter_unbind_wristband),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BleSdkWrapper.unBind();
                            runOnUiThread(() -> refreshViews());
                        }
                    }, null);
        } else {
            if (CommonUtil.isOpenBle(MainActivity.this)) {
                return;
            }
            IntentUtil.goToActivity(MainActivity.this, ScanDeviceReadyActivity.class);
        }
    }

    /**
     * action: 刷新UI [手环数据和状态显示]<br/>
     */
    private void refreshViews() {
        if (isBackground())
            return;
        if (!BleSdkWrapper.isBind()
                || !mWristbandManager.isConnectIng()) {
            Log.d(TAG, "resetViews");
            mBtnScanDevice.setText(R.string.unbind_wristband);
            mTvDeviceInfoName.setText(R.string.bound_wristband_no_connect);
            mContentViewGrop.onComplete();
            if (!BleSdkWrapper.isBind()) {
                mTvDeviceInfoName.setText(R.string.contenct_device_info_none);
                mBtnScanDevice.setText(R.string.bound_wristband);
                mContentViewGrop.setEnabled(false);
            } else {
                mContentViewGrop.setOnLoading(false);
            }
            mIvWrisbandStatus.setImageResource(R.mipmap.ic_disconnect);
            mTvDeviceInfoMac.setVisibility(View.INVISIBLE);
            mTvDeviceInfoMac.setEnabled(false);
            mTvDeviceInfoBattery.setVisibility(View.GONE);
            if (null != mWristbandDevice) {
                mTvContectDate.setText(mWristbandDevice.getSyncTimeDef());
            }
            //显示缓存缓存数据
            mWristbandDevice.restorePersistenceData();
            mTvStepCount.setText(mWristbandDevice.getStepCount());
            mTvHeartRate.setText(mWristbandDevice.getHeartRate());
            mTvHeartRate.setText(mWristbandDevice.getHeartRate());
            mTvBloodPressure.setText(new StringBuilder()
                    .append(mWristbandDevice.getSystolicPressure())
                    .append("/")
                    .append(mWristbandDevice.getDiastolicPressure())
                    .toString());
            //显示或隐藏正在加载
            if(BleSdkWrapper.isBind()){
                if(mWristbandDevice.isSyncFinish()){
                    getRecyclerRefreshLayout().removeCallbacks(mRunnableRefreshEnd);
                    getRecyclerRefreshLayout().setRefreshing(false);
                }else{
                    getRecyclerRefreshLayout().setRefreshing(true);
                    getRecyclerRefreshLayout().postDelayed(mRunnableRefreshEnd,5000);
                }
            }
            return;
        }

        Log.d(TAG, "refreshViews");
        if (null == mWristbandDevice) {
            mWristbandManager.initWristbandDevice();
            mWristbandDevice = mWristbandManager.getWristbandDevice();
        }
        mContentViewGrop.setOnLoading(false);
        mContentViewGrop.onComplete();
        mContentViewGrop.setEnabled(true);
        getRecyclerRefreshLayout().setRefreshing(!mWristbandDevice.isSyncFinish());

        mBtnScanDevice.setText(R.string.unbind_wristband);
        mIvWrisbandStatus.setImageResource(R.mipmap.ic_connect);
        mTvBloodPressure.setText(new StringBuilder()
                .append(mWristbandDevice.getSystolicPressure())
                .append("/")
                .append(mWristbandDevice.getDiastolicPressure()));
        String wristbandId = mWristbandDevice.getWristbandId();
        if (null != wristbandId
                && !wristbandId.equals("-1")) {
            mTvDeviceInfoMac.setText(getString(R.string.device_info_mac_none, wristbandId));
            mTvDeviceInfoMac.setVisibility(View.VISIBLE);
            mTvDeviceInfoMac.setEnabled(true);
        }
        if (!mWristbandDevice.getPower().equals("0")) {
            mTvDeviceInfoBattery.setText(mWristbandDevice.getPower() + getString(R.string.info_battery));
            mTvDeviceInfoBattery.setVisibility(View.VISIBLE);
//        }else if (mWristbandDevice.isChargIng()){
//            mTvDeviceInfoBattery.setText("充电中");
//            mTvDeviceInfoBattery.setVisibility(View.VISIBLE);
        }
        mTvDeviceInfoName.setText(mWristbandDevice.getDeviceName());
        mIvState.setImageResource(R.mipmap.device_rssi_3);
        if (Math.abs(mWristbandDevice.getRssi()) <= 70) {
            mIvState.setImageResource(R.mipmap.device_rssi_1);
        } else if (Math.abs(mWristbandDevice.getRssi()) <= 90) {
            mIvState.setImageResource(R.mipmap.device_rssi_2);
        }

        mTvContectDate.setText(mWristbandDevice.getSyncTime());
        mTvStepCount.setText(mWristbandDevice.getStepCount());
        mTvHeartRate.setText(mWristbandDevice.getHeartRate());
    }

    //added by wgx  Usefulness:test
    public void toTraceActivity_Simple(View v) {
        IntentUtil.goToActivity(MainActivity.this, TraceActivity_Simple.class);
    }
    public void toTraceActivity_Upload(View v) {
        IntentUtil.goToActivity(MainActivity.this, TraceActivityUpload.class);
    }
}
