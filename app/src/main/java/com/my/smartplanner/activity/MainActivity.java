package com.my.smartplanner.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.jaeger.library.StatusBarUtil;
import com.my.smartplanner.DatabaseHelper.TodoDatabaseHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.fragment.TodoFragment;
import com.my.smartplanner.adapter.ViewPagerAdapter;

import java.lang.ref.WeakReference;
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
    private TodoFragment todoPageFragment;
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
        addFab = findViewById(R.id.home_add_fab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO 更改启动Activity的方式
                Intent intent = new Intent(MainActivity.this, TodoDetailActivity.class);
                intent.putExtra("mode", TodoDetailActivity.CREATE_MODE);
                //startActivity(intent);
                startActivityForResult(intent, 2);
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
        todoPageFragment = TodoFragment.newInstance();
        todoFragment3 = TodoFragment.newInstance();
        fragments.add(todoFragment1);
        fragments.add(todoPageFragment);
        fragments.add(todoFragment3);
        List<String> titleList = new ArrayList<>();
        titleList.add(getString(R.string.advice));
        titleList.add(getString(R.string.todo));
        titleList.add(getString(R.string.habit));
        tabLayout = findViewById(R.id.home_tab);
        viewPager = findViewById(R.id.home_view_pager_content);
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

    /**
     * 加载菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_page_menu, menu);
        return true;
    }

    /**
     * 菜单选中事件
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.todo_page_refresh:
                todoPageFragment.refresh();
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

    /**
     * 删除所有待办数据
     */
    private void deleteALLTodo() {
        new DeleteAllTodoTask(this, todoPageFragment).execute();
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(MainActivity.this, "TodoDatabase.db", null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("DELETE FROM TodoList");
            }
        }).start();*/
    }

    /**
     * 添加50条待办数据
     */
    private void addMany() {
        new AddManyTask(this, todoPageFragment).execute();
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(MainActivity.this, "TodoDatabase.db", null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                for (int i = 1; i <= 50; i++) {
                    db.execSQL("INSERT INTO TodoList (title,is_complete,is_star,alarm,note,date,create_time) " +
                            "VALUES ('Here is the title.',0,0,'2019-12-12 12:12','Here is a note.','2019-11-11','2019-11-11 11:11:00')");
                }
            }
        }).start();*/
    }

    static class DeleteAllTodoTask extends AsyncTask<Void, Integer, Boolean> {

        private WeakReference<MainActivity> activityReference;
        private WeakReference<TodoFragment> todoFragmentReference;

        //仅持有对activity和Fragment的弱引用
        DeleteAllTodoTask(MainActivity mainActivity, TodoFragment todoFragment) {
            activityReference = new WeakReference<>(mainActivity);
            todoFragmentReference = new WeakReference<>(todoFragment);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(activityReference.get(), "TodoDatabase.db", null, 1);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("DELETE FROM TodoList");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            //super.onPostExecute(aBoolean);
            if (aBoolean) {
                todoFragmentReference.get().refresh();
            }
        }
    }

    static class AddManyTask extends AsyncTask<Void, Integer, Boolean> {

        private WeakReference<MainActivity> activityReference;
        private WeakReference<TodoFragment> todoFragmentReference;

        //仅持有对activity和Fragment的弱引用
        AddManyTask(MainActivity mainActivity, TodoFragment todoFragment) {
            activityReference = new WeakReference<>(mainActivity);
            todoFragmentReference = new WeakReference<>(todoFragment);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(activityReference.get(), "TodoDatabase.db", null, 1);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            for (int i = 1; i <= 50; i++) {
                db.execSQL("INSERT INTO TodoList (title,is_complete,is_star,alarm,note,date,create_time) " +
                        "VALUES ('Here is the title.',0,0,'2019-12-12 12:12','Here is a note.','2019-11-11','2019-11-11 11:11:00')");
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            //super.onPostExecute(aBoolean);
            if (aBoolean) {
                todoFragmentReference.get().refresh();
            }
        }
    }

    /**
     * 提取从其他活动返回的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO 请求码requestCode
        if (resultCode == RESULT_OK)//有变化
        {
            if (data != null) {
                int returnStatus = data.getIntExtra("return_status", 0);
                if (returnStatus == TodoDetailActivity.RETURN_STATUS_ADD_NEW) {//新增
                    todoPageFragment.refresh();
                } else if (returnStatus == TodoDetailActivity.RETURN_STATUS_CHANGE_ITEM) {//修改
                    int listIndex = data.getIntExtra("list_index", 0);
                    int databaseId = data.getIntExtra("database_id", 0);
                    todoPageFragment.updateChange(listIndex, databaseId);
                } else if (returnStatus == TodoDetailActivity.RETURN_STATUS_REMOVE_ITEM) {//移除
                    int pos = data.getIntExtra("list_index", 0);
                    todoPageFragment.removeItemUpdate(pos);//TODO 更简洁地与Fragment通信
                }
            }
        }
    }

}
