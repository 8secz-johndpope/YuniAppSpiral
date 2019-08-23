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
import com.android.yuniapp.model.SatelliteResponseModel;
import com.android.yuniapp.model.StarResponseModel;

import java.util.ArrayList;

public class ArchivedSatellitesAdapter extends RecyclerView.Adapter<ArchivedSatellitesAdapter.MyViewHolder> {
    private Context context;
    private PopupWindowListener popupWindowListener;
    private ArrayList<SatelliteResponseModel> archivedSatellitesList;

    public ArchivedSatellitesAdapter(Context context, PopupWindowListener popupWindowListener, ArrayList<SatelliteResponseModel> archivedSatellitesList) {
        this.context = context;
        this.popupWindowListener = popupWindowListener;
        this.archivedSatellitesList = archivedSatellitesList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_satellite, parent, false);
        return new ArchivedSatellitesAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder viewHolder, int position) {
        SatelliteResponseModel satelliteResponseModel=archivedSatellitesList.get(position);
        if(satelliteResponseModel!=null)
        {
            if(satelliteResponseModel.getName()!=null && !satelliteResponseModel.getName().isEmpty())
                viewHolder.txtSatellite.setText(satelliteResponseModel.getName());
            else viewHolder.txtSatellite.setText("NA");


            viewHolder.txtSatellite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindowListener.onTaskClick(v,viewHolder.getAdapterPosition(),"archive");
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return archivedSatellitesList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtSatellite;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSatellite=itemView.findViewById(R.id.txt_satellite);
        }
    }
}
