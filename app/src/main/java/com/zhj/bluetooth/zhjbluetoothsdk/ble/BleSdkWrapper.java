/*     */ package com.zhj.bluetooth.zhjbluetoothsdk.ble;
/*     */ 
/*     */ import android.content.Context;
/*     */ import android.os.Build;
/*     */ import android.os.Build.VERSION;
/*     */ import android.os.StrictMode;
/*     */ import android.os.StrictMode.ThreadPolicy;
/*     */ import android.os.StrictMode.ThreadPolicy.Builder;
/*     */ import com.zhj.bluetooth.zhjbluetoothsdk.bean.Alarm;
/*     */ import com.zhj.bluetooth.zhjbluetoothsdk.bean.AppNotice;
/*     */ import com.zhj.bluetooth.zhjbluetoothsdk.bean.BLEDevice;
/*     */ import com.zhj.bluetooth.zhjbluetoothsdk.bean.DeviceState;
/*     */ import com.zhj.bluetooth.zhjbluetoothsdk.bean.Goal;
/*     */ import com.zhj.bluetooth.zhjbluetoothsdk.bean.LongSit;
/*     */ import com.zhj.bluetooth.zhjbluetoothsdk.bean.UserBean;
/*     */ import com.zhj.bluetooth.zhjbluetoothsdk.util.BleContant;
/*     */ import com.zhj.bluetooth.zhjbluetoothsdk.util.Constants;
/*     */ import com.zhj.bluetooth.zhjbluetoothsdk.util.LogUtil;
/*     */ import com.zhj.bluetooth.zhjbluetoothsdk.util.SPHelper;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.net.HttpURLConnection;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ import java.util.UUID;
/*     */ import org.json.JSONException;
/*     */ import org.json.JSONObject;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class BleSdkWrapper
/*     */   implements Constants, BleContant
/*     */ {
/*  54 */   public static final UUID RX_SERVICE_UUID = UUID.fromString("00000af0-0000-1000-8000-00805f9b34fb");
/*     */   
/*  56 */   public static boolean isConnected() { if (!BleScanTool.getInstance().isBluetoothOpen()) {
/*  57 */       return false;
/*     */     }
/*  59 */     return BleManager.getInstance().isConnBluetoothSuccess();
/*     */   }
/*     */   
/*     */   public static String getBindMac() {
/*  63 */     return null;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public static void disConnect()
/*     */   {
/*  70 */     BleManager.getInstance().disconnectBluethoothConnection();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void init(Context context, BleCallbackWrapper callback)
/*     */   {
/*  78 */    // String urlAddress = "http://47.75.143.120:8080/keephealth/";
/*  79 */    // String method = "customInfo/check";
/*  80 */    // String getUrl = urlAddress + method + "?appType=1&packageName=" + context.getPackageName();
/*  81 */    // if (VERSION.SDK_INT > 9) {
/*  82 */    //   ThreadPolicy policy = new Builder().permitAll().build();
/*  83 */    //   StrictMode.setThreadPolicy(policy);
/*     */    // }
/*     */    // try {
/*  86 */    //   URL url = new URL(getUrl);
/*  87 */    //   HttpURLConnection connection = (HttpURLConnection)url.openConnection();
/*  88 */    //   connection.connect();
/*     */    //
/*  90 */    //   if (connection.getResponseCode() == 200)
/*     */    //   {
/*  92 */    //     InputStream is = connection.getInputStream();
/*     */    //
/*  94 */    //     BufferedReader reader = new BufferedReader(new InputStreamReader(is));
/*  95 */    //     StringBuffer buffer = new StringBuffer();
/*  96 */    //     String readLine = "";
/*  97 */    //     while ((readLine = reader.readLine()) != null) {
/*  98 */    //       buffer.append(readLine);
/*     */    //     }
/* 100 */    //     is.close();
/* 101 */    //     reader.close();
/* 102 */    //     connection.disconnect();
/* 103 */    //     JSONObject jsonData = new JSONObject(buffer.toString());
/* 104 */    //     String data = jsonData.getString("data");
/* 105 */    //     JSONObject jsonAuthorized = new JSONObject(data);
/* 106 */    //     Boolean authorized = Boolean.valueOf(jsonAuthorized.getBoolean("authorized"));
/* 107 */    //     if (authorized.booleanValue()) {
/* 108 */           mContext = context;
/* 109 */           BleScanTool.getInstance().init(context);
/* 110 */           BleManager.getInstance().init(context);
                    if(null!=callback)
/* 111 */              callback.complete(1, null);
/*     */    //     } else {
/* 113 */    //       callback.complete(0, null);
/*     */    //     }
/*     */    //   } else {
/* 116 */    //     LogUtil.d("包名不符合使用");
/* 117 */    //     callback.complete(0, null);
/*     */    //   }
/*     */    // } catch (MalformedURLException e) {
/* 120 */    //   e.printStackTrace();
/*     */    // } catch (IOException e) {
/* 122 */    //   e.printStackTrace();
/*     */    // } catch (JSONException e) {
/* 124 */    //   e.printStackTrace();
/*     */    // }
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void getDeviceInfo(BleCallback bleCallback)
/*     */   {
/* 135 */     if (!isCanSend(bleCallback)) {
/* 136 */       return;
/*     */     }
/* 138 */     BleManager.getInstance().enqueue(CmdHelper.CMD_GET_DEVICE, bleCallback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void setHeartTest(boolean state, BleCallback bleCallback)
/*     */   {
/* 146 */     if (!isCanSend(bleCallback)) {
/* 147 */       return;
/*     */     }
/* 149 */     BleManager.getInstance().enqueue(CmdHelper.setHeartTest(state ? 1 : 0, 30), bleCallback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public static void getCurrentStep(BleCallback bleCallback)
/*     */   {
/* 156 */     BleManager.getInstance().enqueue(CmdHelper.CMD_GET_CURRENT_STEP, bleCallback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public static void getActivity(BleCallback bleCallback)
/*     */   {
/* 163 */     BleManager.getInstance().enqueue(200, CmdHelper.CMD_GET_ACTIVITY, bleCallback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public static void getHeartRate(BleCallback bleCallback)
/*     */   {
/* 170 */     BleManager.getInstance().enqueue(CmdHelper.getHeartRate(0), bleCallback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void connect(BLEDevice connectDevice)
/*     */   {
/* 178 */     BleManager.getInstance().connect(connectDevice);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void setBleListener(AppBleListener listener)
/*     */   {
/* 186 */     BleManager.getInstance().setBleListener(listener);
/*     */   }
/*     */   
/* 189 */   public static void removeListener(AppBleListener listener) { BleManager.getInstance().removeListener(listener); }
/*     */   
/*     */ 
/*     */   public static void bind() {}
/*     */   
/*     */ 
/*     */   public static void stopScanDevices()
/*     */   {
/* 197 */     BleScanTool.getInstance().scanLeDevice(false, 1000L);
/*     */   }
/*     */   
/* 200 */   public static void removeScanDeviceListener(BleScanTool.ScanDeviceListener scanDeviceListener) { BleScanTool.getInstance().removeScanDeviceListener(scanDeviceListener); }
/*     */   
/*     */   public static void startScanDevices(BleScanTool.ScanDeviceListener listener)
/*     */   {
/* 204 */     //long delay = 8000L; //4000L;
            long delay = 9000L;
/*     */
/*     */ 
/*     */
/*     */     BleScanTool.getInstance().init(mContext);
            //@{ added by wgx Usefulness:
              BleManager.getInstance().init(mContext);
            //}@ end wgx
/* 209 */     BleScanTool.getInstance().addScanDeviceListener(listener);
/* 210 */     BleScanTool.getInstance().scanLeDeviceByService(true, BleScanTool.RX_SERVICE_UUID, delay);
/*     */   }
/*     */   
/* 213 */   private static boolean isExtendScan() { List<String> list = new ArrayList();
/* 214 */     list.add("xiaomi");
/* 215 */     list.add("meizu");
/* 216 */     return list.contains(getPhoneManufacturer());
/*     */   }
/*     */   
/* 219 */   public static String getPhoneManufacturer() { return Build.MANUFACTURER.toLowerCase(); }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void getUserInfo(BleCallback callback)
/*     */   {
/* 227 */     if (!isCanSend(callback)) {
/* 228 */       return;
/*     */     }
/* 230 */     BleManager.getInstance().enqueue(CmdHelper.CMD_GET_USERINFO, callback);
/*     */   }
/*     */   
/*     */   public static void setUserInfo(UserBean userInfo, BleCallback callback) {
/* 234 */     if (!isCanSend(callback)) {
/* 235 */       return;
/*     */     }
/* 237 */     BleManager.getInstance().enqueue(CmdHelper.getUserInfo(userInfo.getGender(), userInfo.getAge(), userInfo.getHeight(), userInfo.getWeight(), userInfo.getStepDistance()), callback);
/*     */   }
/*     */   
/*     */   private static boolean isCanSend(BleCallback callback) {
/* 241 */     if ((!isConnected()) && 
/* 242 */       (callback != null)) {
/* 243 */       callback.complete(-4, null);
/* 244 */       BleClient.showMessage("未连接....不发送数据");
/* 245 */       return false;
/*     */     }
/*     */     
/* 248 */     return true;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void setTime(BleCallback callback)
/*     */   {
/* 256 */     if (!isCanSend(callback)) {
/* 257 */       return;
/*     */     }
/* 259 */     BleManager.getInstance().enqueue(CmdHelper.getTime(), callback);
/*     */   }
/*     */   
/* 262 */   public static void write(byte[] datas, BleCallback callback) { BleManager.getInstance().enqueue(datas, callback); }
/*     */   
/*     */   public static void write(int flat, byte[] datas, BleCallback callback) {
/* 265 */     BleManager.getInstance().enqueue(flat, datas, callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void getPower(BleCallback callback)
/*     */   {
/* 274 */     if (!isCanSend(callback)) {
/* 275 */       return;
/*     */     }
/* 277 */     BleManager.getInstance().enqueue(CmdHelper.CMD_GET_CURRENT_POWER, callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public static void getDeviceState(BleCallback callback)
/*     */   {
/* 284 */     if (!isCanSend(callback)) {
/* 285 */       return;
/*     */     }
/* 287 */     BleManager.getInstance().enqueue(CmdHelper.CMD_GET_DEVICE_STATE, callback);
/*     */   }
/*     */   
/* 290 */   public static void setDeviceState(DeviceState deviceState, BleCallback callback) { Locale locale = Locale.getDefault();
/* 291 */     String lang = locale.getLanguage();
/* 292 */     switch (lang) {
/*     */     case "en": 
/* 294 */       deviceState.language = 0;
/* 295 */       break;
/*     */     case "zh": 
/* 297 */       deviceState.language = 1;
/* 298 */       break;
/*     */     case "ru": 
/*     */     case "be": 
/* 301 */       deviceState.language = 2;
/* 302 */       break;
/*     */     case "uk": 
/* 304 */       deviceState.language = 3;
/* 305 */       break;
/*     */     case "fr": 
/* 307 */       deviceState.language = 4;
/* 308 */       break;
/*     */     case "ca": 
/*     */     case "es": 
/* 311 */       deviceState.language = 5;
/* 312 */       break;
/*     */     case "pt": 
/* 314 */       deviceState.language = 6;
/* 315 */       break;
/*     */     case "de": 
/* 317 */       deviceState.language = 7;
/* 318 */       break;
/*     */     case "ja": 
/* 320 */       deviceState.language = 8;
/* 321 */       break;
/*     */     case "pl": 
/* 323 */       deviceState.language = 9;
/* 324 */       break;
/*     */     case "it": 
/* 326 */       deviceState.language = 10;
/* 327 */       break;
/*     */     case "ro": 
/* 329 */       deviceState.language = 11;
/*     */     }
/*     */     
/* 332 */     int screenLight = deviceState.screenLight;
/* 333 */     int screenTime = deviceState.screenTime;
/* 334 */     int theme = deviceState.theme;
/* 335 */     int language = deviceState.language;
/* 336 */     int unit = deviceState.unit;
/* 337 */     int timeFormat = deviceState.timeFormat;
/* 338 */     int uphand = deviceState.upHander;
/* 339 */     int appnotice = deviceState.isNotice;
/* 340 */     write(CmdHelper.setDeviceState(screenLight, screenTime, theme, language, unit, timeFormat, uphand, appnotice, deviceState.handHabits), callback);
/*     */   }
/*     */   
/*     */   public static void setDeviceData(BleCallback callback) {
/* 344 */     BleManager.getInstance().enqueue(CmdHelper.setDeviceData(), callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void getStepOrSleepHistory(int year, int month, int day, BleCallback callback)
/*     */   {
/* 355 */     BleManager.getInstance().enqueue(200, CmdHelper.getHistoryData(1, year, month, day), callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void setLongSit(LongSit longSit, BleCallback callback)
/*     */   {
/* 364 */     if (!isCanSend(callback)) {
/* 365 */       return;
/*     */     }
/* 367 */     write(CmdHelper.setLongSit(longSit), callback);
/*     */   }
/*     */   
/*     */   public static void getLongSit(BleCallback bleCallback) {
/* 371 */     if (!isCanSend(bleCallback)) {
/* 372 */       return;
/*     */     }
/* 374 */     BleManager.getInstance().enqueue(CmdHelper.CMD_GET_LONGSIT, bleCallback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void setAlarm(List<Alarm> alarms, BleCallback callback)
/*     */   {
/* 384 */     if (!isCanSend(callback)) {
/* 385 */       return;
/*     */     }
/* 387 */     List<byte[]> datas = CmdHelper.setAlarm(alarms);
/* 388 */     if (datas.size() > 1) {
/* 389 */       for (int i = 0; i < datas.size(); i++) {
/* 390 */         if (i == 0) {
/* 391 */           write(1, (byte[])datas.get(i), callback);
/* 392 */         } else if (i == datas.size() - 1) {
/* 393 */           write(3, (byte[])datas.get(i), callback);
/*     */         } else {
/* 395 */           write(2, (byte[])datas.get(i), callback);
/*     */         }
/*     */       }
/*     */     } else {
/* 399 */       write(3, (byte[])datas.get(0), callback);
/*     */     }
/*     */   }
/*     */   
/*     */   public static void getAlarmList(BleCallback bleCallback) {
/* 404 */     if (!isCanSend(bleCallback)) {
/* 405 */       return;
/*     */     }
/* 407 */     BleManager.getInstance().enqueue(200, CmdHelper.CMD_GET_ALARM, bleCallback);
/*     */   }
/*     */   
/*     */   public static void getNotice(BleCallback callback) {
/* 411 */     if (!isCanSend(callback)) {
/* 412 */       return;
/*     */     }
/* 414 */     BleManager.getInstance().enqueue(CmdHelper.CMD_GET_NOTICE, callback);
/*     */   }
/*     */   
/* 417 */   public static void setNotice(AppNotice notice, BleCallback callback) { if (!isCanSend(callback)) {
/* 418 */       return;
/*     */     }
/* 420 */     write(CmdHelper.setNotice(notice), callback);
/*     */   }
/*     */   
/* 423 */   public static void clearHeartData(BleCallback callback) { if (!isCanSend(callback)) {
/* 424 */       return;
/*     */     }
/* 426 */     write(CmdHelper.clearHeartData(), callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void setHeartRange(int state, int maxHr, BleCallback callback)
/*     */   {
/* 441 */     if (!isCanSend(callback)) {
/* 442 */       return;
/*     */     }
/*     */     
/* 445 */     write(CmdHelper.setHeartRange(state, maxHr, 0), callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public static void getTarget(BleCallback callback)
/*     */   {
/* 452 */     if (!isCanSend(callback)) {
/* 453 */       return;
/*     */     }
/* 455 */     BleManager.getInstance().enqueue(CmdHelper.CMD_GET_TARGE, callback);
/*     */   }
/*     */   
/*     */   public static void setTarget(Goal goal, BleCallback callback) {
/* 459 */     if (!isCanSend(callback)) {
/* 460 */       return;
/*     */     }
/* 462 */     write(CmdHelper.setTarget(goal.sleepstate, goal.goalSleep, goal.stepstate, goal.goalStep, goal.calstate, goal.goalCal, goal.distancestate, goal.goalDistanceKm), callback);
/*     */   }
/*     */   
/*     */   public static void setPairingcode(int i1, int i2, int i3, int i4, BleCallback callback) {
/* 466 */     if (!isCanSend(callback)) {
/* 467 */       return;
/*     */     }
/* 469 */     write(CmdHelper.setPairing(i1, i2, i3, i4), callback);
/*     */   }
/*     */   
/*     */   public static void exitPairingcode(boolean isRight, BleCallback callback) {
/* 473 */     if (!isCanSend(callback)) {
/* 474 */       return;
/*     */     }
/* 476 */     write(CmdHelper.exitPairing(isRight), callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void setMessage(int type, String title, String message, BleCallback callback)
/*     */   {
/* 487 */     if (!isCanSend(callback)) {
/* 488 */       return;
/*     */     }
/* 490 */     write(1, CmdHelper.setMessageType(type), callback);
/* 491 */     List<byte[]> datas = CmdHelper.setMessage2(1, title);
/* 492 */     for (int i = 0; i < datas.size(); i++) {
/* 493 */       write(2, (byte[])datas.get(i), callback);
/*     */     }
/* 495 */     List<byte[]> datas2 = CmdHelper.setMessage2(2, message);
/* 496 */     for (int i = 0; i < datas2.size(); i++) {
/* 497 */       write(2, (byte[])datas2.get(i), callback);
/*     */     }
/* 499 */     write(3, CmdHelper.END_MESSAGE, callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static Context mContext;
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void getHistoryData(int type, int year, int month, int day, BleCallback callback)
/*     */   {
/* 613 */     write(CmdHelper.getHistoryData(type, year, month, day), callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void getHistoryHeartRateData(int year, int month, int day, BleCallback callback)
/*     */   {
/* 623 */     write(200, CmdHelper.getHistoryHeartRateData(1, year, month, day), callback);
/*     */   }
/*     */   
/*     */   public static void getHartRong(BleCallback callback) {
/* 627 */     if (!isCanSend(callback)) {
/* 628 */       return;
/*     */     }
/* 630 */     BleManager.getInstance().enqueue(CmdHelper.CMD_GET_HART_RONG, callback);
/*     */   }
/*     */   
/*     */   public static void getHeartOpen(BleCallback callback) {
/* 634 */     if (!isCanSend(callback)) {
/* 635 */       return;
/*     */     }
/* 637 */     BleManager.getInstance().enqueue(CmdHelper.CMD_GET_HART_OPEN, callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void enterUpdate(BleCallback callback)
/*     */   {
/* 646 */     if (!isCanSend(callback)) {
/* 647 */       return;
/*     */     }
/* 649 */     write(CmdHelper.enterUpdate(), callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public static void enterCamare(BleCallback callback)
/*     */   {
/* 656 */     if (!isCanSend(callback)) {
/* 657 */       return;
/*     */     }
/* 659 */     write(CmdHelper.controlDeviceCamare(1), callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public static void camare(BleCallback callback)
/*     */   {
/* 666 */     if (!isCanSend(callback)) {
/* 667 */       return;
/*     */     }
/* 669 */     write(CmdHelper.controlDeviceCamare(2), callback);
/*     */   }
/*     */   
/* 672 */   public static void exitCamare(BleCallback callback) { if (!isCanSend(callback)) {
/* 673 */       return;
/*     */     }
/* 675 */     write(CmdHelper.controlDeviceCamare(3), callback);
/*     */   }
/*     */   
/* 678 */   public static void musicControl(BleCallback callback) { if (!isCanSend(callback)) {
/* 679 */       return;
/*     */     }
/* 681 */     write(CmdHelper.controlMusic(1), callback);
/*     */   }
/*     */   
/* 684 */   public static void preMusic(BleCallback callback) { if (!isCanSend(callback)) {
/* 685 */       return;
/*     */     }
/* 687 */     write(CmdHelper.controlMusic(2), callback);
/*     */   }
/*     */   
/* 690 */   public static void nextMusic(BleCallback callback) { if (!isCanSend(callback)) {
/* 691 */       return;
/*     */     }
/* 693 */     write(CmdHelper.controlMusic(3), callback);
/*     */   }
/*     */   
/* 696 */   public static void stopMusic(BleCallback callback) { if (!isCanSend(callback)) {
/* 697 */       return;
/*     */     }
/* 699 */     write(CmdHelper.controlMusic(4), callback);
/*     */   }
/*     */   
/* 702 */   public static void addVol(BleCallback callback) { if (!isCanSend(callback)) {
/* 703 */       return;
/*     */     }
/* 705 */     write(CmdHelper.controlMusic(5), callback);
/*     */   }
/*     */   
/* 708 */   public static void subVol(BleCallback callback) { if (!isCanSend(callback)) {
/* 709 */       return;
/*     */     }
/* 711 */     write(CmdHelper.controlMusic(6), callback);
/*     */   }
/*     */   
/* 714 */   public static void findPhone(BleCallback callback) { if (!isCanSend(callback)) {
/* 715 */       return;
/*     */     }
/* 717 */     write(CmdHelper.findDevice(), callback);
/*     */   }
/*     */   
/* 720 */   public static void callComing(BleCallback callback) { if (!isCanSend(callback)) {
/* 721 */       return;
/*     */     }
/* 723 */     write(CmdHelper.controlDeviceCall(1), callback);
/*     */   }
/*     */   
/* 726 */   public static void callDisturb(BleCallback callback) { if (!isCanSend(callback)) {
/* 727 */       return;
/*     */     }
/* 729 */     write(CmdHelper.controlDeviceCall(2), callback);
/*     */   }
/*     */   
/*     */   public static void sos(BleCallback callback) {
/* 733 */     if (!isCanSend(callback)) {
/* 734 */       return;
/*     */     }
/* 736 */     write(CmdHelper.controlDeviceSos(1), callback);
/*     */   }
/*     */   
/* 739 */   public static boolean isInitCon() { return BleManager.getInstance().isInitCon; }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   public static void clearSportData(BleCallback callback)
/*     */   {
/* 746 */     if (!isCanSend(callback)) {
/* 747 */       return;
/*     */     }
/* 749 */     write(CmdHelper.clearDeviceData(1), callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public static void recoverSet(BleCallback callback)
/*     */   {
/* 756 */     if (!isCanSend(callback)) {
/* 757 */       return;
/*     */     }
/* 759 */     write(CmdHelper.clearDeviceData(2), callback);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   public static void rebootDevice(BleCallback callback)
/*     */   {
/* 766 */     if (!isCanSend(callback)) {
/* 767 */       return;
/*     */     }
/* 769 */     write(CmdHelper.clearDeviceData(3), callback);
/*     */   }
/*     */   
/*     */   public static void clearActivity(BleCallback callback) {
/* 773 */     if (!isCanSend(callback)) {
/* 774 */       return;
/*     */     }
/* 776 */     BleManager.getInstance().enqueue(CmdHelper.CMD_CLEAR_ACTIVITY, callback);
/*     */   }
/*     */   
/*     */ 
/*     */   public static void setBloodPressureAdjustPara(int shrinkValue, int diastolicValue) {}
/*     */   
/*     */ 
/*     */   public static void queryBloodPressureAdjustResult() {}
/*     */   
/*     */ 
/*     */   public static void startMeasureBloodPressure() {}
/*     */   
/*     */ 
/*     */   public static void getBloodPressureData() {}
/*     */   
/*     */   public static void endMeasureBloodPressed() {}
/*     */   
/*     */   public static void bindDevice(BleCallback callback)
/*     */   {
/* 795 */     if (!isCanSend(callback)) {
/* 796 */       return;
/*     */     }
/* 798 */     BleManager.getInstance().enqueue(CmdHelper.getBind(), callback);
/*     */   }
/*     */   
/*     */   public static boolean isBind() {
/* 802 */     if (SPHelper.getBindBLEDevice(mContext) == null) {
/* 803 */       return false;
/*     */     }
/* 805 */     return true;
/*     */   }
/*     */   
/*     */   public static void unBind()
/*     */   {
/* 810 */     SPHelper.cleanBLEDevice(mContext);
/*     */     
/* 812 */     disConnect();
/*     */   }
/*     */ }


/* Location:              /home/enjoy/code/EnjoyWorkSpace/SmartWristband/app/libs/classes.jar!/com/zhj/bluetooth/zhjbluetoothsdk/ble/BleSdkWrapper.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */