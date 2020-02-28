package com.my.smartplanner.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.my.smartplanner.DatabaseHelper.TodoDatabaseHelper;
import com.my.smartplanner.MyLayoutAnimationHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.activity.ManageTodoTagsActivity;
import com.my.smartplanner.adapter.TodoItemAdapter;
import com.my.smartplanner.Item.TodoListItem;
import com.my.smartplanner.util.CalendarUtil;
import com.my.smartplanner.util.LogUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/**
 * 待办事项页面的Fragment
 */
public class TodoFragment extends LazyLoadFragment/*BaseFragment*/ {
    private TodoItemAdapter todoItemAdapter;
    private RecyclerView todoListRecyclerView;
    private List<TodoListItem> list = new LinkedList<>();

    private static final int QUERY_TODAY = 0;//今天
    private static final int QUERY_IMPORTANT = 1;//重要
    private static final int QUERY_HAS_PLANNED = 2;//已计划
    private static final int QUERY_HAS_NOT_PLANNED = 3;//未计划
    private static final int QUERY_ALL = 4;//全部
    private int queryType;//查询哪种类型的待办
    private boolean isFirstSelect = true;//spinner是否是初次选中

    private List<String> filterList = new ArrayList<>();//筛选列表项
    private boolean showCompleted;//是否显示已完成待办
    private String filterTag = null;//要展示带有哪个标签的待办

    private String tempFilterTag;
    private boolean tempShowCompleted;

    /*public static final String ARGS_PAGE = "args_page";
    private int mPage;*/

    public static TodoFragment newInstance(/*int page*/) {
        Bundle args = new Bundle();
        //args.putInt(ARGS_PAGE, page);
        TodoFragment fragment = new TodoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mPage = getArguments().getInt(ARGS_PAGE);
    }

    @Override
    public void loadData() {
        //获取偏好的待办类型
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(
                "todo_preference", Context.MODE_PRIVATE);
        queryType = sharedPreferences.getInt("select_todo_type", QUERY_ALL);

        //获取是否显示已完成待办的偏好
        showCompleted = sharedPreferences.getBoolean("show_completed", true);

        //从数据库中加载数据
        getDataFromDatabase();
        todoItemAdapter = new TodoItemAdapter(list);
        todoItemAdapter.setShowCompleted(showCompleted);//对adapter进行设置是否显示已完成

        getDataFromTagDataBase();

        /*//给筛选列表项装填数据
        filterList.clear();
        filterList.add(mActivity.getString(R.string.do_not_use_tag_filter));
        //从数据库中读取标签
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(getContext(), "TodoDatabase.db", null, TodoDatabaseHelper.NOW_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TodoTag", null);
        if (cursor.moveToFirst()) {
            do {
                filterList.add(cursor.getString(cursor.getColumnIndex("tag_name")));
            } while (cursor.moveToNext());
        }
        cursor.close();*/
    }

