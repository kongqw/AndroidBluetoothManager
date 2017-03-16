package com.kongqw.androidbluetoothmanager.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kongqw.androidbluetoothmanager.R;

import java.util.ArrayList;

/**
 * Created by kongqingwei on 2017/3/15.
 * 蓝牙列表适配器
 */

public class BluetoothAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private final LayoutInflater mLayoutInflater;

    public void addBluetoothDevice(BluetoothDevice device) {
        if (null == devices) {
            devices = new ArrayList<>();
        }
        // 添加设备
        devices.add(device);
        // 刷新列表
        notifyDataSetChanged();
    }

    public void clearDevices(){
        devices.clear();
        // 刷新列表
        notifyDataSetChanged();
    }

    public BluetoothAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (null == convertView) {
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.item_bluetooth_device, null);
            holder.bluetoothName = (TextView) convertView.findViewById(R.id.tv_bluetooth_name);
            holder.bluetoothMacAddress = (TextView) convertView.findViewById(R.id.tv_bluetooth_mac_address);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 蓝牙名称
        holder.bluetoothName.setText(getItem(position).getName());
        holder.bluetoothMacAddress.setText(getItem(position).getAddress());

        return convertView;
    }

    private final class ViewHolder {
        TextView bluetoothName;
        TextView bluetoothMacAddress;
    }
}
