package com.kuyou.smartwristband.gps.filter;

import android.location.Location;
import android.util.Log;

/**
 * action:轨迹信息节点
 * <p>
 * author: wuguoxian <br/>
 * date: 20200411 <br/>
 * remark1:<p> Location的基础信息加部分算法需要自定义信息 </p>
 *
 *
 */
public class TrackPoint {
    private static final String TAG = "TrackPoint";

    protected final static double POINT_SPEED_MAX = 310 / 3.6f; // 最大速度 310km/h ,不考虑开飞机等极端情况

    public static final double NONE = -1;
    protected double mLongitude = NONE; //经度
    protected double mLatitude = NONE; //纬度
    protected double mAltitude = NONE; //海拔高度
    protected float mAccuracy = -1;//精度
    protected float mSpeed = -1f; //速度
    protected float mBearing = -1; //角度
    protected long mTime = -1; //时间
    protected long mElapsedRealtimeNanos = -1;
    protected boolean isFromMockProvider = false;

    //自定义参数
    protected float mBearingSpeed = -1; //角速度
    protected double mAngle = NONE; //点间夹角

    public TrackPoint() {
    }

    public TrackPoint(TrackPoint point) {
        applyTrackPoint(point);
    }

    public TrackPoint(Location location) {
        setLocation(location);
    }

    /**
     * action:复制轨迹点信息
     * <p/>
     */
    public void applyTrackPoint(TrackPoint point) {
        mLongitude = point.getLongitude();
        mLatitude = point.getLatitude();
        mAltitude = point.getAltitude();
        mAccuracy = point.getAccuracy();
        mSpeed = point.getSpeed();
        mTime = point.getTime();
        mAngle = point.getAngle();
        mBearing = point.getBearing();
        mBearingSpeed = point.getBearingSpeed();
    }

    /**
     * action:更新位置信息
     * <p/>
     */
    public void setLocation(Location location) {
        if (null == location) {
            Log.e(TAG, "setLocation fail > location is null");
            return;
        }
        mLongitude = location.getLongitude();
        mLatitude = location.getLatitude();
        mAltitude = location.getAltitude();
        mAccuracy = location.getAccuracy();
        mSpeed = location.getSpeed();
        mTime = location.getTime();
        mBearing = location.getBearing();
    }

    public void setLongitude(boolean val) {
        isFromMockProvider = val;
    }

    public boolean isFromMockProvider() {
        return isFromMockProvider;
    }

    public void setLongitude(double val) {
        mLongitude = val;
    }

    public void setLatitude(double val) {
        mLatitude = val;
    }

    public void setAltitude(double val) {
        mAltitude = val;
    }

    public void setAccuracy(float val) {
        mAccuracy = val;
    }

    public void setBearing(float val) {
        mBearing = val;
    }

    public float getBearing() {
        return mBearing;
    }

    public void setBearingSpeed(float val) {
        mBearingSpeed = val;
    }

    public float getBearingSpeed() {
        return mBearingSpeed;
    }

    public void setAngle(double val) {
        mAngle = val;
    }

    public void setSpeed(float val) {
        mSpeed = val;
    }

    public void setTime(long val) {
        mTime = val;
    }

    public void setElapsedRealtimeNanos(long val) {
        mElapsedRealtimeNanos = val;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getAltitude() {
        return mAltitude;
    }

    public float getAccuracy() {
        return mAccuracy;
    }

    public float getSpeed() {
        return mSpeed;
    }

    /**
     * action: 获取位置的GPS的UTC时间戳
     * <p>
     * author: wuguoxian <br/>
     * date: 20200422 <br/>
     *
     * @return long 时间戳
     */
    public long getTime() {
        return mTime;
    }

    public long getElapsedRealtimeNanos() {
        return mElapsedRealtimeNanos;
    }

    public double getAngle() {
        return mAngle;
    }

    public boolean hasAltitude() {
        return mAltitude != NONE;
    }

    public boolean hasSpeed() {
        return mSpeed != NONE;
    }

    public boolean hasBearing() {
        return mBearing != NONE;
    }

    public boolean hasBearingSpeed() {
        return mBearingSpeed != NONE;
    }

    public boolean hasLocation(){
        return NONE!=mLatitude&&NONE!=mLongitude;
    }

    public boolean isEqualsLocation(TrackPoint point){
         return getLongitude()==point.getLongitude()
                 &&getLatitude()==point.getLatitude();
    }

    public boolean isSpeeding(){
        if(hasSpeed())
            return getSpeed()>POINT_SPEED_MAX;
        return false;
    }

    public void printfLocationInfo(String TAG){
        Log.d(TAG, new StringBuilder("printfLocationInfo\n======================================\n")
                .append("[Latitude ]: ").append(getLatitude())
                .append(" [Longitude]: ").append(getLongitude())
                .append(" [Altitude]: ").append(getAltitude())
                .append("\n======================================")
                .toString());
    }
}
