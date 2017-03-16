package com.kongqw.bluetoothlibrary.listener;

import android.bluetooth.BluetoothDevice;

/**
 * Created by kongqingwei on 2017/3/14.
 * 扫描附近的蓝牙设备的监听
 */

public interface OnDiscoveryDeviceListener {

    /**
     * 开始扫描附近的蓝牙设备
     */
    void onDiscoveryDeviceStarted();

    /**
     * 发现蓝牙设备
     *
     * @param device 蓝牙设备
     */
    void onDiscoveryDeviceFound(BluetoothDevice device);

    /**
     * 扫描附近的蓝牙设备完成
     */
    void onDiscoveryDeviceFinished();
}
