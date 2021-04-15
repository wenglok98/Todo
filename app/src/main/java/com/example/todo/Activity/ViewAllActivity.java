package com.example.todo.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Filter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.todo.Adapter.Todo_Adapter;
import com.example.todo.Adapter.ViewAll_Todo_Adapter;
import com.example.todo.Model.TodoModel;
import com.example.todo.R;
import com.example.todo.databinding.ActivityMainBinding;
import com.example.todo.databinding.ActivityViewAllBinding;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.todo.Util.NetUtils.NET_URL;

public class ViewAllActivity extends AppCompatActivity {

    ActivityViewAllBinding activityViewAllBinding;
    private RequestQueue requestQueue;
    private ViewAll_Todo_Adapter todo_adapter;

    private JsonArrayRequest todoRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //JetPack View Binding Setup
        activityViewAllBinding = ActivityViewAllBinding.inflate(getLayoutInflater());
        View view = activityViewAllBinding.getRoot();
        setContentView(view);
        //End of JetPack View Binding Setup

        requestQueue = Volley.newRequestQueue(getApplicationContext());//init volley request


        initData();
        initView();
    }

    private void initView() {
        activityViewAllBinding.searchLayout.searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                todo_adapter.getFilter().filter(activityViewAllBinding.searchLayout.searchEdt
                                .getText().toString(),
                        new Filter.FilterListener() {
                            @Override
                            public void onFilterComplete(int count) {


                            }
                        });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initData() {
        String url = NET_URL + "?_start=0&_limit=15";

        todoRequest = new JsonArrayRequest(Request.Method.GET, url, null
                , new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    List<TodoModel> tempList = Arrays.asList(
                            mapper.readValue(response.toString(), TodoModel[].class));
                    ArrayList<TodoModel> todoModelArrayList = new ArrayList<TodoModel>(tempList);
                    todo_adapter = new ViewAll_Todo_Adapter(ViewAllActivity.this, todoModelArrayList);
                    activityViewAllBinding.todoViewallRecyclerView
                            .setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    activityViewAllBinding.todoViewallRecyclerView.setAdapter(todo_adapter);
                    activityViewAllBinding.todoViewallRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));

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

}