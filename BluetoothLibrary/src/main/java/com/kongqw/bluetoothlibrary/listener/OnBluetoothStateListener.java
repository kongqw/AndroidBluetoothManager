package com.kongqw.bluetoothlibrary.listener;


/**
 * Created by kongqingwei on 2017/3/15.
 * 蓝牙开关状态的监听接口
 */
public interface OnBluetoothStateListener {

    /**
     * 正在关闭蓝牙的回调
     */
    void onBluetoothStateTurningOff();

    /**
     * 蓝牙断开的回调
     */
    void onBluetoothStateOff();

    /**
     * 正在打开蓝牙的回调
     */
    void onBluetoothStateTurningOn();

    /**
     * 蓝牙打开的回调
     */
    void onBluetoothStateOn();
}
