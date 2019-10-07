package com.my.smartplanner;

import android.app.Application;
import android.content.Context;

/*自定义的Application，提供全局获取Context功能*/
public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    /*全局获取Context*/
    public static Context getContext(){
        return context;
    }
}
