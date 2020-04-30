package com.kuyou.smartwristband.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.TextView;

import com.kuyou.smartwristband.base.BaseAppcation;
import com.kuyou.smartwristband.R;
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleScanTool;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Describe 工具栏
 */

public class CommonUtil {
    private static final String TAG = "CommonUtil_123456";

    /**
     * 是否是24小时
     *
     * @return
     */
    public static boolean is24Hour() {
//        int timeStyle = (int) SPUtils.get(Constant.TIME_STYLE,0);
        boolean is24;
//        if (timeStyle == 0) {//跟随系统
        ContentResolver cv = BaseAppcation.getInstance().getContentResolver();
        // 获取当前系统设置
        String time_12_24 = android.provider.Settings.System.getString(cv,
                android.provider.Settings.System.TIME_12_24);
        is24 = "24".equals(time_12_24) ? true : false;
//        }else{
//            is24 = timeStyle == Constants.TIME_MODE_24;
//        }
        return true;
    }

    public static int format24To12(int hour) {
        int h = hour % 12;
        if (hour == 12) {
            h = h == 0 ? 12 : h;
        } else {
            h = h == 0 ? 0 : h;
        }
        return h;
    }

    public static boolean isAM(int hour) {
        return hour < 12;
    }

    /**
     * 将一天中的分钟序列数变为对应的hh:mm形式
     *
     * @param mins 00:00为第一分钟， mins = h * 60 + m;范围1~1440
     * @return
     */
    public static String timeFormatter(int mins, boolean is24, String[] amOrPm, boolean isUnit) {
        if (mins >= 0 && mins < 1440) {
            int h = getHourAndMin(mins, is24)[0];
            int min = mins % 60;
            if (is24) {
                return String.format("%1$02d:%2$02d", h == 24 ? 0 : h, min);
            } else {
                String m = "";
                if (isUnit) {
                    if (amOrPm != null) {
                        m = mins <= 12 * 60 ? amOrPm[0] : amOrPm[1];
                    } else {
                        m = mins <= 12 * 60 ? "am" : "pm";
                    }
                }
//                if(m.equals("下午")||m.equals("上午")){
//                    return m+ String.format("%1$02d:%2$02d", h == 24 ? 0 : h, min);
//                }else {
                return String.format("%1$02d:%2$02d", h == 24 ? 0 : h, min) + m;
//                }
            }
        } else if (mins >= 1440) {
            mins -= 1440;
            int h = 0;
            int min = 0;
            if (mins > 0) {
                h = getHourAndMin(mins, is24)[0];
                min = mins % 60;
            }
            if (is24) {
                return String.format("%1$02d:%2$02d", h == 24 ? 0 : h, min);
            } else {
                String m = "";
                if (isUnit) {
                    if (amOrPm != null) {
                        m = mins <= 12 * 60 ? amOrPm[0] : amOrPm[1];
                    } else {
                        m = mins <= 12 * 60 ? "am" : "pm";
                    }
                }

                return String.format("%1$02d:%2$02d", h == 24 ? 0 : h, min) + m;
            }
        }

//        Log.e("Util", "timeFormatter Error : mins is out of range [0 , 1440).");
//        return "--:--";
        return "00:00";
    }

    /**
     * 将一天中的分钟序列数变为对应的hh:mm形式
     *
     * @param mins 00:00为第一分钟， mins = h * 60 + m;范围1~1440
     * @return
     */
    public static String timeFormatter(int mins, boolean is24, String[] amOrPm, boolean isUnit, boolean isStart) {
        if (mins >= 0 && mins < 1440) {
            int h = getHourAndMin(mins, is24)[0];
            int min = mins % 60;
            if (!isStart && min != 0) {
                h += 1;
            }
            min = 0;
            if (is24) {
                return String.format("%1$02d:%2$02d", h == 24 ? 0 : h, min);
            } else {
                String m = "";
                if (isUnit) {
                    if (amOrPm != null) {
                        m = mins < 12 * 60 ? amOrPm[0] : amOrPm[1];
                    } else {
                        m = mins < 12 * 60 ? "am" : "pm";
                    }
                }
                return m + String.format("%1$02d:%2$02d", h == 24 ? 0 : h, min);
            }
        } else if (mins >= 1440) {
            mins -= 1440;
            int h = 0;
            int min = 0;
            if (mins > 0) {
                h = getHourAndMin(mins, is24)[0];
                min = mins % 60;
            }
            if (!isStart && min != 0) {
                h += 1;
            }
            min = 0;
            if (is24) {
                return String.format("%1$02d:%2$02d", h == 24 ? 0 : h, min);
            } else {
                String m = "";
                if (isUnit) {
                    if (amOrPm != null) {
                        m = mins < 12 * 60 ? amOrPm[0] : amOrPm[1];
                    } else {
                        m = mins < 12 * 60 ? "am" : "pm";
                    }
                }
                return m + String.format("%1$02d:%2$02d", h == 24 ? 0 : h, min);
            }
        }

//        Log.e("Util", "timeFormatter Error : mins is out of range [0 , 1440).");
//        return "--:--";
        return "00:00";
    }

