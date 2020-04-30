package com.kuyou.smartwristband.wristband;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.kuyou.smartwristband.R;
import com.kuyou.smartwristband.base.BaseAppcation;

import com.kuyou.smartwristband.gps.GPSUtils;
import com.kuyou.smartwristband.gps.filter.TrackPoint;
import com.kuyou.smartwristband.utils.HttpReqUtil;
import com.kuyou.smartwristband.utils.ThreadPoolUtils;
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleManager;
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BaseAppBleListener;
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleScanTool;
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper;
import com.zhj.bluetooth.zhjbluetoothsdk.util.SPHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.tencent.mmkv.MMKV;

/**
 * action: 手环管理器
 * <p>
 * author: wuguoxian <br/>
 * date: 20200126 <br/>
 * <p>
 * remark1:
 * <p>01 手环状态设置 </p>
 * <p>02 手环状态保持 </p>
 * <p>03 手环数据同步到终端 </p>
 * <p>04 终端数据同步到后台 </p>
 */
public class WristbandManager implements WristbandManagePolicy {

    private static final String TAG = "WristbandManager_123456";

    /**
     * action: 流程心跳,设备状态维持更新<br/>
     */
    private static final int MSG_HEARTBEAT_WRISTBAND_STATUS = 1;
    /**
     * action: 手环重新连接<br/>
     **/
    private static final int MSG_RECONNECT = 2;
    /**
     * action: 手环重新连接超时<br/>
     **/
    private static final int MSG_RECONNECT_TIME_OUT = 3;
    /**
     * action: 流程心跳,手环数据同步<br/>
     */
    private static final int MSG_HEARTBEAT_DATA_SYNC = 4;
    /**
     * action: 手环数据同步到终端状态确认<br/>
     */
    private static final int MSG_DATA_SYNC_CHECK = 5;
    /**
     * action: 手环断开或重连多次失败后,停止相关流程心跳<br/>
     **/
    private static final int MSG_RESET = 6;

    /**
     * action: 蓝牙是否正在重连<br/>
     */
    public static boolean sIsReConnectIng = false;
    /**
     * action: 蓝牙近期是否被开关判定<br/>
     */
    public static long sSwitchBluetoothTime = -1;
    /**
     * action: 终端数据同步连续失败次数记录<br/>
     */
    public static int sWristbandDataSyncDelayCount = 0;

    /**
     * action: 消息处理器 <br/>
     * remark: 手环状态维持
     */
    private Handler mHandlerWristbandControl;
    /**
     * action: 消息处理线程 <br/>
     * remark: 手环状态维持
     */
    private HandlerThread mHandlerThreadWristbandControl;
    /**
     * action: 消息处理器 <br/>
     * remark: 手环数据同步和上传
     */
    private Handler mHandlerWristbandDataSync;
    /**
     * action: 消息处理线程 <br/>
     * remark: 手环数据同步和上传
     */
    private HandlerThread mHandlerThreadWristbandDataSync;

    private Context mContext;
    private BaseAppcation mBaseAppcation;
    private WristbandDevice mWristbandDevice;
    private static WristbandManager sInstance;
    private IOnWristbandSyncListener mOnWristbandSyncListener;

    private boolean isFirstBoot = true;

    /**
     * action: 蓝牙连续重连失败次数记录<br/>
     */
    private int mAutoReConnectCount = 0;
    /**
     * action: 蓝牙连续上的时间点<br/>
     */
    private long mConnectTime = -1;

    /**
     * action: 蓝牙状态监听器
     * <p>
     * author: wuguoxian <br/>
     * date: 20200301 <br/>
     */
    private BaseAppBleListener mBaseAppBleListener = new BaseAppBleListener() {
        @Override
        public void initComplete() {
            super.initComplete();
            mConnectTime = System.currentTimeMillis();
            sIsReConnectIng = false;
            Log.d(TAG, "mBaseAppBleListener>initComplete: wristband connect success");
            reStart();
        }
    };

