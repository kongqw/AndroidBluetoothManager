[![](https://jitpack.io/v/kongqw/AndroidBluetoothManager.svg)](https://jitpack.io/#kongqw/AndroidBluetoothManager)

To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

``` gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

``` gradle
dependencies {
        compile 'com.github.kongqw:AndroidBluetoothManager:1.0.0'
}
```

# 效果图

![效果图](https://github.com/kongqw/AndroidBluetoothManager/blob/master/gif/pic.png)

![效果图](https://github.com/kongqw/AndroidBluetoothManager/blob/master/gif/gif.gif)

# 基础功能


## 添加权限

``` xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## 初始化

``` java
mBluetoothManager = new BluetoothManager();
```

## 打开蓝牙

``` java
mBluetoothManager.openBluetooth();
```

## 关闭蓝牙

``` java
mBluetoothManager.closeBluetooth();
```

## 添加蓝牙开关状态的监听

``` java
mBluetoothManager.setOnBluetoothStateListener(this);
```

``` java
/**
 * 正在关闭蓝牙的回调
 */
@Override
public void onBluetoothStateTurningOff() {
    // TODO
}

/**
 * 蓝牙关闭的回调
 */
@Override
public void onBluetoothStateOff() {
    // TODO
}

/**
 * 正在打开蓝牙的回调
 */
@Override
public void onBluetoothStateTurningOn() {
    // TODO
}

/**
 * 蓝牙打开的回调
 */
@Override
public void onBluetoothStateOn() {
    // TODO
}
```

## 移除蓝牙开关状态的监听

``` java
mBluetoothManager.removeOnBluetoothStateListener();
```

## 设置蓝牙可见

``` java
startActivity(mBluetoothManager.getDurationIntent(0));
```

## 获取蓝牙名称

``` java
mBluetoothManager.getName()
```

## 修改蓝牙名称

``` java
mBluetoothManager.setName(newName);
```

## 扫描附近的蓝牙设备

``` java
mBluetoothManager.discovery();
```

## 添加扫描蓝牙设备的监听

``` java
mBluetoothManager.setOnDiscoveryDeviceListener(this);
```

``` java
/**
 * 开始扫描附近蓝牙设备的回调
 */
@Override
public void onDiscoveryDeviceStarted() {
    // TODO
}

/**
 * 扫描到附近蓝牙设备的回调
 *
 * @param device 蓝牙设备
 */
@Override
public void onDiscoveryDeviceFound(BluetoothDevice device) {
    // TODO
}

/**
 * 扫描附近蓝牙设备完成的回调
 */
@Override
public void onDiscoveryDeviceFinished() {
    // TODO
}
```

## 移除扫描蓝牙设备的监听

``` java
mBluetoothManager.removeOnDiscoveryDeviceListener();
```

# 服务端

## 初始化

``` java
mBluetoothService = new BluetoothService() {

    @Override
    protected UUID onSecureUuid() {
        // TODO 设置自己的UUID
        return UUID_SECURE;
    }

    @Override
    protected UUID onInsecureUuid() {
        // TODO 设置自己的UUID
        return UUID_INSECURE;
    }
};
```

## 等待客户端连接

``` java
mBluetoothService.start();
```

## 断开连接/释放资源

``` java
mBluetoothService.stop();
```

## 添加蓝牙连接的监听

``` java
mBluetoothService.setOnServiceConnectListener(new OnServiceConnectListener() {
    @Override
    public void onConnectListening() {
        // TODO
    }

    @Override
    public void onConnectSuccess(BluetoothDevice device) {
        // TODO
    }

    @Override
    public void onConnectFail(Exception e) {
        // TODO
    }

    @Override
    public void onConnectLost(Exception e) {
        // TODO
    }
});
```

## 发送消息

``` java
mBluetoothService.send(chatText);
```

## 添加消息收发的监听

``` java
mBluetoothClient.setOnMessageListener(this);
```

``` java
/**
 * 蓝牙发送了消息
 *
 * @param message 发送的消息
 */
@Override
public void onSend(String message) {
    // TODO
}

/**
 * 蓝牙接收到消息
 *
 * @param message 接收的消息
 */
@Override
public void onRead(String message) {
    // TODO
}
```

# 客户端

## 初始化

``` java
mBluetoothClient = new BluetoothClient() {
    @Override
    protected UUID onSecureUuid() {
        // TODO 设置自己的UUID
        return UUID_SECURE;
    }

    @Override
    protected UUID onInsecureUuid() {
        // TODO 设置自己的UUID
        return UUID_INSECURE;
    }
};
```

## 蓝牙连接（安全）

``` java
mBluetoothClient.connect(mBluetoothDevice, true);
```

## 蓝牙连接（不安全）

``` java
mBluetoothClient.connect(mBluetoothDevice, false);
```

## 断开连接/释放资源

``` java
mBluetoothClient.stop();
```

## 添加蓝牙连接的监听

``` java
mBluetoothClient.setOnClientConnectListener(new OnClientConnectListener() {
    @Override
    public void onConnecting() {
        // TODO
    }

    @Override
    public void onConnectSuccess(BluetoothDevice device) {
        // TODO
    }

    @Override
    public void onConnectFail(Exception e) {
        // TODO
    }

    @Override
    public void onConnectLost(Exception e) {
        // TODO
    }
});
```

## 发送消息

``` java
mBluetoothClient.send(chatText);
```

## 添加消息收发的监听

``` java
mBluetoothClient.setOnMessageListener(this);
```

``` java
/**
 * 蓝牙发送了消息
 *
 * @param message 发送的消息
 */
@Override
public void onSend(String message) {
    // TODO
}

/**
 * 蓝牙接收到消息
 *
 * @param message 接收的消息
 */
@Override
public void onRead(String message) {
    // TODO
}
```

