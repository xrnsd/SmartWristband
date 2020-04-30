package com.kuyou.smartwristband.gps.filter.kalman;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.util.Log;

import com.kuyou.smartwristband.gps.filter.geohash.GeoPoint;
import com.kuyou.smartwristband.gps.filter.geohash.GeohashRTFilter;
import com.kuyou.smartwristband.gps.filter.TrackPoint;
import com.kuyou.smartwristband.gps.filter.TrajectoryFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class TrajectoryKalmanFilter extends TrajectoryFilter implements SensorEventListener {

    public static final String TAG = "TrajectoryKalmanFilter";

    public static double ACCELEROMETER_DEFAULT_DEVIATION = 0.1;
    public static final int SENSOR_POSITION_MIN_TIME = 500;
    public static final int GPS_MIN_TIME = 2000;
    public static final int GPS_MIN_DISTANCE = 0;
    public static final double SENSOR_DEFAULT_FREQ_HZ = 10.0;
    public static final int GEOHASH_DEFAULT_PREC = 6;
    public static final int GEOHASH_DEFAULT_MIN_POINT_COUNT = 2;
    public static final double DEFAULT_VEL_FACTOR = 1.0;
    public static final double DEFAULT_POS_FACTOR = 1.0;

    public enum LogMessageType {
        KALMAN_ALLOC,
        KALMAN_PREDICT,
        KALMAN_UPDATE,
        GPS_DATA,
        ABS_ACC_DATA,
        FILTERED_GPS_DATA
    }


    protected TrackPoint m_lastLocation;
    private GeohashRTFilter m_geoHashRTFilter = null;
    private Settings m_settings;
    private GPSAccKalmanFilter m_kalmanFilter;
    private SensorDataEventLoopTask m_eventLoopTask;
    private List<Sensor> m_lstSensors;
    private SensorManager m_sensorManager;

    private double m_magneticDeclination = 0.0;
    private static int[] sensorTypes = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
    };
    private float[] rotationMatrix = new float[16];
    private float[] rotationMatrixInv = new float[16];
    private float[] absAcceleration = new float[4];
    private float[] linearAcceleration = new float[4];
    private Queue<SensorGpsDataItem> m_sensorDataQueue = new PriorityBlockingQueue<>();
    private OnDataFilterListener mOnDataFilterListener;

    class SensorDataEventLoopTask extends AsyncTask {
        boolean needTerminate = false;
        long deltaTMs;
        TrajectoryKalmanFilter owner;

        SensorDataEventLoopTask(long deltaTMs, TrajectoryKalmanFilter owner) {
            this.deltaTMs = deltaTMs;
            this.owner = owner;
        }

        private void handlePredict(SensorGpsDataItem sdi) {
            Log.d(TAG, String.format("%d%d KalmanPredict : accX=%f, accY=%f",
                    LogMessageType.KALMAN_PREDICT.ordinal(),
                    (long) sdi.getTimestamp(),
                    sdi.getAbsEastAcc(),
                    sdi.getAbsNorthAcc()));
            m_kalmanFilter.predict(sdi.getTimestamp(), sdi.getAbsEastAcc(), sdi.getAbsNorthAcc());
        }

        private void handleUpdate(SensorGpsDataItem sdi) {
            double xVel = sdi.getSpeed() * Math.cos(sdi.getCourse());
            double yVel = sdi.getSpeed() * Math.sin(sdi.getCourse());
            Log.d(TAG, String.format("%d%d KalmanUpdate : pos lon=%f, lat=%f, xVel=%f, yVel=%f, posErr=%f, velErr=%f",
                    LogMessageType.KALMAN_UPDATE.ordinal(),
                    (long) sdi.getTimestamp(),
                    sdi.getGpsLon(),
                    sdi.getGpsLat(),
                    xVel,
                    yVel,
                    sdi.getPosErr(),
                    sdi.getVelErr()
            ));

            m_kalmanFilter.update(
                    sdi.getTimestamp(),
                    Coordinates.longitudeToMeters(sdi.getGpsLon()),
                    Coordinates.latitudeToMeters(sdi.getGpsLat()),
                    xVel,
                    yVel,
                    sdi.getPosErr(),
                    sdi.getVelErr()
            );
        }

        private TrackPoint locationAfterUpdateStep(SensorGpsDataItem sdi) {
            double xVel, yVel;
            TrackPoint loc = new TrackPoint();
            GeoPoint pp = Coordinates.metersToGeoPoint(m_kalmanFilter.getCurrentX(),
                    m_kalmanFilter.getCurrentY());
            loc.setLatitude(pp.Latitude);
            loc.setLongitude(pp.Longitude);
            loc.setAltitude(sdi.getGpsAlt());
            xVel = m_kalmanFilter.getCurrentXVel();
            yVel = m_kalmanFilter.getCurrentYVel();
            double speed = Math.sqrt(xVel * xVel + yVel * yVel); //scalar speed without bearing
            loc.setBearing((float) sdi.getCourse());
            loc.setSpeed((float) speed);
            loc.setTime(System.currentTimeMillis());
            loc.setElapsedRealtimeNanos(System.nanoTime());
            loc.setAccuracy((float) sdi.getPosErr());

            if (m_geoHashRTFilter != null) {
                //m_geoHashRTFilter.filter(loc);
            }

            return loc;
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected Object doInBackground(Object[] objects) {
            while (!needTerminate) {
                try {
                    Thread.sleep(deltaTMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue; //bad
                }

                SensorGpsDataItem sdi;
                double lastTimeStamp = 0.0;
                while ((sdi = m_sensorDataQueue.poll()) != null) {
                    if (sdi.getTimestamp() < lastTimeStamp) {
                        continue;
                    }
                    lastTimeStamp = sdi.getTimestamp();

                    //warning!!!
                    if (sdi.getGpsLat() == SensorGpsDataItem.NOT_INITIALIZED
                        ||sdi.getGpsAlt() == SensorGpsDataItem.NOT_INITIALIZED) {
                        handlePredict(sdi);
                    } else {
                        handleUpdate(sdi);
                        TrackPoint loc = locationAfterUpdateStep(sdi);
                        publishProgress(loc);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            onLocationChangedImp((TrackPoint) values[0]);
        }

        void onLocationChangedImp(TrackPoint location) {
            if (location == null || location.getLatitude() == 0 ||
                    location.getLongitude() == 0) {
                Log.d(TAG, "onLocationChangedImp location is none");
                return;
            }
            m_lastLocation = location;
            onDataAfterFilter(location);
        }
    }

    public TrajectoryKalmanFilter(Context context, OnDataFilterListener listener) {
        super(listener);
        mOnDataFilterListener = listener;

        m_lstSensors = new ArrayList<Sensor>();
        m_eventLoopTask = null;

        reset();
        init(context);
    }

    public void init(Context context) {
        Log.d(TAG, "init");
        m_kalmanFilter = null;

        /*
        m_settings = new Settings(
                ACCELEROMETER_DEFAULT_DEVIATION,
                0,// Integer.parseInt(mSharedPref.getString("pref_gps_min_distance", "")),
                0,// Integer.parseInt(mSharedPref.getString("pref_gps_min_time", "")),
                0,// Integer.parseInt(mSharedPref.getString("pref_position_min_time", "")),
                0,// Integer.parseInt(mSharedPref.getString("pref_geohash_precision", "")),
                0,// Integer.parseInt(mSharedPref.getString("pref_geohash_min_point", "")),
                0,// Double.parseDouble(mSharedPref.getString("pref_sensor_frequency", "")),
                false,
                DEFAULT_VEL_FACTOR,
                DEFAULT_POS_FACTOR
        );
        * */
        m_settings = new Settings(
                ACCELEROMETER_DEFAULT_DEVIATION,
                GPS_MIN_DISTANCE,
                GPS_MIN_TIME,
                SENSOR_POSITION_MIN_TIME,
                GEOHASH_DEFAULT_PREC,
                GEOHASH_DEFAULT_MIN_POINT_COUNT,
                SENSOR_DEFAULT_FREQ_HZ,
                true,
                DEFAULT_VEL_FACTOR,
                DEFAULT_POS_FACTOR
        );

        m_sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (m_sensorManager == null) {
            Log.e(TAG, "m_sensorManager init fail");
            return;
        }
        for (Integer st : sensorTypes) {
            Sensor sensor = m_sensorManager.getDefaultSensor(st);
            if (sensor == null) {
                Log.e(TAG, String.format("Couldn't get sensor %d", st));
                continue;
            }
            m_lstSensors.add(sensor);
        }
        m_sensorDataQueue.clear();
        for (Sensor sensor : m_lstSensors) {
            m_sensorManager.unregisterListener(this, sensor);
            m_sensorManager.registerListener(this, sensor,
                    hertz2periodUs(m_settings.sensorFrequencyHz));
        }

        if (m_settings.geoHashPrecision != 0 &&
                m_settings.geoHashMinPointCount != 0) {
            m_geoHashRTFilter = new GeohashRTFilter(m_settings.geoHashPrecision,
                    m_settings.geoHashMinPointCount);
        }
        if (m_geoHashRTFilter != null) {
            m_geoHashRTFilter.stop();
        }

        m_eventLoopTask = new SensorDataEventLoopTask(m_settings.positionMinTime, this);
        m_eventLoopTask.needTerminate = false;
        m_eventLoopTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void reset() {
        if (m_eventLoopTask != null) {
            m_eventLoopTask.needTerminate = true;
            m_eventLoopTask.cancel(true);
        }

        if (null != m_sensorManager) {
            for (Sensor sensor : m_lstSensors) {
                m_sensorManager.unregisterListener(this, sensor);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /*do nothing*/
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final int east = 0;
        final int north = 1;
        final int up = 2;

        long now = android.os.SystemClock.elapsedRealtimeNanos();
        long nowMs = nano2milli(now);
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, linearAcceleration, 0, event.values.length);
                android.opengl.Matrix.multiplyMV(absAcceleration, 0, rotationMatrixInv,
                        0, linearAcceleration, 0);

                if (m_kalmanFilter == null) {
                    break;
                }

                SensorGpsDataItem sdi = new SensorGpsDataItem(nowMs,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        absAcceleration[north],
                        absAcceleration[east],
                        absAcceleration[up],
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        SensorGpsDataItem.NOT_INITIALIZED,
                        m_magneticDeclination);
                m_sensorDataQueue.add(sdi);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                android.opengl.Matrix.invertM(rotationMatrixInv, 0, rotationMatrix, 0);
                break;
        }
    }

    @Override
    public void filter(TrackPoint point) {
        if (point == null) return;
        Log.d(TAG, "> filter");

        double x, y, xVel, yVel, posDev, course, speed;
        long timeStamp;
        speed = point.getSpeed();
        course = point.getBearing();
        x = point.getLongitude();
        y = point.getLatitude();
        xVel = speed * Math.cos(course);
        yVel = speed * Math.sin(course);
        posDev = point.getAccuracy();
        timeStamp = nano2milli(point.getElapsedRealtimeNanos());
        //WARNING!!! here should be speed accuracy, but point.hasSpeedAccuracy()
        // and point.getSpeedAccuracyMetersPerSecond() requares API 26
        double velErr = point.getAccuracy() * 0.1;

        String logStr = String.format("%d%d GPS : pos lat=%f, lon=%f, alt=%f, hdop=%f, speed=%f, bearing=%f, sa=%f",
                LogMessageType.GPS_DATA.ordinal(),
                timeStamp, point.getLatitude(),
                point.getLongitude(), point.getAltitude(), point.getAccuracy(),
                point.getSpeed(), point.getBearing(), velErr);
        Log.d(TAG, logStr);

        GeomagneticField f = new GeomagneticField(
                (float) point.getLatitude(),
                (float) point.getLongitude(),
                (float) point.getAltitude(),
                timeStamp);
        m_magneticDeclination = f.getDeclination();

        if (m_kalmanFilter == null) {
            Log.d(TAG, String.format(" KalmanAloc : lon=%f, lat=%f, speed=%f, course=%f, m_accDev=%f, posDev=%f",
                    x, y, speed, course, m_settings.accelerationDeviation, posDev));
            m_kalmanFilter = new GPSAccKalmanFilter(
                    false, //todo move to settings
                    Coordinates.longitudeToMeters(x),
                    Coordinates.latitudeToMeters(y),
                    xVel,
                    yVel,
                    m_settings.accelerationDeviation,
                    posDev,
                    timeStamp,
                    m_settings.mVelFactor,
                    m_settings.mPosFactor);
            return;
        }

        SensorGpsDataItem sdi = new SensorGpsDataItem(
                timeStamp, point.getLatitude(), point.getLongitude(), point.getAltitude(),
                SensorGpsDataItem.NOT_INITIALIZED,
                SensorGpsDataItem.NOT_INITIALIZED,
                SensorGpsDataItem.NOT_INITIALIZED,
                point.getSpeed(),
                point.getBearing(),
                point.getAccuracy(),
                velErr,
                m_magneticDeclination);

        m_sensorDataQueue.add(sdi);
    }

    public static class Settings {
        private double accelerationDeviation;
        private int gpsMinDistance;
        private int gpsMinTime;
        private int positionMinTime;
        private int geoHashPrecision;
        private int geoHashMinPointCount;
        private double sensorFrequencyHz;
        private boolean filterMockGpsCoordinates;

        private double mVelFactor;
        private double mPosFactor;

        public Settings(double accelerationDeviation,
                        int gpsMinDistance,
                        int gpsMinTime,
                        int positionMinTime,
                        int geoHashPrecision,
                        int geoHashMinPointCount,
                        double sensorFrequencyHz,
                        boolean filterMockGpsCoordinates,
                        double velFactor,
                        double posFactor) {
            this.accelerationDeviation = accelerationDeviation;
            this.gpsMinDistance = gpsMinDistance;
            this.gpsMinTime = gpsMinTime;
            this.positionMinTime = positionMinTime;
            this.geoHashPrecision = geoHashPrecision;
            this.geoHashMinPointCount = geoHashMinPointCount;
            this.sensorFrequencyHz = sensorFrequencyHz;
            this.filterMockGpsCoordinates = filterMockGpsCoordinates;
            this.mVelFactor = velFactor;
            this.mPosFactor = posFactor;
        }
    }

    /**
     * Convert frequency to microseconds.
     *
     * @param hz
     * @return
     */
    public static int hertz2periodUs(double hz) {
        return (int) (1.0e6 / hz);
    }

    public static long nano2milli(long nano) {
        return (long) (nano / 1e6);
    }
}
