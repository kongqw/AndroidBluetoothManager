package com.kongqw.bluetoothlibrary.engine;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.kongqw.bluetoothlibrary.listener.OnMessageListener;

import java.util.UUID;

/**
 * Created by kongqingwei on 2017/1/10.
 * Bluetooth
 */

public abstract class Bluetooth {

    private static final String TAG = "Bluetooth";

    protected abstract UUID onSecureUuid();

    protected abstract UUID onInsecureUuid();

    // Name for the SDP record when creating server socket
    static final String NAME_SECURE = "BluetoothChatSecure";
    static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    // protected static UUID UUID_SECURE = UUID.fromString("f291a307-6ee4-4bec-8f1a-1a1bdb8b3844");
    // protected static UUID UUID_INSECURE = UUID.fromString("dae8ce34-29de-499f-bc12-be586ccf3881");
    UUID UUID_SECURE;
    UUID UUID_INSECURE;

    // 保存蓝牙状态
    private static int mState;
    // 蓝牙状态
    static final int STATE_NONE = 0;       // 什么都没做
    static final int STATE_LISTEN = 1;     // 服务端等待客户端连接
    static final int STATE_CONNECTING = 2; // 正在连接
    static final int STATE_CONNECTED = 3;  // 已经连接

    final BluetoothAdapter mAdapter;

    private OnMessageListener mOnMessageListener;

    private static final int IO_WRITE = 0;
    private static final int IO_READ = 1;
    private Handler mIOHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case IO_READ:
                    String read = (String) msg.obj;
                    Log.i(TAG, "handleMessage [接收]: " + read);
                    if (null != mOnMessageListener) {
                        mOnMessageListener.onRead(read);
                    }
                    break;
                case IO_WRITE:
                    String send = (String) msg.obj;
                    Log.i(TAG, "handleMessage: [发送]" + send);
                    if (null != mOnMessageListener) {
                        mOnMessageListener.onSend(send);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 构造方法
     */
    Bluetooth() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;

        UUID_SECURE = onSecureUuid();
        UUID_INSECURE = onInsecureUuid();
    }

    /**
     * 设置蓝牙连接状态
     *
     * @param state 状态
     */
    synchronized void setState(int state) {
        Log.i(TAG, "setState: " + mState + " -> " + state);
        // 设置蓝牙状态
        mState = state;
    }


    /**
     * 返回蓝牙连接状态
     *
     * @return 连接状态
     */
    synchronized int getState() {
        return mState;
    }


    /**
     * 添加消息传递的回调
     *
     * @param listener 回调接口
     */
    public void setOnMessageListener(OnMessageListener listener) {
        mOnMessageListener = listener;
    }

    /**
     * 移除消息监听
     */
    public void removeOnMessageListener() {
        mOnMessageListener = null;
    }


    /**
     * 发送数据
     *
     * @param send 发送的数据
     */
    void onSend(String send) {
        Message message = Message.obtain();
        message.what = IO_WRITE;
        message.obj = send;
        mIOHandler.sendMessage(message);
    }

    /**
     * 收到消息
     *
     * @param read 读到的数据
     */
    void onRead(String read) {
        Message message = Message.obtain();
        message.what = IO_READ;
        message.obj = read;
        mIOHandler.sendMessage(message);
    }
}
