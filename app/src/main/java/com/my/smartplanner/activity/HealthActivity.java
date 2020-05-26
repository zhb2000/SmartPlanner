package com.my.smartplanner.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jaeger.library.StatusBarUtil;
import com.my.smartplanner.DatabaseHelper.HealthDBHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.service.BindService;
import com.my.smartplanner.view.ActionRateView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 此项目用于在一个服务中实现计步
 */
public class HealthActivity extends AppCompatActivity implements View.OnClickListener {
    static double[] oriValues = new double[3];//存放三轴数据
    static final int valueNum = 4; //用于存放之前4次计算阈值的波峰波谷差值的数组
    static double[] tempValue = new double[valueNum];
    static int tempCount = 0;//数组tempValue的下标
    static boolean isDirectionUp = false; //是否上升的标志位
    static int continueUpCount = 0;//持续上升次数
    static int continueUpFormerCount = 0; //上一点的持续上升的次数，为了记录波峰的上升次数
    static boolean lastStatus = false;//上一点的状态，上升还是下降 true为上升 false为下降
    static double peakOfWave = 0;//波峰值
    static double valleyOfWave = 0; //波谷值
    static long timeOfThisPeak = 0;//此次波峰的时间
    static long timeOfLastPeak = 0;//上次波峰的时间
    static long timeOfNow = 0; //当前的时间
    static double gravityOld;//上次传感器的值
    static int rate_new = 0;  //新的目标完成率
    static int rate_old = 0;  //旧的目标完成率
    public static int Old_STEP = 0;//之前的步数
    public static int CURRENT_STEP = 0;//当前步数
    public static int Old_Date = 0; //之前的日期
    public static int Current_Date = 0; //现在的日期
    public static long START_TIME = 0;//开始时间 即走第一步的时间
    //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    static final double initialValue = (float) 1.7;    //初始阈值下限
    static double ThreadValue = (float) 2.0;    //初始阈值上限
    static int step_goal = 1000;//步数目标默认为1000

    static TextView Tv_step;//显示步数
    static TextView Tv_distance;//显示距离
    static TextView Tv_calorie;//显示卡路里
    static TextView Tv_goal;//显示目标步数
    static Button Bt_goal;//拉起修改目标界面的Bt
    static Button Bt_chart;//拉起柱状图界面的Bt
    static ActionRateView MyArv;
    private BindService.DownloadBinder downloadBinder;
    private View v;

    /**
     * 设置Toolbar
     */
    private void toolbarSetting() {
        Toolbar toolbar = findViewById(R.id.health_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 菜单选中事件：返回箭头
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    /**
     * 设置状态栏和导航栏
     */
    private void statusBarAndNavigationBarSetting() {
        StatusBarUtil.setColor(this, getResources()
                .getColor(R.color.health_background), 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources()
                    .getColor(R.color.health_background));
        }
    }

    public HealthActivity() {
        gravityOld = 0;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health);
        toolbarSetting();
        statusBarAndNavigationBarSetting();
//        //隐藏标题
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }

        //关联控件
        //TextView
        Tv_step = findViewById(R.id.tv_step);
        Tv_distance = findViewById(R.id.tv_distance);
        Tv_calorie = findViewById(R.id.tv_calorie);
        Tv_goal = findViewById(R.id.tv_goal);
        //Button
        Bt_goal = findViewById(R.id.bt_goal);
        Bt_chart = findViewById(R.id.bt_chart);
        //AcctionRateView
        MyArv = findViewById(R.id.achievement_arv);

