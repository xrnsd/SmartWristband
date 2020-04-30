package com.kuyou.smartwristband.wristband;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.kuyou.smartwristband.gps.filter.TrackPoint;

/**
 * action : 用于获取[手环]基本设备信息
 * <p>
 * author: wuguoxian <br/>
 * date: 20200126 <br/>
 * <p>
 * remark1:
 * <p>01 手环数据记录[单次] </p>
 */
public class WristbandInfo extends TrackPoint {
    private static final String TAG = "WristbandInfo";

    public static final String NONE = "-1";
    protected Context mContext;

    //手环基本信息
    protected int mRssi = 0; //手环信号强度
    protected String mDeviceName = NONE; //手环设备名
    protected String mWristbandId = NONE; //手环MAC地址
    protected String mPower = NONE; //手环电量
    //手环扩展信息
    protected String mSyncTime = NONE; //信息刷新时间
    protected String mHeartRate = NONE; //心率
    protected String mDiastolicPressure = NONE; //舒张压
    protected String mSystolicPressure = NONE; //收缩压
    protected String mStepCount = NONE; //步数

    public WristbandInfo(Context context) {
        mContext = context;
        mSyncTime = getSyncTimeDef();
        mHeartRate = getHeartRateDef();
        mStepCount = getStepCountDef();
        mDiastolicPressure = getDiastolicPressureDef();
        mSystolicPressure = getDiastolicPressureDef();
        mPower = getPowerDef();
    }

    public WristbandInfo(WristbandInfo device) {
        applyTrackPoint(device);

        mAngle = device.getAngle();
        mBearing = device.getBearing();
        mBearingSpeed = device.getBearingSpeed();

        mHeartRate = device.getHeartRate();
        mDiastolicPressure = device.getDiastolicPressure();
        mSystolicPressure = device.getSystolicPressure();
        mStepCount = device.getStepCount();
        mWristbandId = device.getWristbandId();
        mSyncTime = device.getSyncTime();
    }

    /**
     * action: 手环数据同步更新时间<br/>
     */
    public String getSyncTime() {
        return mSyncTime;
    }

    /**
     * action: 手环数据同步更新时间默认值<br/>
     */
    public String getSyncTimeDef() {
        return "-----";
    }

    public String getDeviceName() {
        if (mDeviceName.equals(NONE))
            return getDeviceNameDef();
        return mDeviceName;
    }

    public String getWristbandId() {
        return mWristbandId;
    }

    public String getHeartRate() {
        return mHeartRate;
    }

    protected void setHeartRate(String val) {
        mHeartRate = val;
    }

    public String getStepCount() {
        return mStepCount;
    }

    protected void setStepCount(String val) {
        mStepCount = val;
    }

    public String getPower() {
        if (NONE.equals(mPower) || mPower.equals(getPowerDef()))
            return "0";
        return mPower;
    }

    @Override
    public void applyTrackPoint(TrackPoint point) {
        super.applyTrackPoint(point);
    }

    @Override
    public void setLocation(Location location) {
        super.setLocation(location);
    }

    public int getRssi() {
        return mRssi;
    }

    public String getDiastolicPressure() {
        return mDiastolicPressure;
    }

    protected void setDiastolicPressure(String val) {
        mDiastolicPressure = val;
    }

    public String getSystolicPressure() {
        return mSystolicPressure;
    }

    protected void setSystolicPressure(String val) {
        mSystolicPressure = val;
    }

    public String getHeartRateDef() {
        return "-";
    }

    public String getDeviceNameDef() {
        return "-";
    }

    public String getStepCountDef() {
        return "-";
    }

    public String getPowerDef() {
        return "-";
    }

    public String getDiastolicPressureDef() {
        return "-";
    }

    public String getSystolicPressureDef() {
        return "-";
    }

    /**
     * action:返回按JSON格式封装的手环数据和手持终端数据
     * <p/>
     * author: wuguoxian
     */
    public String getJsonMsg() {
        String msg = new StringBuilder()
                .append("{\"lon\":\"").append(mLongitude)
                .append("\",\"lat\":\"").append(mLatitude)
                .append("\",\"alt\":\"").append(mAltitude)
                .append("\",\"heartbeat\":\"").append(mHeartRate)
                .append("\",\"diastolic\":\"").append(mDiastolicPressure)
                .append("\",\"systolic\":\"").append(mSystolicPressure)
                .append("\",\"steps\":\"").append(mStepCount)
                .append("\",\"deviceId\":\"").append(mWristbandId)
                .append("\",\"createtime\":\"").append(mSyncTime)
                .append("\"}")
                .toString();
        Log.d(TAG, "getJsonMsg>msg=" + msg);
        return msg;
    }

    public WristbandInfo clone(WristbandInfo info) {
        mLongitude = info.getLongitude();
        mLatitude = info.getLatitude();
        mAltitude = info.getAltitude();
        mAccuracy = info.getAccuracy();
        mHeartRate = info.getHeartRate();
        mDiastolicPressure = info.getDiastolicPressure();
        mSystolicPressure = info.getSystolicPressure();
        mStepCount = info.getStepCount();
        mWristbandId = info.getWristbandId();
        mTime = info.getTime();
        return this;
    }
}
