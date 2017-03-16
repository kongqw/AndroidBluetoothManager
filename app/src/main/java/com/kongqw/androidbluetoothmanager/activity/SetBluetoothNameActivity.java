package com.kongqw.androidbluetoothmanager.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kongqw.androidbluetoothmanager.R;
import com.kongqw.bluetoothlibrary.BluetoothManager;

public class SetBluetoothNameActivity extends AppCompatActivity {

    private EditText mEtBluetoothName;
    private BluetoothManager mBluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_bluetooth_name);

        mEtBluetoothName = (EditText) findViewById(R.id.et_name);

        mBluetoothManager = new BluetoothManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEtBluetoothName.setText(mBluetoothManager.getName());
    }

    /**
     * 保存蓝牙名字
     *
     * @param view view
     */
    public void onSaveBluetoothName(View view) {
        String newName = mEtBluetoothName.getText().toString();
        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(getApplicationContext(), "蓝牙名称不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newName.equals(mBluetoothManager.getName())) {
            Toast.makeText(getApplicationContext(), "名称相同！", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean isUpdate = mBluetoothManager.setName(newName);
        Toast.makeText(getApplicationContext(), isUpdate ? "蓝牙名称修改成功！" : "蓝牙名字修改失败！", Toast.LENGTH_SHORT).show();
    }
}
