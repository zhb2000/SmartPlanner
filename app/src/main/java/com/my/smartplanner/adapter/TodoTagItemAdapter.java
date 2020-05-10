package com.my.smartplanner.adapter;

import android.content.Context;
import android.content.DialogInterface;
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

import com.my.smartplanner.DatabaseHelper.TodoDBHelper;
import com.my.smartplanner.R;
import com.my.smartplanner.activity.ManageTodoTagsActivity;
import com.my.smartplanner.item.TodoTagListItem;

import java.util.List;

/**
 * TodoTagItem的Adapter
 */
public class TodoTagItemAdapter extends RecyclerView.Adapter<TodoTagItemAdapter.ViewHolder> {

    private ManageTodoTagsActivity activity;
    /**
     * 上下文
     */
    private Context mContext = null;
    /**
     * 装有标签名字的List
     */
    private List<TodoTagListItem> todoTagListItems;
    /**
     * <p>重命名对话框的自定义视图的输入框控件</p>
     * <p>为了方便在匿名类中引用所以弄成了成员变量</p>
     */
    private EditText renameEditText;

    /**
     * static内部类ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tagNameTextView;//标签名称的TextView
        LinearLayout editArea;//编辑按钮的区域
        LinearLayout deleteArea;//删除按钮的区域

        /**
         * ViewHolder的构造方法
         */
        ViewHolder(View view) {
            super(view);
            //获取控件的引用
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
    public TodoTagItemAdapter(List<TodoTagListItem> todoTagListItems, ManageTodoTagsActivity activity) {
        this.todoTagListItems = todoTagListItems;
        this.activity = activity;
    }

    /**
     * 设置点击编辑按钮的事件
     */
    private void setEditListener(final ViewHolder holder) {
        holder.editArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建对话框
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                dialogBuilder.setTitle(R.string.rename_todo_tag);//设置对话框标题
                dialogBuilder.setView(R.layout.dialog_rename_todo_tag);//设置对话框自定义布局

                //设置确定按钮的事件
                dialogBuilder.setPositiveButton(R.string.confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int position = holder.getBindingAdapterPosition();//获取下标
                                final String oldTag = todoTagListItems.get(position).getTag();//旧标签
                                String newTag = null;//新标签
                                if (renameEditText != null) {
                                    newTag = renameEditText.getText().toString();
                                }
                                if (newTag != null) {
                                    newTag = newTag.trim();
                                }
                                if (!TextUtils.isEmpty(newTag)) {
                                    if (TodoDBHelper.hasTag(mContext, newTag)) {
                                        //新标签已经存在，发送Toast提示用户
                                        Toast.makeText(mContext, mContext.getString(
                                                R.string.todo_tag_already_exist), Toast.LENGTH_LONG).show();
                                    } else {
                                        //新标签并不存在
                                        final String theNewTag = newTag;
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                TodoDBHelper.renameTag(mContext, oldTag, theNewTag);
                                            }
                                        }).start();//更新数据库中的数据
                                        todoTagListItems.get(position).setTag(newTag);//更改对象的属性
                                        notifyItemChanged(position);//通知Adapter数据发生变化
                                        activity.setModified();
                                    }
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

                //对自定义对话框中的控件进行操作
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();//要先show()才能findViewById()
                renameEditText = alertDialog.findViewById(R.id.rename_todo_tag_dialog_edit_text);//获取输入框控件的引用
                String oldTag = todoTagListItems.get(holder.getBindingAdapterPosition()).getTag();
                renameEditText.setText(oldTag);//把旧名字设置在输入框中
            }
        });
    }

    /**
     * 设置点击删除按钮的事件
     */
    private void setDeleteListener(final ViewHolder holder) {
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
                        int position = holder.getBindingAdapterPosition();//获取该标签条目的下标
                        final String tag = todoTagListItems.get(position).getTag();//该条目的tag的名称
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                TodoDBHelper.deleteTag(mContext, tag);
                            }
                        }).start();
                        todoTagListItems.remove(position);//把List中的这一项移走
                        notifyItemRemoved(position);//通知Adapter该标签条目被移走
                        activity.setModified();
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
    }

    /**
     * 创建ViewHolder时执行的方法
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();//获取Context
        }

        //进行视图相关的操作
        View view = LayoutInflater.from(mContext).inflate(R.layout.li_todo_tag_item,
                parent, false);//为该列表项加载布局
        final ViewHolder holder = new ViewHolder(view);//创建ViewHolder
        setEditListener(holder);
        setDeleteListener(holder);

        return holder;//返回创建的ViewHolder
    }

    /**
     * 该列表项进入屏幕所做的操作
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodoTagListItem item = todoTagListItems.get(position);
        holder.tagNameTextView.setText(item.getTag());//把tag显示在TextView中
    }

    /**
     * 返回数量
     */
    @Override
    public int getItemCount() {
        return todoTagListItems.size();
    }
}
