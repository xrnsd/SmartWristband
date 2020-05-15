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

    /**
     * action:过滤速度<br/>
     */
    public static final int POLICY_FILTER_SPEED = (1 << 0);
    /**
     * action:过滤角速度<br/>
     */
    public static final int POLICY_FILTER_BEARING_SPEED = (1 << 1);
    /**
     * action:过滤海拔<br/>
     */
    public static final int POLICY_FILTER_ALTITUDE = (1 << 2);

    private final int LIST_LENGTH_FLAG = 20;
    private List<TrackPoint> mTrackPointList = new ArrayList<>();
    private List<TrackPoint> mTrackPointFailList = new ArrayList<>();
    private OnDataFilterControl mOnDataFilterControl;

    /**
     * action: 轨迹过滤器[速度/角速度/海拔]
     * <p>
     * author: wuguoxian <br/>
     * date: 20200422 <br/>
     * remark: 过滤器默认运行于独立线程
     */
    public TrajectoryFluctuationFilter(OnDataFilterControl listener) {
        super(listener);
        mOnDataFilterControl = listener;
        enableIndependentThread();
    }

    @Override
    protected synchronized void filterByThread(TrackPoint point) {
        point = traceFilterBySpeed(point);
        if (null != point) {
            point.printfLocationInfo(TAG);
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
        if (null == point) {
            Log.w(TAG, "轨迹点无效");
            return null;
        }
        if (point.isSpeeding() && isEnableFilterByPolicy(POLICY_FILTER_SPEED)) {
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
                //TrajectoryFilter的线程的相关实现里面对连续的两个轨迹点已经做了位置重复处理
                //下面是对整个轨迹做位置重复处理
                for (TrackPoint pointOld : mTrackPointList) {
                    if (point.isEqualsLocation(pointOld)) {
                        point.printfLocationInfo(TAG + "：轨迹点无效>位置重复");
                        return null;
                    }
                }
            }
        }

        mTrackPointList.add(point);
        Log.d(TAG, "traceFilterBySpeed> pList.size()=" + mTrackPointList.size());
        if (mTrackPointList.size() < LIST_LENGTH_FLAG) {
            Log.d(TAG, new StringBuilder()
                    .append("pList size is Less than : ").append(LIST_LENGTH_FLAG)
                    .toString());
            return point;
        }
        //mTrackPointList 最大长度保持小于 LIST_LENGTH_FLAG*3
        if (mTrackPointList.size() > LIST_LENGTH_FLAG * 3) {
            Log.d(TAG, "traceFilterBySpeed> trackPoint data reduction ");
            for (int index = 0; index < LIST_LENGTH_FLAG; index++) {
                mTrackPointList.remove(0);
            }
        }

        //计算轨迹点的信息
        double lengthAB = 0f;
        TrackPoint pointA = null, pointB = null;
        for (int index = 0, size = mTrackPointList.size(); index < size - 1; index += 1) {
            pointA = mTrackPointList.get(index);//第一个点
            pointB = mTrackPointList.get(index + 1);

            //计算角速度
            if (isEnableFilterByPolicy(POLICY_FILTER_BEARING_SPEED) && !pointB.hasBearingSpeed() && pointB.hasBearing()) {
                float bearingSpeed = (pointB.getBearing() - pointA.getBearing()) / (pointB.getTime() - pointA.getTime()) * 1000f;
                pointB.setBearingSpeed(bearingSpeed);
            }

            //计算速度
            if (isEnableFilterByPolicy(POLICY_FILTER_SPEED) && !pointB.hasSpeed()) {
                lengthAB = GPSUtils.getDistance(
                        pointA.getLatitude(), pointA.getLongitude(),
                        pointB.getLatitude(), pointB.getLongitude());
                pointB.setSpeed((float) (lengthAB / ((pointB.getTime() - pointA.getTime()) / 1000f)));
            }
        }

        //轨迹点速度偏离判断
        if (isEnableFilterByPolicy(POLICY_FILTER_SPEED)) {
            double[] resultSpeed = varianceSpeed(mTrackPointList);
            final double speedAverageValue = resultSpeed[0], speedVariance = resultSpeed[1];
            double tempVal = point.getSpeed() - speedAverageValue;
            if ((point.getSpeed() > speedAverageValue
                    && tempVal * tempVal / 3f > speedVariance)) {
                mTrackPointList.remove(point);
                TrackPoint pNew = resetAbnormallyEffectiveTrajectory();

                Log.d(TAG, new StringBuilder("轨迹点无效>速度:").append(point.getSpeed())
                        .append("\nspeedAverageValue = ").append(speedAverageValue)
                        .append("\nspeedVariance = ").append(speedVariance)
                        .append("\n--------------------------------").toString());
                for (TrackPoint p : mTrackPointList) {
                    Log.d(TAG, "TrackPoint.getSpeed=" + p.getSpeed());
                }
                return pNew;
            }
        }

        //轨迹点角速度偏离判断
        if (isEnableFilterByPolicy(POLICY_FILTER_BEARING_SPEED)) {
            double[] resultBearingSpeed = varianceBearingSpeed(mTrackPointList);
            final double bearingSpeedAverageValue = resultBearingSpeed[0], bearingSpeedVariance = resultBearingSpeed[1];
            double tempVal = point.getBearingSpeed() - bearingSpeedAverageValue;
            if ((point.getBearingSpeed() > bearingSpeedAverageValue
                    && tempVal * tempVal / 3f > bearingSpeedVariance)) {
                mTrackPointList.remove(point);
                mTrackPointFailList.add(new TrackPoint(point));
                TrackPoint pNew = resetAbnormallyEffectiveTrajectory();

                Log.d(TAG, new StringBuilder("轨迹点无效>角速度:").append(point.getBearingSpeed())
                        .append("\nbearingSpeedAverageValue = ").append(bearingSpeedAverageValue)
                        .append("\nbearingSpeedVariance = ").append(bearingSpeedVariance)
                        .append("\nmTrackPointList.size() = ").append(mTrackPointList.size())
                        .append("\n--------------------------------").toString());
                for (int index = 0, size = mTrackPointList.size(); index < size; index++) {
                    Log.d(TAG, "TrackPoint.getBearingSpeed=" + mTrackPointList.get(index).getBearingSpeed());
                }
//                for (TrackPoint p : mTrackPointList) {
//                    Log.d(TAG, "TrackPoint.getBearingSpeed=" + p.getBearingSpeed());
//                }
                return pNew;
            }
        }

        //海拔高度偏离,修正后使用前一个有效点的海拔高度
        if (isEnableFilterByPolicy(POLICY_FILTER_ALTITUDE)) {
            double[] resultAltitude = varianceAltitude(mTrackPointList);
            final double altitudeAverageValue = resultAltitude[0], altitudeVariance = resultAltitude[1];
            double tempVal = point.getAltitude() - altitudeAverageValue;
            if ((point.getAltitude() > altitudeAverageValue
                    && tempVal * tempVal / 3f > altitudeVariance)) {
                double altitudeOld = point.getAltitude();
                mTrackPointFailList.add(new TrackPoint(point));

                Log.d(TAG, new StringBuilder("轨迹点海拔无效:").append(altitudeOld)
                        .append(" 已修正为： ").append(point.getAltitude())
                        .append("\naltitudeAverageValue = ").append(altitudeAverageValue)
                        .append("\naltitudeVariance = ").append(altitudeVariance)
                        .append("\n--------------------------------").toString());
                for (TrackPoint p : mTrackPointList) {
                    Log.d(TAG, "TrackPoint.getAltitude=" + p.getAltitude());
                }

                TrackPoint pNew = resetAbnormallyEffectiveTrajectory();
                if (null != pNew) {
                    point = pNew;
                } else {
                    point.setAltitude(mTrackPointList.get(mTrackPointList.size() - 2).getAltitude());
                }
                return point;
            }
        }

        if (null != mTrackPointFailList
                && mTrackPointFailList.size() > 0) {
            mTrackPointFailList.clear();
        }
        return point;
    }

    //算法校验连续失败次数大于阈值时，怀疑原始轨迹[mTrackPointList]已无效，进行强制更新
    //返回轨迹点用于更新手环中的位置信息
    private TrackPoint resetAbnormallyEffectiveTrajectory() {
        if (null == mTrackPointFailList
                || mTrackPointFailList.size() < LIST_LENGTH_FLAG) {
            return null;
        }
        Log.w(TAG, "resetAbnormallyEffectiveTrajectory > abnormal effective trajectory, original trajectory cleaning");
        mTrackPointList.clear();
        mTrackPointList.addAll(mTrackPointFailList);
        mTrackPointFailList.clear();
        return mTrackPointList.get(mTrackPointList.size() - 1);
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

    private boolean isEnableFilterByPolicy(final int policyFlag) {
        if (null != mOnDataFilterControl)
            return (mOnDataFilterControl.getDataFilterPolicy() & policyFlag) != 0;
        return true;
    }

    public static interface OnDataFilterControl extends OnDataFilterListener {
        public void onDataAfterFilter(TrackPoint point);

        /**
         * action:轨迹过滤的要素配置
         * <p>
         * author: wuguoxian <br/>
         * date: 20200506 <br/>
         * remark：<br/>
         * int policy = 0;<br/>
         * policy |= TrajectoryFluctuationFilter.POLICY_FILTER_SPEED;<br/>
         * policy |= TrajectoryFluctuationFilter.POLICY_FILTER_BEARING_SPEED;<br/>
         * policy |= TrajectoryFluctuationFilter.POLICY_FILTER_ALTITUDE;
         * return policy;
         *
         * @return policy :要素配置
         */
        public int getDataFilterPolicy();
    }
}
