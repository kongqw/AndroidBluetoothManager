package com.kongqw.androidbluetoothmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.kongqw.androidbluetoothmanager.activity.BluetoothClientActivity;
import com.kongqw.androidbluetoothmanager.activity.BluetoothServiceActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onService(View view) {
        startActivity(new Intent(this, BluetoothServiceActivity.class));
    }

    public void onClient(View view) {
        startActivity(new Intent(this, BluetoothClientActivity.class));
    }
}
