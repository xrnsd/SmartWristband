package com.kuyou.smartwristband.gps.filter;

import android.util.Log;

import com.kuyou.smartwristband.gps.GPSUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * action : 波动过滤器[速度/角速度/海拔]
 * <p>
 * author: wuguoxian <br/>
 * date: 20200421 <br/>
 * <p>
 */
public class TrajectoryFluctuationFilter extends TrajectoryFilter {

    private static final String TAG = "TrajectoryFluctuationFilter";

    private final int LIST_LENGTH_FLAG = 10;
    private List<TrackPoint> mTrackPointList = new ArrayList<>();

    /**
     * action: 轨迹过滤器[速度]
     * <p>
     * author: wuguoxian <br/>
     * date: 20200422 <br/>
     */
    public TrajectoryFluctuationFilter(OnDataFilterListener listener) {
        super(listener);
    }

    @Override
    public void filter(TrackPoint point) {
        point = traceFilterBySpeed(point);
        if (null != point) {
            onDataAfterFilter(point);
        }
    }

    /*
    点i的速度vi大于 MAX_SPEED 该点是一个漂移点

    计算处理单元序列中每个点的速度vi
    对速度序列计算出它的均值μ以及方差σ.
    对每个点的速度进行判断，若 (vi-μ)2＞3σ，并且vi＞μ，则该点是一个漂移点。
    A--->B
    * */
    private TrackPoint traceFilterBySpeed(TrackPoint point) {
        if (null==point) {
            Log.w(TAG, "轨迹点无效");
            return null;
        }
        if (point.isSpeeding()) {
            Log.w(TAG, "轨迹点无效>已超速");
            return null;
        }
        if (null == mTrackPointList)
            mTrackPointList = new ArrayList<>();
        //避免位置重复,轨迹集合超过30分钟后失效
        if (mTrackPointList.size() >= 1) {
            if (point.getTime() - mTrackPointList.get(mTrackPointList.size() - 1).getTime() > 1000 * 60 * 30) {
                mTrackPointList.clear();
            } else {
                for (TrackPoint pointOld : mTrackPointList) {
                    if (point.isEqualsLocation(pointOld)) {
                        Log.w(TAG, "轨迹点无效>位置重复");
                        return null;
                    }
                }
            }
        }

        mTrackPointList.add(point);
        if (mTrackPointList.size() < LIST_LENGTH_FLAG) {
            Log.d(TAG, "traceFilterBySpeed> pList.size()=" + mTrackPointList.size());
            Log.d(TAG, "traceFilterBySpeed> pList size is Less than :" + LIST_LENGTH_FLAG);
            return null;
        }
        //mTrackPointList 最大长度保持小于 LIST_LENGTH_FLAG*3
        if (mTrackPointList.size() > LIST_LENGTH_FLAG * 4) {
            for (int index = 0; index < LIST_LENGTH_FLAG; index++) {
                mTrackPointList.remove(0);
            }
        }

        Log.d(TAG, "traceFilterBySpeed> pList.size()=" + mTrackPointList.size());

        //计算轨迹点的信息
        double lengthAB = 0f;
        TrackPoint pointA = null, pointB = null;
        for (int index = 0, size = mTrackPointList.size(); index < size - 1; index += 1) {
            pointA = mTrackPointList.get(index);//第一个点
            pointB = mTrackPointList.get(index + 1);

            //计算角速度
            if (!pointB.hasBearingSpeed() && pointB.hasBearing()) {
                float bearingSpeed = (pointB.getBearing() - pointA.getBearing()) / (pointB.getTime() - pointA.getTime()) * 1000f;
                pointB.setBearingSpeed(bearingSpeed);
                //Log.d(TAG, "pointB.get Angle Speed()=" + bearingSpeed);
            }

            //计算速度
            if (!pointB.hasSpeed()) {
                lengthAB = GPSUtils.getDistance(
                        pointA.getLatitude(), pointA.getLongitude(),
                        pointB.getLatitude(), pointB.getLongitude());
                pointB.setSpeed((float) (lengthAB / ((pointB.getTime() - pointA.getTime()) / 1000f)));
            }
        }

        //轨迹点速度偏离判断
        double[] resultSpeed = varianceSpeed(mTrackPointList);
        final double speedAverageValue = resultSpeed[0], speedVariance = resultSpeed[1];
        double tempVal = point.getSpeed() - speedAverageValue;
        Log.d(TAG, "================================");
        Log.d(TAG, "speedAverageValue=" + speedAverageValue);
        Log.d(TAG, "speedVariance=" + speedVariance);
        if ((point.getSpeed() > speedAverageValue
                && tempVal * tempVal / 3f > speedVariance)) {
            Log.w(TAG, "轨迹点无效>速度:" + point.getSpeed());
            mTrackPointList.remove(point);
            return null;
        }

        //轨迹点角速度偏离判断
        double[] resultBearingSpeed = varianceBearingSpeed(mTrackPointList);
        final double bearingSpeedAverageValue = resultBearingSpeed[0], bearingSpeedVariance = resultSpeed[1];
        tempVal = point.getBearingSpeed() - bearingSpeedAverageValue;
        if ((point.getBearingSpeed() > bearingSpeedAverageValue
                && tempVal * tempVal / 3f > bearingSpeedVariance)) {
            Log.w(TAG, "轨迹点无效>角速度:" + point.getBearingSpeed());
            mTrackPointList.remove(point);
            return null;
        }

        //海拔高度偏离,修正后使用前一个有效点的海拔高度
        double[] resultAltitude = varianceAltitude(mTrackPointList);
        final double altitudeAverageValue = resultAltitude[0], altitudeVariance = resultAltitude[1];
        tempVal = point.getAltitude() - altitudeAverageValue;
        if ((point.getAltitude() > altitudeAverageValue
                && tempVal * tempVal / 3f > altitudeVariance)) {
            double altitudeOld=point.getAltitude();
            point.setAltitude(mTrackPointList.get(mTrackPointList.size()-2).getAltitude());
            Log.w(TAG, new StringBuilder("轨迹点海拔无效:")
                    .append(altitudeOld).append(" 已修正为： ").append(point.getAltitude())
                    .toString());
        }
        return point;
    }

