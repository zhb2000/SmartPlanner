package com.my.smartplanner.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.my.smartplanner.DatabaseHelper.TodoDBHelper;
import com.my.smartplanner.MyLayoutAnimationHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.activity.MainActivity;
import com.my.smartplanner.activity.ManageTodoTagsActivity;
import com.my.smartplanner.adapter.TodoItemAdapter;
import com.my.smartplanner.item.TodoListItem;
import com.my.smartplanner.util.CalendarUtil;
import com.my.smartplanner.util.LogUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;


/**
 * 待办事项页面的Fragment
 */
public class TodoFragment extends LazyLoadFragment {
    private TodoItemAdapter todoItemAdapter;
    private RecyclerView todoListRecyclerView;
    private List<TodoListItem> list = new LinkedList<>();

    private AppCompatSpinner typeSpinner;
    private LinearLayout filterArea;
    private TextView filterText;
    private ImageView filterIcon;

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


    public static TodoFragment newInstance() {
        Bundle args = new Bundle();
        TodoFragment fragment = new TodoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * @return 返回该Fragment的layout id
     */
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_todo;
    }

    @Override
    public void loadData() {
        loadPreferences();
        fillItemsWithoutTag();
        fillItemsTags();
        fillTags();
    }

    @Override
    public void loadView() {
        findViews();
        recyclerViewSetting();
        spinnerSetting();
        filterSetting();
        setFilterUI(null);
    }


    /**
     * （从详情返回）新增或点击刷新
     */
    public void addNewItemOrRefresh() {
        new AddNewItemOrRefreshTask(this).execute();
        LogUtil.d("data", "add new or refresh");
    }

    /**
     * 从详情返回，修改
     */
    public void updateItem(int listIndex, TodoListItem item) {
        list.set(listIndex, item);
        todoItemAdapter.notifyItemChanged(listIndex);
        new Thread(new Runnable() {
            @Override
            public void run() {
                fillTags();
            }
        }).start();
    }

    /**
     * 从详情返回，删除
     *
     * @param listIndex 被移除的条目在List中的位置
     */
    public void removeItem(int listIndex) {
        list.remove(listIndex);
        todoItemAdapter.notifyItemRemoved(listIndex);
    }

