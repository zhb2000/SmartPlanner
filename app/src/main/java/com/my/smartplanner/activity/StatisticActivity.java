package com.my.smartplanner.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.my.smartplanner.R;

/**
 * 统计页面的Activity
 */
public class StatisticActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        //Toolbar相关操作
        Toolbar toolbar = findViewById(R.id.statistic_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //步数统计卡片
        CardView healthCard = findViewById(R.id.statistic_health_card);
        healthCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(StatisticActivity.this,StepStatisticActivity.class);
                Intent intent = new Intent(StatisticActivity.this, HealthActivity.class);
                startActivity(intent);
            }
        });

        CardView healthCard2 = findViewById(R.id.statistic_health_card2);
        healthCard2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StatisticActivity.this, ChartActivity.class);
                startActivity(intent);
            }
        });
    }


    /**
     * 菜单选中事件：返回的箭头
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //点击返回的箭头
            finish();
        }
        return true;
    }

}
