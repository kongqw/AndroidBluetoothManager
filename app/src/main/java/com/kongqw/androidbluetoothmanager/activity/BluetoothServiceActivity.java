package com.kongqw.androidbluetoothmanager.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kongqw.androidbluetoothmanager.R;
import com.kongqw.bluetoothlibrary.BluetoothManager;
import com.kongqw.bluetoothlibrary.listener.OnBluetoothStateListener;

public class BluetoothServiceActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, OnBluetoothStateListener {

    private static final String TAG = "ServiceActivity";
    private SwitchCompat mSwitchBluetooth;
    private BluetoothManager mBluetoothManager;
    private SwitchCompat mSwitchDuration;
    private RelativeLayout mRlName;
    private TextView mTvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_service);

        mBluetoothManager = new BluetoothManager();

        // 蓝牙开关
        mSwitchBluetooth = (SwitchCompat) findViewById(R.id.switch_bluetooth);
        mSwitchBluetooth.setOnCheckedChangeListener(this);
        // 开放检测开关
        mSwitchDuration = (SwitchCompat) findViewById(R.id.switch_duration);
        mSwitchDuration.setOnCheckedChangeListener(this);
        // 手机名称布局
        mRlName = (RelativeLayout) findViewById(R.id.rl_name);
        mRlName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BluetoothServiceActivity.this, SetBluetoothNameActivity.class));
            }
        });
        // 手机名称
        mTvName = (TextView) findViewById(R.id.tv_name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 回显蓝牙开关状态
        mSwitchBluetooth.setChecked(mBluetoothManager.isEnabled());
        // 显示手机名字
        mTvName.setText(mBluetoothManager.getName());
        // 添加蓝牙状态的监听
        mBluetoothManager.setOnBluetoothStateListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 移除蓝牙状态的监听
        mBluetoothManager.removeOnBluetoothStateListener();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch_bluetooth:
                Log.i(TAG, "onCheckedChanged [蓝牙开关]: " + isChecked);
                mSwitchDuration.setEnabled(isChecked);
                mRlName.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (isChecked) {
                    mBluetoothManager.openBluetooth();
                } else {
                    mBluetoothManager.closeBluetooth();
                }
                break;
            case R.id.switch_duration:
                Log.i(TAG, "onCheckedChanged [开放检测开关]: " + isChecked);
                if (isChecked) {
                    startActivity(mBluetoothManager.getDurationIntent(0));
                } else {
                    // startActivity(mBluetoothManager.getDurationIntent(0));
                }
                break;
            default:
                break;
        }
    }

    /**
     * 正在关闭蓝牙的回调
     */
    @Override
    public void onBluetoothStateTurningOff() {
        Toast.makeText(getApplicationContext(), "正在关闭蓝牙...", Toast.LENGTH_SHORT).show();
    }

    /**
     * 蓝牙关闭的回调
     */
    @Override
    public void onBluetoothStateOff() {
        mSwitchBluetooth.setChecked(false);
    }

    /**
     * 正在打开蓝牙的回调
     */
    @Override
    public void onBluetoothStateTurningOn() {
        Toast.makeText(getApplicationContext(), "正在打开蓝牙...", Toast.LENGTH_SHORT).show();
    }

    /**
     * 蓝牙打开的回调
     */
    @Override
    public void onBluetoothStateOn() {
        mSwitchBluetooth.setChecked(true);
    }

    /**
     * 进入聊天界面 等待客户端连接
     *
     * @param view view
     */
    public void onChat(View view) {
        ChatActivity.startByService(this);
    }
}
