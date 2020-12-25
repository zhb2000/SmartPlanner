package com.my.smartplanner.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.my.smartplanner.activity.HealthActivity;

public class BindService extends Service { //开启加速度监传感器听器的服务（在监听器中实现计步）
    //1.声明一个传感器管理器的变量
    private SensorManager manager;
    //声明一个传感器变量
    private Sensor sensor;
    //实例化一个监听器类的对象
    //声明一个监听器类MySensorEventListener的常量
    private HealthActivity.MySensorEventListener mListener = new HealthActivity.MySensorEventListener(this);

    //Thread Thread1=new MainActivity.testThread();
    @Override
    public void onCreate() {
        //获取传感器管理器的实例
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (manager != null) {
            //当manager不空时 获取加速度传感器的实例
            sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (sensor != null) {
                //注册加速度传感器
                manager.registerListener(mListener, sensor, SensorManager.SENSOR_DELAY_GAME);
            }
        }
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        Log.i("MyService", "OnDestroy!");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("MyService", "OnUnbind!");
        return super.onUnbind(intent);
    }

    public class DownloadBinder extends Binder {
        public void StartDownLoad() {
            Log.i("BindService", "DownloadBinder()!");
        }//之后活动会调用此方法

        public int progress() {
            Log.i("BindService", "progress()!");
            return 0;
        }
    }

    private DownloadBinder mBinder = new DownloadBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("MyService", "OnBind!");
        return mBinder;
    }

}
