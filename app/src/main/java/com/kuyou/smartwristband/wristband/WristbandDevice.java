package com.kuyou.smartwristband.wristband;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;

import com.kuyou.smartwristband.R;
import com.kuyou.smartwristband.gps.GPSUtils;
import com.kuyou.smartwristband.gps.filter.TrackPoint;
import com.kuyou.smartwristband.gps.filter.TrajectoryFilter;
import com.kuyou.smartwristband.gps.filter.TrajectoryFluctuationFilter;
import com.kuyou.smartwristband.gps.filter.kalman.TrajectoryKalmanFilter;
import com.kuyou.smartwristband.utils.CommonUtil;
import com.tencent.mmkv.MMKV;
import com.zhj.bluetooth.zhjbluetoothsdk.bean.BLEDevice;
import com.zhj.bluetooth.zhjbluetoothsdk.bean.HealthHeartRate;
import com.zhj.bluetooth.zhjbluetoothsdk.bean.HealthSport;
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleCallback;
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper;
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult;
import com.zhj.bluetooth.zhjbluetoothsdk.util.SPHelper;
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * action : 用于获取记录[手环+终端]完整设备信息
 * <p>
 * author: wuguoxian <br/>
 * date: 20200126 <br/>
 * <p>
 * remark1:
 * <p>01 手环数据同步 </p>
 * <p>02 终端位置更新 </p>
 */
public class WristbandDevice extends WristbandInfo implements View.OnLongClickListener {
    private static final String TAG = "WristbandDevice";

    private ILocationChangerListener mILocationChangerListener;
    private boolean isHeartRateGetFinish = false,
            isStepGetFinish = false,
            isDeviceBaseInfoGetFinish = false,
            isPowerGetFinish = false;

    public WristbandDevice(Context context, ILocationChangerListener listener) {
        super(context);
        mILocationChangerListener = listener;
        initWristbandConfig();
        initGaoDeLocationSDK(mContext);
    }

    //初始化设备配置
    private void initWristbandConfig() {

        //手环时间设成和终端一致
        BleSdkWrapper.setDeviceData(new BleCallback() {
            @Override
            public void complete(int i, Object o) {

            }

            @Override
            public void setSuccess() {

            }
        });

        //开启心率检测
        BleSdkWrapper.setHeartTest(true, new BleCallback() {
            @Override
            public void complete(int i, Object o) {
                Log.d(TAG, "setHeartTest true> complete");
            }

            @Override
            public void setSuccess() {
                Log.d(TAG, "setHeartTest true> setSuccess");
            }
        });
    }

