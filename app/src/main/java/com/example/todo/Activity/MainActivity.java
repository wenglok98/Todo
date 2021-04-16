package com.example.todo.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.todo.Adapter.Todo_Adapter;
import com.example.todo.Model.TodoModel;
import com.example.todo.R;
import com.example.todo.Util.SnackUtil;
import com.example.todo.databinding.ActivityMainBinding;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.todo.Util.NetUtils.NET_URL;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding activityMainBinding;
    private ArrayList<TodoModel> todoModelArrayList = new ArrayList<TodoModel>();
    private ArrayList<TodoModel> completedArraylist = new ArrayList<TodoModel>();
    private Todo_Adapter todo_adapter;
    private Todo_Adapter completed_adapter;
    private RequestQueue requestQueue;

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

        initAdapter();
        initData();
        initView();
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
                TodoModel todoModel = new TodoModel();
                todoModel.setCompleted(false);
                todoModel.setId("123");
                todoModel.setTitle("Do wht");
                todoModel.setUserId("321");
                todoModelArrayList.add(todoModel);
                todo_adapter.notifyItemInserted(todoModelArrayList.size());
                activityMainBinding.todoRecyclerView.scrollToPosition(todoModelArrayList.size() - 1);
            }
        });
        activityMainBinding.parentRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                todo_adapter.disabled_EDT();
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

}