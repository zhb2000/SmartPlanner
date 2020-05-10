package com.my.smartplanner.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.my.smartplanner.DatabaseHelper.TodoDBHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.activity.MainActivity;
import com.my.smartplanner.activity.TodoDetailActivity;
import com.my.smartplanner.item.TodoListItem;
import com.my.smartplanner.util.CalendarUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 待办列表的适配器类
 */
public class TodoItemAdapter extends RecyclerView.Adapter<TodoItemAdapter.ViewHolder> {

    private Context mContext = null;//上下文
    private List<TodoListItem> todoListItems;//待办条目列表
    private boolean showCompleted;//是否显示已完成的条目


    /**
     * 内部类ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        int idInDB;//在数据库中的id
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


        /**
         * ViewHolder的构造函数
         */
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


    /**
     * 创建ViewHolder时被调用
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();//获取Context
        }

        //进行视图相关的操作
        View view = LayoutInflater.from(mContext).inflate(R.layout.li_todo_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        //设置完成复选框的点击事件
        holder.completeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isComplete = holder.completeCheckBox.isChecked();
                TodoListItem item = todoListItems.get(holder.getBindingAdapterPosition());
                setCompleteStatus(isComplete, item, holder);
            }
        });
        //设置星标复选框的点击事件
        holder.starCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isStar = holder.starCheckBox.isChecked();
                TodoListItem item = todoListItems.get(holder.getBindingAdapterPosition());
                setStarStatus(isStar, item, holder);
            }
        });
        //设置整个布局的点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getBindingAdapterPosition();//获取条目的下标
                TodoListItem item = todoListItems.get(position);
                TodoDetailActivity.startTheActivityForResultInEdit(
                        mContext, position, item,
                        (Activity) mContext, MainActivity.OPEN_TODO_DETAIL);
            }
        });

        return holder;
    }


    /**
     * 列表项进入屏幕时调用
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodoListItem item = todoListItems.get(position);//获取实体类的对象
        holder.idInDB = item.getId();
        holder.title.setText(item.getTitle());//设置视图中的标题
        setCompleteUI(item.getIsComplete(), holder);
        setStarUI(item.getIsStar(), holder);
        setDownUI(holder, item.getNote(), item.getAlarm(), item.getEndDate());
    }

    @Override
    public int getItemCount() {
        return todoListItems.size();
    }

    /**
     * 设置是否显示已完成的待办条目
     *
     * @param showCompleted 是否显示已完成
     */
    public void setShowCompleted(boolean showCompleted) {
        this.showCompleted = showCompleted;
    }


    /**
     * 更改完成状态：修改item对象、修改UI、修改数据库
     */
    private void setCompleteStatus(boolean isComplete, final TodoListItem item, ViewHolder holder) {
        item.currentComplete(isComplete);
        setCompleteUI(isComplete, holder);
        new Thread(new Runnable() {
            @Override
            public void run() {
                TodoDBHelper.updateComplete(mContext, item.getId(), item.getIsComplete(), item.getCompleteTime());
            }
        }).start();
    }

    /**
     * 更改星标状态：修改item对象、修改UI、修改数据库
     */
    private void setStarStatus(boolean isStar, final TodoListItem item, ViewHolder holder) {
        item.setIsStar(isStar);
        setStarUI(isStar, holder);
        new Thread(new Runnable() {
            @Override
            public void run() {
                TodoDBHelper.updateStar(mContext, item.getId(), item.getIsStar());
            }
        }).start();
    }


    /**
     * 设置完成状态的UI
     *
     * @param holder     被设置的ViewHolder
     * @param isComplete 完成状态
     */
    private void setCompleteUI(boolean isComplete, ViewHolder holder) {
        holder.completeCheckBox.setChecked(isComplete);
        TextView titleText = holder.title;
        if (isComplete) {
            //变为完成
            titleText.setTextColor(mContext.getResources().getColor(R.color.grey));//文字颜色变成灰色
            titleText.setPaintFlags(titleText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//设置删除线
            //当前模式为不显示已完成任务
            if (!showCompleted) {
                int position = holder.getBindingAdapterPosition();//获取条目的下标
                todoListItems.remove(position);
                notifyItemRemoved(position);
            }
        } else {
            //变为未完成
            titleText.setTextColor(mContext.getResources().getColor(R.color.black));//文字颜色变成黑色
            titleText.setPaintFlags(titleText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));//取消删除线
        }
    }

