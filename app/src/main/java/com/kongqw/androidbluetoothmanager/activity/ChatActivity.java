package com.kongqw.androidbluetoothmanager.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kongqw.androidbluetoothmanager.R;
import com.kongqw.androidbluetoothmanager.adapter.ChatListAdapter;
import com.kongqw.androidbluetoothmanager.entity.ChatEntity;
import com.kongqw.bluetoothlibrary.BluetoothManager;
import com.kongqw.bluetoothlibrary.engine.BluetoothClient;
import com.kongqw.bluetoothlibrary.engine.BluetoothService;
import com.kongqw.bluetoothlibrary.listener.OnClientConnectListener;
import com.kongqw.bluetoothlibrary.listener.OnMessageListener;
import com.kongqw.bluetoothlibrary.listener.OnServiceConnectListener;

import java.util.UUID;

public class ChatActivity extends AppCompatActivity implements OnMessageListener {

    private static final String TAG = "ChatActivity";
    private static final String BLUETOOTH_DEVICE = "BLUETOOTH_DEVICE";
    private static final String IS_SECURE = "IS_SECURE";
    public static final String TYPE = "TYPE";
    public static final int SERVICE = 0;
    public static final int CLIENT = 1;
    public static final int UNKNOWN = 3;
    private BluetoothService mBluetoothService;
    private int mType;
    private BluetoothClient mBluetoothClient;

    private static UUID UUID_SECURE = UUID.fromString("f291a307-6ee4-4bec-8f1a-1a1bdb8b3844");
    private static UUID UUID_INSECURE = UUID.fromString("dae8ce34-29de-499f-bc12-be586ccf3881");
    private EditText mEtChat;
    private ProgressDialog mProgressDialog;
    private ChatListAdapter mChatListAdapter;
    private RecyclerView mChatListView;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothManager mBluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatListView = (RecyclerView) findViewById(R.id.rl_chat);
        mChatListView.setHasFixedSize(true);
        mChatListView.setLayoutManager(new LinearLayoutManager(this));
        // 给RecyclerView添加一个适配器显示数据
        mChatListAdapter = new ChatListAdapter(this);
        mChatListView.setAdapter(mChatListAdapter);

        mEtChat = (EditText) findViewById(R.id.et_chat);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        mType = getIntent().getIntExtra(TYPE, UNKNOWN);
        switch (mType) {
            case SERVICE:
                initService();
                mBluetoothService.start();
                break;
            case CLIENT:
                initClient();
                mBluetoothDevice = getIntent().getParcelableExtra(BLUETOOTH_DEVICE);
                boolean isSecure = getIntent().getBooleanExtra(IS_SECURE, false);
                mBluetoothClient.connect(mBluetoothDevice, isSecure);
                break;
            default:
                finish();
                break;
        }

