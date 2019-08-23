package com.android.yuniapp.adapter;

import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.model.MoonStorageModel;
import com.android.yuniapp.model.SatelliteStorageModel;

import java.util.ArrayList;

public class StorageMoonAdapter extends RecyclerView.Adapter<StorageMoonAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<MoonStorageModel> moonStorageList;
    private boolean isExpanded;

    public StorageMoonAdapter(Context context, ArrayList<MoonStorageModel> moonStorageList) {
        this.context = context;
        this.moonStorageList = moonStorageList;
    }

    public StorageMoonAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_storage_moon, parent, false);
        return new StorageMoonAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
        final MoonStorageModel moonStorageModel=moonStorageList.get(myViewHolder.getAdapterPosition());

        if(moonStorageModel!=null)
        {

            if(moonStorageModel.getName()!=null && !moonStorageModel.getName().isEmpty())
                myViewHolder.txtMoonName.setText(moonStorageModel.getName());
            else myViewHolder.txtMoonName.setText("NA");

            if(moonStorageModel.getSatellites()!=null && moonStorageModel.getSatellites().size()>0)
            {
               StorageSatelliteAdapter storageSatelliteAdapter=new StorageSatelliteAdapter(context,moonStorageModel.getSatellites());
                myViewHolder.recyclerViewSatellite.setAdapter(storageSatelliteAdapter);
            }else {

            }
             if(myViewHolder.getAdapterPosition()==0)
                 myViewHolder.viewHor.setVisibility(View.VISIBLE);
             else myViewHolder.viewHor.setVisibility(View.INVISIBLE);


            if (myViewHolder.getAdapterPosition() == moonStorageList.size() - 1)
                myViewHolder.viewVer.setVisibility(View.GONE);
            else myViewHolder.viewVer.setVisibility(View.VISIBLE);


        }

        myViewHolder.imgMoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(myViewHolder.recyclerViewSatellite.getVisibility()==View.VISIBLE)
                {
                    myViewHolder.recyclerViewSatellite.setVisibility(View.GONE);
                    if (myViewHolder.getAdapterPosition() == moonStorageList.size() - 1)
                        myViewHolder.viewVer.setVisibility(View.GONE);
                    else myViewHolder.viewVer.setVisibility(View.VISIBLE);
                }else {
                    myViewHolder.recyclerViewSatellite.setVisibility(View.VISIBLE);
                    if(moonStorageModel.getSatellites().size()>0)
                        myViewHolder.viewVer.setVisibility(View.VISIBLE);
                }
               /* if(isExpanded)
                {
                    isExpanded=false;
                    myViewHolder.recyclerViewSatellite.setVisibility(View.GONE);
                    if (myViewHolder.getAdapterPosition() == moonStorageList.size() - 1)
                        myViewHolder.viewVer.setVisibility(View.GONE);
                    else myViewHolder.viewVer.setVisibility(View.VISIBLE);
                }else {
                    isExpanded=true;
                    myViewHolder.recyclerViewSatellite.setVisibility(View.VISIBLE);
                    if(moonStorageModel.getSatellites().size()>0)
                    myViewHolder.viewVer.setVisibility(View.VISIBLE);
                }*/
            }
        });
    }

    @Override
    public int getItemCount() {
        return moonStorageList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgMoon;
        private RecyclerView recyclerViewSatellite;
        private TextView txtMoonName;
        private View viewHor,viewVer;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtMoonName=itemView.findViewById(R.id.txt_moon_name);
            imgMoon=itemView.findViewById(R.id.img_moon);
            recyclerViewSatellite=itemView.findViewById(R.id.recycler_satellite);
            recyclerViewSatellite.setLayoutManager(new LinearLayoutManager(context));

            viewHor=itemView.findViewById(R.id.view_hor);
            viewVer=itemView.findViewById(R.id.view_ver);
        }
    }
}