    /**
     * action: 手环控制器监听器
     * <p>
     * author: wuguoxian <br/>
     * date: 20200110 <br/>
     */
    public static interface IOnWristbandSyncListener extends WristbandDevice.ILocationChangerListener {
        /**
         * action: 同步完成一次手环数据<br/>
         */
        public void onSyncFinish();

        /**
         * action: 同步手环数据失败或UI需要重置刷新<br/>
         * remark: 同步手环数据失败通常由于连接异常，多次连续异常时建议重新绑定<br/>
         */
        public void onSyncFail(String error);

        /**
         * action: 手环信息后台同步完成<br/>
         * parameter： result 为服务器返回信息，200表示上传成功
         * remark: 正常情况下此方法被调用次数和同步次数一致<br/>
         */
        public void onUploadFinish(Response result);
    }


    public static WristbandManager getInstance(BaseAppcation app, IOnWristbandSyncListener listener) {
        if (null == sInstance) {
            sInstance = new WristbandManager(app, listener);
        }
        return sInstance;
    }

    private WristbandManager(BaseAppcation app, IOnWristbandSyncListener listener) {
        mBaseAppcation = app;
        mContext = app.getApplicationContext();
        mOnWristbandSyncListener = listener;
        BleSdkWrapper.init(mContext, null);//手环SDK初始化
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) { // 不需要动态权限申请时,直接初始化WristbandDevice
            initWristbandDevice();
        }
        initWristbandControlThread(mContext);
        initSyncWorker(mContext, 16);
    }

    public void initWristbandDevice() {
        if (null != mWristbandDevice)
            return;
        mWristbandDevice = new WristbandDevice(mContext, mOnWristbandSyncListener);
    }

    private void initWristbandControlThread(final Context context) {
        if (null != mHandlerThreadWristbandControl)
            return;
        mHandlerThreadWristbandControl = new HandlerThread("WristbandControlThread");
        mHandlerThreadWristbandControl.start();
        mHandlerWristbandControl = new Handler(mHandlerThreadWristbandControl.getLooper()) {

            /**
             * action: 流程心跳频率[时长]设定 <p/>
             * remark1:<p>未绑定手环时为 DELAY_HEARTBEAT_WRISTBAND_STATUS_UNBOUND </p>
             * remark2:<p>在蓝牙开关一次后40秒内,流程心跳为40S </p>
             * remark3:<p>手环重连连续失败次数大于 FAST_RECONNECT_TIMES_MAX 时频率会线性增加到DELAY_HEARTBEAT_WRISTBAND_STATUS_UNBOUND </p>
             * remark4:<p>APP处于后台时频率为 DELAY_HEARTBEAT_WRISTBAND_STATUS_BACKGROUND </p>
             */
            private int getHeartbeatWristbandStatusDelay() {
                if (!BleSdkWrapper.isBind()) {
                    return DELAY_HEARTBEAT_WRISTBAND_STATUS_UNBOUND;
                }
                if (-1 != sSwitchBluetoothTime
                        && System.currentTimeMillis() - sSwitchBluetoothTime < 40000) {
                    return 40000;
                }
                if (mAutoReConnectCount > FAST_RECONNECT_TIMES_MAX) {
                    final int timeLongBase = mBaseAppcation.isBackground()
                            ? DELAY_HEARTBEAT_WRISTBAND_STATUS_BACKGROUND
                            : DELAY_HEARTBEAT_WRISTBAND_STATUS;

                    return (DELAY_HEARTBEAT_WRISTBAND_STATUS_UNBOUND - timeLongBase)
                            / (TIMES_RECONNECT_MAX_CONSECUTIVE_FAIL - FAST_RECONNECT_TIMES_MAX)
                            * (mAutoReConnectCount - FAST_RECONNECT_TIMES_MAX)
                            + timeLongBase;
                }
                return mBaseAppcation.isBackground()
                        ? DELAY_HEARTBEAT_WRISTBAND_STATUS_BACKGROUND
                        : DELAY_HEARTBEAT_WRISTBAND_STATUS;
            }

            /**
             * action: 自动重新连接手环超时时长设定 <br/>
             * remark1:<br/>在蓝牙开关一次后60秒内，超时时间为60秒。避免重连失败<br/>
             * remark2:<br/>在蓝牙重连TIMES_RECONNECT_MAX_CONSECUTIVE_FAIL次失败后,会自动解绑手环<br/>
             */
            private int getRemConnectTimeout() {
                if (-1 != sSwitchBluetoothTime
                        && System.currentTimeMillis() - sSwitchBluetoothTime < 60000) {
                    return 60000;
                }
                return RECONNECT_TIME_OUT;
            }

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_RESET:
                        Log.d(TAG, "MSG_RESET");
                        mHandlerWristbandControl.removeMessages(MSG_HEARTBEAT_WRISTBAND_STATUS);
                        mHandlerWristbandControl.removeMessages(MSG_RECONNECT_TIME_OUT);
                        mHandlerWristbandControl.removeMessages(MSG_RECONNECT);
                        mHandlerWristbandDataSync.removeMessages(MSG_DATA_SYNC_CHECK);
                        mHandlerWristbandDataSync.removeMessages(MSG_HEARTBEAT_DATA_SYNC);
                        break;

                    case MSG_RECONNECT:
                        mHandlerWristbandControl.removeMessages(MSG_RECONNECT_TIME_OUT);
                        Log.d(TAG, "MSG_RECONNECT > reConnectCount=" + mAutoReConnectCount);

                        BleSdkWrapper.setBleListener(mBaseAppBleListener);
                        BleManager.getInstance().connect(SPHelper.getBindBLEDevice(mContext));
                        mHandlerWristbandControl.sendEmptyMessageDelayed(MSG_RECONNECT_TIME_OUT, getRemConnectTimeout());
                        break;

                    case MSG_RECONNECT_TIME_OUT:
                        mHandlerWristbandControl.removeMessages(MSG_RECONNECT_TIME_OUT);
                        Log.d(TAG, "MSG_RECONNECT_TIME_OUT");

                        BleSdkWrapper.removeListener(mBaseAppBleListener);
                        BleSdkWrapper.stopScanDevices();
                        if (isIgnorePrompt()) {
                            mConnectTime = -1;
                            onSyncFail(mContext.getString(R.string.connect_fail));
                        } else {
                            Log.w(TAG, "MSG_HEARTBEAT_WRISTBAND_STATUS > wristband is reconnect fail but does not prompt ");
                        }
                        sIsReConnectIng = false;
                        break;

                    case MSG_HEARTBEAT_WRISTBAND_STATUS:
                        mHandlerWristbandControl.removeMessages(MSG_HEARTBEAT_WRISTBAND_STATUS);
                        if (!BleScanTool.getInstance().isBluetoothOpen()) {//蓝牙未打开
                            Log.d(TAG, "MSG_HEARTBEAT_WRISTBAND_STATUS > bluetooth is off");
                            onSyncFail(null);
                            break;
                        } else if (!BleSdkWrapper.isBind()) { //手环未绑定
                            Log.w(TAG, "MSG_HEARTBEAT_WRISTBAND_STATUS > wristband is unBind");
                            mHandlerWristbandControl.sendEmptyMessage(MSG_RESET);
                            break;

                        } else if (BleSdkWrapper.isConnected()) { //手环绑定并连接
                            if (sIsReConnectIng) {//重连成功,开启数据同步心跳
                                sIsReConnectIng = false;
                                //防止手环连接上后流程重复两遍
                                BleSdkWrapper.removeListener(mBaseAppBleListener);
                                BleSdkWrapper.stopScanDevices();
                                mHandlerWristbandControl.removeMessages(MSG_RECONNECT_TIME_OUT);

                                Log.d(TAG, "MSG_HEARTBEAT_WRISTBAND_STATUS > wristband reconnect success");
                            }
                            if (-1 == mConnectTime)//第一次连接上时,或很久都没连接上导致数据同步心跳已停止时,需要马上启动数据同步心跳
                                mHandlerWristbandDataSync.sendEmptyMessage(MSG_HEARTBEAT_DATA_SYNC);
                            mConnectTime = System.currentTimeMillis();
                            mAutoReConnectCount = 0;
                            Log.d(TAG, "MSG_HEARTBEAT_WRISTBAND_STATUS > is conntecting");

                        } else if (sIsReConnectIng) {//正在重连
                            Log.d(TAG, "MSG_HEARTBEAT_WRISTBAND_STATUS > is ReConnectIng");

                        } else if (mAutoReConnectCount < TIMES_RECONNECT_MAX_CONSECUTIVE_FAIL) {//连续重连次数小于 TIMES_RECONNECT_MAX_CONSECUTIVE_FAIL 时尝试重连
                            sIsReConnectIng = true;
                            mAutoReConnectCount += 1;
                            mHandlerWristbandControl.removeMessages(MSG_RECONNECT);
                            mHandlerWristbandControl.sendEmptyMessage(MSG_RECONNECT);
                            Log.d(TAG, "MSG_HEARTBEAT_WRISTBAND_STATUS > is goto ReConnect");

                        } else {//连续重连次数大于 TIMES_RECONNECT_MAX_CONSECUTIVE_FAIL 后自动解绑手环
                            BleSdkWrapper.unBind();
                            Log.w(TAG, "MSG_HEARTBEAT_WRISTBAND_STATUS > Wristband auto unBind");
                            mHandlerWristbandControl.sendEmptyMessage(MSG_RESET);
                            onSyncFail(null);
                            break;
                        }
                        final int time = getHeartbeatWristbandStatusDelay();
                        if (DELAY_HEARTBEAT_WRISTBAND_STATUS != time)
                            Log.d(TAG, "MSG_HEARTBEAT_WRISTBAND_STATUS > next msg delay : " + time);
                        mHandlerWristbandControl.sendEmptyMessageDelayed(MSG_HEARTBEAT_WRISTBAND_STATUS, time);
                        break;
                    default:
                        break;
                }
            }
        };
        mHandlerThreadWristbandDataSync = new HandlerThread("WristbandDataSyncThread");
        mHandlerThreadWristbandDataSync.start();
        mHandlerWristbandDataSync = new Handler(mHandlerThreadWristbandDataSync.getLooper()) {

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_HEARTBEAT_DATA_SYNC:
                        mHandlerWristbandDataSync.removeMessages(MSG_HEARTBEAT_DATA_SYNC);
                        if (null == mWristbandDevice) {
                            Log.d(TAG, "MSG_HEARTBEAT_DATA_SYNC > cancel sync > wristbandDevice is null");
                            break;
                        }
                        if (sIsReConnectIng || !BleSdkWrapper.isConnected()) {
                            Log.d(TAG, "MSG_HEARTBEAT_DATA_SYNC > cancel sync");
                            break;
                        }
                        Log.d(TAG, "MSG_HEARTBEAT_DATA_SYNC");
                        mWristbandDevice.startSync(mContext);
                        mHandlerWristbandDataSync.sendEmptyMessageDelayed(MSG_DATA_SYNC_CHECK, DELAY_DATA_SYNC_CHECK);
                        break;

                    case MSG_DATA_SYNC_CHECK:
                        mHandlerWristbandDataSync.removeMessages(MSG_DATA_SYNC_CHECK);
                        Log.d(TAG, "MSG_DATA_SYNC_CHECK");

                        if (mWristbandDevice.isSyncFinish()) {
                            onSyncFinishAndUpload();
                            break;
                        }

                        if (sWristbandDataSyncDelayCount > TIMES_DATA_SYNC_CHECK) {//连续 TIMES_DATA_SYNC_CHECK 次不成功，确认同步失败
                            Log.e(TAG, "WristbandInfo sync timeout");
                            onSyncFail(null);
                            mHandlerWristbandDataSync.sendEmptyMessageDelayed(MSG_HEARTBEAT_DATA_SYNC, getHeartbeatDataDelay());
                            break;
                        }
                        sWristbandDataSyncDelayCount += 1;

                        mHandlerWristbandDataSync.sendEmptyMessageDelayed(MSG_DATA_SYNC_CHECK, DELAY_DATA_SYNC_CHECK); //不成功，延时确认
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * action: 数据同步上传心跳频率[时长]设定 <br/>
     * remark1:<p>手环重连连续失败次数大于 FAST_RECONNECT_TIMES_MAX 时为 DATA_HEARTBEAT_EXCEPTION_DELAY </p>
     * remark2:<p>APP处于后台时频率为 DELAY_HEARTBEAT_DATA_BACKGROUND </p>
     */
    private int getHeartbeatDataDelay() {
        if (mBaseAppcation.isBackground()) {
            return DELAY_HEARTBEAT_DATA_BACKGROUND;
        }
        return DELAY_HEARTBEAT_DATA;
    }

    /**
     * action: 断开连接的特殊处理 <br/>
     *
     * @return false 单次断开连接或断开时间在1分钟内
     */
    private boolean isIgnorePrompt() {
        return ((-1 != mConnectTime && System.currentTimeMillis() - mConnectTime > 60000)
                || mAutoReConnectCount > 2);
    }

    /**
     * action: 获取手环连接状态[非即时的]<br/>
     * remark1:忽略[单次断开连接或1分钟内的断开对]连接状态的影响
     */
    public boolean isConnectIng() {
        if (!BleScanTool.getInstance().isBluetoothOpen())
            return false;
        if (isFirstBoot) //第一次启动时，使用即时连接状态
            return BleSdkWrapper.isConnected();
        return BleSdkWrapper.isConnected() || !isIgnorePrompt();
    }

    /**
     * action: 手环同步成功后,数据显示和上传服务器 <br/>
     */
    public void onSyncFinishAndUpload() {
        Log.d(TAG, "onSyncFinishAndUpload");
        sWristbandDataSyncDelayCount = 0;
        if (null != mOnWristbandSyncListener) {
            mOnWristbandSyncListener.onSyncFinish();
        }

        mOnWristbandSyncListener.onUploadFinish(
                upload(getWristbandDevice().getJsonMsg()));
        mHandlerWristbandDataSync.sendEmptyMessageDelayed(MSG_HEARTBEAT_DATA_SYNC, getHeartbeatDataDelay());
    }

    /**
     * action: 手环同步失败或UI重置 <br/>
     */
    private void onSyncFail(String error) {
        Log.w(TAG, "onSyncFail");
        sWristbandDataSyncDelayCount = 0;
        if (null != mOnWristbandSyncListener)
            mOnWristbandSyncListener.onSyncFail(error);
    }

    /**
     * action: 获取手环信息描述类
     * <p>
     * author: wuguoxian <br/>
     */
    public WristbandDevice getWristbandDevice() {
        if (null == mWristbandDevice)
            initWristbandDevice();
        return mWristbandDevice;
    }

    /**
     * action: 重新开始手环的流程心跳,重置流程频率
     * <p>
     * author: wuguoxian <br/>
     *
     * @param isManualRefresh 标示是否为手动刷新
     */
    public void reStart(boolean isManualRefresh) {
        if (isManualRefresh) { //手动刷新的话,就不等待马上开始刷
            mConnectTime = -1;
        }
        if (!sIsReConnectIng) {
            mHandlerWristbandControl.removeMessages(MSG_RECONNECT);
            mHandlerWristbandControl.removeMessages(MSG_RECONNECT_TIME_OUT);
            BleSdkWrapper.removeListener(mBaseAppBleListener);
            BleSdkWrapper.stopScanDevices();
        }
        reStartWristbandHeartbeat();
    }

    private void reStart() {
        reStart(false);
    }

    /**
     * action: 重新开始手环的流程心跳<br/>
     */
    private void reStartWristbandHeartbeat() {
        mHandlerWristbandControl.removeMessages(MSG_HEARTBEAT_WRISTBAND_STATUS);
        mHandlerWristbandControl.sendEmptyMessage(MSG_HEARTBEAT_WRISTBAND_STATUS);
        mHandlerWristbandDataSync.removeMessages(MSG_HEARTBEAT_DATA_SYNC);
        mHandlerWristbandDataSync.sendEmptyMessage(MSG_HEARTBEAT_DATA_SYNC);
    }

    /**
     * action: 需要进行检测的权限数组
     * <p>
     * author: wuguoxian <br/>
     * date: 20200402 <br/>
     */
    public String[] getPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P
                && mContext.getApplicationInfo().targetSdkVersion > 28) {
            return new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
        }
        return new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        };
    }

    /**
     * action: 停止后台定位,重置流程心跳
     * <p>
     * author: wuguoxian <br/>
     * date: 20200402 <br/>
     */
    public void onResume() {
        reStartWristbandHeartbeat();
        if (null != mWristbandDevice)
            mWristbandDevice.onResume();
    }

    /**
     * action: 开始后台定位
     * <p>
     * author: wuguoxian <br/>
     * date: 20200402 <br/>
     * remark1:<p> 绑定手环情况下生效 </p>
     */
    public void onStop() {
        isFirstBoot = false;
        if (BleSdkWrapper.isBind()
                && mBaseAppcation.isBackground()
                && null != mWristbandDevice) {
            mWristbandDevice.onStop();
        }
    }

    /**
     * action: 注销位置获取
     * <p>
     * author: wuguoxian <br/>
     */
    public void onDestroy() {
        if (null != mWristbandDevice)
            mWristbandDevice.destroyLocation();
    }

    /**
     * action: 蓝牙打开处理
     * <p>
     * author: wuguoxian <br/>
     */
    public void onBluetoothOn() {
        sSwitchBluetoothTime = System.currentTimeMillis();
    }

    /**
     * action: 蓝牙关闭处理
     * <p>
     * author: wuguoxian <br/>
     */
    public void onBluetoothOff() {
        onSyncFail(null);
        mHandlerWristbandControl.sendEmptyMessage(MSG_RESET);
        sSwitchBluetoothTime = System.currentTimeMillis();
    }

    // =========================================      周期任务相关      ================================================

    /**
     * action: 用于长时间熄屏下的设备休眠后周期唤醒数据同步和上传失败数据的重传的任务<br/>
     */
    public class SyncWorker extends Worker {
        private Context mContext;

        public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
            mContext = context.getApplicationContext();
        }

        @NonNull
        @Override
        public Result doWork() {
            Log.d(TAG, "SyncWorker > doWork()");
            uploadFailListRetransmission();
            //if(mBaseAppcation.isBackground())
            reStart();
            return Result.success();
        }
    }

    /**
     * action:初始化SyncWorker
     * <p>
     * author: wuguoxian <br/>
     * date: 20200406 <br/>
     * remark1:<p> 具体说明查看SyncWorker相关实现 </p>
     *
     * @param minute SyncWorker运行的周期,最小为15,单位:分钟
     */
    private void initSyncWorker(Context context, int minute) {
        //任务触发或运行条件
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(true)
                .build();
        //设定任务为周期运行
        minute = minute < 15 ? 15 : minute;
        PeriodicWorkRequest uploadWorkRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, minute, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag("WristbandSyncWorker")
                .build();
        //把任务加入列表
        WorkManager.getInstance(context).enqueue(uploadWorkRequest);
    }

    // =========================================      上传相关      ================================================

    /* 轨迹优化,上传,失败缓存维护相关流程说明:
            1:上传出现失败时认为网络连接异常,只缓存不重新上传
            2:上传出现成功时认为网络连接恢复,开始清理缓存,重新上传
            3:本地缓存使用MMKV实现,限制和问题暂时还不清楚

                        mergeCacheToUploadFailList()
                          :                      A
                          :                      | 更新本地缓存
                          :                      |          mUploadFailList长度小于阈值
                          : 清空mUploadFailList   |-------------------------------------->流程结束
                          :                      |
                          :                      | 更新mUploadFailList
                          V                      |
                        autoSaveAsPersistentDataUploadFailList()
                          A
                          | 上传失败
                          |
           流程开始 ---> upload() <---------------
                          |                     |
                          | 上传成功              | 重新上传
                          |                     |          mUploadFailList.szie()等于0
                          V                     |-------------------------------------->流程结束
                        uploadFailListRetransmission()
                          |                      A
                          | 更新mUploadFailList   : //清空本地缓存
                          V                      :
                        mergeCacheToUploadFailList()
    */

    /**
     * action: 后台同步字符串使用格式<br/>
     */
    private static final MediaType JSON = MediaType.parse("application/json");
    /**
     * action: 后台同步的服务器地址<br/>
     */
    private static final String SERVER_URL = "http://118.25.57.40:8109/smart-cap/wristband/receive";
    private static final String FILE_NAME_UPLOAD_FAIL_LIST = "UploadFailList";
    private static final String KEY_UPLOAD_FAIL_LIST_JSON = "UploadFailList_json";

    private MMKV mPreferDataList;
    private boolean isUploadFailListRetransmissioning = false;
    /**
     * action: 手环数据上传失败的数据集合的列表<br/>
     */
    private List<String> mUploadFailList = new ArrayList<>();

    /**
     * action: 手环数据上传服务器 [服务器参数为默认] <br/>
     *
     * @return Response 上传返回信息
     */
    private Response upload(String content) {
        Response result = null;
        try {
            RequestBody body = RequestBody.create(JSON, content);
            result = HttpReqUtil.post(SERVER_URL, body);
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (NullPointerException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        if (null == result || result.code() != 200) { //失败处理
            autoSaveAsPersistentDataUploadFailList(content);
        } else { //成功处理
            Log.i(TAG, new StringBuilder("upload > upload success > ")
                    .append("content:").append(content).toString());
            //成功上传后,尝试重新上传前面上传失败的数据
            uploadFailListRetransmission();
        }
        return result;
    }

    /**
     * action: 网络连接恢复后,重新上传曾经上传失败的手环数据 <br/>
     */
    private void uploadFailListRetransmission() {
        if (!IS_ENABLE_FAILED_REUPLOAD || isUploadFailListRetransmissioning)
            return;
        isUploadFailListRetransmissioning = true;
        Log.d(TAG, "uploadFailListRetransmission");
        String[] uploadList;
        synchronized (mUploadFailList) {
            if (0 >= mUploadFailList.size()) {
                mergeCacheToUploadFailList();//取出缓存
                if (mUploadFailList.size() <= 0) {
                    isUploadFailListRetransmissioning = false;
                    Log.d(TAG, "uploadFailListRetransmission > uploadFailList is null > cancel retransmission");
                    return;
                }
                Log.d(TAG, "uploadFailListRetransmission > local uploadFailList size :" + mUploadFailList.size());
                //缓存成功取出后的清理
                MMKV.Editor editor = mPreferDataList.edit();
                editor.clear();
                //editor.apply();
            }
            uploadList = mUploadFailList.toArray(new String[mUploadFailList.size()]);
            mUploadFailList.clear();
        }
        if (null == uploadList) {
            isUploadFailListRetransmissioning = false;
            return;
        }
        Log.d(TAG, "uploadFailListRetransmission > successful merge cache > uploadList size:" + uploadList.length);
        ThreadPoolUtils.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                for (String content : uploadList) {
                    Log.d(TAG, "uploadFailListRetransmission > retransmission ");
                    upload(content);
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                }
                isUploadFailListRetransmissioning = false;
            }
        });
    }

    /**
     * action1: 将失败数据记录到 mUploadFailList 上 <br/>
     * action2: 当上传失败次数达到 AUTO_SAVE_UPLOAD_FAIL_DATA_FREQ 就缓存到本地 <br/>
     */
    private void autoSaveAsPersistentDataUploadFailList(String content) {
        Log.w(TAG, new StringBuilder("autoSaveAsPersistentDataUploadFailList > upload fail count:")
                .append(mUploadFailList.size()).append(" > ")
                .append("content:").append(content).toString());
        if (!IS_ENABLE_FAILED_REUPLOAD)
            return;
        if (mUploadFailList.size() < NUMBER_OF_UPLOAD_FAIL_DATA_MAX) { //失败数据最多记录 NUMBER_OF_UPLOAD_FAIL_DATA_MAX 个
            mUploadFailList.add(content);
        } else {
            Log.e(TAG, "autoSaveAsPersistentDataUploadFailList > failed data cache is full, give up saving");
        }
        if (mUploadFailList.size() < AUTO_SAVE_UPLOAD_FAIL_DATA_FREQ)
            return;
        String json = "";
        synchronized (mUploadFailList) {
            mergeCacheToUploadFailList();
            json = new Gson().toJson(mUploadFailList);
            mUploadFailList.clear();
        }
        SharedPreferences.Editor editor = mPreferDataList.edit();
        editor.putString(KEY_UPLOAD_FAIL_LIST_JSON, json);
        //editor.apply();
    }

    /**
     * action: 把缓存添加到mUploadFailList,并自动去重 <br/>
     * <p>
     * remark: 务必使用 synchronized(mUploadFailList) 包裹,以保证数据更新的一致性
     */
    private void mergeCacheToUploadFailList() {
        if (null == mPreferDataList)
            mPreferDataList = MMKV.mmkvWithID(FILE_NAME_UPLOAD_FAIL_LIST);
        String oldJson = mPreferDataList.getString(KEY_UPLOAD_FAIL_LIST_JSON, "");
        if (0 >= oldJson.length()) {
            Log.w(TAG, "mergeCacheToUploadFailList > cache is null");
            return;
        }
        String[] arrayOld = null;
        if (mUploadFailList.size() > 0)
            arrayOld = mUploadFailList.toArray(new String[mUploadFailList.size()]);
        //叠加
        try {
            JSONArray jsonArray = new JSONArray(oldJson);
            int count = jsonArray.length();
            if (mUploadFailList.size() + jsonArray.length() > NUMBER_OF_UPLOAD_FAIL_DATA_MAX) { //数据长度限制,会抛弃部分数据
                count = NUMBER_OF_UPLOAD_FAIL_DATA_MAX - mUploadFailList.size();
            }
            for (int i = 0; i < count; i++) {
                mUploadFailList.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, android.util.Log.getStackTraceString(e));
            //恢复旧的list
            if (null != arrayOld)
                mUploadFailList = new ArrayList<>(Arrays.asList(arrayOld));
            else
                mUploadFailList.clear();
            return;
        }
        //去重
        LinkedHashSet<String> set = new LinkedHashSet<String>(mUploadFailList.size());
        set.addAll(mUploadFailList);
        mUploadFailList.clear();
        mUploadFailList.addAll(set);
    }
}