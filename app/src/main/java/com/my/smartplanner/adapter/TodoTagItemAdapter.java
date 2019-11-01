package com.my.smartplanner.adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.my.smartplanner.DatabaseHelper.TodoDatabaseHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.TodoTagListItem;

import java.util.List;

public class TodoTagItemAdapter extends RecyclerView.Adapter<TodoTagItemAdapter.ViewHolder> {

    private Context mContext;
    private List<TodoTagListItem> todoTagListItems;
    private SQLiteDatabase db;

    /**
     * 内部类ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        TextView tagNameTextView;
        LinearLayout editArea;
        LinearLayout deleteArea;
        ViewHolder(View view){
            super((view));
            itemView=view;
            tagNameTextView=view.findViewById(R.id.todo_tag_item_tag_name_text_view);
            editArea=view.findViewById(R.id.todo_tag_item_edit_area);
            deleteArea=view.findViewById(R.id.todo_tag_item_delete_area);
        }

    }

    /**
     * Adapter的构造函数
     * @param todoTagListItems 装有待办标签实体类的List
     */
    public TodoTagItemAdapter(List<TodoTagListItem> todoTagListItems){
        this.todoTagListItems=todoTagListItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //保存上下文
        if (mContext == null) {
            mContext = parent.getContext();
        }
        //打开数据库
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(mContext, "TodoDatabase.db", null, TodoDatabaseHelper.NOW_VERSION);
        db = dbHelper.getWritableDatabase();
        //进行视图相关的操作
        View view = LayoutInflater.from(mContext).inflate(R.layout.todo_tag_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "out area", Toast.LENGTH_SHORT).show();
            }
        });
        holder.editArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "edit", Toast.LENGTH_SHORT).show();
                //TODO edit tag
            }
        });
        holder.deleteArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "delete", Toast.LENGTH_SHORT).show();
                //TODO delete tag
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodoTagListItem item=todoTagListItems.get(position);
        holder.tagNameTextView.setText(item.getTagName());
    }

    @Override
    public int getItemCount() {
        return todoTagListItems.size();
    }
}
