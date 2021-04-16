package com.example.todo.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.todo.Adapter.Todo_Adapter;
import com.example.todo.BaseActivity;
import com.example.todo.Model.TodoModel;
import com.example.todo.R;
import com.example.todo.Util.SnackUtil;
import com.example.todo.databinding.ActivityMainBinding;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
import static com.example.todo.Util.NetUtils.NET_URL;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding activityMainBinding;
    private ArrayList<TodoModel> todoModelArrayList = new ArrayList<TodoModel>();
    private ArrayList<TodoModel> completedArraylist = new ArrayList<TodoModel>();
    private Todo_Adapter todo_adapter;
    private Todo_Adapter completed_adapter;
    private RequestQueue requestQueue;
    BottomSheetDialog newTodoDialog;
    private JsonArrayRequest todoRequest;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //JetPack View Binding Setup
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = activityMainBinding.getRoot();
        setContentView(view);
        //End of JetPack View Binding Setup

        requestQueue = Volley.newRequestQueue(getApplicationContext());//init volley request

        initAdapter(); //Initial the adapter first will notify the adapter with data later
        initData(); // Load data
        initView(); //Each kind of view binding and onclick listener


    }

    private void initView() {
        activityMainBinding.viewAllTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ViewAllActivity.class));
            }
        });

        activityMainBinding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetCreate();
            }
        });
        //Whereever place touch in this activity will close the keyboard
        activityMainBinding.parentRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
            }
        });

        activityMainBinding.parentRelative.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                activityMainBinding.parentRelative.getWindowVisibleDisplayFrame(r);
                int screenHeight = activityMainBinding.parentRelative.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) {

                } else {

                    todo_adapter.disabled_EDT(); //When keyboard gone- will disable the edit text and lock
                }
            }
        });
    }

    private void initAdapter() {
        todo_adapter = new Todo_Adapter(MainActivity.this, todoModelArrayList);
        activityMainBinding.todoRecyclerView
                .setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        activityMainBinding.todoRecyclerView.setAdapter(todo_adapter);


        completed_adapter = new Todo_Adapter(MainActivity.this, completedArraylist);
        activityMainBinding.completedRecyclerView
                .setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        activityMainBinding.completedRecyclerView.setAdapter(completed_adapter);


    }

    private void initData() {
        String url = NET_URL + "?_start=0&_limit=5";

        todoRequest = new JsonArrayRequest(Request.Method.GET, url, null
                , new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    List<TodoModel> tempList = Arrays.asList(
                            mapper.readValue(response.toString(), TodoModel[].class));
                    for (TodoModel todoModel : tempList) {
                        if (todoModel.getCompleted().equals(false)) {
                            todoModelArrayList.add(todoModel);
                        } else if (todoModel.getCompleted().equals(true)) {
                            completedArraylist.add(todoModel);
                        }
                    }
                    completed_adapter.notifyDataSetChanged();
                    todo_adapter.notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(todoRequest);

    }

    public void updateCompleteList(TodoModel todoModel, int position) {
        completedArraylist.add(todoModel);
        completed_adapter.notifyDataSetChanged();

    }

    public void updateTodoList(TodoModel todoModel, int position) {
        todoModelArrayList.add(todoModel);
        todo_adapter.notifyDataSetChanged();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }


    private void showBottomSheetCreate() {
        newTodoDialog = new BottomSheetDialog(
                MainActivity.this, R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(MainActivity.this).inflate(
                R.layout.layout_create_bottom_sheet, activityMainBinding.parentRelative, false
        );
        //Make the view will show above the keyboard
        Objects.requireNonNull(newTodoDialog.getWindow())
                .setSoftInputMode(SOFT_INPUT_STATE_VISIBLE);

        newTodoDialog.setContentView(bottomSheetView);
        newTodoDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        bottomSheetView.findViewById(R.id.save_bt_create_bottom_sheet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText tempEdt = bottomSheetView.findViewById(R.id.create_edt);
                saveTodo(tempEdt.getText().toString(), v);
            }
        });

        newTodoDialog.show();

    }

    private void saveTodo(String todo, View v) {
        //Just simply saving to variables only, Acting saving to API
        TodoModel todoModel = new TodoModel();
        todoModel.setCompleted(false);
        todoModel.setId("1");
        todoModel.setTitle(todo);
        todoModel.setUserId("1");
        todoModelArrayList.add(todoModel);
        try {
            newTodoDialog.dismiss();
            hideKeyboard(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        todo_adapter.notifyItemInserted(todoModelArrayList.size());
        activityMainBinding.todoRecyclerView.scrollToPosition(todoModelArrayList.size() - 1);
        SnackUtil.show(MainActivity.this, "Saved Successfully ! ");


    }

}