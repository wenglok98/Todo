package com.example.todo.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.todo.Adapter.Todo_Adapter;
import com.example.todo.Adapter.ViewAll_Todo_Adapter;
import com.example.todo.Model.TodoModel;
import com.example.todo.OnLoadMoreListener;
import com.example.todo.R;
import com.example.todo.Util.SnackUtil;
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
    private Boolean flag_loading = false;
    private Boolean trigger_LoadMore = true;
    private Boolean last_load = false;
    private JsonArrayRequest todoRequest;
    ArrayList<TodoModel> todoModelArrayList;
    private LinearLayoutManager mLinearLayoutManager;

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
                /*When searching dont trigger load_more function because when filter results will
                are less will make visible item = last item which match the load_more condition */

                //Add flag to disable loadmore when searching
                trigger_LoadMore = false;
                if (
                        activityViewAllBinding.searchLayout.searchEdt.getText().toString().equals("")
                                ||
                                activityViewAllBinding.searchLayout.searchEdt.getText().toString().equals(null)
                ) {
                    trigger_LoadMore = true; // Set flag to enable load more when edittext is empty
                }

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


        //Close Keyboard upon scrolling/touching recyclerView

        try {
            activityViewAllBinding.todoViewallRecyclerView.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    int start = 16;

    private void loadMore() {
        activityViewAllBinding.loadingLottie.setVisibility(View.VISIBLE); // Show Loading Sign



        String url = NET_URL + "?_start=" + String.valueOf(start) + "&_limit=15";

        todoRequest = new JsonArrayRequest(Request.Method.GET, url, null
                , new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    List<TodoModel> tempList = Arrays.asList(
                            mapper.readValue(response.toString(), TodoModel[].class));
                    if (tempList.size() != 0) {
//
                        todoModelArrayList.addAll(tempList);

                        todo_adapter.updateFilterList(todoModelArrayList);
                        //Replace the filter Array List with new Array list

                    } else {
                        last_load = true;
                        SnackUtil.show(ViewAllActivity.this, "You Have Reached Bottom !");

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
                todo_adapter.notifyDataSetChanged();//Notify the adapter with new data

                flag_loading = false; //Set loading state to false, so that new loading request can come in

                start += 15; // Plus 15 for the offset

                activityViewAllBinding.loadingLottie.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(todoRequest);

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
                    todoModelArrayList = new ArrayList<TodoModel>(tempList);
                    activityViewAllBinding.todoViewallRecyclerView
                            .setLayoutManager(new LinearLayoutManager(ViewAllActivity.this));

                    todo_adapter = new ViewAll_Todo_Adapter(ViewAllActivity.this,
                            todoModelArrayList, activityViewAllBinding.todoViewallRecyclerView);

                    activityViewAllBinding.todoViewallRecyclerView.setAdapter(todo_adapter);
                    activityViewAllBinding.todoViewallRecyclerView.addItemDecoration(
                            new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
                    initLoadMoreListener();

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

    private void initLoadMoreListener() {
        todo_adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {

                //Three conditions are Calling, whether the user is using search filter, Is the Request Ongoing and No more Items from API
                //Therefore, will not call load more function when the user are using search
                // filter or the api is loading or there is no more item return from api

                if (trigger_LoadMore == true && flag_loading == false && last_load == false) {
                    flag_loading = true;
                    loadMore();
                }

            }
        });
    }

}