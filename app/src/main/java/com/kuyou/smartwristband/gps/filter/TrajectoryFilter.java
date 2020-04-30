package com.kuyou.smartwristband.gps.filter;

import android.util.Log;

/**
 * action : 轨迹过滤器[]
 * <p>抽象
 * author: wuguoxian <br/>
 * date: 20200421 <br/>
 * <p>
 */
public abstract class TrajectoryFilter {

    protected final String TAG = this.getClass().getSimpleName();

    protected OnDataFilterListener mOnDataFilterListener;

    public TrajectoryFilter(OnDataFilterListener listener) {
        mOnDataFilterListener = listener;
    }

    public void filter(TrackPoint point) {

    }

    protected void onDataAfterFilter(TrackPoint point) {
        if (null == point) {
            Log.e(TAG, "onDataAfterFilter > list is none ");
            return;
        }
        if (null != mOnDataFilterListener) {
            Log.d(TAG, "onDataAfterFilter");
            mOnDataFilterListener.onDataAfterFilter(point);
        } else
            Log.e(TAG, "onDataAfterFilter > mOnDataFilterListener is null");
    }

    public static interface OnDataFilterListener {
        public void onDataAfterFilter(TrackPoint point);
    }

}
