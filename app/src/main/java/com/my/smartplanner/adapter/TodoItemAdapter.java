package com.my.smartplanner.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.my.smartplanner.DatabaseHelper.TodoDatabaseHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.activity.TodoDetailActivity;
import com.my.smartplanner.TodoListItem;
import com.my.smartplanner.util.CalendarUtil;

import java.util.Calendar;
import java.util.List;

/**
 * 待办列表的适配器类
 */
public class TodoItemAdapter extends RecyclerView.Adapter<TodoItemAdapter.ViewHolder> {

    private Context mContext;
    private List<TodoListItem> todoListItems;
    private SQLiteDatabase db;

    /**
     * 内部类ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        int idInDatabase;//在数据库中的id
        View itemView;//最外层布局
        View downLayout;//装下面一行的布局框
        CheckBox completeCheckBox;//完成复选框
        CheckBox starCheckBox;//星标复选框
        TextView title;//标题文字
        TextView note;//备注复选框
        ImageView noteIcon;//备注图标
        TextView dateText;//日期文字
        ImageView dateIcon;//日期图标
        ImageView alarmIcon;//闹钟图标

        ViewHolder(View view) {
            super(view);
            itemView = view;
            downLayout = view.findViewById(R.id.todo_item_down_layout);
            completeCheckBox = view.findViewById(R.id.todo_item_checkbox);
            starCheckBox = view.findViewById(R.id.todo_item_star);
            title = view.findViewById(R.id.todo_item_title);
            note = view.findViewById(R.id.todo_item_note);
            dateText = view.findViewById(R.id.todo_item_date_text);
            noteIcon = view.findViewById(R.id.todo_item_note_icon);
            dateIcon = view.findViewById(R.id.todo_item_date_icon);
            alarmIcon = view.findViewById(R.id.todo_item_alarm_icon);
        }
    }

    /**
     * 适配器的构造函数
     *
     * @param todoListItems 装有待办子项实体类的List
     */
    public TodoItemAdapter(List<TodoListItem> todoListItems) {
        this.todoListItems = todoListItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //保存上下文
        if (mContext == null) {
            mContext = parent.getContext();
        }
        //打开数据库
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(mContext, "TodoDatabase.db", null, 1);
        db = dbHelper.getWritableDatabase();
        //进行视图相关的操作
        View view = LayoutInflater.from(mContext).inflate(R.layout.todo_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        //设置完成复选框的点击事件
        holder.completeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView titleText = holder.title;
                if (holder.completeCheckBox.isChecked()) {//变为完成
                    titleText.setTextColor(mContext.getResources().getColor(R.color.grey));//文字颜色变成灰色
                    titleText.setPaintFlags(titleText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//设置删除线
                    db.execSQL("UPDATE TodoList SET is_complete = 1 WHERE id = ?", new String[]{"" + holder.idInDatabase});
                } else {//变为未完成
                    titleText.setTextColor(mContext.getResources().getColor(R.color.black));//文字颜色变成黑色
                    titleText.setPaintFlags(titleText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));//取消删除线
                    db.execSQL("UPDATE TodoList SET is_complete = 0 WHERE id = ?", new String[]{"" + holder.idInDatabase});
                }
            }
        });
        //设置星标复选框的点击事件
        holder.starCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.starCheckBox.isChecked()) {//变为已加星标
                    db.execSQL("UPDATE TodoList SET is_star = 1 WHERE id = ?", new String[]{"" + holder.idInDatabase});
                } else {//变为未加星标
                    db.execSQL("UPDATE TodoList SET is_star = 0 WHERE id = ?", new String[]{"" + holder.idInDatabase});
                }
            }
        });
        //设置整个布局的点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                int idInDatabase = todoListItems.get(position).getIdInDatabase();
                //TODO 更改启动Activity的方式
                Intent intent = new Intent(mContext, TodoDetailActivity.class);
                intent.putExtra("mode", TodoDetailActivity.EDIT_MODE);
                intent.putExtra("id_in_database", idInDatabase);
                intent.putExtra("position_in_adapter", position);
                ((Activity)mContext).startActivityForResult(intent,1);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodoListItem item = todoListItems.get(position);//获取实体类的对象
        holder.idInDatabase = item.getIdInDatabase();
        holder.title.setText(item.getTitle());//设置视图中的标题
        boolean displayNote, displayAlarm, displayDate;
        displayNote = displayAlarm = displayDate = true;
        //设置视图中是否完成的复选框
        if (item.getIsComplete()) {
            holder.completeCheckBox.setChecked(true);
            TextView titleText = holder.title;
            titleText.setTextColor(mContext.getResources().getColor(R.color.grey));//文字颜色变成灰色
            titleText.setPaintFlags(titleText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//设置删除线
        } else {
            holder.completeCheckBox.setChecked(false);
            TextView titleText = holder.title;
            titleText.setTextColor(mContext.getResources().getColor(R.color.black));//文字颜色变成黑色
            titleText.setPaintFlags(titleText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));//取消删除线
        }
        //设置视图中星标的复选框
        if (item.getIsStar()) {
            holder.starCheckBox.setChecked(true);
        } else {
            holder.starCheckBox.setChecked(false);
        }
        //设置视图中的备注
        if (item.getNote() != null) {
            holder.note.setVisibility(View.VISIBLE);
            holder.noteIcon.setVisibility(View.VISIBLE);
            holder.note.setText(item.getNote());
        } else {
            holder.note.setVisibility(View.GONE);
            holder.noteIcon.setVisibility(View.GONE);
            displayNote = false;
        }
        //设置视图中的闹钟图标
        if (item.getHasAlarm()) {
            holder.alarmIcon.setVisibility(View.VISIBLE);
        } else {
            holder.alarmIcon.setVisibility(View.GONE);
            displayAlarm = false;
        }
        //设置视图中的日期
        if (item.getDate() != null) {
            holder.dateIcon.setVisibility(View.VISIBLE);
            String displayDateText = null;
            Calendar dateCalendar = item.getDate();
            switch (CalendarUtil.dateLocation(dateCalendar)) {
                case CalendarUtil.DATE_LOCATION_TODAY://今天
                    displayDateText = mContext.getResources().getString(R.string.today);
                    //颜色改成蓝色
                    holder.dateText.setTextColor(mContext.getResources().getColor(R.color.blue));
                    holder.dateIcon.setColorFilter(mContext.getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);
                    break;
                case CalendarUtil.DATE_LOCATION_YESTERDAY://昨天
                    displayDateText = mContext.getResources().getString(R.string.yesterday);
                    //颜色改成红色
                    holder.dateText.setTextColor(mContext.getResources().getColor(R.color.red));
                    holder.dateIcon.setColorFilter(mContext.getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
                    break;
                case CalendarUtil.DATE_LOCATION_TOMORROW://明天
                    displayDateText = mContext.getResources().getString(R.string.tomorrow);
                    //颜色改成灰色
                    holder.dateText.setTextColor(mContext.getResources().getColor(R.color.grey));
                    holder.dateIcon.setColorFilter(mContext.getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
                    break;
                case CalendarUtil.DATE_LOCATION_LONG_BEFORE://以前
                    displayDateText = item.getMonth() + "月" + item.getDayOfMonth() + "日";
                    //颜色改成红色
                    holder.dateText.setTextColor(mContext.getResources().getColor(R.color.red));
                    holder.dateIcon.setColorFilter(mContext.getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
                    break;
                case CalendarUtil.DATE_LOCATION_LONG_AFTER://未来
                    displayDateText = item.getMonth() + "月" + item.getDayOfMonth() + "日";
                    //颜色改成灰色
                    holder.dateText.setTextColor(mContext.getResources().getColor(R.color.grey));
                    holder.dateIcon.setColorFilter(mContext.getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_IN);
                    break;
            }
            holder.dateText.setVisibility(View.VISIBLE);
            holder.dateText.setText(displayDateText);
        } else {
            holder.dateIcon.setVisibility(View.GONE);
            holder.dateText.setVisibility(View.GONE);
            displayDate = false;
        }
        //设置下面一行布局框是否占位
        if (!displayNote && !displayAlarm && !displayDate) {
            holder.downLayout.setVisibility(View.GONE);
        } else {
            holder.downLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return todoListItems.size();
    }
}
