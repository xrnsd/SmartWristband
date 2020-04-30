package com.kuyou.smartwristband.presenter;

import com.kuyou.smartwristband.base.IBaseView;
import com.zhj.bluetooth.zhjbluetoothsdk.bean.BLEDevice;


/**
 * Created by Administrator on 2019/7/8.
 */

public interface ScanDeviceContract {
    interface View extends IBaseView {
        void requestSuccess(int code, BLEDevice device);
        void requestFaild();
    }
    interface Presenter {
        void startScanBle(int cmd);
        void stopScanBle();
        void connecting(int cmd, BLEDevice device);
    }
}
