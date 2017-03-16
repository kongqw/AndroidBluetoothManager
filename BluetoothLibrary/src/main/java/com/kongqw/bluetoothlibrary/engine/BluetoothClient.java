package com.kongqw.bluetoothlibrary.engine;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.kongqw.bluetoothlibrary.listener.OnClientConnectListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 类包含了蓝牙连接，消息的收发
 */
public abstract class BluetoothClient extends Bluetooth {
    // TAG
    private static final String TAG = "BluetoothClient";

    private static ConnectThread mConnectThread;
    private static ConnectedThread mConnectedThread;

    private static final int CONNECTING = 0;
    private static final int CONNECT_SUCCESS = 1;
    private static final int CONNECT_FAIL = 2;
    private static final int CONNECT_LOST = 3;

    private OnClientConnectListener mOnClientConnectListener;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECTING: // 开始蓝牙连接
                    if (null != mOnClientConnectListener) {
                        mOnClientConnectListener.onConnecting();
                    }
                    // 修改蓝牙状态为正在连接
                    setState(STATE_CONNECTING);
                    break;
                case CONNECT_SUCCESS: // 蓝牙连接成功
                    if (null != mOnClientConnectListener) {
                        BluetoothDevice device = (BluetoothDevice) msg.obj;
                        mOnClientConnectListener.onConnectSuccess(device);
                    }
                    // 设置蓝牙状态为已经连接
                    setState(STATE_CONNECTED);
                    break;
                case CONNECT_FAIL: // 蓝牙连接失败
                    if (null != mOnClientConnectListener) {
                        IOException exception = (IOException) msg.obj;
                        mOnClientConnectListener.onConnectFail(exception);
                    }
                    // 设置蓝牙为断开状态
                    setState(STATE_NONE);
                    break;
                case CONNECT_LOST: // 蓝牙连接丢失
                    if (null != mOnClientConnectListener) {
                        IOException exception = (IOException) msg.obj;
                        mOnClientConnectListener.onConnectLost(exception);
                    }
                    // 设置蓝牙为断开状态
                    setState(STATE_NONE);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 判断是否正在连接
     *
     * @return 连接状态
     */
    public synchronized boolean isConnected() {
        return STATE_CONNECTED == getState();
    }

    /**
     * 初始化蓝牙连接
     */
    public synchronized void init() {
        // 取消之前的连接线程
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 取消之前的消息收发线程
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 设置蓝牙状态为等待连接
        setState(STATE_NONE);
    }

    /**
     * 连接蓝牙设备
     *
     * @param device 蓝牙设备
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.i(TAG, "connect: " + device);

        // 取消之前的连接线程
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 取消之前的消息收发线程
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 客户端发起连接请求
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();

        // 修改蓝牙状态为正在连接
        setState(STATE_CONNECTING);
    }

    /**
     * 蓝牙连接成功以后开启消息传递的线程
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device, String socketType) {
        Log.i(TAG, "connected, Socket Type:" + socketType);
        // 设置蓝牙状态为已经连接
        setState(STATE_CONNECTED);

        // 蓝牙连接成功
        changeConnectState(CONNECT_SUCCESS, device);

        // 关闭客户端的连接线程
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 关闭消息收发的线程
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 开启新的消息收发线程
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();
    }

    /**
     * 关闭所有线程
     */
    public synchronized void stop() {
        Log.i(TAG, "stop: ");
        // 关闭客户端连接线程
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 关闭消息收发线程
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // mBluetoothClient = null;

        // 设置蓝牙为断开连接状态
        setState(STATE_NONE);

        // 蓝牙断开
        changeConnectState(CONNECT_LOST, null);
    }

    /**
     * 发送消息
     *
     * @param out 发送的消息
     * @see ConnectedThread#write(byte[])
     */
    private void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (getState() != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    /**
     * 发送指令
     *
     * @param cmd 指令
     */
    public void send(String cmd) {
        if (!TextUtils.isEmpty(cmd)) {
            write(cmd.getBytes());
        }
    }

    /**
     * 改变蓝牙连接状态
     *
     * @param state  状态
     * @param object 传递的数据
     */
    private void changeConnectState(int state, Object object) {
        Message message = Message.obtain();
        message.what = state;
        message.obj = object;
        mHandler.sendMessage(message);
    }


    /**
     * 客户端发起连接请求的线程
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;
        private String mSocketType;

        ConnectThread(BluetoothDevice device, boolean secure) {
            BluetoothSocket tmp = null;
            try {
                mmDevice = device;
                mSocketType = secure ? "Secure" : "Insecure";
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(UUID_INSECURE);
                }
            } catch (Exception e) {
                Log.i(TAG, "Socket Type: " + mSocketType + "create() failed.\n" + e.getMessage());
            } finally {
                mmSocket = tmp;
            }
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // 停止设备扫描
            mAdapter.cancelDiscovery();

            try {
                // 正在连接...
                changeConnectState(CONNECTING, null);
                // 开始连接 阻塞线程 连接成功继续执行 连接失败抛异常
                mmSocket.connect();
            } catch (IOException e) {
                // 连接失败
                changeConnectState(CONNECT_FAIL, e);
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.i(TAG, "unable to close() " + mSocketType + " socket during connection failure.\n" + e2.getMessage());
                }
                return;
            }

            // 线程执行完置空重置
            synchronized (BluetoothClient.this) {
                mConnectThread = null;
            }

            // 开启消息传递的线程
            connected(mmSocket, mmDevice, mSocketType);
        }

        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.i(TAG, "close() of connect " + mSocketType + " socket failed.\n" + e.getMessage());
            }
        }
    }

    /**
     * 蓝牙连接成功以后消息传递的线程
     */
    private class ConnectedThread extends Thread {
        private BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.i(TAG, "create ConnectedThread: " + socketType);
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                mSocket = socket;
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.i(TAG, "temp sockets not created.\n" + e.getMessage());
            } finally {
                mInStream = tmpIn;
                mOutStream = tmpOut;
            }
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            // 只有蓝牙处于连接状态就一直循环读取数据
            while (BluetoothClient.this.getState() == STATE_CONNECTED) {
                try {
                    final byte[] buffer = new byte[2048];
                    final int bytes = mInStream.read(buffer);
                    // 读取到数据的回调
//                    if (0 < bytes) {
                    String readMessage = new String(buffer, 0, bytes);
                    onRead(readMessage);
//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // 读取数据出现异常
                    changeConnectState(CONNECT_LOST, e);
                    break;
                }
            }
        }

        /**
         * 发数据
         *
         * @param buffer 发送内容
         */
        void write(final byte[] buffer) {
            try {
                mOutStream.write(buffer);
                // 发送数据的回调
                String writeMessage = new String(buffer);
                onSend(writeMessage);
            } catch (IOException e) {
                // 发送数据出现失败
                changeConnectState(CONNECT_LOST, e);
            }
        }

        void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.i(TAG, "close() of connect socket failed.\n" + e.getMessage());
            }
        }
    }


    /**
     * 添加连接蓝牙的回调
     *
     * @param listener 回调接口
     */
    public void setOnClientConnectListener(OnClientConnectListener listener) {
        mOnClientConnectListener = listener;
    }

    /**
     * 移除监听
     */
    public void removeOnClientConnectListener() {
        mOnClientConnectListener = null;
    }
}
