package com.kuyou.smartwristband.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.kuyou.smartwristband.base.BaseAdapter;
import com.kuyou.smartwristband.base.BaseViewHolder;
import com.kuyou.smartwristband.R;
import com.zhj.bluetooth.zhjbluetoothsdk.bean.BLEDevice;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Administrator on 2019/7/10.
 */

public class ScanDeviceAdapter extends BaseAdapter<BLEDevice,ScanDeviceAdapter.ViewHolder> {
    public ScanDeviceAdapter(Context mContext, List<BLEDevice> mList) {
        super(mContext, mList);
    }

    public void setData(List<BLEDevice> mList){
        this.mList = mList;
        notifyDataSetChanged();
    }
    @Override
    protected void onNormalBindViewHolder(ScanDeviceAdapter.ViewHolder holder, BLEDevice itemBean, int position) {
        if(position == connPosition){
            holder.tvState.setVisibility(View.GONE);
            holder.tvConnect.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.progress_drawable);
            holder.tvConnect.startAnimation(animation);//開始动画
        }else{
            holder.tvConnect.setVisibility(View.GONE);
            holder.tvState.setVisibility(View.VISIBLE);

            if(Math.abs(itemBean.mRssi) <= 70){
                holder.tvState.setImageResource(R.mipmap.device_rssi_1);
            }else if(Math.abs(itemBean.mRssi) <= 90){
                holder.tvState.setImageResource(R.mipmap.device_rssi_2);
            }else{
                holder.tvState.setImageResource(R.mipmap.device_rssi_3);
            }
        }
        holder.tvDeviceName.setText(itemBean.mDeviceName);
        holder.tvMac.setText(itemBean.mDeviceAddress);
        holder.layoutItem.setOnClickListener(v -> mOnItemClickListener.onItemClick(holder.layoutItem,position));
    }

    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.item_scan_device,parent,false);
        return new ViewHolder(view);
    }

    private int connPosition = -1;
    public void connecting(int position) {
        connPosition = position;
        notifyDataSetChanged();
    }

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tvDeviceName)
        TextView tvDeviceName;
        @BindView(R.id.tvMac)
        TextView tvMac;
        @BindView(R.id.layoutItem)
        RelativeLayout layoutItem;
        @BindView(R.id.tvState)
        ImageView tvState;
        @BindView(R.id.tvConnect)
        ImageView tvConnect;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
