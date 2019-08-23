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
import com.android.yuniapp.model.StarResponseModel;

import java.util.ArrayList;

public class ArchivedStarsAdapter extends RecyclerView.Adapter<ArchivedStarsAdapter.MyViewHolder> {
    private Context context;
    private PopupWindowListener popupWindowListener;
    private ArrayList<StarResponseModel> archivedStarsList;

    public ArchivedStarsAdapter(Context context, PopupWindowListener popupWindowListener, ArrayList<StarResponseModel> archivedStarsList) {
        this.context = context;
        this.popupWindowListener = popupWindowListener;
        this.archivedStarsList = archivedStarsList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_stars, parent, false);
        return new ArchivedStarsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder viewHolder, int posiiton) {
        StarResponseModel starResponseModel=archivedStarsList.get(posiiton);
        if(starResponseModel!=null)
        {
            if(starResponseModel.getName()!=null && !starResponseModel.getName().isEmpty())
                viewHolder.txtStar.setText(starResponseModel.getName());
            else viewHolder.txtStar.setText("NA");


            viewHolder.txtStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindowListener.onTaskClick(v,viewHolder.getAdapterPosition(),"archive");
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return archivedStarsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtStar;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtStar=itemView.findViewById(R.id.txt_star);
        }
    }
}