    /**
     * 从标签管理返回，修改
     */
    public void modifyTag() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                fillTags();
                fillItemsTags();
            }
        }).start();
    }

    /**
     * 更改查询待办类型或更改筛选标签
     */
    private void changeQueryTypeOrFilterTag() {
        new ChangeQueryTypeOrFilterTagTask(this).execute();
    }


    /**
     * 加载首选项，设置queryType和showCompleted
     */
    private void loadPreferences() {
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(
                "todo_preference", Context.MODE_PRIVATE);
        queryType = sharedPreferences.getInt("select_todo_type", QUERY_ALL);
        showCompleted = sharedPreferences.getBoolean("show_completed", true);
    }

    /**
     * 装填items数组（暂不装填对象的tags）
     */
    private void fillItemsWithoutTag() {
        SQLiteDatabase db = TodoDBHelper.getWDB(getContext());
        Cursor cursor = createQueryCursor(db);
        list.clear();
        list.addAll(getTodoItemsFromCursor(cursor));
        cursor.close();
        db.close();
    }

    /**
     * 装填items中对象的tags
     */
    private void fillItemsTags() {
        SQLiteDatabase db = TodoDBHelper.getWDB(getContext());
        for (TodoListItem item : list) {
            item.clearTags();
            Cursor cursor1 = db.rawQuery("SELECT DISTINCT tag FROM TodoTag WHERE id = ?",
                    new String[]{String.valueOf(item.getId())});
            if (cursor1.moveToFirst()) {
                do {
                    item.addTag(cursor1.getString(cursor1.getColumnIndex("tag")));
                } while (cursor1.moveToNext());
            }
            cursor1.close();
        }
        db.close();
    }

    /**
     * 装填tags数组
     */
    private void fillTags() {
        SQLiteDatabase db = TodoDBHelper.getWDB(getContext());
        filterList.clear();
        filterList.add(mActivity.getString(R.string.do_not_use_tag_filter));
        Cursor cursor = db.rawQuery("SELECT DISTINCT tag FROM TodoTag", null);
        if (cursor.moveToFirst()) {
            do {
                filterList.add(cursor.getString(cursor.getColumnIndex("tag")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }


    /**
     * 按相应要求进行SQL查询并返回Cursor对象
     *
     * @param db 数据库对象
     * @return 查询结果的Cursor对象
     */
    private Cursor createQueryCursor(SQLiteDatabase db) {
        final String QUERY_TODAY_WHERE = "(end_date = ?)";//条件：今天
        final String QUERY_IMPORTANT_WHERE = "(is_star = 1)";//条件：加星
        final String QUERY_HAS_PLANNED_WHERE = "(NOT end_date IS NULL)";//条件：已计划
        final String QUERY_HAS_NOT_PLANNED_WHERE = "(end_date IS NULL)";//条件：未计划
        final String QUERY_NOT_COMPLETED_WHERE = "(is_complete = 0)";//条件：未完成
        final String QUERY_TAG_WHERE = "id IN (SELECT DISTINCT id from TodoTag WHERE tag = ?)";

        StringBuilder selection = new StringBuilder();//条件语句
        List<String> selectionArgs = new ArrayList<>();//查询参数
        //尝试添加查询条件1 ---- 来自下拉框中的类型
        switch (queryType) {
            case QUERY_TODAY:
                selection.append(QUERY_TODAY_WHERE);
                //添加一个查询参数：今天的日期
                selectionArgs.add(CalendarUtil.calendarToString(
                        Calendar.getInstance(), TodoDBHelper.END_DATE_PATTERN));
                break;
            case QUERY_IMPORTANT:
                selection.append(QUERY_IMPORTANT_WHERE);
                break;
            case QUERY_HAS_PLANNED:
                selection.append(QUERY_HAS_PLANNED_WHERE);
                break;
            case QUERY_HAS_NOT_PLANNED:
                selection.append(QUERY_HAS_NOT_PLANNED_WHERE);
                break;
        }
        //尝试添加查询条件2 ---- 来自“显示已完成”复选框（只显示未完成）
        if (!showCompleted) {
            if (selection.length() != 0) {
                selection.append(" AND ");
            }
            selection.append(QUERY_NOT_COMPLETED_WHERE);
        }
        //尝试添加查询条件3 ---- 要求包含某个标签
        if (filterTag != null) {
            if (selection.length() != 0) {
                selection.append(" AND ");
            }
            selection.append(QUERY_TAG_WHERE);
            //添加一个查询参数：标签
            selectionArgs.add(filterTag);
        }
        return db.query(
                "TodoList", null,
                selection.toString(), selectionArgs.toArray(new String[0]),
                null, null,
                "is_complete ASC, end_date ASC");
    }

    /**
     * 从Cursor中获取装有TodoListItem对象的List
     *
     * @param cursor Cursor对象
     * @return 装有对象的List
     */
    private static List<TodoListItem> getTodoItemsFromCursor(Cursor cursor) {
        List<TodoListItem> items = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id;
                String title;
                boolean isComplete;
                boolean isStar;
                String alarmStr;
                String note;
                String endDateStr;
                String createTimeStr;
                String completeTimeStr;

                id = cursor.getInt(cursor.getColumnIndex("id"));
                title = cursor.getString(cursor.getColumnIndex("title"));
                isComplete = cursor.getInt(cursor.getColumnIndex("is_complete")) == 1;
                isStar = cursor.getInt(cursor.getColumnIndex("is_star")) == 1;
                alarmStr = cursor.getString(cursor.getColumnIndex("alarm"));
                note = cursor.getString(cursor.getColumnIndex("note"));
                endDateStr = cursor.getString(cursor.getColumnIndex("end_date"));
                createTimeStr = cursor.getString(cursor.getColumnIndex("create_time"));
                completeTimeStr = cursor.getString(cursor.getColumnIndex("complete_time"));
                TodoListItem item = new TodoListItem(id, title, isComplete, isStar, alarmStr,
                        note, endDateStr, createTimeStr, completeTimeStr);
                items.add(item);
            } while (cursor.moveToNext());
        }
        return items;
    }


    /**
     * 查找控件的引用
     */
    private void findViews() {
        todoListRecyclerView = mRootView.findViewById(R.id.todo_list_recycler_view);
        typeSpinner = mRootView.findViewById(R.id.todo_page_type_spinner);
        filterArea = mRootView.findViewById(R.id.todo_page_filter_area);
        filterText = mRootView.findViewById(R.id.todo_page_filter_text);
        filterIcon = mRootView.findViewById(R.id.todo_page_filter_icon);
    }

    /**
     * 设置RecyclerView的布局、Adapter、动画
     */
    private void recyclerViewSetting() {
        //布局
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        todoListRecyclerView.setLayoutManager(layoutManager);
        //Adapter
        todoItemAdapter = new TodoItemAdapter(list);
        todoItemAdapter.setShowCompleted(showCompleted);//对adapter设置是否显示已完成
        todoListRecyclerView.setAdapter(todoItemAdapter);
        //动画
        AnimationSet animation = MyLayoutAnimationHelper.getAnimationSetAlpha();
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        controller.setDelay(0.1f);
        todoListRecyclerView.setLayoutAnimation(controller);
    }

    /**
     * Spinner设置初始选中项、设置监听器
     */
    private void spinnerSetting() {
        typeSpinner.setSelection(queryType);//设置初始选中项
        //设置监听器
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
                    changeQueryTypeOrFilterTag();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * 设置筛选器按钮点击事件、对话框事件
     */
    private void filterSetting() {
        filterArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);//对话框
                dialogBuilder.setView(R.layout.dialog_todo_filter);//设置自定义对话框布局
                dialogBuilder.setTitle(mActivity.getString(R.string.filter));//设置对话框标题

                //设置点击确定按钮的事件
                dialogBuilder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //修改成员变量
                        filterTag = tempFilterTag;
                        showCompleted = tempShowCompleted;
                        setFilterUI(filterTag);
                        todoItemAdapter.setShowCompleted(showCompleted);//给adapter传递消息
                        SharedPreferences.Editor editor = mActivity.getSharedPreferences(
                                "todo_preference", Context.MODE_PRIVATE).edit();
                        editor.putBoolean("show_completed", showCompleted);//存储偏好
                        editor.apply();
                        changeQueryTypeOrFilterTag();
                    }
                });

                //设置点击取消按钮的事件：什么都不干
                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                //设置“管理标签”按钮的事件
                dialogBuilder.setNeutralButton(R.string.manage_todo_tags, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ManageTodoTagsActivity.startTheActivityForResult(mActivity, mActivity, MainActivity.OPEN_MANAGE_TAG);
                    }
                });

                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();//要先show()才能findViewById()
                AppCompatSpinner spinner = alertDialog.findViewById(R.id.todo_filter_dialog_spinner);
                AppCompatCheckBox checkBox = alertDialog.findViewById(R.id.todo_filter_dialog_checkbox);
                if (spinner != null) {
                    filterDialogSpinnerSetting(spinner);
                }
                if (checkBox != null) {
                    filterDialogCheckboxSetting(checkBox);
                }
            }
        });
    }

    /**
     * 设置对话框中Spinner的初始选中和监听器
     */
    private void filterDialogSpinnerSetting(AppCompatSpinner spinner) {
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

    /**
     * 设置对话框中“显示已完成”复选框的初始选中、监听器
     */
    private void filterDialogCheckboxSetting(AppCompatCheckBox checkBox) {
        tempShowCompleted = showCompleted;
        checkBox.setChecked(tempShowCompleted);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempShowCompleted = !tempShowCompleted;
            }
        });
    }

    /**
     * 设置筛选器的UI
     */
    private void setFilterUI(String currentTag) {
        if (currentTag != null) {
            filterText.setTypeface(Typeface.DEFAULT_BOLD);
            filterText.setTextColor(getResources().getColor(R.color.blue));
            String str = getString(R.string.tag_colon) + currentTag;
            filterText.setText(str);
            filterIcon.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);
        } else {
            filterText.setTypeface(Typeface.DEFAULT);
            filterText.setTextColor(getResources().getColor(R.color.grey));
            filterText.setText(getString(R.string.filter));
            filterIcon.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
        }
    }


    /**
     * （从详情返回）新增或点击刷新
     */
    private static class AddNewItemOrRefreshTask extends AsyncTask<Void, Integer, Boolean> {
        private WeakReference<TodoFragment> weakFragment;

        AddNewItemOrRefreshTask(TodoFragment fragment) {
            this.weakFragment = new WeakReference<>(fragment);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            TodoFragment fragment = weakFragment.get();
            fragment.fillItemsWithoutTag();
            fragment.fillItemsTags();
            fragment.fillTags();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                TodoFragment fragment = weakFragment.get();
                fragment.todoItemAdapter.notifyDataSetChanged();
                fragment.todoListRecyclerView.scheduleLayoutAnimation();
            }
        }
    }

    /**
     * 更改查询待办类型或更改筛选标签
     */
    private static class ChangeQueryTypeOrFilterTagTask extends AsyncTask<Void, Integer, Boolean> {
        private WeakReference<TodoFragment> weakFragment;

        ChangeQueryTypeOrFilterTagTask(TodoFragment fragment) {
            this.weakFragment = new WeakReference<>(fragment);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            TodoFragment fragment = weakFragment.get();
            fragment.fillItemsWithoutTag();
            fragment.fillItemsTags();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                TodoFragment fragment = weakFragment.get();
                fragment.todoItemAdapter.notifyDataSetChanged();
                fragment.todoListRecyclerView.scheduleLayoutAnimation();
                LogUtil.d("data", "data set change");
            }
        }
    }
}