    /**
     * action:获取同步最新的手环数据
     * <p/>
     * author: wuguoxian
     */
    public void startSync(Context context) {
        mSyncTime = CommonUtil.formatLocalTimeByMilSecond(0, "yyyy-MM-dd HH:mm:ss");        //设备基本信息

        isDeviceBaseInfoGetFinish = false;
        BleSdkWrapper.getDeviceInfo(new BleCallback() {
            @Override
            public void complete(int i, Object o) {
                try {
                    HandlerBleDataResult result = (HandlerBleDataResult) o;
                    BLEDevice bleDevice = (BLEDevice) result.data;
                    if (null == bleDevice.mDeviceName || bleDevice.mDeviceName.length() <= 0)
                        bleDevice = SPHelper.getBindBLEDevice(mContext);
                    mDeviceName = bleDevice.mDeviceName;
                    mWristbandId = bleDevice.mDeviceAddress.replaceAll(":", "");
                    int rssi = bleDevice.mRssi;
                    if (rssi < 0)
                        mRssi = rssi;
                    Log.d(TAG, new StringBuilder("getDeviceInfo > ")
                            .append("mRssi = ").append(rssi)
                            .toString());
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                isDeviceBaseInfoGetFinish = true;
            }

            @Override
            public void setSuccess() {
            }
        });

        //电量
        isPowerGetFinish = false;
        BleSdkWrapper.getPower(new BleCallback() {
            @Override
            public void setSuccess() {
            }

            @Override
            public void complete(int i, Object o) {
                try {
                    HandlerBleDataResult result = (HandlerBleDataResult) o;
                    int power = (int) result.data;
                    if (power >= 0)
                        mPower = String.valueOf(power);
                    //isChargIng=(-1 ==power);
                    Log.d(TAG, "getPower > mPower=" + mPower);
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                isPowerGetFinish = true;
            }
        });

        //心率,血压
        isHeartRateGetFinish = false;
        BleSdkWrapper.getHeartRate(new BleCallback() {
            @Override
            public void setSuccess() {
            }

            @Override
            public void complete(int i, Object o) {
                try {
                    HandlerBleDataResult result = (HandlerBleDataResult) o;
                    HealthHeartRate heartRate = (HealthHeartRate) result.data;
                    mHeartRate = String.valueOf(heartRate.getSilentHeart());
                    mDiastolicPressure = String.valueOf(heartRate.getFz());
                    mSystolicPressure = String.valueOf(heartRate.getSs());
                    Log.d(TAG, new StringBuilder("getHeartRate > ")
                            .append("\n mHeartRate = ").append(mHeartRate)
                            .append("\n mDiastolicPressure = ").append(mDiastolicPressure)
                            .append("\n mSystolicPressure = ").append(mSystolicPressure)
                            .toString());
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                isHeartRateGetFinish = true;
            }
        });

        //步数
        isStepGetFinish = false;
        BleSdkWrapper.getCurrentStep(new BleCallback() {
            @Override
            public void setSuccess() {
            }

            @Override
            public void complete(int i, Object o) {
                try {
                    HandlerBleDataResult result = (HandlerBleDataResult) o;
                    HealthSport sport = (HealthSport) result.data;
                    if (sport.getTotalStepCount() >= 0)
                        mStepCount = String.valueOf(sport.getTotalStepCount());
                    Log.d(TAG, "getCurrentStep > mStepCount=" + mStepCount);
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                isStepGetFinish = true;
            }
        });
    }

    /**
     * action: 确认手环数据是否已经同步更新完成<br/>
     */
    public boolean isSyncFinish() {
        boolean result = isHeartRateGetFinish
                && isStepGetFinish
                && isPowerGetFinish
                && isDeviceBaseInfoGetFinish
                && isUploadReady();
        if (result) {
            autoSaveAsPersistentData();
        }
        return result;
    }

    /**
     * action: 确认手环数据是否做好上传准备<br/>
     */
    public boolean isUploadReady() {
        return null != mWristbandId && !NONE.equals(mWristbandId)
               && hasLocation();
    }

    //手环ID长按复制
    @Override
    public boolean onLongClick(View v) {
        ClipboardManager cm = (ClipboardManager) v.getContext().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", getWristbandId());
        cm.setPrimaryClip(mClipData);
        ToastUtil.showToast(v.getContext().getApplicationContext(), "已复制手环ID");
        return false;
    }

    // =========================================      高德位置SDK V2.8      ================================================

    /**
     * action: 高德位置定位频率<br/>
     */
    public static final int GAO_DE_POSITION_FREQ = 3000;
    private static final String NOTIFICATION_CHANNEL_NAME = "状态";
    private boolean isCreateChannel = false;
    private boolean isBackgroundIng = false;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private NotificationManager notificationManager = null;
    private AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null == location) {
                Log.d(TAG, "AMapLocationListener > onLocationChanged > 定位失败 > use base location ");
                return;
            }
            if (location.getErrorCode() == 0) { //定位成功,进行处理
                filter(new TrackPoint(location));
            }
            //确定是否打印log
            if (!mILocationChangerListener.onLocationChanged(location))
                return;
            printfAMapLocationInfo(location);
        }
    };

    private void printfAMapLocationInfo(AMapLocation location) {
        StringBuffer sb = new StringBuffer("AMapLocationListener > onLocationChanged > printfAMapLocationInfo :");

        //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
        if (location.getErrorCode() == 0) {
            sb.append("定位成功" + "\n");
            sb.append("定位类型: " + location.getLocationType() + "\n");
            sb.append("提供者    : " + location.getProvider() + "\n");
            sb.append("经    度    : " + location.getLongitude() + "\n");
            sb.append("纬    度    : " + location.getLatitude() + "\n");
            sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
            sb.append("高    度    : " + location.getAltitude() + "米" + "\n");
            sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
            sb.append("角    度    : " + location.getBearing() + "\n");
            sb.append("星    数    : " + location.getSatellites() + "\n");
            sb.append("国    家    : " + location.getCountry() + "\n");
            sb.append("省            : " + location.getProvince() + "\n");
            sb.append("市            : " + location.getCity() + "\n");
            sb.append("城市编码 : " + location.getCityCode() + "\n");
            sb.append("区            : " + location.getDistrict() + "\n");
            sb.append("区域 码   : " + location.getAdCode() + "\n");
            sb.append("地    址    : " + location.getAddress() + "\n");
            sb.append("地    址    : " + location.getDescription() + "\n");
            sb.append("兴趣点    : " + location.getPoiName() + "\n");
            //定位完成的时间
            sb.append("定位时间: " + CommonUtil.formatLocalTimeByMilSecond(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");

        } else { //定位失败
            sb.append("定位失败" + "\n");
            sb.append("错误码:" + location.getErrorCode() + "\n");
            sb.append("错误信息:" + location.getErrorInfo() + "\n");
            sb.append("错误描述:" + location.getLocationDetail() + "\n");
        }
        sb.append("***定位质量报告***").append("\n");
        sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启" : "关闭").append("\n");
        sb.append("* GPS状态：").append(getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
        sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
        sb.append("****************").append("\n");
        sb.append("回调时间: " + CommonUtil.formatLocalTimeByMilSecond(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");

        Log.d(TAG, sb.toString());
    }

    /**
     * 初始化定位
     * <p>
     * since: 2.8.0 <br/>
     * author wuguoxian <br/>
     * belong 高德地图SDK <br/>
     */
    private void initGaoDeLocationSDK(Context context) {
        if (null != locationClient)
            return;
        //初始化client
        locationClient = new AMapLocationClient(context);
        locationOption = getDefaultOption();
        if (null == locationClient) {
            Log.e(TAG, "initGaoDeLocationSDK init fail > locationClient is null");
            return;
        }
        if (null == locationOption) {
            Log.e(TAG, "initGaoDeLocationSDK init fail > locationOption is null");
            return;
        }

        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    /**
     * 默认的定位参数
     * <p>
     * since: 2.8.0 <br/>
     * author wuguoxian <br/>
     * belong 高德地图SDK <br/>
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(GAO_DE_POSITION_FREQ);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(false);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(true);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    /**
     * 获取GPS状态的字符串
     * <p>
     * since: 2.8.0 <br/>
     * author wuguoxian <br/>
     * belong 高德地图SDK <br/>
     *
     * @param statusCode GPS状态码
     */
    private String getGPSStatusString(int statusCode) {
        switch (statusCode) {
            case AMapLocationQualityReport.GPS_STATUS_OK:
                return "GPS状态正常";
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
                return "手机中没有GPS Provider，无法进行GPS定位";
            case AMapLocationQualityReport.GPS_STATUS_OFF:
                return "GPS关闭，建议开启GPS，提高定位质量";
            case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
                return "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
                return "没有GPS定位权限，建议开启gps定位权限";
            default:
                return "";
        }
    }

    /**
     * 开始定位
     * <p>
     * since: 2.8.0 <br/>
     * author wuguoxian <br/>
     * belong 高德地图SDK <br/>
     */
    public void startLocation() {
        if (null == locationClient) {
            locationClient = new AMapLocationClient(mContext);
        }
        if (null == locationOption) {
            locationOption = getDefaultOption();
        }
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    /**
     * 停止定位
     * <p>
     * since: 2.8.0 <br/>
     * author wuguoxian <br/>
     * belong 高德地图SDK <br/>
     */
    public void stopLocation() {
        if (isStartLocation())
            locationClient.stopLocation();
    }

    public boolean isStartLocation() {
        return locationClient.isStarted();
    }

    /**
     * action:注销位置获取
     * <p>
     * since: 2.8.0 <br/>
     * author wuguoxian <br/>
     * belong 高德地图SDK <br/>
     * date: 20200326 <br/>
     */
    public void destroyLocation() {
        if (null != locationClient) {
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    /**
     * action:开始后台定位<br/>
     * <p>
     * since: 2.8.0 <br/>
     * author wuguoxian <br/>
     * belong 高德地图SDK <br/>
     */
    @SuppressLint("NewApi")
    private Notification buildNotification(Context context) {

        Notification.Builder builder = null;
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (null == notificationManager) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = context.getPackageName();
            if (!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
                notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(context, channelId);
        } else {
            builder = new Notification.Builder(context);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(PendingIntent.getActivity(
                        context, 0, context.getPackageManager().getLaunchIntentForPackage(
                                context.getPackageName()), PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(mContext.getString(R.string.run_background))
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            return builder.getNotification();
        }
        return notification;
    }

    /**
     * action:开始后台定位<br/>
     * <p>
     * since: 2.8.0 <br/>
     * author wuguoxian <br/>
     * belong 高德地图SDK <br/>
     */
    public void onStop() {
        isBackgroundIng = true;
        if (!BleSdkWrapper.isBind()
                || !BleSdkWrapper.isConnected()) {//没绑定且连接的时候不进行后台定位
            stopLocation();
            return;
        }
        if (null != locationClient) {
            Log.d(TAG, "onStop > enableBackgroundLocation");
            locationClient.enableBackgroundLocation(2001, buildNotification(mContext));
        }
    }

    /**
     * action:停止后台定位<br/>
     * <p>
     * since: 2.8.0 <br/>
     * author wuguoxian <br/>
     * belong 高德地图SDK <br/>
     */
    public void onResume() {
        isBackgroundIng = false;
        if (null != locationClient) {
            Log.d(TAG, "onResume > disableBackgroundLocation");
            locationClient.disableBackgroundLocation(true);
        }
        startLocation();
    }

    public static interface ILocationChangerListener {
        /**
         * action: 位置变化,只用于终端显示或调试<br/>
         *
         * @return true 打印详细的 Location相关log
         */
        public boolean onLocationChanged(final Location location);
    }

    // =========================================      单次同步数据缓存持久化      ================================================

    private static final String KEY_FILE_NAME_WRISTBAND_DATA_UI_CACHE = "WristbandPersistentInfo",
            KEY_SYSTOLIC_PRESSURE = "value.SystolicPressure",
            KEY_DIASTOLIC_PRESSURE = "value.DiastolicPressure",
            KEY_STEPCOUNT = "value.StepCount",
            KEY_HEARTRATE = "value.HeartRate";
    private static final int AUTO_SAVE_FREQ = 15;
    private static int sAutoSaveIndexWristband = 0;
    private MMKV mSPWristbandDataUICache;

    private boolean initSharedPreferencesWristbandDataUICache(Context context) {
        if (null != mSPWristbandDataUICache)
            return false;
        mSPWristbandDataUICache = MMKV.mmkvWithID(KEY_FILE_NAME_WRISTBAND_DATA_UI_CACHE);
        return true;
    }

    /**
     * action: 缓存最新的单次同步数据到本地,用于断开或APP重启后的数据显示 <br/>
     * <p>
     * remark:缓存的是单次数据
     */
    private void saveAsPersistentData() {
        Log.d(TAG, "saveAsPersistentData");
        initSharedPreferencesWristbandDataUICache(mContext);
        MMKV.Editor editor = mSPWristbandDataUICache.edit();
        if (!getStepCountDef().equals(getStepCount()))
            editor.putString(KEY_STEPCOUNT, getStepCount());
        if (!getHeartRateDef().equals(getHeartRate()))
            editor.putString(KEY_HEARTRATE, getHeartRate());
        if (!getSystolicPressureDef().equals(getSystolicPressure()))
            editor.putString(KEY_SYSTOLIC_PRESSURE, getSystolicPressure());
        if (!getDiastolicPressureDef().equals(getDiastolicPressure()))
            editor.putString(KEY_DIASTOLIC_PRESSURE, getDiastolicPressure());
        //editor.apply();
    }

    /**
     * action: 加载本地缓存数据 <br/>
     * <p>
     * remark:加载的是单次数据
     */
    public void restorePersistenceData() {
        initSharedPreferencesWristbandDataUICache(mContext);
        MMKV.Editor editor = mSPWristbandDataUICache.edit();

        setStepCount(mSPWristbandDataUICache.getString(KEY_STEPCOUNT, getStepCount()));
        setHeartRate(mSPWristbandDataUICache.getString(KEY_HEARTRATE, getHeartRate()));
        setSystolicPressure(mSPWristbandDataUICache.getString(KEY_SYSTOLIC_PRESSURE, getSystolicPressure()));
        setDiastolicPressure(mSPWristbandDataUICache.getString(KEY_DIASTOLIC_PRESSURE, getDiastolicPressure()));
        //editor.apply();
    }

    /**
     * action: 每同步次数达到 AUTO_SAVE_FREQ 就缓存到本地 <br/>
     */
    private void autoSaveAsPersistentData() {
        sAutoSaveIndexWristband += 1;
        if (sAutoSaveIndexWristband < AUTO_SAVE_FREQ)
            return;
        saveAsPersistentData();
        sAutoSaveIndexWristband = 0;
    }

    // =========================================      位置轨迹处理      ================================================
    private TrajectoryFilter mFluctuationFilter, mTrajectoryKalmanFilter;

    @Override
    public void setLocation(Location location) {
        super.setLocation(location);
    }

    /**
     * action: 过滤轨迹 <br/>
     * <p>
     */
    private void filter(TrackPoint point) {
        if (null == mTrajectoryKalmanFilter) {
            mTrajectoryKalmanFilter = new TrajectoryKalmanFilter(mContext.getApplicationContext(),
            new TrajectoryFilter.OnDataFilterListener() {
                @Override
                public void onDataAfterFilter(TrackPoint point) {
                    point.printfLocationInfo(TAG+">mTrajectoryKalmanFilter>onDataAfterFilter");
                    mFluctuationFilter.filter(point);
                }
            });
            mFluctuationFilter = new TrajectoryFluctuationFilter(new TrajectoryFilter.OnDataFilterListener() {
                @Override
                public void onDataAfterFilter(TrackPoint point) {
                    point.printfLocationInfo(TAG+">SpeedFilter>onDataAfterFilter");

                    //将轨迹信息直接合并到当前手环信息里面
                    applyTrackPoint(point);
                }
            });
        }
        if(!hasLocation()){ //第一次定位时，先初始化位置
            applyTrackPoint(point);
        }
        mTrajectoryKalmanFilter.filter(point);
    }

    @Override
    public void applyTrackPoint(TrackPoint point) {
        //坐标转化为火星坐标
        double[] ll = GPSUtils.gcj02_To_Gps84(point.getLatitude(), point.getLongitude());
        point.setLatitude(ll[0]);
        point.setLongitude(ll[1]);

        super.applyTrackPoint(point);

        printfLocationInfo(TAG);
    }
}
