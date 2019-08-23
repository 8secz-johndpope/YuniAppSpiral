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
import com.android.yuniapp.model.PlanetResponseModel;
import com.android.yuniapp.model.StarResponseModel;

import java.util.ArrayList;

public class UnLaunchedPlanetsAdapter extends RecyclerView.Adapter<UnLaunchedPlanetsAdapter.MyViewHolder> {
    private Context context;
    private PopupWindowListener popupWindowListener;
    private ArrayList<PlanetResponseModel> unLaunchedPlanetsList;

    public UnLaunchedPlanetsAdapter(Context context, PopupWindowListener popupWindowListener, ArrayList<PlanetResponseModel> unLaunchedPlanetsList) {
        this.context = context;
        this.popupWindowListener = popupWindowListener;
        this.unLaunchedPlanetsList = unLaunchedPlanetsList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_planet, parent, false);
        return new UnLaunchedPlanetsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder viewHolder, int posiiton) {
        PlanetResponseModel planetResponseModel=unLaunchedPlanetsList.get(posiiton);
        if(planetResponseModel!=null)
        {
            if(planetResponseModel.getName()!=null && !planetResponseModel.getName().isEmpty())
                viewHolder.txtPlanet.setText(planetResponseModel.getName());
            else viewHolder.txtPlanet.setText("NA");


            viewHolder.txtPlanet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindowListener.onTaskClick(v,viewHolder.getAdapterPosition(),"unLaunch");
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return unLaunchedPlanetsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtPlanet;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlanet=itemView.findViewById(R.id.txt_planet);
        }
    }
}
