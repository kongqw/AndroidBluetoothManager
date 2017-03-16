package com.kongqw.androidbluetoothmanager.activity;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.kongqw.androidbluetoothmanager.R;
import com.kongqw.androidbluetoothmanager.adapter.BluetoothAdapter;
import com.kongqw.bluetoothlibrary.BluetoothManager;
import com.kongqw.bluetoothlibrary.listener.OnBluetoothStateListener;
import com.kongqw.bluetoothlibrary.listener.OnDiscoveryDeviceListener;
import com.kongqw.permissionslibrary.PermissionsManager;

public class BluetoothClientActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, SwipeRefreshLayout.OnRefreshListener, OnDiscoveryDeviceListener, OnBluetoothStateListener, AdapterView.OnItemClickListener {

    private static final String TAG = "BluetoothClientActivity";
    // 要校验的权限
    private String[] PERMISSIONS = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private SwitchCompat mSwitchBluetooth;
    private BluetoothManager mBluetoothManager;
    private FrameLayout mFlBluetooth;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BluetoothAdapter mBluetoothAdapter;
    private PermissionsManager mPermissionsManager;
    private CheckBox mCbSecure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_client);

        mBluetoothManager = new BluetoothManager();
        // 蓝牙开关
        mSwitchBluetooth = (SwitchCompat) findViewById(R.id.switch_bluetooth);
        mSwitchBluetooth.setOnCheckedChangeListener(this);

        mCbSecure = (CheckBox) findViewById(R.id.cb_secure);

        mFlBluetooth = (FrameLayout) findViewById(R.id.fl_bluetooth);
        // 下拉刷新
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // 蓝牙列表
        ListView bluetoothListView = (ListView) findViewById(R.id.bluetooth_list);
        bluetoothListView.setEmptyView(findViewById(R.id.empty_view));
        // 蓝牙列表适配器
        mBluetoothAdapter = new BluetoothAdapter(this);
        bluetoothListView.setAdapter(mBluetoothAdapter);
        bluetoothListView.setOnItemClickListener(this);

        // Android 6.0 动态检查权限
        mPermissionsManager = new PermissionsManager(this) {
            @Override
            public void authorized(int i) {

            }

            @Override
            public void noAuthorization(int i, String[] strings) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothClientActivity.this);
                builder.setTitle("提示");
                builder.setMessage("当前缺少必要权限！\n将无法扫描到附近的蓝牙设备！");
                builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionsManager.startAppSettings(getApplicationContext());
                    }
                });
                builder.show();
            }

            @Override
            public void ignore() {

            }
        };
        // 检查权限
        mPermissionsManager.checkPermissions(0, PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 用户做出选择以后复查权限，判断是否通过了权限申请
        mPermissionsManager.recheckPermissions(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 回显蓝牙开关状态
        mSwitchBluetooth.setChecked(mBluetoothManager.isEnabled());
        // 添加蓝牙状态的监听
        mBluetoothManager.setOnBluetoothStateListener(this);
        // 添加扫描设备的监听
        mBluetoothManager.setOnDiscoveryDeviceListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 移除蓝牙状态的监听
        mBluetoothManager.removeOnBluetoothStateListener();
        // 移除扫描设备的监听
        mBluetoothManager.removeOnDiscoveryDeviceListener();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch_bluetooth:
                mFlBluetooth.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (isChecked) {
                    mBluetoothManager.openBluetooth();
                } else {
                    mBluetoothManager.closeBluetooth();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 下拉刷新的回调
     */
    @Override
    public void onRefresh() {
        mBluetoothManager.discovery();
    }

    /**
     * 开始扫描附近蓝牙设备的回调
     */
    @Override
    public void onDiscoveryDeviceStarted() {
        Log.i(TAG, "onDiscoveryDeviceStarted: ");
        mBluetoothAdapter.clearDevices();
    }

    /**
     * 扫描到附近蓝牙设备的回调
     *
     * @param device 蓝牙设备
     */
    @Override
    public void onDiscoveryDeviceFound(BluetoothDevice device) {
        Log.i(TAG, "onDiscoveryDeviceFound: " + device.getAddress());
        mBluetoothAdapter.addBluetoothDevice(device);
    }

    /**
     * 扫描附近蓝牙设备完成的回调
     */
    @Override
    public void onDiscoveryDeviceFinished() {
        Log.i(TAG, "onDiscoveryDeviceFinished: ");
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * 蓝牙正在关闭的回调
     */
    @Override
    public void onBluetoothStateTurningOff() {

    }

    /**
     * 蓝牙关闭的回调
     */
    @Override
    public void onBluetoothStateOff() {
        mSwitchBluetooth.setChecked(false);
    }

    /**
     * 蓝牙正在打开的回调
     */
    @Override
    public void onBluetoothStateTurningOn() {

    }

    /**
     * 蓝牙打开的回调
     */
    @Override
    public void onBluetoothStateOn() {
        mSwitchBluetooth.setChecked(true);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 停止扫描
        mBluetoothManager.discovery();
        // 收回下拉刷新
        mSwipeRefreshLayout.setRefreshing(false);

        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getItem(position);
        ChatActivity.startByClient(this, bluetoothDevice, mCbSecure.isChecked());
    }
}
