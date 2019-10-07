package com.my.smartplanner.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.Toast;

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

    private AppCompatSpinner typeSpinner;

    private static final int QUERY_TODAY = 0;
    private static final int QUERY_IMPORTANT = 1;
    private static final int QUERY_HAS_PLANNED = 2;
    private static final int QUERY_HAS_NOT_PLANNED = 3;
    private static final int QUERY_ALL = 4;
    private int queryType;
    private boolean isFirstSelect = true;

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
        typeSpinner = mRootView.findViewById(R.id.todo_page_type_spinner);
        typeSpinner.setSelection(queryType);
        //设置选中的监听器
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                queryType = position;
                if (isFirstSelect) {
                    //页面初始化时首次选中，数据已经加载好了，无需刷新
                    isFirstSelect = false;
                } else {
                    SharedPreferences.Editor editor = mActivity.getSharedPreferences(
                            "todo_preference", Context.MODE_PRIVATE).edit();
                    editor.putInt("select_todo_type", queryType);
                    editor.apply();
                    refresh();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
        Cursor cursor;
        switch (queryType) {
            case QUERY_TODAY:
                Calendar today = Calendar.getInstance();
                today.setTime(new Date());
                cursor = db.rawQuery("SELECT id, title, is_complete, is_star, alarm, note, date " +
                                "FROM TodoList WHERE date = ? " +
                                "ORDER BY is_complete ASC, date ASC",
                        new String[]{CalendarUtil.calendarToString(today, "yyyy-MM-dd")});
                break;
            case QUERY_IMPORTANT:
                cursor = db.rawQuery("SELECT id, title, is_complete, is_star, alarm, note, date " +
                        "FROM TodoList WHERE is_star = 1 " +
                        "ORDER BY is_complete ASC, date ASC", null);
                break;
            case QUERY_HAS_PLANNED:
                cursor = db.rawQuery("SELECT id, title, is_complete, is_star, alarm, note, date " +
                        "FROM TodoList WHERE NOT date IS NULL " +
                        "ORDER BY is_complete ASC, date ASC", null);
                break;
            case QUERY_HAS_NOT_PLANNED:
                cursor = db.rawQuery("SELECT id, title, is_complete, is_star, alarm, note, date " +
                        "FROM TodoList WHERE date IS NULL " +
                        "ORDER BY is_complete ASC, date ASC", null);
                break;
            default://QUERY_ALL
                cursor = db.rawQuery("SELECT id, title, is_complete, is_star, alarm, note, date " +
                        "FROM TodoList ORDER BY is_complete ASC, date ASC", null);
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
