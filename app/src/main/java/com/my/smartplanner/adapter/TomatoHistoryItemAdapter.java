package com.my.smartplanner.adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.my.smartplanner.R;
import com.my.smartplanner.item.TomatoHistoryListItem;

import java.util.List;

public class TomatoHistoryItemAdapter extends RecyclerView.Adapter<TomatoHistoryItemAdapter.ViewHolder> {
    /**
     * 保存的Context
     */
    private Context mContext;
    /**
     * 历史记录
     */
    private List<TomatoHistoryListItem> tomatoHistories;
    /**
     * TomatoDatabase.db数据库，里面有TomatoHistory表
     */
    private SQLiteDatabase db;

    /**
     * static内部类ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView titleTextView, startTimeTextView, timeSumTextView;

        /**
         * ViewHolder的构造方法
         */
        ViewHolder(View view) {
            super((view));
            //获取控件的引用
            itemView = view;
            titleTextView = view.findViewById(R.id.li_tomato_history_title);
            startTimeTextView = view.findViewById(R.id.li_tomato_history_start_time);
            timeSumTextView = view.findViewById(R.id.li_tomato_history_time_sum);
        }
    }

    /**
     * Adapter的构造方法
     *
     * @param tomatoHistories 装有历史记录实体类的List
     */
    public TomatoHistoryItemAdapter(List<TomatoHistoryListItem> tomatoHistories) {
        this.tomatoHistories = tomatoHistories;
    }

    /**
     * 创建ViewHolder时执行的方法
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //保存上下文
        if (mContext == null) {
            mContext = parent.getContext();
        }

        //进行视图相关的操作
        View view = LayoutInflater.from(mContext).inflate(R.layout.li_tomato_history_item,
                parent, false);//为该列表项加载布局
        final ViewHolder holder = new ViewHolder(view);//创建ViewHolder
        //设置点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO click outer option
                Toast.makeText(mContext, "click outer!", Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    /**
     * 该列表项进入屏幕所做的操作
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //获取对应的实体类
        TomatoHistoryListItem item=tomatoHistories.get(position);
        //设置列表项中控件的显示状态
        holder.titleTextView.setText(item.getTitle());
        holder.startTimeTextView.setText(item.getLiStartTimeStr());
        holder.timeSumTextView.setText(item.getLiTimeSumStr());
    }

    /**
     * 返回数量
     *
     * @return 列表项数量
     */
    @Override
    public int getItemCount() {
        return tomatoHistories.size();
    }
}
