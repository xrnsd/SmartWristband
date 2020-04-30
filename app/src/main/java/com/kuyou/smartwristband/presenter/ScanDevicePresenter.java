package com.kuyou.smartwristband.presenter;

import android.bluetooth.BluetoothGatt;
import android.util.Log;

import com.kuyou.smartwristband.base.BaseAppcation;
import com.kuyou.smartwristband.base.BasePersenter;
import com.zhj.bluetooth.zhjbluetoothsdk.bean.BLEDevice;
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BaseAppBleListener;
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleScanTool;
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper;
import com.zhj.bluetooth.zhjbluetoothsdk.util.SPHelper;

import static com.kuyou.smartwristband.ui.ScanDeviceReadyActivity.CMD_CONNECTING;
import static com.kuyou.smartwristband.ui.ScanDeviceReadyActivity.CMD_SCAN;


/**
 * Created by Administrator on 2019/7/8.
 */

public class ScanDevicePresenter extends BasePersenter<ScanDeviceContract.View> implements ScanDeviceContract.Presenter{
    private static final String TAG = "ScanDevicePresenter";

    @Override
    public void startScanBle(int cmd) {
        if (BleSdkWrapper.isConnected()) {
            BleSdkWrapper.disConnect();
        }
        BleSdkWrapper.startScanDevices(scanCallback);
    }

    @Override
    public void stopScanBle() {
        BleSdkWrapper.stopScanDevices();
        BleScanTool.getInstance().removeScanDeviceListener(scanCallback);
    }

    private BLEDevice connectDevice;
    @Override
    public void connecting(int cmd,BLEDevice device) {
        if (device == null) {
            return;
        }
        connectDevice = device;
        stopScanBle();
        if (BleSdkWrapper.isConnected()) {
            BleSdkWrapper.disConnect();
        }
        BleSdkWrapper.setBleListener(baseAppBleListener);
        BleSdkWrapper.connect(device);
    }

    private BaseAppBleListener baseAppBleListener = new BaseAppBleListener(){
        @Override
        public void onBLEConnected(BluetoothGatt bluetoothGatt) {
            super.onBLEConnected(bluetoothGatt);
        }

        @Override
        public void initComplete() {
            super.initComplete();
            SPHelper.saveBLEDevice(BaseAppcation.getInstance(),connectDevice);
            mView.showMsg("连接成功");
            mView.requestSuccess(CMD_CONNECTING,connectDevice);
            BleSdkWrapper.removeListener(baseAppBleListener);
        }

        @Override
        public void onBLEDisConnected(String s) {
            super.onBLEDisConnected(s);
            BleSdkWrapper.removeListener(baseAppBleListener);
            mView.showMsg("连接失败："+s);
            mView.requestFaild();
        }

        @Override
        public void onBLEConnectTimeOut() {
            super.onBLEConnectTimeOut();
            BleSdkWrapper.removeListener(baseAppBleListener);
            mView.showMsg("连接超时");
            mView.requestFaild();
        }
    };

    private  BleScanTool.ScanDeviceListener scanCallback=new BleScanTool.ScanDeviceListener() {
        @Override
        public void onFind(BLEDevice device) {
            if(null==device){
                Log.e(TAG,"ScanDevice > onFind BLEDevice is null");
                return;
            }
            mView.requestSuccess(CMD_SCAN,device);
        }

        @Override
        public void onFinish() {
            BleSdkWrapper.stopScanDevices();
        }
    };

    @Override
    public void detachView() {
        super.detachView();
        BleSdkWrapper.removeListener(baseAppBleListener);
        BleScanTool.getInstance().removeScanDeviceListener(scanCallback);
    }

}
