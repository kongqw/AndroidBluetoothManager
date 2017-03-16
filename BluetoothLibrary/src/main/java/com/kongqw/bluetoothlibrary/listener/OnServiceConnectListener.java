package com.kongqw.bluetoothlibrary.listener;

import android.bluetooth.BluetoothDevice;

/**
 * Created by kqw on 2016/8/2.
 * 蓝牙连接的回调
 */
public interface OnServiceConnectListener {

    // 等待连接
    void onConnectListening();

    // 蓝牙连接成功
    void onConnectSuccess(BluetoothDevice device);

    // 连接失败
    void onConnectFail(Exception e);

    // 连接中断
    void onConnectLost(Exception e);
}
