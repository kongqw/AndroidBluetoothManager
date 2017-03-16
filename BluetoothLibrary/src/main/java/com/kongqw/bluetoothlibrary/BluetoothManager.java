package com.kongqw.bluetoothlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.kongqw.bluetoothlibrary.listener.OnBluetoothStateListener;
import com.kongqw.bluetoothlibrary.listener.OnDiscoveryDeviceListener;

import java.util.Set;

/**
 * Created by kqw on 2016/7/27.
 * 蓝牙管理器
 */
public class BluetoothManager {

    private static final String TAG = "BluetoothManager";
    private final BluetoothAdapter mBluetoothAdapter;

    public BluetoothManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    /**
     * 开启蓝牙
     */
    public void openBluetooth() {
        if (null != mBluetoothAdapter) {
            mBluetoothAdapter.enable();
        }
    }

    /**
     * 关闭蓝牙
     */
    public void closeBluetooth() {
        if (null != mBluetoothAdapter) {
            mBluetoothAdapter.disable();
        }
    }

    /**
     * 获取蓝牙名字
     *
     * @return 蓝牙名字
     */
    public String getName() {
        if (null != mBluetoothAdapter) {
            return mBluetoothAdapter.getName();
        }
        return null;
    }

    /**
     * 设置蓝牙名字
     *
     * @param name 蓝牙名字
     * @return 设置是否成功
     */
    public boolean setName(String name) {
        return !TextUtils.isEmpty(name) && null != mBluetoothAdapter && mBluetoothAdapter.setName(name);
    }

    // Android 6.0 以后返回 "02:00:00:00:00:00"
    //    /**
    //     * 获取蓝牙MAC地址
    //     *
    //     * @return 蓝牙MAC地址
    //     */
    //    public String getAddress() {
    //        if (null != mBluetoothAdapter) {
    //            return mBluetoothAdapter.getAddress();
    //        }
    //        return null;
    //    }

    /**
     * 获取设置设备可见时间的Intent
     *
     * @param current [0 ~ 120] 可见时间
     * @return Intent
     */
    public Intent getDurationIntent(int current) {
        Intent duration = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        duration.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, current);
        duration.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return duration;
    }

    /**
     * 获取已经绑定的设备
     *
     * @return 已经绑定过的设备
     */
    public Set<BluetoothDevice> getBondedDevices() {
        if (null != mBluetoothAdapter) {
            return mBluetoothAdapter.getBondedDevices();
        }
        return null;
    }

    /**
     * 扫描附近可用的蓝牙设备
     *
     * @return 是否正常开始扫描
     */
    public boolean discovery() {
        if (null != mBluetoothAdapter) {
            if (mBluetoothAdapter.isDiscovering()) {
                // 取消之前的扫描 重新开始扫描设备
                return mBluetoothAdapter.cancelDiscovery() && mBluetoothAdapter.startDiscovery();
            } else {
                // 开始扫描设备
                return mBluetoothAdapter.startDiscovery();
            }
        }
        return false;
    }

    /**
     * 是否正在搜索附近的蓝牙设备
     *
     * @return 是否正在搜索
     */
    public boolean isDiscovering() {
        return null != mBluetoothAdapter && mBluetoothAdapter.isDiscovering();
    }

    /**
     * 获取蓝牙的开关状态
     *
     * @return 蓝牙的开关状态
     */
    public boolean isEnabled() {
        return null != mBluetoothAdapter && mBluetoothAdapter.isEnabled();
    }

    /**
     * Created by kqw on 2016/8/2.
     * 蓝牙的广播接收者
     */
    public static class FoundDeviceBroadcastReceiver extends BroadcastReceiver {

        private static final String TAG = "FoundDeviceBroadcast";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    // 获取设备
                    BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.i(TAG, "onReceive: 发现新设备 : " + btDevice.getName() + "  MAC Address : " + btDevice.getAddress());
                    if (null != mOnDiscoveryDeviceListener) {
                        mOnDiscoveryDeviceListener.onDiscoveryDeviceFound(btDevice);
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.i(TAG, "onReceive: 开始附近的蓝牙设备搜索");
                    if (null != mOnDiscoveryDeviceListener) {
                        mOnDiscoveryDeviceListener.onDiscoveryDeviceStarted();
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.i(TAG, "onReceive: 结束附近的蓝牙设备搜索");
                    if (null != mOnDiscoveryDeviceListener) {
                        mOnDiscoveryDeviceListener.onDiscoveryDeviceFinished();
                    }
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.i(TAG, "onReceive: 正在断开蓝牙...");
                            if (null != mOnBluetoothStateListener) {
                                mOnBluetoothStateListener.onBluetoothStateTurningOff();
                            }
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            Log.i(TAG, "onReceive: 蓝牙已经断开");
                            if (null != mOnBluetoothStateListener) {
                                mOnBluetoothStateListener.onBluetoothStateOff();
                            }
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.i(TAG, "onReceive: 正在打开蓝牙...");
                            if (null != mOnBluetoothStateListener) {
                                mOnBluetoothStateListener.onBluetoothStateTurningOn();
                            }
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Log.i(TAG, "onReceive: 蓝牙已经打开");
                            if (null != mOnBluetoothStateListener) {
                                mOnBluetoothStateListener.onBluetoothStateOn();
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static OnDiscoveryDeviceListener mOnDiscoveryDeviceListener;

    public void setOnDiscoveryDeviceListener(OnDiscoveryDeviceListener listener) {
        mOnDiscoveryDeviceListener = listener;
    }

    public void removeOnDiscoveryDeviceListener() {
        mOnDiscoveryDeviceListener = null;
    }

    private static OnBluetoothStateListener mOnBluetoothStateListener;

    public void setOnBluetoothStateListener(OnBluetoothStateListener listener) {
        mOnBluetoothStateListener = listener;
    }

    public void removeOnBluetoothStateListener() {
        mOnBluetoothStateListener = null;
    }
}
