package com.my.smartplanner.fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.my.smartplanner.DatabaseHelper.TodoDatabaseHelper;
import com.my.smartplanner.MyLayoutAnimationHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.adapter.TodoItemAdapter;
import com.my.smartplanner.other.TodoListItem;
import com.my.smartplanner.util.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * 待办事项页面的Fragment
 */
public class TodoFragment extends LazyLoadFragment/*BaseFragment*/ {
    private TodoItemAdapter todoItemAdapter;
    private RecyclerView todoListRecyclerView;
    private List<TodoListItem> list = new ArrayList<>();

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
        initData();
        todoItemAdapter = new TodoItemAdapter(list);
    }

    @Override
    public void loadView() {
        todoListRecyclerView = mRootView.findViewById(R.id.todo_list_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        AnimationSet animation = MyLayoutAnimationHelper.getAnimationSetAlpha();
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        controller.setDelay(0.1f);
        todoListRecyclerView.setLayoutAnimation(controller);

        todoListRecyclerView.setLayoutManager(layoutManager);
        todoListRecyclerView.setAdapter(todoItemAdapter);
    }

    /**
     * @return 返回该Fragment的layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_todo;
    }

    //暂时测试
    private void initDataForTest() {
        List<TodoListItem> list = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Calendar tempDate = Calendar.getInstance();
            tempDate.set(2019, 9 - 1, 9);
            String title = "This is the title.";
            String note = "Here is a note.";
            TodoListItem item = new TodoListItem(2233, title, false, false, true, note, tempDate);
            list.add(item);
        }
        todoItemAdapter = new TodoItemAdapter(list);
    }

    /**
     * 从数据库中载入数据
     */
    private void initData() {
        list.clear();
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(getContext(), "TodoDatabase.db", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, title, is_complete, is_star, alarm, note, date FROM TodoList",
                null);
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
                    date = DateUtil.stringToCalendar(dateString,"yyyy-MM-dd");
                }
                TodoListItem item = new TodoListItem(id, title, isComplete, isStar, hasAlarm, note, date);
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * 刷新List并通知RecyclerView刷新视图
     */
    public void refresh(){
        initData();
        todoItemAdapter.notifyDataSetChanged();
        todoListRecyclerView.scheduleLayoutAnimation();
    }

}
