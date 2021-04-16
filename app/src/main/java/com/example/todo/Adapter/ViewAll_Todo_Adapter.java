package com.example.todo.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.Activity.MainActivity;
import com.example.todo.Model.TodoModel;
import com.example.todo.OnLoadMoreListener;
import com.example.todo.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ViewAll_Todo_Adapter extends RecyclerView.Adapter<ViewAll_Todo_Adapter.MyViewHolder> implements
        Filterable {
    private Context context;
    private ArrayList<TodoModel> todoModelArrayLists;
    private ArrayList<TodoModel> todoModelArrayLists_filter;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;


    public ViewAll_Todo_Adapter(Context ct, ArrayList<TodoModel> todoModelArrayList, RecyclerView recyclerView) {
        context = ct;
        todoModelArrayLists = todoModelArrayList;
        todoModelArrayLists_filter = new ArrayList<>(todoModelArrayLists);

//        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                .getLayoutManager();
        recyclerView
                .addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView,
                                           int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager
                                .findLastVisibleItemPosition();
                        if (totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                            // End has been reached
                            // Do something
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore();
                            }

                        }
                    }
                });

    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
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

        holder.todo_user_id_Tv.setText("User ID : " + todoModelArrayLists.get(position).getUserId());
        holder.todo_title_Edt.setText(todoModelArrayLists.get(position).getTitle());
        holder.todo_title_Edt.setEnabled(false);

        holder.checkBox.setChecked(todoModelArrayLists.get(position).getCompleted());


    }

    @Override
    public int getItemCount() {
        return todoModelArrayLists.size();
    }

    @Override
    public Filter getFilter() {
        return todoFilter;
    }

    private Filter todoFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            ArrayList<TodoModel> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(todoModelArrayLists_filter);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (TodoModel todoModel : todoModelArrayLists_filter) {

                    if (todoModel.getTitle().toLowerCase().trim().contains(filterPattern)) {
                        filteredList.add(todoModel);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            todoModelArrayLists.clear();
            todoModelArrayLists.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBox;
        private EditText todo_title_Edt;
        private TextView todo_user_id_Tv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.checkbox_cb);
            todo_title_Edt = itemView.findViewById(R.id.todo_Edt);
            todo_user_id_Tv = itemView.findViewById(R.id.user_id_tv);
        }
    }

    public void updateFilterList(ArrayList<TodoModel> todoModels) {
        todoModelArrayLists_filter.clear();
        todoModelArrayLists_filter = new ArrayList<>(todoModels);

    }


}
