package com.kongqw.bluetoothlibrary.engine;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.text.TextUtils;
import android.util.Log;

import com.kongqw.bluetoothlibrary.listener.OnServiceConnectListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 蓝牙通信服务端
 */
public abstract class BluetoothService extends Bluetooth {

    private static final String TAG = "BluetoothService";

    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectedThread mConnectedThread;

    private OnServiceConnectListener mOnServiceConnectListener;

    /**
     * 服务端开启监听 等待客户端连接
     */
    public synchronized void start() {
        Log.i(TAG, "start: ");
        // 取消之前的消息收发线程
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 设置蓝牙状态为等待连接
        setState(STATE_LISTEN);

        // 服务端开启等待连接线程
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }

    /**
     * 蓝牙连接成功以后开启消息传递的线程
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    private synchronized void connected(BluetoothSocket socket, final BluetoothDevice device, final String socketType) {
        Log.i(TAG, "connected, Socket Type:" + socketType);
        // 设置蓝牙状态为已经连接
        setState(STATE_CONNECTED);

        // 连接成功回调
        connectionSuccess(device);

        // 关闭消息收发的线程
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 关闭客户端等待连接线程
//        if (mSecureAcceptThread != null) {
//            mSecureAcceptThread.cancel();
//            mSecureAcceptThread = null;
//        }
//        if (mInsecureAcceptThread != null) {
//            mInsecureAcceptThread.cancel();
//            mInsecureAcceptThread = null;
//        }

        // 开启新的消息收发线程
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();
    }

    /**
     * 关闭所有线程
     */
    public synchronized void stop() {
        Log.i(TAG, "stop: ");
        // 关闭消息收发线程
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 关闭服务端等待连接线程
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // 设置蓝牙为断开连接状态
        setState(STATE_NONE);
    }

    /**
     * 判断是否正在连接
     *
     * @return 连接状态
     */
    public synchronized boolean isConnected() {
        return STATE_CONNECTED == getState();
    }

    /**
     * 服务端等待连接的线程
     */
    private class AcceptThread extends Thread {
        private BluetoothServerSocket mServerSocket;
        private String mSocketType;

        AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            try {
                mSocketType = secure ? "Secure" : "Insecure";
//                Log.i(TAG, "AcceptThread: ----------------------------------------------------");
//                Log.i(TAG, "AcceptThread: UUID_SECURE " + UUID_SECURE);
//                Log.i(TAG, "AcceptThread: UUID_INSECURE " + UUID_INSECURE);
//                Log.i(TAG, "AcceptThread: ----------------------------------------------------");
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.i(TAG, "Socket Type: " + mSocketType + "listen() failed.\n" + e.getMessage());
            } finally {
                mServerSocket = tmp;
            }
        }

        public void run() {
            Log.i(TAG, "Socket Type: " + mSocketType + "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket;

            // 当前没有连接
            while (STATE_CONNECTED != BluetoothService.this.getState()) {
                try {
                    // 正在等待客户端连接...
                    connectListening();
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    // 蓝牙连接失败
                    connectionFail(e);
                    break;
                }

                // 连接成功
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (BluetoothService.this.getState()) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(), mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.i(TAG, "Could not close unwanted socket.\n" + e.getMessage());
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);
        }

        void cancel() {
            Log.i(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.i(TAG, "Socket Type" + mSocketType + "close() of server failed.\n" + e.getMessage());
            }
        }
    }

    /**
     * 蓝牙连接成功以后消息传递的线程
     */
    private class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.i(TAG, "create ConnectedThread: " + socketType);
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                mmSocket = socket;
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.i(TAG, "temp sockets not created.\n" + e.getMessage());
            } finally {
                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }
        }

        public void run() {
            // 只有蓝牙处于连接状态就一直循环读取数据
            while (BluetoothService.this.getState() == STATE_CONNECTED) {
                try {
                    final byte[] buffer = new byte[2048];
                    final int bytes = mmInStream.read(buffer);
                    // 读取到数据的回调
//                    if (0 < bytes) {
                    String readMessage = new String(buffer, 0, bytes);
                    onRead(readMessage);
//                    }
                } catch (IOException e) {
                    // TODO 待定 socket close

                    // 读取数据出现异常 连接中断
                    connectionLost(e);
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
                mmOutStream.write(buffer);
                // 发送数据的回调
                String writeMessage = new String(buffer);
                onSend(writeMessage);
            } catch (IOException e) {
                // TODO 待定 socket close

                // 发送数据出现异常 连接中断
                connectionLost(e);
            }
        }

        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.i(TAG, "close() of connect socket failed.\n" + e.getMessage());
            }
        }
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
     * 等待连接
     */
    private void connectListening() {
        if (null != mOnServiceConnectListener) {
            mOnServiceConnectListener.onConnectListening();
        }
    }

    /**
     * 蓝牙连接成功
     */
    private void connectionSuccess(BluetoothDevice device) {
        if (null != mOnServiceConnectListener) {
            mOnServiceConnectListener.onConnectSuccess(device);
        }
    }

    /**
     * 连接失败
     *
     * @param e 异常
     */
    private void connectionFail(Exception e) {
        if (null != mOnServiceConnectListener) {
            mOnServiceConnectListener.onConnectFail(e);
        }
        // 连接失败以后重新等待客户端连接
        // BluetoothService.this.start();
    }

    /**
     * 连接中断
     *
     * @param e 异常
     */
    private void connectionLost(Exception e) {
        if (null != mOnServiceConnectListener) {
            mOnServiceConnectListener.onConnectLost(e);
        }
        // 连接中断以后重新等待客户端连接
        // BluetoothService.this.start();
    }


    /**
     * 添加连接蓝牙的回调
     *
     * @param listener 回调接口
     */
    public void setOnServiceConnectListener(OnServiceConnectListener listener) {
        mOnServiceConnectListener = listener;
    }

    /**
     * 移除监听
     */
    public void removeOnServiceConnectListener() {
        mOnServiceConnectListener = null;
    }
}
