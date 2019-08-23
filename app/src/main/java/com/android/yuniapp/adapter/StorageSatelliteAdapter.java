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
import com.android.yuniapp.model.SatelliteStorageModel;

import java.util.ArrayList;

public class StorageSatelliteAdapter extends RecyclerView.Adapter<StorageSatelliteAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<SatelliteStorageModel> satelliteStorageList;

    public StorageSatelliteAdapter(Context context, ArrayList<SatelliteStorageModel> satelliteStorageList) {
        this.context = context;
        this.satelliteStorageList = satelliteStorageList;
    }

    public StorageSatelliteAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_storage_satellite, parent, false);
        return new StorageSatelliteAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
     SatelliteStorageModel satelliteStorageModel=satelliteStorageList.get(myViewHolder.getAdapterPosition());

     if(satelliteStorageModel!=null)
     {

         if(satelliteStorageModel.getName()!=null && !satelliteStorageModel.getName().isEmpty())
              myViewHolder.txtSatellite.setText(satelliteStorageModel.getName());
         else myViewHolder.txtSatellite.setText("NA");

         if (myViewHolder.getAdapterPosition() == satelliteStorageList.size() - 1)
             myViewHolder.viewVer.setVisibility(View.GONE);
         else myViewHolder.viewVer.setVisibility(View.VISIBLE);

         if(myViewHolder.getAdapterPosition()==0)
             myViewHolder.viewHor.setVisibility(View.VISIBLE);
         else myViewHolder.viewHor.setVisibility(View.INVISIBLE);
     }
    }

    @Override
    public int getItemCount() {
        return satelliteStorageList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgSatellite;
        private TextView txtSatellite;
        private View viewHor,viewVer;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgSatellite=itemView.findViewById(R.id.img_satellite);
            txtSatellite=itemView.findViewById(R.id.txt_satellite_name);
            viewHor=itemView.findViewById(R.id.view_hor);
            viewVer=itemView.findViewById(R.id.view_ver);
        }
    }
}
