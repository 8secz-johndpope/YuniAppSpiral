package com.android.yuniapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.listener.PopupWindowListener;
import com.android.yuniapp.model.MoonResponseModel;
import com.android.yuniapp.model.StarResponseModel;

import java.util.ArrayList;

public class UnLaunchedMoonsAdapter extends RecyclerView.Adapter<UnLaunchedMoonsAdapter.MyViewHolder> {
    private Context context;
    private PopupWindowListener popupWindowListener;
    private ArrayList<MoonResponseModel> unLaunchedMoonsList;

    public UnLaunchedMoonsAdapter(Context context, PopupWindowListener popupWindowListener, ArrayList<MoonResponseModel> unLaunchedMoonsList) {
        this.context = context;
        this.popupWindowListener = popupWindowListener;
        this.unLaunchedMoonsList = unLaunchedMoonsList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_moon, parent, false);
        return new UnLaunchedMoonsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder viewHolder, int position) {
        MoonResponseModel  moonResponseModel=unLaunchedMoonsList.get(position);
        if(moonResponseModel!=null)
        {
            if(moonResponseModel.getName()!=null && !moonResponseModel.getName().isEmpty())
                viewHolder.txtMoon.setText(moonResponseModel.getName());
            else viewHolder.txtMoon.setText("NA");


            viewHolder.txtMoon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindowListener.onTaskClick(v,viewHolder.getAdapterPosition(),"unLaunch");
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return unLaunchedMoonsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtMoon;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMoon=itemView.findViewById(R.id.txt_moon);
        }
    }
}
