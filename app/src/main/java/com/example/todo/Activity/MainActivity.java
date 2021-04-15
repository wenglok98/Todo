package com.example.todo.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.todo.Adapter.Todo_Adapter;
import com.example.todo.Model.TodoModel;
import com.example.todo.R;
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
    Todo_Adapter todo_adapter;
    Todo_Adapter completed_adapter;
    private RequestQueue requestQueue;

    private JsonArrayRequest todoRequest;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = activityMainBinding.getRoot();
        setContentView(view);
        requestQueue = Volley.newRequestQueue(getApplicationContext());


        todo_adapter = new Todo_Adapter(MainActivity.this, todoModelArrayList);
        activityMainBinding.todoRecyclerView
                .setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        activityMainBinding.todoRecyclerView.setAdapter(todo_adapter);

        completed_adapter = new Todo_Adapter(MainActivity.this, completedArraylist);
        activityMainBinding.completedRecyclerView
                .setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        activityMainBinding.completedRecyclerView.setAdapter(completed_adapter);
//        activityMainBinding.todoRecyclerView.addItemDecoration(new DividerItemDecoration(activityMainBinding.todoRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

//Test Data
//        TodoModel td = new TodoModel();
//        td.setCompleted("false");
//        td.setId("1");
//        td.setTitle("asdasd");
//        td.setUserId("123");
//        todoModelArrayList.add(td);
//        todo_adapter.notifyDataSetChanged();
        initData();
    }

    private void initData() {
        String url = NET_URL + "?_start=0&_limit=5";

        todoRequest = new JsonArrayRequest(Request.Method.GET, url, null
                , new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    List<TodoModel> tempList = Arrays.asList(mapper.readValue(response.toString(), TodoModel[].class));
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

    public void updateCompleteList(TodoModel todoModel,int position) {
        completedArraylist.add(todoModel);
        completed_adapter.notifyDataSetChanged();

    }

    public void updateTodoList(TodoModel todoModel,int position) {
        todoModelArrayList.add(todoModel);
        todo_adapter.notifyDataSetChanged();


    }

}