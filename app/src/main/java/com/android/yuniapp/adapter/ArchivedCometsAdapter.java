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
import com.android.yuniapp.model.CometResponseModel;
import com.android.yuniapp.model.StarResponseModel;

import java.util.ArrayList;

public class ArchivedCometsAdapter extends RecyclerView.Adapter<ArchivedCometsAdapter.MyViewHolder> {
    private Context context;
    private PopupWindowListener popupWindowListener;
    private ArrayList<CometResponseModel> archivedCometsList;

    public ArchivedCometsAdapter(Context context, PopupWindowListener popupWindowListener, ArrayList<CometResponseModel> archivedCometsList) {
        this.context = context;
        this.popupWindowListener = popupWindowListener;
        this.archivedCometsList = archivedCometsList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comets, parent, false);
        return new ArchivedCometsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder viewHolder, int posiiton) {
        CometResponseModel cometResponseModel=archivedCometsList.get(posiiton);
        if(cometResponseModel!=null)
        {
            if(cometResponseModel.getName()!=null && !cometResponseModel.getName().isEmpty())
                viewHolder.txtComet.setText(cometResponseModel.getName());
            else viewHolder.txtComet.setText("NA");


            viewHolder.txtComet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindowListener.onTaskClick(v,viewHolder.getAdapterPosition(),"archive");
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return archivedCometsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtComet;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtComet=itemView.findViewById(R.id.txt_comet);
        }
    }
}
