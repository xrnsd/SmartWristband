package com.kuyou.smartwristband.ui;

import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kuyou.smartwristband.base.BaseAdapter;
import com.kuyou.smartwristband.base.BaseMvpActivity;
import com.kuyou.smartwristband.presenter.ScanDeviceContract;
import com.kuyou.smartwristband.presenter.ScanDevicePresenter;
import com.kuyou.smartwristband.utils.CommonUtil;
import com.kuyou.smartwristband.utils.DialogHelperNew;
import com.kuyou.smartwristband.R;
import com.zhj.bluetooth.zhjbluetoothsdk.bean.BLEDevice;
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleScanTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;

public class ScanDeviceReadyActivity extends BaseMvpActivity<ScanDevicePresenter> implements
        ScanDeviceContract.View,
        BaseAdapter.OnItemClickListener{

    public static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 100;
    public static final int CMD_SCAN = 0X00;
    public static final int CMD_CONNECTING = 0X01;
    private static final int AUTO_RE_REQUEST_COUNT_MAX=2;

    @BindView(R.id.refresh_recyclerView)
    RecyclerView mRecyclerView;

    private ScanDeviceAdapter mAdapter;
    private boolean isConnecting = false;

    @Override
    protected int getContentView() {
        return R.layout.activity_scan_device;
    }

    @Override
    protected void initViews() {
        super.initViews();
        titleName.setText("设备连接");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (!CommonUtil.isOPen(this)){
            DialogHelperNew.showRemindDialog(this, getResources().getString(R.string.permisson_location_title),
                    getResources().getString(R.string.permisson_location_tips), getResources().getString(R.string.permisson_location_open), view -> {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 1000);
                    }, view -> ScanDeviceReadyActivity.this.finish());
        }else{
            getScanDevice();
        }
    }

    private void getScanDevice(){
        getRecyclerRefreshLayout().post(() -> {
            getRecyclerRefreshLayout().setRefreshing(true);
            showList.clear();
            if(mAdapter != null){
                mAdapter.setData(showList);
            }
            //@{ added by wgx Usefulness:
            mHandlerMain.removeCallbacks(mRequestTimeOut);
            mHandlerMain.postDelayed(mRequestTimeOut,REQUESTT_IMEOUT_SIZE);
            //}@ end wgx
            mPresenter.startScanBle(CMD_SCAN);
        });
    }

    private List<BLEDevice> showList = new ArrayList<>();

    @Override
    public void requestSuccess(int code, BLEDevice device) {
        switch (code){
            case CMD_SCAN:
                //@{ added by wgx Usefulness:
                mHandlerMain.removeCallbacks(mRequestTimeOut);
                isEnbaleAutoRequest=true;
                //}@ end wgx
                getRecyclerRefreshLayout().onComplete();
                if(!showList.contains(device)){
                    showList.add(device);
                    Collections.sort(showList);
                    if(mAdapter == null){
                        mAdapter = new ScanDeviceAdapter(this,showList);
                        mRecyclerView.setAdapter(mAdapter);
                    }else{
                        mAdapter.setData(showList);
                    }
                    mAdapter.setOnItemClickListener(this);
                }
                
                Log.d(TAG,"requestSuccess>CMD_SCAN");
                break;
            case CMD_CONNECTING:
                isConnecting = false;
                ScanDeviceReadyActivity.this.finish();
                break;
        }
    }

    @Override
    public void requestFaild() {
        if(mAdapter != null){
            isConnecting = false;
            mAdapter.connecting(-1);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();;
        mPresenter.stopScanBle();
        hideLoading();
        finish();
    }

    @Override
    public void onItemClick(View view, int position) {
        //链接设备
        if (!BleScanTool.getInstance().isBluetoothOpen()){
            showToast("蓝牙未打开");
        }else{
            if(!isConnecting){
                mAdapter.connecting(position);
                isConnecting = true;
                mPresenter.connecting(CMD_CONNECTING,showList.get(position));
            }
        }
    }

    @Override
    public void onRefreshing() {
        super.onRefreshing();
        if(isConnecting){
            showToast("正在绑定好");
        }else{
            if(!BleScanTool.getInstance().isBluetoothOpen())
                getRecyclerRefreshLayout().onComplete();
            if(!BleScanTool.getInstance().isBluetoothOpen()){
               showToast(getString(R.string.blurtooth_use_cntent));
               finish();
               return;
            }
            getScanDevice();
        }
    }

    @Override
    public void onLoadMore() {
        super.onLoadMore();
    }

    @Override
    public void onBackPressed() {
        if(isConnecting){
            showToast("正在绑定");
        }else{
            this.finish();
        }
    }
    //@{ added by wgx Usefulness:修复手动刷新没有超时
    private static final int REQUESTT_IMEOUT_SIZE = 5000;
    private Handler mHandlerMain=new Handler();
    private boolean isEnbaleAutoRequest=true;

    Runnable mRequestTimeOut = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"mRequestTimeOut>run");
            mPresenter.stopScanBle();
            if(isEnbaleAutoRequest){
                isEnbaleAutoRequest=false;
                getScanDevice();
                return;
            }
            getRecyclerRefreshLayout().onComplete();
            hideLoading();
            showToast(getString(R.string.request_timeout));
            onBackPressed();
        }
    };
    //added by wgx  Usefulness:
    @Override
    protected void onBluetoothOff() {
        super.onBluetoothOff();
        mHandlerMain.removeCallbacks(mRequestTimeOut);
        showToast("蓝牙已关闭,停止搜索");
        finish();
    }
    //}@ end wgx

}