        //Dialog弹窗
        View view = getLayoutInflater().inflate(R.layout.half_dialog_view, null);
        final EditText editText = (EditText) view.findViewById(R.id.dialog_edit);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("修改目标步数")//设置对话框的标题
                .setView(view)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String content = editText.getText().toString();
                        step_goal = Integer.parseInt(content);
                        dialog.dismiss();
                    }
                }).create();
        Bt_goal.setOnClickListener(new View.OnClickListener() {//关联点击事件 呼出弹窗
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
        Bt_chart.setOnClickListener(new View.OnClickListener() {//关联点击事件 拉起柱状图界面
            @Override
            public void onClick(View v) {
                //定义intent 一个意图
                Intent intent = new Intent(HealthActivity.this, ChartActivity.class);
                //调用start activity传入intent
                startActivity(intent);
                //开启第二个activity
            }
        });

        //启动服务并绑定计步服务
        Intent startintent = new Intent(this, BindService.class);
        startService(startintent);//启动服务

        Intent bindintent = new Intent(this, BindService.class);
        bindService(bindintent, connection, BIND_AUTO_CREATE);//绑定服务

    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (BindService.DownloadBinder) service;
            downloadBinder.progress();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onClick(View v) {

    }

    public static class MySensorEventListener implements SensorEventListener {
        Context context;

        public MySensorEventListener(Context context) {
            this.context = context;
        }

        //当传感器的数值发生变化 调用onSensorChanged（）
        @Override
        public void onSensorChanged(SensorEvent arg0) {
            //利用这个方便监听加速度传感器的变化 所以在这个方法里写一系列的代码
            //当前传感器的值可能为负值 利用math.abs方法取其绝对值
            float xValue = Math.abs(arg0.values[0]);//获取x轴的加速度
            float yValue = Math.abs(arg0.values[1]);//获取y轴的加速度
            float zValue = Math.abs(arg0.values[2]);//获取z轴的加速度
            double average = Math.abs(Math.sqrt(Math.pow(xValue, 2) + Math.pow(yValue, 2) + Math.pow(zValue, 2)));
            //求出合加速的大小（减掉重力加速度大小再取绝对值）
            DetectorNewStep(average);//调用DetectorNewStep()方法检测新步子
            if (Old_STEP != CURRENT_STEP) {//当步数变化时才通过textview输出到界面
                Old_STEP = CURRENT_STEP;
                Log.d("MainActivity", "步数：" + CURRENT_STEP);
                //获取系统当前日期 用于保存每日的步数记录
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                Date date1 = new Date(System.currentTimeMillis());
                Log.d("MainActivity", "时间" + simpleDateFormat.format(date1));
                Current_Date = Integer.parseInt(simpleDateFormat.format(date1));
                Log.i("MainActivity", "Current_Date:" + Current_Date);
                Log.i("MainActivity", "Old_Date:" + Old_Date);
                //更新步数
                Tv_step.setText("已走：" + CURRENT_STEP + "步");
                double distance = 0.0005 * CURRENT_STEP;
                Tv_distance.setText("已走距离：" + String.format("%.2f", distance) + "km");//距离与卡路里保留两位小数输出
                /*卡路里计算
                计算公式
                体重（kg）* 距离（km）* 运动系数（k）
                运动系数
                健走：k=0.8214
                 */
                double calorie = 0.8214 * 65 * distance;
                Tv_calorie.setText("已消耗的卡路里：" + String.format("%.2f", calorie) + "cal");
                Tv_goal.setText("目标步数：" + step_goal);
                //更新数据库
                UpDataBase(Current_Date, CURRENT_STEP);

                //更新目标完成率
                rate_new = (int) (100 * CURRENT_STEP / step_goal);
                Log.i("MainActivitay", "rate:" + rate_new);
                if (rate_new != 0) {//目标完成率不为0时更新MyArv控件
                    if (rate_new != rate_old) {//若完成率改变了 则更新控件 否则不用更新
                        MyArv.setCurrentCount(100, rate_new);
                    }
                    rate_old = rate_new;
                }
            }
        }

        //当传感器精度发生变化 调用onAccuracyChanged（）
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        /*
         * 检测步子，并开始计步
         * 1.传入sensor中的数据
         * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
         * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
         * */
        public void UpDataBase(int Date, int Step) {
            //更新数据
            //声明一个对象values并实例化 用于封装数据
            HealthDBHelper database = new HealthDBHelper(context);
            SQLiteDatabase db = database.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("date", Current_Date);
            values.put("steps", CURRENT_STEP);
            if (Old_Date == 0) {//如果是第一次写数据库就插入一条数据
                Log.i("MainActivity", "FirstTime!");
                db.delete("UserInfo", "date=?", new String[]{String.valueOf(Current_Date)});
                db.insert("UserInfo", null, values);
                values.clear();
                db.close();
                Old_Date = Current_Date;
            } else if (Old_Date == Current_Date) {
                Log.i("MainActivity", "UpDate!");
                db.update("UserInfo", values, "date=?", new String[]{String.valueOf(Current_Date)});
                values.clear();
                db.close();
            } else {//日期改变时 先更新前一日的数据 再将步数清零并更新日期
                //更新数据
                //声明一个对象values并实例化 用于封装数据
                Log.i("MainActivitay", "NewData!");
                values.clear();
                values.put("date", Old_Date);
                values.put("steps", CURRENT_STEP);
                db.update("UserInfo", values, "date=?", new String[]{String.valueOf(Old_Date)});
                values.clear();
                db.close();
                //清空前日数据
                CURRENT_STEP = 0;
                Old_Date = Current_Date;
            }
        }


        public void DetectorNewStep(double values) {//values即求出的加速度平均值
            //Log.i("MainActivity", "**********执行DetectorNewStep()中！***********");
            Log.i("MainActivity", "GravityOld：" + gravityOld);
            if (gravityOld == 9.810001316516791) {//如果当先步为第0步(gravityOld==重力加速度) 则将当前加速度的值赋给gravityOld
                //Log.i("MainActivity", "**********第0步检测中！***********");
                START_TIME = System.currentTimeMillis();//获取当前时间
                gravityOld = values;
            } else {//当先步子不是第一步时 判断当前步是否为潜在波峰
                //Log.i("MainActivity", "**********第n步检测中！***********");
                if (DetectorPeak(values, gravityOld)) {//如果是波峰 则更新timeOfLastPeak 并记录当前时刻
                    timeOfLastPeak = timeOfThisPeak;
                    timeOfNow = System.currentTimeMillis();//获取当前时间
                    //若当前波峰与上场波峰的时间间隔满足范围0.2s-2s且波峰与波谷的差值大于当前阀值 且时间超过了延迟时间(3s)(timeOfNow-START_TIME>3000) 则认定为走了1步
                    //不超过3s的振动不作为步数
                    if (timeOfNow - timeOfLastPeak >= 200 && (peakOfWave - valleyOfWave >= ThreadValue) && timeOfNow - timeOfLastPeak <= 2000) {
                        CURRENT_STEP++;
                        //Log.i("MainActivity","已走步数："+CURRENT_STEP);
                        timeOfThisPeak = timeOfNow;

                    }
                    if (timeOfNow - timeOfLastPeak >= 200 && (peakOfWave - valleyOfWave >= initialValue)) {//波差大于阀值时用于更新阀值
                        timeOfThisPeak = timeOfNow;
                        //Log.i("MainActivity", "**********调用 Peak_Valley_Thread()中！***********");
                        ThreadValue = Peak_Valley_Thread(peakOfWave - valleyOfWave);//利用Peak_Valley_Thread方法 更新阀值

                    }
                }

            }
            gravityOld = values;//更新gravityOld
        }

        /*
         * 检测波峰
         * 以下四个条件判断为波峰：
         * 1.目前点为下降的趋势：isDirectionUp为false
         * 2.之前的点为上升的趋势：lastStatus为true
         * 3.到波峰为止，持续上升大于等于2次
         * 4.波峰值大于1.2g,小于2g
         * 记录波谷值
         * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
         * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
         * */
//检测波峰的方法 利用当前加速度平均值和之前的加速度平均值
        public boolean DetectorPeak(double newValue, double oldValue) {
            //Log.i("MainActivity", "**********执行DetectorPeak()中！***********");
            lastStatus = isDirectionUp;
            if (newValue >= oldValue) {//如果当前值大于等于上一次的值 置上升标记isDirectionUp为true 持续上升标记continueUpCount自增
                isDirectionUp = true;
                continueUpCount++;
            } else {//如果当前值小于上一次的值 置上升标记isDirectionUp为false 持续上升标记continueUpCount作为前次的持续上升数continueUpFormerCount  持续上升数置0
                continueUpFormerCount = continueUpCount;
                continueUpCount = 0;
                isDirectionUp = false;
            }
            if (!isDirectionUp && lastStatus && (continueUpFormerCount >= 2 && (oldValue >= 11.76 && oldValue < 19.6))) {//判断是否为波峰
                peakOfWave = oldValue;
                // Log.i("MainActivity","****************PeakDetected!****************");
                return true;
            } else if (!lastStatus && isDirectionUp) {//如果上次为上升 这次为下降 则用上次的oldValue更新波谷 返回false表示未探测到波峰
                valleyOfWave = oldValue;
                // Log.i("MainActivity","****************WaveDetected!****************");
                return false;
            } else {    //其他情况默认返回false
                return false;
            }
        }

        /*
         * 阈值的计算
         * 1.通过波峰波谷的差值计算阈值
         * 2.记录4个值，存入tempValue[]数组中
         * 3.在将数组传入函数averageValue中计算阈值
         * */
        public double Peak_Valley_Thread(double value) {
            Log.i("MainActivity", "****************执行Peak_Valley_Thread中！****************");
            double tempThread = ThreadValue;
            if (tempCount < valueNum) {//当tempValue[]数组未满时: 将当前值加入数组
                tempValue[tempCount] = value;
                tempCount++;
            }//数组下标自增

            else {    //当tempValue[]数组已满时 利用 averageValue()方法求出新的阀值 并赋给当前阀值变量tempThread
                tempThread = averageValue(tempValue, valueNum);

                for (int i = 1; i < valueNum; i++) {//数组tempValue[]中数据前移1格 为下一个value腾出空间
                    tempValue[i - 1] = tempValue[i];
                }

                tempValue[valueNum - 1] = value;
            }//利用当前value更新数组tempValue[]

            return tempThread;//返回更新后的阀值
        }


        /*
         * 梯度化阈值
         * 1.计算数组的均值
         * 2.通过均值将阈值梯度化在一个范围里
         * */
        public double averageValue(double value[], int n) {
            double ave = 0;
            for (int i = 0; i < n; i++) {
                ave += value[i];
            }
            ave = ave / valueNum;//求出平均值
            if (ave >= 8)
                ave = 4.3;
            else if (ave >= 7 && ave < 8)
                ave = 3.3;
            else if (ave >= 4 && ave < 7)
                ave = 2.3;
            else if (ave >= 3 && ave < 4)
                ave = 2.0;
            else {
                ave = 1.3;
            }
            Log.d("MainActivity", "****************阀值已更新！****************");
            return ave;
        }
    }
}