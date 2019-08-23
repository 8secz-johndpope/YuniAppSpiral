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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.model.MoonResponseModel;
import com.android.yuniapp.model.MoonStorageModel;
import com.android.yuniapp.model.PlanetStorageModel;
import com.google.android.gms.auth.api.signin.internal.Storage;

import java.util.ArrayList;
import java.util.List;

public class StoragePlanetAdapter extends RecyclerView.Adapter<StoragePlanetAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<PlanetStorageModel> planetStorageList;
    private int expendedPosition=-1;
    private boolean isExpanded=false;

    public StoragePlanetAdapter(Context context, ArrayList<PlanetStorageModel> planetStorageList) {
        this.context = context;
        this.planetStorageList = planetStorageList;
    }


    @NonNull
    @Override
    public StoragePlanetAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_storage_planet, parent, false);
        return new StoragePlanetAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final StoragePlanetAdapter.MyViewHolder myViewHolder, int position) {
        final PlanetStorageModel planetStorageModel = planetStorageList.get(myViewHolder.getAdapterPosition());

        if (planetStorageModel != null) {
            if (planetStorageModel.getName() != null && !planetStorageModel.getName().isEmpty())
                myViewHolder.txtPlanetName.setText(planetStorageModel.getName());
            else myViewHolder.txtPlanetName.setText("NA");


            if (planetStorageModel.getMoons() != null && planetStorageModel.getMoons().size() > 0) {
                StorageMoonAdapter storageMoonAdapter = new StorageMoonAdapter(context, planetStorageList.get(myViewHolder.getAdapterPosition()).getMoons());
                myViewHolder.recyclerViewMoon.setAdapter(storageMoonAdapter);
            } else {
            }


            if (myViewHolder.getAdapterPosition() == planetStorageList.size() - 1)
                myViewHolder.viewVer.setVisibility(View.GONE);
            else myViewHolder.viewVer.setVisibility(View.VISIBLE);

            myViewHolder.imgPlanet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(myViewHolder.recyclerViewMoon.getVisibility()==View.VISIBLE)
                    {
                        myViewHolder.recyclerViewMoon.setVisibility(View.GONE);
                        if (myViewHolder.getAdapterPosition() == planetStorageList.size() - 1)
                            myViewHolder.viewVer.setVisibility(View.GONE);
                        else myViewHolder.viewVer.setVisibility(View.VISIBLE);
                    }else {
                        myViewHolder.recyclerViewMoon.setVisibility(View.VISIBLE);
                        if(planetStorageModel.getMoons().size()>0)
                            myViewHolder.viewVer.setVisibility(View.VISIBLE);
                    }



                     /*  if(expendedPosition!=myViewHolder.getAdapterPosition())
                           isExpanded=false;

                    if (isExpanded) {

                        isExpanded = false;
                        myViewHolder.recyclerViewMoon.setVisibility(View.GONE);
                        if (myViewHolder.getAdapterPosition() == planetStorageList.size() - 1)
                            myViewHolder.viewVer.setVisibility(View.GONE);
                        else myViewHolder.viewVer.setVisibility(View.VISIBLE);
                    } else {
                        expendedPosition=myViewHolder.getAdapterPosition();
                        isExpanded = true;
                        myViewHolder.recyclerViewMoon.setVisibility(View.VISIBLE);
                        if(planetStorageModel.getMoons().size()>0)
                        myViewHolder.viewVer.setVisibility(View.VISIBLE);
                    }*/

                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return planetStorageList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private RecyclerView recyclerViewMoon;

        private ImageView imgPlanet;
        private TextView txtPlanetName;
        private View viewHor, viewVer;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtPlanetName = itemView.findViewById(R.id.txt_planet_name);
            imgPlanet = itemView.findViewById(R.id.img_planet);
            recyclerViewMoon = itemView.findViewById(R.id.recycler_moon);
            recyclerViewMoon.setLayoutManager(new LinearLayoutManager(context));
            viewVer = itemView.findViewById(R.id.view_ver);

        }
    }
}