        mBluetoothManager = new BluetoothManager();
    }

    private void initService() {
        mBluetoothService = new BluetoothService() {

            @Override
            protected UUID onSecureUuid() {
                return UUID_SECURE;
            }

            @Override
            protected UUID onInsecureUuid() {
                return UUID_INSECURE;
            }
        };
        mBluetoothService.setOnServiceConnectListener(new OnServiceConnectListener() {
            @Override
            public void onConnectListening() {
                Log.i(TAG, "onConnectListening [服务端等待连接...]: ");
                showProgressDialog("服务端", "正在等待客户端连接...");
            }

            @Override
            public void onConnectSuccess(BluetoothDevice device) {
                Log.i(TAG, "onConnectSuccess [服务端蓝牙连接成功]: " + device.getAddress());
                mBluetoothDevice = device;
                dismissProgressDialog();
            }

            @Override
            public void onConnectFail(Exception e) {
                Log.i(TAG, "onConnectFail [服务端蓝牙连接失败]: " + e.getMessage());
                finish();
            }

            @Override
            public void onConnectLost(Exception e) {
                Log.i(TAG, "onConnectLost [服务端蓝牙连接断开]: " + e.getMessage());
                finish();
            }
        });
        mBluetoothService.setOnMessageListener(this);
    }

    private void initClient() {
        mBluetoothClient = new BluetoothClient() {
            @Override
            protected UUID onSecureUuid() {
                return UUID_SECURE;
            }

            @Override
            protected UUID onInsecureUuid() {
                return UUID_INSECURE;
            }
        };
        mBluetoothClient.setOnClientConnectListener(new OnClientConnectListener() {
            @Override
            public void onConnecting() {
                Log.i(TAG, "onConnecting [客户端正在连接...]: ");
                showProgressDialog("客户端", "正在连接服务端...");
            }

            @Override
            public void onConnectSuccess(BluetoothDevice device) {
                Log.i(TAG, "onConnectSuccess [客户端蓝牙连接成功]: " + device.getAddress());
                mBluetoothDevice = device;
                dismissProgressDialog();
            }

            @Override
            public void onConnectFail(Exception e) {
                Log.i(TAG, "onConnectFail [客户端蓝牙连接失败]: " + e.getMessage());
                finish();
            }

            @Override
            public void onConnectLost(Exception e) {
                Log.i(TAG, "onConnectLost [客户端蓝牙连接断开]: ");
                finish();
            }
        });
        mBluetoothClient.setOnMessageListener(this);
    }

    @Override
    protected void onDestroy() {
        switch (mType) {
            case SERVICE:
                mBluetoothService.stop();
                break;
            case CLIENT:
                mBluetoothClient.stop();
                break;
            default:
                finish();
                break;
        }
        super.onDestroy();
    }

    public static void startByService(Context packageContext) {
        Intent intent = new Intent(packageContext, ChatActivity.class);
        intent.putExtra(ChatActivity.TYPE, ChatActivity.SERVICE);
        packageContext.startActivity(intent);
    }

    public static void startByClient(Context packageContext, BluetoothDevice bluetoothDevice, boolean isSecure) {
        Intent intent = new Intent(packageContext, ChatActivity.class);
        intent.putExtra(ChatActivity.TYPE, ChatActivity.CLIENT);
        intent.putExtra(BLUETOOTH_DEVICE, bluetoothDevice);
        intent.putExtra(IS_SECURE, isSecure);
        packageContext.startActivity(intent);
    }

    /**
     * 显示Loading框
     *
     * @param title   title
     * @param message message
     */
    private void showProgressDialog(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.setTitle(title);
                mProgressDialog.setMessage(message);
                mProgressDialog.show();
            }
        });
    }

    /**
     * 隐藏Loading框
     */
    private void dismissProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
            }
        });
    }

    /**
     * 发送消息
     *
     * @param view View
     */
    public void onSend(View view) {
        String chatText = mEtChat.getText().toString();
        if (!TextUtils.isEmpty(chatText)) {
            switch (mType) {
                case SERVICE:
                    mBluetoothService.send(chatText);
                    break;
                case CLIENT:
                    mBluetoothClient.send(chatText);
                    break;
                default:
                    break;
            }
        }
        mEtChat.setText(null);
    }

    /**
     * 蓝牙发送了消息
     *
     * @param message 发送的消息
     */
    @Override
    public void onSend(String message) {
        Toast.makeText(getApplicationContext(), "发送消息:\n" + message, Toast.LENGTH_SHORT).show();
        mChatListAdapter.addChatMessage(new ChatEntity(true, mBluetoothManager.getName(), null, message));
        mChatListView.scrollToPosition(mChatListAdapter.getItemCount() - 1);
    }

    /**
     * 蓝牙接收到消息
     *
     * @param message 接收的消息
     */
    @Override
    public void onRead(String message) {
        Toast.makeText(getApplicationContext(), "收到消息:\n" + message, Toast.LENGTH_SHORT).show();
        mChatListAdapter.addChatMessage(new ChatEntity(false, mBluetoothDevice.getName(), mBluetoothDevice.getAddress(), message));
        mChatListView.scrollToPosition(mChatListAdapter.getItemCount() - 1);
    }
}
