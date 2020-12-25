package com.my.smartplanner.activity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.my.smartplanner.DatabaseHelper.HealthDBHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.util.LogUtil;
import com.my.smartplanner.view.ColumnView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 用来显示柱状图的界面
 */
public class ChartActivity extends AppCompatActivity {


    /**
     * 设置Toolbar
     */
    private void toolbarSetting() {
        Toolbar toolbar = findViewById(R.id.chart_toolbar);
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

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        toolbarSetting();

        //查询数据库中的步数数据用于绘制柱状图
        HealthDBHelper database = new HealthDBHelper(this);
        SQLiteDatabase db = database.getWritableDatabase();

        Cursor cursor = db.query("UserInfo", null, null, null, null, null, null);
        String[] dateStrings;
        int[] stepData;
        if (cursor.getCount() == 0) {
            dateStrings = new String[]{"无数据"};
            stepData = new int[]{0};
        } else {
            dateStrings = new String[cursor.getCount()];
            stepData = new int[cursor.getCount()];
            Log.i("ChartActivity", "out:cutsor:" + cursor.getCount());

            int i = 0;
            while (cursor.moveToNext()) {
                dateStrings[i] = cursor.getString(cursor.getColumnIndex("date"));
                dateStrings[i] = dateStrings[i].substring(4);
                Log.i("ChartActivity", "out:date" + i + ":" + dateStrings[i]);
                stepData[i] = cursor.getInt(cursor.getColumnIndex("steps"));
                Log.i("ChartActivity", "out:steps" + i + ":" + stepData[i]);
                i = i + 1;
            }
        }
        cursor.close();
        db.close();
        barChart(dateStrings, stepData);
        LogUtil.d("string_array", Arrays.toString(dateStrings));
    }

    // 初始化柱状图数据（可以根据自己需要插入数据）
    private void barChart(String[] transverse, int[] data) {
        LinearLayout column;//柱状图绘制的地方
        column = findViewById(R.id.column);
        //这里的数据是根据你横列有几个来设的，如上面的横列星期有周一到周日，所以这里设置七个数据
        //这里的颜色就对应线条、文字和柱状图（可以根据自己的需要到color里设置）
        List<Integer> color = new ArrayList<>();
        color.add(R.color.colorAccent);
        color.add(R.color.colorPrimary);
        color.add(R.color.color_blue);
        column.addView(new ColumnView(this, transverse, color, data));
    }
    //将数据库中取出的数据调整形式后用于绘制柱状图
}
