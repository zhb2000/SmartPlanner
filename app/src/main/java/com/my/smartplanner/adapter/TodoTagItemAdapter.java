package com.my.smartplanner.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView tagNameTextView;
        LinearLayout editArea;
        LinearLayout deleteArea;

        ViewHolder(View view) {
            super((view));
            itemView = view;
            tagNameTextView = view.findViewById(R.id.todo_tag_item_tag_name_text_view);
            editArea = view.findViewById(R.id.todo_tag_item_edit_area);
            deleteArea = view.findViewById(R.id.todo_tag_item_delete_area);
        }

    }

    /**
     * Adapter的构造函数
     *
     * @param todoTagListItems 装有待办标签实体类的List
     */
    public TodoTagItemAdapter(List<TodoTagListItem> todoTagListItems) {
        this.todoTagListItems = todoTagListItems;
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
        //点击最外层布局的事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        //点击编辑按钮的事件
        holder.editArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "edit", Toast.LENGTH_SHORT).show();
                //TODO edit tag
            }
        });
        //点击删除按钮的事件
        holder.deleteArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                dialogBuilder.setTitle(R.string.confirm_to_delete_todo_tag);//设置对话框标题
                dialogBuilder.setMessage(R.string.delete_todo_tag_message);//设置对话框提示消息
                //设置确定删除按钮的点击事件
                dialogBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int position = holder.getAdapterPosition();//获取该标签条目的下标
                        String tagName = todoTagListItems.get(position).getTagName();//该条目的tag的名称
                        //
                        Cursor cursor = db.rawQuery("SELECT id, tag FROM TodoList WHERE tag GLOB ?", new String[]{"* " + tagName + " *"});
                        //更新TodoList表中的数据，更新所有符合的待办条目的tag字段
                        if (cursor.moveToFirst()) {
                            do {
                                int itemId = cursor.getInt(cursor.getColumnIndex("id"));//待办条目的id
                                String oldTagSequence = cursor.getString(cursor.getColumnIndex("tag"));//旧tag序列
                                //把旧tag序列中的tag名字换成空串
                                String newTagSequence = oldTagSequence.replace(" " + tagName + " ", "");//新tag序列
                                if (!TextUtils.isEmpty(newTagSequence.trim())) {
                                    newTagSequence = " " + newTagSequence.trim() + " ";
                                } else {
                                    newTagSequence = null;
                                }
                                db.execSQL("UPDATE TodoList SET tag = ? WHERE id = ?", new String[]{newTagSequence, itemId + ""});
                            } while (cursor.moveToNext());
                        }
                        cursor.close();
                        //更新TodoTag表中的数据
                        db.execSQL("DELETE FROM TodoTag WHERE tag_name = ?", new String[]{tagName});
                        todoTagListItems.remove(position);//把List中的这一项移走
                        notifyItemRemoved(position);//通知Adapter该标签条目被移走
                    }
                });
                //设置取消按钮的点击事件
                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialogBuilder.show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodoTagListItem item = todoTagListItems.get(position);
        holder.tagNameTextView.setText(item.getTagName());
    }

    @Override
    public int getItemCount() {
        return todoTagListItems.size();
    }
}