    @Override
    public void loadView() {
        todoListRecyclerView = mRootView.findViewById(R.id.todo_list_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //动画
        AnimationSet animation = MyLayoutAnimationHelper.getAnimationSetAlpha();
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        controller.setDelay(0.1f);
        todoListRecyclerView.setLayoutAnimation(controller);

        todoListRecyclerView.setLayoutManager(layoutManager);
        todoListRecyclerView.setAdapter(todoItemAdapter);

        //spinner相关
        AppCompatSpinner typeSpinner = mRootView.findViewById(R.id.todo_page_type_spinner);
        typeSpinner.setSelection(queryType);
        //设置选中的监听器
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                queryType = position;//修改成员变量
                if (isFirstSelect) {
                    //页面初始化时首次选中，数据已经加载好了，无需刷新
                    isFirstSelect = false;
                } else {
                    SharedPreferences.Editor editor = mActivity.getSharedPreferences(
                            "todo_preference", Context.MODE_PRIVATE).edit();
                    editor.putInt("select_todo_type", queryType);//存储偏好
                    editor.apply();
                    refresh();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //筛选器区域
        LinearLayout filterArea = mRootView.findViewById(R.id.todo_page_filter_area);
        //设置点击筛选器区域的事件
        filterArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);//对话框
                dialogBuilder.setView(R.layout.todo_filter_dialog);//设置自定义对话框布局
                dialogBuilder.setTitle(mActivity.getString(R.string.filter));//设置对话框标题

                //设置点击确定按钮的事件
                dialogBuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //修改成员变量
                        filterTag = tempFilterTag;
                        showCompleted = tempShowCompleted;
                        todoItemAdapter.setShowCompleted(showCompleted);//给adapter传递消息
                        SharedPreferences.Editor editor = mActivity.getSharedPreferences(
                                "todo_preference", Context.MODE_PRIVATE).edit();
                        editor.putBoolean("show_completed", showCompleted);//存储偏好
                        editor.apply();
                        refresh();
                    }
                });

                //设置点击取消按钮的事件
                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                //设置管理标签按钮的事件
                dialogBuilder.setNeutralButton(R.string.manage_todo_tags, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(mActivity, ManageTodoTagsActivity.class);
                        startActivity(intent);
                    }
                });

                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();//要先show()才能findViewById()
                AppCompatSpinner spinner = alertDialog.findViewById(R.id.todo_filter_dialog_spinner);
                final AppCompatCheckBox checkBox = alertDialog.findViewById(R.id.todo_filter_dialog_checkbox);
                if (spinner != null) {//TODO may null
                    tempFilterTag = filterTag;
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity,
                            android.R.layout.simple_spinner_item, filterList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    int setSelectionPosition;//下拉菜单初始选中位置
                    if (filterTag == null) {
                        setSelectionPosition = 0;//无筛选标签
                    } else {
                        setSelectionPosition = filterList.indexOf(filterTag);
                    }
                    spinner.setSelection(setSelectionPosition);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent,
                                                   View view, int position, long id) {
                            if (position == 0) {//选中的是“无筛选标签”
                                tempFilterTag = null;
                            } else {
                                tempFilterTag = (String) parent.getSelectedItem();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                }
                if (checkBox != null) {
                    tempShowCompleted = showCompleted;
                    checkBox.setChecked(tempShowCompleted);
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            tempShowCompleted = !tempShowCompleted;
                        }
                    });
                }
            }
        });

        LogUtil.d("old_phone","load view in method ok");
    }

    /**
     * @return 返回该Fragment的layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_todo;
    }

    /**
     * 按相应要求进行SQL查询并返回Cursor对象
     *
     * @param db 数据库对象
     * @return 查询结果的Cursor对象
     */
    private Cursor createQueryCursor(SQLiteDatabase db) {
        final String SELECT_FROM = "SELECT id, title, is_complete, is_star, alarm, note, date FROM TodoList";

        final String QUERY_TODAY_WHERE = "(date = ?)";//查询今天的待办
        final String QUERY_IMPORTANT_WHERE = "(is_star = 1)";//查询重要的待办
        final String QUERY_HAS_PLANNED_WHERE = "(NOT date IS NULL)";//查询已计划的待办
        final String QUERY_HAS_NOT_PLANNED_WHERE = "(date IS NULL)";//查询未计划的待办
        final String QUERY_ALL_WHERE = "(NOT id IS NULL)";//查询全部

        final String NOT_SHOW_COMPLETED_WHERE = "(is_complete = 0)";//不显示已完成

        final String FILTER_TAG_WHERE = "(tag GLOB ?)";//筛选出含有某个标签的待办

        final String ORDER_BY = "ORDER BY is_complete ASC, date ASC";

        List<String> selectionArgs = new ArrayList<>();//查询参数
        StringBuilder querySQL = new StringBuilder(SELECT_FROM);
        querySQL.append(" WHERE ");
        switch (queryType) {
            case QUERY_TODAY:
                querySQL.append(QUERY_TODAY_WHERE);
                //添加一个查询参数：今天的日期
                Calendar today = Calendar.getInstance();
                today.setTime(new Date());
                selectionArgs.add(CalendarUtil.calendarToString(today, "yyyy-MM-dd"));
                break;
            case QUERY_IMPORTANT:
                querySQL.append(QUERY_IMPORTANT_WHERE);
                break;
            case QUERY_HAS_PLANNED:
                querySQL.append(QUERY_HAS_PLANNED_WHERE);
                break;
            case QUERY_HAS_NOT_PLANNED:
                querySQL.append(QUERY_HAS_NOT_PLANNED_WHERE);
                break;
            default://QUERY_ALL
                querySQL.append(QUERY_ALL_WHERE);
                break;
        }
        if (!showCompleted) {//若要求不展示已完成待办，则加一个AND条件
            querySQL.append(" AND ").append(NOT_SHOW_COMPLETED_WHERE);
        }
        //filterTag = "ttt";
        if (filterTag != null) {//筛选标签
            querySQL.append(" AND ").append(FILTER_TAG_WHERE);
            //添加一个查询参数：标签
            selectionArgs.add("* " + filterTag + " *");
        }
        querySQL.append(ORDER_BY);
        return db.rawQuery(querySQL.toString(), selectionArgs.toArray(new String[0]));
    }

    /**
     * 从数据库中载入数据
     */
    private void getDataFromDatabase() {
        list.clear();
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(getContext(), "TodoDatabase.db", null, TodoDatabaseHelper.NOW_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = createQueryCursor(db);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                boolean isComplete = cursor.getInt(cursor.getColumnIndex("is_complete")) == 1;
                boolean isStar = cursor.getInt(cursor.getColumnIndex("is_star")) == 1;
                String note = cursor.getString(cursor.getColumnIndex("note"));
                boolean hasAlarm = !cursor.isNull(cursor.getColumnIndex("alarm"));
                Calendar date = null;
                if (!cursor.isNull(cursor.getColumnIndex("date"))) {
                    String dateString = cursor.getString(cursor.getColumnIndex("date"));
                    date = CalendarUtil.stringToCalendar(dateString, "yyyy-MM-dd");
                }
                TodoListItem item = new TodoListItem(id, title, isComplete, isStar, hasAlarm, note, date);
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        LogUtil.d("old_phone","todo list db ok");
    }

    /**
     * 从数据库中给筛选列表项装填数据
     */
    private void getDataFromTagDataBase() {
        //给筛选列表项装填数据
        filterList.clear();
        filterList.add(mActivity.getString(R.string.do_not_use_tag_filter));
        //从数据库中读取标签
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(getContext(), "TodoDatabase.db", null, TodoDatabaseHelper.NOW_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TodoTag", null);
        if (cursor.moveToFirst()) {
            do {
                filterList.add(cursor.getString(cursor.getColumnIndex("tag_name")));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * 刷新整个List并通知RecyclerView刷新视图
     */
    public void refresh() {
        //TODO bug
        getDataFromDatabase();
        getDataFromTagDataBase();
        //loadData();
        todoItemAdapter.notifyDataSetChanged();
        //todoListRecyclerView.getAdapter().notifyDataSetChanged();
        todoListRecyclerView.scheduleLayoutAnimation();
        //TODO
    }

    /**
     * 通知有条目被更新
     *
     * @param listIndex  条目在List中的位置
     * @param databaseId 条目在数据库中的id
     */
    public void updateChange(int listIndex, int databaseId) {
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(getContext(), "TodoDatabase.db", null, TodoDatabaseHelper.NOW_VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, title, is_complete, is_star, alarm, note, date FROM TodoList WHERE id = ?",
                new String[]{"" + databaseId});
        cursor.moveToFirst();
        int id = cursor.getInt(cursor.getColumnIndex("id"));
        String title = cursor.getString(cursor.getColumnIndex("title"));
        boolean isComplete = cursor.getInt(cursor.getColumnIndex("is_complete")) == 1;
        boolean isStar = cursor.getInt(cursor.getColumnIndex("is_star")) == 1;
        String note = cursor.getString(cursor.getColumnIndex("note"));
        boolean hasAlarm = !cursor.isNull(cursor.getColumnIndex("alarm"));
        Calendar date = null;
        if (!cursor.isNull(cursor.getColumnIndex("date"))) {
            String dateString = cursor.getString(cursor.getColumnIndex("date"));
            date = CalendarUtil.stringToCalendar(dateString, "yyyy-MM-dd");
        }
        TodoListItem item = new TodoListItem(id, title, isComplete, isStar, hasAlarm, note, date);
        list.set(listIndex, item);
        cursor.close();

        todoItemAdapter.notifyItemChanged(listIndex);

        //更新标签的数据
        getDataFromTagDataBase();//TODO 异步加载数据
    }

    /**
     * 通知有条目被移除
     *
     * @param listIndex 被移除的条目在List中的位置
     */
    public void removeItemUpdate(int listIndex) {
        list.remove(listIndex);
        todoItemAdapter.notifyItemRemoved(listIndex);
    }

}
