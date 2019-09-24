package com.my.smartplanner.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.jaeger.library.StatusBarUtil;
import com.my.smartplanner.DatabaseHelper.TodoDatabaseHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.fragment.TodoFragment;
import com.my.smartplanner.adapter.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/*主页的Activity*/
public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;//标题栏
    private DrawerLayout mDrawerLayout;//滑动抽屉
    private NavigationView navigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton addFab;//添加待办浮动按钮

    //TODO 怎样更好地获取fragment的实例
    private TodoFragment todoFragment1;
    private TodoFragment todoFragment2;
    private TodoFragment todoFragment3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //滑动菜单相关操作
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_main_page);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mDrawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.nav_main_page:
                        break;
                    case R.id.nav_tomato:
                        //do something
                        break;
                    case R.id.nav_statistic:
                        //do something
                        break;

                    case R.id.nav_calendar:
                        //do something
                        break;
                    case R.id.nav_setting:
                        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_about:
                        //do something
                        break;
                }
                return true;
            }
        });

        //Toolbar相关操作
        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_menu);
        }

        //浮动按钮相关
        addFab = (FloatingActionButton) findViewById(R.id.home_add_fab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TodoDetailActivity.class);
                intent.putExtra("mode", TodoDetailActivity.CREATE_MODE);
                startActivity(intent);
            }
        });
        /*addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "pop up", Snackbar.LENGTH_SHORT)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(MainActivity.this, "click undo", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        });*/

        //tab栏相关操作
        List<Fragment> fragments = new ArrayList<>();
        todoFragment1 = TodoFragment.newInstance();
        todoFragment2 = TodoFragment.newInstance();
        todoFragment3 = TodoFragment.newInstance();
        fragments.add(todoFragment1);
        fragments.add(todoFragment2);
        fragments.add(todoFragment3);
        List<String> titleList = new ArrayList<>();
        titleList.add(getString(R.string.advice));
        titleList.add(getString(R.string.todo));
        titleList.add(getString(R.string.habit));
        tabLayout = (TabLayout) findViewById(R.id.home_tab);
        viewPager = (ViewPager) findViewById(R.id.home_view_pager_content);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments, titleList);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);//修改缓存页数
        tabLayout.setupWithViewPager(viewPager);
        //重写选中行为
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    addFab.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    addFab.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        TabLayout.Tab defaultSelectTab = tabLayout.getTabAt(1);//默认选中的页面
        if (defaultSelectTab != null) {
            defaultSelectTab.select();
        }

        //状态栏
        StatusBarUtil.setColorForDrawerLayout(this, mDrawerLayout,
                getResources().getColor(R.color.colorPrimary), 0);

    }

    @Override
    protected void onStart() {
        super.onStart();
        navigationView.setCheckedItem(R.id.nav_main_page);//侧滑抽屉选中
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//加载菜单
        getMenuInflater().inflate(R.menu.todo_page_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {//菜单选中事件
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.todo_page_refresh:
                todoPageRefresh();
                break;
            case R.id.todo_page_add_many:
                addMany();
                break;
            case R.id.todo_page_delete_all_todo:
                deleteALLTodo();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawers();
        }else{
            super.onBackPressed();
        }
    }

    /*刷新待办页面*/
    private void todoPageRefresh() {
        //TODO 刷新待办页面
        todoFragment2.refresh();
    }

    /*删除所有待办数据*/
    private void deleteALLTodo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(MainActivity.this, "TodoDatabase.db", null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("DELETE FROM TodoList");
            }
        }).start();
    }

    /*添加50条待办数据*/
    private void addMany() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(MainActivity.this, "TodoDatabase.db", null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                for (int i = 1; i <= 50; i++) {
                    db.execSQL("INSERT INTO TodoList (title,is_complete,is_star,alarm,note,date,create_time) " +
                            "VALUES ('Here is the title.',0,0,'2019-12-12 12:12','Here is a note.','2019-11-11','2019-11-11 11:11:00')");
                }
            }
        }).start();
    }

}
