package com.android.yuniapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.listener.RecyclerItemClickListener;
import com.android.yuniapp.model.GetAllTasksResponseModel;
import com.android.yuniapp.model.TaskModel;

import java.util.ArrayList;

public class CalendarTaskAdapter extends RecyclerView.Adapter<CalendarTaskAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<TaskModel> taskList;
    private RecyclerItemClickListener recyclerItemClickListener;

    public CalendarTaskAdapter(Context context, ArrayList<TaskModel> taskList, RecyclerItemClickListener recyclerItemClickListener) {
        this.context = context;
        this.taskList = taskList;
        this.recyclerItemClickListener = recyclerItemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_calendar_task, parent, false);
        return new CalendarTaskAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
        TaskModel taskModel=taskList.get(myViewHolder.getAdapterPosition());

        if(taskModel!=null)
        {
            if(taskModel.getName()!=null && !taskModel.getName().isEmpty())
               myViewHolder.txtTask.setText(taskModel.getName());
            else myViewHolder.txtTask.setText("NA");

            myViewHolder.imgRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerItemClickListener.onItemClick(myViewHolder.getAdapterPosition());
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtTask;
        private ImageView imgRemove;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTask=itemView.findViewById(R.id.txt_task);
            imgRemove=itemView.findViewById(R.id.img_remove);
        }
    }
}
