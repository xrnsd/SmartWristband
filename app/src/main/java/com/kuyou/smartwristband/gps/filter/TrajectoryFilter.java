package com.kuyou.smartwristband.gps.filter;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

/**
 * <p>
 * action : 轨迹过滤器[抽象]<br/>
 * author: wuguoxian <br/>
 * date: 20200421 <br/>
 * remark:<br/>
 * &nbsp 01 过滤器默认运行于当前线程 <br/>
 * &nbsp 02 过滤器需要运行于独立线程，请进行如下配置 <br/>
 * &nbsp&nbsp&nbsp 02.1 调用方法[enableIndependentThread]初始化线程配置 <br/>
 * &nbsp&nbsp&nbsp 02.２ 在方法[filterByThread]执行过滤相关算法 <br/>
 * &nbsp&nbsp&nbsp 02.３ 在方法[filter]中放入原始轨迹点，使用接口[OnDataFilterListener]获取过滤后的轨迹点 <br/>
 * </p>
 */
public abstract class TrajectoryFilter {

    protected final String TAG = this.getClass().getSimpleName();
    protected OnDataFilterListener mOnDataFilterListener;
    protected boolean isStopFilter = false;

    public TrajectoryFilter(OnDataFilterListener listener) {
        mOnDataFilterListener = listener;
    }

    public void filter(TrackPoint point) {
        if (null == point)
            return;
        if (null != mHandlerFilter && null == mTrackPoint) {
            //Log.d(TAG, "开启原始轨迹数据更新循环－－－－－－－－－－－－－");
            mHandlerFilter.post(mRunnableFilter);
        }
        mTrackPoint = point;
    }

    public void stop() {
        isStopFilter = true;
        if (null != mHandlerFilter)
            mHandlerFilter.removeCallbacks(mRunnableFilter);
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

        /**
         * action:轨迹过滤后的点
         * <p>
         * author: wuguoxian <br/>
         * date: 20200421 <br/>
         */
        public void onDataAfterFilter(TrackPoint point);
    }

    //===================================   独立线程相关   ===================================

    protected Runnable mRunnableFilter;
    protected Handler mHandlerFilter;
    protected HandlerThread mHandlerThreadFilter;
    protected TrackPoint mTrackPoint;

    /**
     * action:启用独立线程
     * <p>
     * author: wuguoxian <br/>
     * date: 20200507 <br/>
     */
    protected void enableIndependentThread() {
        if (null != mHandlerThreadFilter)
            return;
        mHandlerThreadFilter = new HandlerThread(TAG + ".filter.thread");
        mHandlerThreadFilter.start();
        mHandlerFilter = new Handler(mHandlerThreadFilter.getLooper());
        mRunnableFilter = new Runnable() {
            @Override
            public void run() {
                TrackPoint point = new TrackPoint(mTrackPoint), point2 = new TrackPoint(mTrackPoint);
                while (!isStopFilter) {
                    filterByThread(point);
                    while (point2.isEqualsLocation(mTrackPoint)) {
                        try {
                            //Log.d(TAG, "等待原始轨迹数据更新中");
                            Thread.sleep(1500);
                        } catch (Exception e) {
                        }
                    }
                    //Log.d(TAG, "原始轨迹数据已更新＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
                    point = new TrackPoint(mTrackPoint);
                    point2 = new TrackPoint(mTrackPoint);
                }
            }
        };
    }

    protected synchronized void filterByThread(TrackPoint point) {
        //Log.d(TAG,"isMainThread="+isMainThread());
    }

    protected boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

}