    public static int[] getHourAndMin(int mins, boolean is24) {
        int h = mins / 60;
        // 0 ,12,24都是12点 ， 下午的-12
        h = is24 ? h : (h % 12 == 0 ? 12 : h > 12 ? h - 12 : h);
        return new int[]{h, mins % 60};
    }


    /**
     * @param h
     * @param min
     * @param is24
     * @return
     */

    /**
     * @param time 00:00
     * @param is24
     * @return
     */


    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static boolean isOPen(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    /**
     * 强制帮用户打开GPS
     *
     * @param context
     */
    public static void openGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据星期开始日获取周
     * 如果开始日为周日则 返回 日 一 二 三 四
     * 如果开始日为 六 则返回 六 日 一 二
     * 如果开始日为一 则返回 一 二 三 四
     *
     * @param context
     * @param weekStartDay
     * @return
     */

    /**
     * 把发送闹钟的week转换为显示的week
     * 发送闹钟的week 固定是从星期一开始的
     * 显示的week 根据开始星期日决定的
     *
     * @return 返回星期一为开始日的数组
     * @startWeek 0:周六，1：周日，2：周一
     */
    public static boolean[] alarmToShowAlarm(boolean[] week, int startWeek) {
        boolean[] tempAlarm = new boolean[7];
        if (startWeek == 0) {
            tempAlarm[0] = week[2];
            tempAlarm[1] = week[3];
            tempAlarm[2] = week[4];
            tempAlarm[3] = week[5];
            tempAlarm[4] = week[6];
            tempAlarm[5] = week[0];
            tempAlarm[6] = week[1];
        } else if (startWeek == 1) {
            tempAlarm[0] = week[1];
            tempAlarm[1] = week[2];
            tempAlarm[2] = week[3];
            tempAlarm[3] = week[4];
            tempAlarm[4] = week[5];
            tempAlarm[5] = week[6];
            tempAlarm[6] = week[0];
        } else {
            tempAlarm = Arrays.copyOf(week, week.length);
        }
        return tempAlarm;
    }


    /**
     * @param week
     * @param startWeek 0:周六，1：周日，2：周一
     * @return 将星期一为开始日的数组，转换成其他一种
     */
    public static boolean[] alarmToShowAlarm2(boolean[] week, int startWeek) {
        boolean[] tempAlarm = new boolean[7];
        if (startWeek == 0) {
            tempAlarm[0] = week[5];
            tempAlarm[1] = week[6];
            tempAlarm[2] = week[0];
            tempAlarm[3] = week[1];
            tempAlarm[4] = week[2];
            tempAlarm[5] = week[3];
            tempAlarm[6] = week[4];
        } else if (startWeek == 1) {
            tempAlarm[0] = week[6];
            tempAlarm[1] = week[0];
            tempAlarm[2] = week[1];
            tempAlarm[3] = week[2];
            tempAlarm[4] = week[3];
            tempAlarm[5] = week[4];
            tempAlarm[6] = week[5];
        } else {
            tempAlarm = Arrays.copyOf(week, week.length);
        }
        return tempAlarm;
    }

    /**
     * 是否有轨迹
     *
     * @param type
     * @return
     */
    public static boolean hasOrbit(int type) {
        //0x01;// 走路
        //0x02;// 跑步
        //0x03;// 骑行
        //0x04;// 徒步
        int[] types = {1, 2, 3, 4};
        for (int t : types) {
            if (t == type) {
                return true;
            }
        }
        return false;
    }

    public static String noHeartRate(String s) {
        if (TextUtils.isEmpty(s) || s.equals("0")) {
            return "--";
        }
        return s + "";
    }

    public static String noBloodPressure(int systolicPressure, int diastolicPressure) {
        if (systolicPressure == 0 || diastolicPressure == 0) {
            return "--/--";
        }
        return systolicPressure + "/" + diastolicPressure;
    }

    public static String noPace(int speed) {
        if (speed == 0) {
            return "--";
        }
        StringBuffer avgPace = new StringBuffer();
        avgPace.append(speed / 60);
        avgPace.append("'");
        avgPace.append(speed % 60);  //转换字符串
        avgPace.append("\"");
        return avgPace.toString();
    }


    public static void adjustTvTextSize(TextView tv, int maxWidth, String text) {
        int avaiWidth = maxWidth - tv.getPaddingLeft() - tv.getPaddingRight() - 10;

        if (avaiWidth <= 0) {
            return;
        }

        TextPaint textPaintClone = new TextPaint(tv.getPaint());
        // note that Paint text size works in px not sp
        float trySize = textPaintClone.getTextSize();

        while (textPaintClone.measureText(text) > avaiWidth) {
            trySize--;
            textPaintClone.setTextSize(trySize);
        }

        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
    }

    private static DecimalFormat df;
    private static DecimalFormat decimalFormat;

    static {
        Locale.setDefault(Locale.CHINA);
        df = new DecimalFormat("#,###");
        decimalFormat = new DecimalFormat("###,###,###,##0.00");
    }

    public static String formatThree(int value) {
        return df.format(value);
    }

    public static String formatThree(float value) {
        return df.format(value);
    }

    public static String formatNumber(int num) {
        String formatNum;
        if (num > 10000) {
            formatNum = "10,000+";
        } else {
            formatNum = df.format(num);
        }
        return formatNum;
    }

    /**
     * 保留两位并且三位用“，”隔开
     *
     * @param num
     * @return
     */
    public static String formatDistance(float num) {
        return decimalFormat.format(num);
    }

    public static SimpleDateFormat getFormat(String format) {
        return new SimpleDateFormat(format);
    }

    /**
     * 计算月数
     *
     * @return
     */
    private static int calculationDaysOfMonth(int year, int month) {
        int day = 0;
        switch (month) {
            // 31天
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                day = 31;
                break;
            // 30天
            case 4:
            case 6:
            case 9:
            case 11:
                day = 30;
                break;
            // 计算2月天数
            case 2:
                day = year % 100 == 0 ? year % 400 == 0 ? 29 : 28
                        : year % 4 == 0 ? 29 : 28;
                break;
        }

        return day;
    }


    /**
     * 目标时间选择列表（无单位）
     *
     * @return
     */
    public static List<Float> getTimeList() {
        final List<Float> times = new ArrayList<>();
        times.add(5f);
        for (int i = 10; i <= 6000; i += 10) {
            times.add(i * 1f);
        }
        return times;
    }

    /**
     * 目标距离选择列表（无单位）
     *
     * @return
     */
    public static List<Float> getDistanceList() {
        final List<Float> distances = new ArrayList<>();
        distances.add(0.5f);
        for (int i = 1; i <= 100; i++) {
            distances.add(i * 1f);
        }
        return distances;
    }

    /**
     * 目标卡路里选择列表（无单位）
     *
     * @return
     */
    public static List<Float> gettCalorieList() {
        final List<Float> distances = new ArrayList<>();
        for (int i = 300; i <= 9000; i += 300) {
            distances.add(i * 1f);
        }
        return distances;
    }

    //added by wgx
    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager.getAdapter();
    }

