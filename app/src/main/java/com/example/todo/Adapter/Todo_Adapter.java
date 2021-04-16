package com.example.todo.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.todo.Activity.MainActivity;
import com.example.todo.Model.TodoModel;
import com.example.todo.R;
import com.example.todo.Util.SnackUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.todo.Util.NetUtils.NET_URL;

public class Todo_Adapter extends RecyclerView.Adapter<Todo_Adapter.MyViewHolder> {
    private Context context;
    private ArrayList<TodoModel> todoModelArrayLists;
    private MyViewHolder tempHolder;

    public Todo_Adapter(Context ct, ArrayList<TodoModel> todoModelArrayList) {
        context = ct;
        todoModelArrayLists = todoModelArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.adapter_todo_recycler, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        tempHolder = holder;
        holder.todo_user_id_Tv.setText("User ID : " + todoModelArrayLists.get(position).getUserId());
        holder.todo_title_Edt.setText(todoModelArrayLists.get(position).getTitle());

        holder.todo_title_Edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean isChecked = holder.checkBox.isChecked();

                if (isChecked) {
                    uncheckTodo(position);

                } else if (!isChecked) {
                    checkTodo(position);

                }

            }
        });

        holder.todo_title_Edt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        context, R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(context).inflate(
                        R.layout.layout_edit_bottom_sheet, (LinearLayout) v
                                .findViewById(R.id.bottomSheetContainer)
                );

                bottomSheetView.findViewById(R.id.edit_BT_bottomSheets)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (todoModelArrayLists.get(position).getCompleted() == false) {
                                    bottomSheetDialog.dismiss();
                                    holder.todo_title_Edt.setFocusableInTouchMode(true);
                                    holder.todo_title_Edt.setFocusable(true);
                                    holder.todo_title_Edt.setCursorVisible(true);
                                    holder.todo_title_Edt.requestFocus();

                                    showKeyboard();
                                } else {
                                    bottomSheetDialog.dismiss();
                                    SnackUtil.show(context, "Completed Task Not Allowed to Edit !");
                                }
                            }
                        });
                bottomSheetView.findViewById(R.id.remove_BT_bottomSheets)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bottomSheetDialog.dismiss();
                                TodoModel tempModel = new TodoModel();
                                tempModel = todoModelArrayLists.get(position);
                                todoModelArrayLists.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, todoModelArrayLists.size());//add here, this can refresh position

                                TodoModel finalTempModel = tempModel;
                                Snackbar deleteSnack = Snackbar.make(holder.recycler_rl,
                                        "To-Do Removed Successfully !", BaseTransientBottomBar.LENGTH_SHORT)
                                        .setAction("UNDO", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                todoModelArrayLists.add(finalTempModel);
                                                notifyItemInserted(todoModelArrayLists.size());

                                            }
                                        });

                                deleteSnack.show();
                            }
                        });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();

                return true;

            }
        });


        holder.checkBox.setChecked(todoModelArrayLists.get(position).getCompleted());
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean isChecked = holder.checkBox.isChecked();

                if (isChecked) {
                    checkTodo(position);
                } else if (!isChecked) {
                    uncheckTodo(position);
                }

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!holder.checkBox.isChecked()) {
                    checkTodo(position);

                } else if (holder.checkBox.isChecked()) {
                    uncheckTodo(position);
                }


            }
        });
    }

    @Override
    public int getItemCount() {
        return todoModelArrayLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBox;
        private EditText todo_title_Edt;
        private TextView todo_user_id_Tv;
        private RelativeLayout recycler_rl;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.checkbox_cb);
            todo_title_Edt = itemView.findViewById(R.id.todo_Edt);
            todo_user_id_Tv = itemView.findViewById(R.id.user_id_tv);
            recycler_rl = itemView.findViewById(R.id.recycler_rl);
        }
    }

    private void checkTodo(int position) {
        todoModelArrayLists.get(position).setCompleted(true);
        ((MainActivity) context).updateCompleteList(todoModelArrayLists.get(position), position);
        todoModelArrayLists.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, todoModelArrayLists.size());//add here, this can refresh position

    }

    private void uncheckTodo(int position) {
        todoModelArrayLists.get(position).setCompleted(false);
        ((MainActivity) context).updateTodoList(todoModelArrayLists.get(position), position);
        todoModelArrayLists.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, todoModelArrayLists.size());//add here, this can refresh position


    }

    public Context getContext() {
        return context;
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }


    public void disabled_EDT() {

        try {
            tempHolder.todo_title_Edt.setFocusableInTouchMode(false);
            tempHolder.todo_title_Edt.clearFocus();

            tempHolder.todo_title_Edt.setFocusable(false);
            tempHolder.todo_title_Edt.setCursorVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