    /**
     * action:轨迹速度的平均值和方差
     * <p>
     * author: wuguoxian <br/>
     * date: 20200427 <br/>
     *
     * @param list：轨迹
     * @return double[0]：平均值 ， double[1]：方差s^2=[(x1-x)^2 +...(xn-x)^2]/n
     */
    public static double[] varianceSpeed(List<TrackPoint> list) {
        int m = list.size();
        double sum = 0;
        double[] result = new double[2];
        for (int i = 0; i < m; i++) {//求和
            sum += list.get(i).getSpeed();
        }
        double dAve = sum / m;//求平均值
        result[0] = dAve;
        double dVar = 0;
        for (int i = 0; i < m; i++) {//求方差
            dVar += (list.get(i).getSpeed() - dAve) * (list.get(i).getSpeed() - dAve);
        }
        result[1] = dVar / m;
        return result;
    }

     /**
     * action:轨迹角速度的平均值和方差
     * <p>
     * author: wuguoxian <br/>
     * date: 20200427 <br/>
     *
     * @param list：轨迹
     * @return double[0]：平均值 ， double[1]：方差s^2=[(x1-x)^2 +...(xn-x)^2]/n
     */
    public static double[] varianceBearingSpeed(List<TrackPoint> list) {
        int m = list.size();
        double sum = 0;
        double[] result = new double[2];
        for (int i = 0; i < m; i++) {//求和
            sum += list.get(i).getBearingSpeed();
        }
        double dAve = sum / m;//求平均值
        result[0] = dAve;
        double dVar = 0;
        for (int i = 0; i < m; i++) {//求方差
            dVar += (list.get(i).getBearingSpeed() - dAve) * (list.get(i).getBearingSpeed() - dAve);
        }
        result[1] = dVar / m;
        return result;
    }

    /**
     * action:轨迹海拔的平均值和方差
     * <p>
     * author: wuguoxian <br/>
     * date: 20200427 <br/>
     *
     * @param list：轨迹
     * @return double[0]：平均值 ， double[1]：方差s^2=[(x1-x)^2 +...(xn-x)^2]/n
     */
    public static double[] varianceAltitude(List<TrackPoint> list) {
        int m = list.size();
        double sum = 0;
        double[] result = new double[2];
        for (int i = 0; i < m; i++) {//求和
            sum += list.get(i).getAltitude();
        }
        double dAve = sum / m;//求平均值
        result[0] = dAve;
        double dVar = 0;
        for (int i = 0; i < m; i++) {//求方差
            dVar += (list.get(i).getAltitude() - dAve) * (list.get(i).getAltitude() - dAve);
        }
        result[1] = dVar / m;
        return result;
    }
}
