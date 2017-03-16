package com.kongqw.bluetoothlibrary.listener;

/**
 * Created by kqw on 2016/8/2.
 * 蓝牙消息传递的回调
 */
public interface OnMessageListener {

    // 发送消息
    void onSend(String message);

    // 接收消息
    void onRead(String message);
}