    /**
     * 设置星标的UI
     *
     * @param holder 被设置的ViewHolder
     * @param isStar 星标状态
     */
    private void setStarUI(boolean isStar, ViewHolder holder) {
        holder.starCheckBox.setChecked(isStar);
    }

    /**
     * 设置日期图标和日期文字的UI
     */
    private void setEndDateUI(ViewHolder holder, @Nullable Calendar endDate) {
        int visibility;//日期文字和图标的可见性
        if (endDate != null) {
            visibility = View.VISIBLE;
            String dateStr = null;
            int colorRes = 0;
            switch (CalendarUtil.dateLocation(endDate)) {
                case CalendarUtil.DATE_LOCATION_TODAY://今天
                    dateStr = mContext.getResources().getString(R.string.today);
                    colorRes = R.color.blue;
                    break;
                case CalendarUtil.DATE_LOCATION_YESTERDAY://昨天
                    dateStr = mContext.getResources().getString(R.string.yesterday);
                    colorRes = R.color.red;
                    break;
                case CalendarUtil.DATE_LOCATION_TOMORROW://明天
                    dateStr = mContext.getResources().getString(R.string.tomorrow);
                    colorRes = R.color.grey;
                    break;
                case CalendarUtil.DATE_LOCATION_LONG_BEFORE://以前
                    dateStr = monthDateStr(endDate);
                    colorRes = R.color.red;
                    break;
                case CalendarUtil.DATE_LOCATION_LONG_AFTER://未来
                    dateStr = monthDateStr(endDate);
                    colorRes = R.color.grey;
                    break;
            }
            holder.dateText.setTextColor(mContext.getResources().getColor(colorRes));//文字颜色
            holder.dateIcon.setColorFilter(mContext.getResources().getColor(colorRes), PorterDuff.Mode.SRC_IN);//图标颜色
            holder.dateText.setText(dateStr);//日期文字
        } else {
            visibility = View.GONE;
        }
        holder.dateText.setVisibility(visibility);
        holder.dateIcon.setVisibility(visibility);
    }

    /**
     * 设置闹钟图标UI
     */
    private static void setAlarmIconUI(ViewHolder holder, Calendar alarm) {
        if (alarm != null) {
            holder.alarmIcon.setVisibility(View.VISIBLE);
        } else {
            holder.alarmIcon.setVisibility(View.GONE);
        }
    }

    /**
     * 设置备注图标UI
     */
    private static void setNoteIconUI(ViewHolder holder, String note) {
        if (note != null) {
            holder.note.setVisibility(View.VISIBLE);
            holder.noteIcon.setVisibility(View.VISIBLE);
            holder.note.setText(note);
        } else {
            holder.note.setVisibility(View.GONE);
            holder.noteIcon.setVisibility(View.GONE);
        }
    }

    /**
     * 设置下面一行布局框是否占位
     */
    private static void setDownOccupyUI(ViewHolder holder) {
        boolean displayNote = holder.noteIcon.getVisibility() == View.VISIBLE;
        boolean displayAlarm = holder.alarmIcon.getVisibility() == View.VISIBLE;
        boolean displayDate = holder.dateIcon.getVisibility() == View.VISIBLE;
        if (!displayNote && !displayAlarm && !displayDate) {
            holder.downLayout.setVisibility(View.GONE);
        } else {
            holder.downLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置备注图标、闹钟图标、日期的UI
     */
    private void setDownUI(ViewHolder holder, String note, Calendar alarm, Calendar endDate) {
        setNoteIconUI(holder, note);
        setAlarmIconUI(holder, alarm);
        setEndDateUI(holder, endDate);
        setDownOccupyUI(holder);
    }


    /**
     * 把Calendar转换成"x月x日"
     */
    private static String monthDateStr(Calendar calendar) {
        return String.format(Locale.PRC, "%d月%d日", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE));
    }

}
