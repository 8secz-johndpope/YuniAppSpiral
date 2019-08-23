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
import com.android.yuniapp.listener.GalaxyListener;
import com.android.yuniapp.model.StarResponseModel;

import java.util.ArrayList;

public class GalaxyStarsAdapter extends RecyclerView.Adapter<GalaxyStarsAdapter.MyViewHolder>{
    private Context context;
    private ArrayList<StarResponseModel> galaxyStarList;
    private GalaxyListener galaxyListener;


    public GalaxyStarsAdapter(Context context, ArrayList<StarResponseModel> galaxyStarList, GalaxyListener galaxyListener) {
        this.context = context;
        this.galaxyStarList = galaxyStarList;
        this.galaxyListener = galaxyListener;
    }

    @NonNull
    @Override
    public GalaxyStarsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_stars, parent, false);
        return new GalaxyStarsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalaxyStarsAdapter.MyViewHolder myViewHolder, int position) {
        final StarResponseModel starResponseModel=galaxyStarList.get(myViewHolder.getAdapterPosition());
        if(starResponseModel!=null)
        {
            if(starResponseModel.getType().equalsIgnoreCase("S"))
            {
                  myViewHolder.txtStar.setVisibility(View.VISIBLE);
                  myViewHolder.txtComet.setVisibility(View.GONE);
                  myViewHolder.txtStar.setText(starResponseModel.getName());
                  myViewHolder.txtStar.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          galaxyListener.onGalaxyItemClick(Integer.parseInt(starResponseModel.getId()));
                      }
                  });
            }else
            {
                myViewHolder.txtStar.setVisibility(View.GONE);
                myViewHolder.txtComet.setVisibility(View.VISIBLE);
                myViewHolder.txtComet.setText(starResponseModel.getName());
                myViewHolder.txtComet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        galaxyListener.onGalaxyItemClick(Integer.parseInt(starResponseModel.getId()));
                    }
                });
            }


        }

    }

    @Override
    public int getItemCount() {
        return galaxyStarList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtStar,txtComet;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtStar=itemView.findViewById(R.id.txt_star);
            txtComet=itemView.findViewById(R.id.txt_comet);

        }
    }
}
