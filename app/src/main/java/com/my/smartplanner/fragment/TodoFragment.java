package com.my.smartplanner.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.my.smartplanner.DatabaseHelper.TodoDatabaseHelper;
import com.my.smartplanner.MyLayoutAnimationHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.adapter.TodoItemAdapter;
import com.my.smartplanner.TodoListItem;
import com.my.smartplanner.util.CalendarUtil;

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

    //查询哪种类型的待办
    private static final int QUERY_TODAY = 0;
    private static final int QUERY_IMPORTANT = 1;
    private static final int QUERY_HAS_PLANNED = 2;
    private static final int QUERY_HAS_NOT_PLANNED = 3;
    private static final int QUERY_ALL = 4;
    private int queryType;
    private boolean isFirstSelect = true;//spinner是否是初次选中

    private List<String> filterList = new ArrayList<>();//筛选列表项
    private boolean showCompleted;//是否显示已完成待办

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

        //从数据库中加载数据
        initData();
        todoItemAdapter = new TodoItemAdapter(list);

        //获取是否显示已完成待办的偏好
        showCompleted = sharedPreferences.getBoolean("show_completed",true);
        todoItemAdapter.setShowCompleted(showCompleted);//对adapter进行设置

        //给筛选列表项装填数据
        filterList.clear();
        filterList.add("显示已完成的任务");
        filterList.add("tag1");
        filterList.add("tag1");
        filterList.add("tag1");
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
        filterArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
                dialog.setTitle(mActivity.getString(R.string.filter));
                //用于记录哪个筛选项被选中的数组
                final boolean[] checkedItems = new boolean[filterList.size()];
                checkedItems[0] = showCompleted;//初始化“显示已完成”这一项
                //多选事件
                dialog.setMultiChoiceItems(filterList.toArray(new String[0]),
                        checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                });
                //点击确定按钮的事件
                dialog.setPositiveButton(mActivity.getString(R.string.confirm),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showCompleted = checkedItems[0];//修改成员变量
                        todoItemAdapter.setShowCompleted(showCompleted);//给adapter传递消息
                        SharedPreferences.Editor editor = mActivity.getSharedPreferences(
                                "todo_preference", Context.MODE_PRIVATE).edit();
                        editor.putBoolean("show_completed",showCompleted);//存储偏好
                        editor.apply();
                        //TODO 其他筛选项被选中
                        refresh();
                    }
                });
                //点击取消按钮的事件
                dialog.setNegativeButton(mActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.show();
            }
        });
    }

    /**
     * @return 返回该Fragment的layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_todo;
    }

    /**
     * 从数据库中载入数据
     */
    private void initData() {
        list.clear();
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(getContext(), "TodoDatabase.db", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int is_complete_bound;
        if (showCompleted) {
            is_complete_bound = 1;
        } else {
            is_complete_bound = 0;
        }
        Cursor cursor;
        switch (queryType) {
            case QUERY_TODAY:
                Calendar today = Calendar.getInstance();
                today.setTime(new Date());
                cursor = db.rawQuery("SELECT id, title, is_complete, is_star, alarm, note, date " +
                                "FROM TodoList WHERE date = ? AND is_complete <= ?" +
                                "ORDER BY is_complete ASC, date ASC",
                        new String[]{CalendarUtil.calendarToString(today, "yyyy-MM-dd"), is_complete_bound + ""});
                break;
            case QUERY_IMPORTANT:
                cursor = db.rawQuery("SELECT id, title, is_complete, is_star, alarm, note, date " +
                        "FROM TodoList WHERE is_star = 1 AND is_complete <= ?" +
                        "ORDER BY is_complete ASC, date ASC", new String[]{is_complete_bound + ""});
                break;
            case QUERY_HAS_PLANNED:
                cursor = db.rawQuery("SELECT id, title, is_complete, is_star, alarm, note, date " +
                        "FROM TodoList WHERE NOT date IS NULL AND is_complete <= ?" +
                        "ORDER BY is_complete ASC, date ASC", new String[]{is_complete_bound + ""});
                break;
            case QUERY_HAS_NOT_PLANNED:
                cursor = db.rawQuery("SELECT id, title, is_complete, is_star, alarm, note, date " +
                        "FROM TodoList WHERE date IS NULL AND is_complete <= ?" +
                        "ORDER BY is_complete ASC, date ASC", new String[]{is_complete_bound + ""});
                break;
            default://QUERY_ALL
                cursor = db.rawQuery("SELECT id, title, is_complete, is_star, alarm, note, date " +
                        "FROM TodoList WHERE is_complete <= ? " +
                        "ORDER BY is_complete ASC, date ASC", new String[]{is_complete_bound + ""});
                break;
        }
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
    }

    /**
     * 刷新整个List并通知RecyclerView刷新视图
     */
    public void refresh() {
        initData();
        todoItemAdapter.notifyDataSetChanged();
        todoListRecyclerView.scheduleLayoutAnimation();
    }

    /**
     * 通知有条目被更新
     *
     * @param listIndex  条目在List中的位置
     * @param databaseId 条目在数据库中的id
     */
    public void updateChange(int listIndex, int databaseId) {
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(getContext(), "TodoDatabase.db", null, 1);
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
