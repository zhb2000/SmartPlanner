package com.my.smartplanner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
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
     * static内部类ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView titleTextView, startTimeTextView,
                timeSumTextView, dateTextView, successTextView;

        /**
         * ViewHolder的构造方法
         */
        ViewHolder(View view) {
            super((view));
            //获取控件的引用
            cardView = view.findViewById(R.id.li_tomato_history_card);
            titleTextView = view.findViewById(R.id.li_tomato_history_title);
            startTimeTextView = view.findViewById(R.id.li_tomato_history_start_time);
            timeSumTextView = view.findViewById(R.id.li_tomato_history_time_sum);
            dateTextView = view.findViewById(R.id.li_tomato_history_date);
            successTextView = view.findViewById(R.id.li_tomato_history_success);
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
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle(R.string.view_detail);
                int position = holder.getBindingAdapterPosition();
                TomatoHistoryListItem item = tomatoHistories.get(position);
                dialog.setMessage(createDialogMsg(item, mContext));
                dialog.show();
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
        TomatoHistoryListItem item = tomatoHistories.get(position);
        //设置列表项中控件的显示状态
        holder.titleTextView.setText(item.getTitle());
        holder.startTimeTextView.setText(item.getLiStartTimeStr());
        holder.timeSumTextView.setText(item.getLiTimeSumStr());
        holder.dateTextView.setText(item.getLiDateStr());
        holder.successTextView.setText(item.getLiSuccessStr());
        switch (item.getLiColor()) {
            case TomatoHistoryListItem.COLOR_MORNING:
                holder.cardView.setCardBackgroundColor(
                        mContext.getResources()
                                .getColor(R.color.tomato_history_morning));
                break;
            case TomatoHistoryListItem.COLOR_AFTERNOON:
                holder.cardView.setCardBackgroundColor(
                        mContext.getResources()
                                .getColor(R.color.tomato_history_afternoon));
                break;
            case TomatoHistoryListItem.COLOR_NIGHT:
                holder.cardView.setCardBackgroundColor(
                        mContext.getResources()
                                .getColor(R.color.tomato_history_night));
                break;
            case TomatoHistoryListItem.COLOR_UNSUCCESSFUL:
                holder.cardView.setCardBackgroundColor(
                        mContext.getResources()
                                .getColor(R.color.tomato_history_unsuccessful));
                break;
        }
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

    /**
     * 生成对话框里的字符串
     *
     * @param item    实体对象
     * @param context Context环境
     * @return 对话框里的字符串
     */
    private static String createDialogMsg(TomatoHistoryListItem item, Context context) {
        String msg = context.getString(R.string.total_time_colon) + " "
                + item.getTimeSum() + context.getString(R.string.minute) + "\n";
        msg += context.getString(R.string.work_time_sum_colon) + " "
                + item.getWorkSum() + context.getString(R.string.minute) + "\n";
        msg += context.getString(R.string.rest_time_sum_colon) + " "
                + item.getRestSum() + context.getString(R.string.minute) + "\n";
        msg += context.getString(R.string.tomato_status_colon) + " "
                + item.getLiSuccessStr() + "\n";
        msg += context.getString(R.string.work_time_len_colon) + " "
                + item.getWorkLen() + context.getString(R.string.minute) + "\n";
        msg += context.getString(R.string.rest_time_len_colon) + " "
                + item.getRestLen() + context.getString(R.string.minute) + "\n";
        msg += context.getString(R.string.clock_cnt_colon) + " "
                + item.getClockCnt() + "\n";
        msg += context.getString(R.string.date_colon) + " "
                + item.getLiDateStr() + "\n";
        msg += context.getString(R.string.start_time_colon) + " "
                + item.getStartTimeStr() + "\n";
        msg += context.getString(R.string.end_time_colon) + " "
                + item.getEndTimeStr() + "\n";

        return msg;
    }
}
