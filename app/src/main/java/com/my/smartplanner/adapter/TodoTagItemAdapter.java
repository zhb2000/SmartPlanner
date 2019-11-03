package com.my.smartplanner.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

    private Context mContext;//上下文
    private List<TodoTagListItem> todoTagListItems;//装有标签名字的List
    private SQLiteDatabase db;//TodoDatabase.db数据库，里面有TodoList表和TodoTag表

    // 重命名对话框的自定义视图的输入框控件
    // 为了方便在匿名类中修改所以弄成了成员变量
    private EditText renameEditText;

    /**
     * 内部类ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;//最外层的布局
        TextView tagNameTextView;//标签名称的TextView
        LinearLayout editArea;//编辑按钮的区域
        LinearLayout deleteArea;//删除按钮的区域

        /**
         * ViewHolder的构造方法
         */
        ViewHolder(View view) {
            super((view));
            //获取控件的引用
            itemView = view;
            tagNameTextView = view.findViewById(R.id.todo_tag_item_tag_name_text_view);
            editArea = view.findViewById(R.id.todo_tag_item_edit_area);
            deleteArea = view.findViewById(R.id.todo_tag_item_delete_area);
        }

    }

    /**
     * Adapter的构造方法
     *
     * @param todoTagListItems 装有待办标签实体类的List
     */
    public TodoTagItemAdapter(List<TodoTagListItem> todoTagListItems) {
        this.todoTagListItems = todoTagListItems;
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

        //打开数据库
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(mContext, "TodoDatabase.db",
                null, TodoDatabaseHelper.NOW_VERSION);
        db = dbHelper.getWritableDatabase();

        //进行视图相关的操作
        View view = LayoutInflater.from(mContext).inflate(R.layout.todo_tag_item,
                parent, false);//为该列表项加载布局
        final ViewHolder holder = new ViewHolder(view);//创建ViewHolder

        //设置点击最外层布局的事件，什么也不做
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        //设置点击编辑按钮的事件
        holder.editArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建对话框
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                dialogBuilder.setTitle(R.string.rename_todo_tag);//设置对话框标题
                dialogBuilder.setView(R.layout.rename_todo_tag_dialog);//设置对话框自定义布局

                //设置确定按钮的事件
                dialogBuilder.setPositiveButton(R.string.confirm,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int position = holder.getAdapterPosition();//获取该标签条目的下标
                        String oldTagName = todoTagListItems.get(position).getTagName();//旧标签名字
                        String newTagName = null;//新标签名字
                        if (renameEditText != null) {//TODO may be null
                            newTagName = renameEditText.getText().toString();//获取新标签名字
                        }
                        //若新标签名字非空
                        if (!TextUtils.isEmpty(newTagName)) {
                            //查询新标签名字是否已经存在
                            Cursor cursor = db.rawQuery("SELECT COUNT(*) AS tag_count FROM TodoTag " +
                                    "WHERE tag_name = ?", new String[]{newTagName});
                            int newTagNameCount = 0;//新标签名字的数量
                            if (cursor.moveToFirst()) {
                                newTagNameCount = cursor.getInt(
                                        cursor.getColumnIndex("tag_count"));
                            }

                            if (newTagNameCount > 0) {//新标签已经存在
                                //发送Toast提示用户
                                Toast.makeText(mContext, mContext.getString(
                                        R.string.todo_tag_already_exist), Toast.LENGTH_LONG).show();
                            } else {//新标签并不存在
                                //更新TodoTag表中的数据
                                db.execSQL("UPDATE TodoTag " +
                                        "SET tag_name = ? " +
                                        "WHERE tag_name = ?",
                                        new String[]{newTagName, oldTagName});

                                //更新TodoList表中的tag字段
                                cursor = db.rawQuery("SELECT id, tag FROM TodoList " +
                                        "WHERE tag GLOB ?", new String[]{"* " + oldTagName + " *"});
                                if (cursor.moveToFirst()) {
                                    do {
                                        int itemId = cursor.getInt(
                                                cursor.getColumnIndex("id"));//待办条目的id
                                        String oldTagSequence = cursor.getString(
                                                cursor.getColumnIndex("tag"));//旧tag序列
                                        //把旧tag序列中的oldTagName换成newTagName
                                        String newTagSequence = oldTagSequence
                                                .replace(oldTagName, newTagName);//新tag序列
                                        db.execSQL("UPDATE TodoList " +
                                                "SET tag = ? " +
                                                "WHERE id = ?",
                                                new String[]{newTagSequence, itemId + ""});
                                    } while (cursor.moveToNext());
                                }

                                todoTagListItems.get(position)
                                        .setTagName(newTagName);//更改List中的对象的成员变量
                                notifyItemChanged(position);//通知Adapter数据发生变化
                            }
                            cursor.close();
                        }
                    }
                });

                //设置取消按钮的事件，什么也不做
                dialogBuilder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                //对自定义视图中的控件进行操作
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();//要先show()才能findViewById()
                renameEditText = alertDialog.findViewById(R.id.rename_todo_tag_dialog_edit_text);//获取输入框控件的引用
                String oldTagName = todoTagListItems.get(holder.getAdapterPosition()).getTagName();
                renameEditText.setText(oldTagName);//把旧名字设置在输入框中
            }
        });

        //设置点击删除按钮的事件
        holder.deleteArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建对话框
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                dialogBuilder.setTitle(R.string.confirm_to_delete_todo_tag);//设置对话框标题
                dialogBuilder.setMessage(R.string.delete_todo_tag_message);//设置对话框提示消息

                //设置确定删除按钮的点击事件
                dialogBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int position = holder.getAdapterPosition();//获取该标签条目的下标
                        String tagName = todoTagListItems.get(position).getTagName();//该条目的tag的名称

                        //从TodoList表中查询所有带有该标签的待办事项
                        Cursor cursor = db.rawQuery("SELECT id, tag FROM TodoList " +
                                "WHERE tag GLOB ?", new String[]{"* " + tagName + " *"});
                        //更新TodoList表中所有符合的待办条目的tag字段
                        if (cursor.moveToFirst()) {
                            do {
                                int itemId = cursor.getInt(cursor.getColumnIndex("id"));//待办条目的id
                                String oldTagSequence = cursor.getString(
                                        cursor.getColumnIndex("tag"));//旧tag序列
                                //把旧tag序列中的tag名字换成空串
                                String newTagSequence = oldTagSequence
                                        .replace(" " + tagName + " ", "");//新tag序列
                                if (!TextUtils.isEmpty(newTagSequence.trim())) {
                                    newTagSequence = " " + newTagSequence.trim() + " ";
                                } else {
                                    newTagSequence = null;
                                }
                                db.execSQL("UPDATE TodoList " +
                                        "SET tag = ? " +
                                        "WHERE id = ?",
                                        new String[]{newTagSequence, itemId + ""});
                            } while (cursor.moveToNext());
                        }
                        cursor.close();

                        //把该Tag的名字从TodoTag表中移除
                        db.execSQL("DELETE FROM TodoTag " +
                                "WHERE tag_name = ?", new String[]{tagName});

                        todoTagListItems.remove(position);//把List中的这一项移走
                        notifyItemRemoved(position);//通知Adapter该标签条目被移走
                    }
                });

                //设置取消按钮的点击事件，什么都不做
                dialogBuilder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                dialogBuilder.show();//显示该对话框
            }
        });
        return holder;//返回创建的ViewHolder
    }

    /**
     * 该列表项进入屏幕所做的操作
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //获取对应的tag名字并显示在TextView中
        TodoTagListItem item = todoTagListItems.get(position);
        holder.tagNameTextView.setText(item.getTagName());
    }

    /**
     * 返回数量
     */
    @Override
    public int getItemCount() {
        return todoTagListItems.size();
    }
}
