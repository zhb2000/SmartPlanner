package com.my.smartplanner.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.jaeger.library.StatusBarUtil;
import com.my.smartplanner.R;
import com.my.smartplanner.adapter.ViewPagerAdapter;
import com.my.smartplanner.fragment.BlankFragment;
import com.my.smartplanner.fragment.TodoFragment;
import com.my.smartplanner.item.TodoListItem;

import java.util.ArrayList;
import java.util.List;

/*主页的Activity*/
public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    /**
     * 滑动抽屉
     */
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    /**
     * 添加待办浮动按钮
     */
    private FloatingActionButton addFab;

    //TODO 怎样更好地获取fragment的实例
    private BlankFragment blankFragmentLeft;
    private TodoFragment todoPageFragment;
    private BlankFragment blankFragmentRight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        navDrawerSetting();
        toolbarSetting();
        statusBarSetting();
        fabSetting();
        tabPageSetting();
    }

    @Override
    protected void onStart() {
        super.onStart();
        navigationView.setCheckedItem(R.id.nav_main_page);//侧滑抽屉选中
    }

    /**
     * 加载菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_page_menu, menu);
        return true;
    }

    /**
     * 菜单选中事件：返回、刷新、管理标签
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.todo_page_menu_refresh:
                todoPageFragment.addNewItemOrRefresh();
                break;
            case R.id.todo_page_menu_manage_todo_tag:
                ManageTodoTagsActivity.startTheActivityForResult(this, this, OPEN_MANAGE_TAG);
                break;
        }
        return true;
    }

    /**
     * 按下返回键
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }


    public static final int OPEN_TODO_DETAIL = 1;
    public static final int OPEN_MANAGE_TAG = 2;

    /**
     * 提取从其他活动返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_TODO_DETAIL && resultCode == RESULT_OK) {
            //从详情页返回，且有变化
            if (data != null) {
                int returnStatus = data.getIntExtra(TodoDetailActivity.RETURN_INTENT_STATUS, 0);
                if (returnStatus == TodoDetailActivity.RETURN_STATUS_ADD_NEW) {
                    //新增
                    todoPageFragment.addNewItemOrRefresh();
                } else if (returnStatus == TodoDetailActivity.RETURN_STATUS_CHANGE_ITEM) {
                    //修改
                    int listIndex = data.getIntExtra(TodoDetailActivity.RETURN_INTENT_POSITION, 0);
                    TodoListItem item = (TodoListItem) data.getSerializableExtra(TodoDetailActivity.RETURN_INTENT_ITEM);
                    todoPageFragment.updateItem(listIndex, item);
                } else if (returnStatus == TodoDetailActivity.RETURN_STATUS_REMOVE_ITEM) {
                    //移除
                    int pos = data.getIntExtra(TodoDetailActivity.RETURN_INTENT_POSITION, 0);
                    todoPageFragment.removeItem(pos);
                }
            }
        } else if (requestCode == OPEN_MANAGE_TAG && resultCode == RESULT_OK) {
            //从标签管理页返回，有变化
            todoPageFragment.modifyTag();
        }
    }


    /**
     * 获取控件引用
     */
    private void findViews() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.home_toolbar);
        addFab = findViewById(R.id.home_add_fab);
        tabLayout = findViewById(R.id.home_tab);
        viewPager = findViewById(R.id.home_view_pager_content);
    }

    /**
     * 设置滑动抽屉
     */
    private void navDrawerSetting() {
        navigationView.setCheckedItem(R.id.nav_main_page);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mDrawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.nav_main_page:
                        break;
                    case R.id.nav_tomato:
                        Intent startTomatoActivityIntent = new Intent(MainActivity.this, TomatoClockActivity.class);
                        startActivity(startTomatoActivityIntent);
                        break;
                    case R.id.nav_statistic:
                        //Intent startStatisticActivityIntent = new Intent(MainActivity.this, StatisticActivity.class);
                        //startActivity(startStatisticActivityIntent);
                        startActivity(new Intent(MainActivity.this, HealthActivity.class));
                        break;
//                    case R.id.nav_calendar:
//                        //do something
//                        break;
                    case R.id.nav_setting:
                        Intent startSettingActivityIntent = new Intent(MainActivity.this, SettingActivity.class);
                        startActivity(startSettingActivityIntent);
                        break;
//                    case R.id.nav_about:
//                        //do something
//                        break;
                }
                return true;
            }
        });
    }

    /**
     * 设置Toolbar
     */
    private void toolbarSetting() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_menu);
        }
    }

    /**
     * 设置状态栏
     */
    private void statusBarSetting() {
        StatusBarUtil.setColorForDrawerLayout(this, mDrawerLayout,
                getResources().getColor(R.color.colorPrimary), 0);
    }

    /**
     * 设置浮动按钮的监听器
     */
    private void fabSetting() {
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TodoDetailActivity.startTheActivityForResultInCreate(
                        MainActivity.this, MainActivity.this, 1);
            }
        });
    }

    /**
     * tab栏和页面设置
     */
    private void tabPageSetting() {
        blankFragmentLeft = BlankFragment.newInstance();
        todoPageFragment = TodoFragment.newInstance();
        blankFragmentRight = BlankFragment.newInstance();
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(blankFragmentLeft);
        fragments.add(todoPageFragment);
        fragments.add(blankFragmentRight);
        List<String> titleList = new ArrayList<>();
        titleList.add(getString(R.string.advice));
        titleList.add(getString(R.string.todo));
        titleList.add(getString(R.string.habit));

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
    }
}
