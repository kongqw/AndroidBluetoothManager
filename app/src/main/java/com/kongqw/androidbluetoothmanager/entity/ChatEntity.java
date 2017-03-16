package com.kongqw.androidbluetoothmanager.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kongqingwei on 2017/3/16.
 * 聊天内容实体
 */
public class ChatEntity {

    private boolean isSend;

    private String time;

    private String bluetoothName;

    private String bluetoothMacAddress;

    private String message;

    public ChatEntity(boolean isSend, String bluetoothName, String bluetoothMacAddress, String message) {
        this.isSend = isSend;
        this.bluetoothName = bluetoothName;
        this.bluetoothMacAddress = bluetoothMacAddress;
        this.message = message;
        this.time = SimpleDateFormat.getTimeInstance(SimpleDateFormat.FULL).format(new Date().getTime());
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getBluetoothName() {
        return bluetoothName;
    }

    public void setBluetoothName(String bluetoothName) {
        this.bluetoothName = bluetoothName;
    }

    public String getBluetoothMacAddress() {
        return bluetoothMacAddress;
    }

    public void setBluetoothMacAddress(String bluetoothMacAddress) {
        this.bluetoothMacAddress = bluetoothMacAddress;
    }

    @Override
    public String toString() {
        return "ChatEntity{" +
                "isSend=" + isSend +
                ", time='" + time + '\'' +
                ", bluetoothName='" + bluetoothName + '\'' +
                ", bluetoothMacAddress='" + bluetoothMacAddress + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
