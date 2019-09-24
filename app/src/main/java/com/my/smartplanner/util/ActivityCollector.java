package com.my.smartplanner.util;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/*活动集合类，作为活动的管理工具*/
public class ActivityCollector {
    private static List<Activity> activities = new ArrayList<>();//存放所有活动引用的列表

    /*向列表中添加某个活动*/
    public static void addActivity(Activity activity){
        activities.add(activity);
    }

    /*从列表中移除某个活动*/
    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }

    /*结束所有的活动*/
    public static void finishAll(){
        for(Activity activity : activities){
            if(!activity.isFinishing()){
                activity.finish();
            }
        }
        activities.clear();
    }

}