    private static CommonDialog commonDialog;

    public static void showSureDialog(Activity context, String msg, DialogInterface.OnClickListener ok, DialogInterface.OnClickListener cancel) {
        if (context.isFinishing()) {
            if (null != commonDialog)
                commonDialog.dismiss();
            commonDialog = null;
            return;
        }
        if (null != commonDialog) {
            if (commonDialog.getActivityContext().isFinishing()) {
                commonDialog = null;
            } else if (commonDialog.getActivityContext() != context) {
                commonDialog = null;
            } else if (null != commonDialog.getMessage()
                    && !commonDialog.getMessage().equals(msg)) {
                commonDialog = null;
            }
        }
        if (null == commonDialog) {
            commonDialog = new CommonDialog.Builder(context)
                    .isVertical(false).setTitle(R.string.bluetooth_disable)
                    .setLeftButton(R.string.cancel, cancel)
                    .setTitle(R.string.tips_title)
                    .setMessage(msg)
                    .setRightButton(context.getString(R.string.sure), ok)
                    .create();
            commonDialog.setContext(context);
        }
        try {
            commonDialog.show();
        } catch (WindowManager.BadTokenException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            commonDialog = null;
        }
    }

    public static boolean isOpenBle(Activity context, DialogInterface.OnClickListener ok) {
        if (!BleScanTool.getInstance().isBluetoothOpen()) {
            if (null != commonDialog) {
                commonDialog.dismiss();
                commonDialog = null;
            }
            context.runOnUiThread(() -> CommonUtil.showSureDialog(context, context.getString(R.string.blurtooth_use_cntent), ok,
                    (dialog, which) -> android.os.Process.killProcess(android.os.Process.myPid())));
            return true;
        }
        return false;

    }

    public static boolean isOpenBle(Activity context) {
        return isOpenBle(context, (dialog, which) -> BleScanTool.getInstance().openBluetooth());
    }

    /**
     * action: 时间戳转UTC时间 <br/>
     */
    public static String formatUTCTimeByMilSecond(long milSecond, String pattern) {
        Date date = new Date(milSecond);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * action: 时间戳转设备本地时间 <br/>
     */
    public static String formatLocalTimeByMilSecond(long timeInMillis, String pattern) {
        if (0 < timeInMillis)
            Calendar.getInstance().setTimeInMillis(timeInMillis);
        return new SimpleDateFormat(pattern).format(Calendar.getInstance().getTime());
    }

    public static long nano2milli(long nano) {return (long) (nano / 1e6);}
    //end wgx
}
