package com.android.yuniapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.model.EntityModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class EntitiesDetailsAdapter extends RecyclerView.Adapter<EntitiesDetailsAdapter.MyViewHolder> {
   private Context context;
   private ArrayList<EntityModel> entityArrayList;

    public EntitiesDetailsAdapter(Context context, ArrayList<EntityModel> entityArrayList) {
        this.context = context;
        this.entityArrayList = entityArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_entities_details, parent, false);
        return new EntitiesDetailsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int position) {
        EntityModel entityModel=entityArrayList.get(myViewHolder.getAdapterPosition());

        if(entityModel!=null)
        {

            if(entityModel.getName()!=null)
                myViewHolder.txtEntityName.setText(entityModel.getName());

            if(entityModel.getStart_date()!=null) {
                try {

                    myViewHolder.txtStartDate.setText("Start Date:"+"      "+new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(entityModel.getFinish_date())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(entityModel.getFinish_date()!=null)
            {
                try {

                    myViewHolder.txtFinishDate.setText("Finish Date:"+"    "+new SimpleDateFormat("MMM dd, yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(entityModel.getFinish_date())));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if(entityModel.getPriority()!=null)
                myViewHolder.txtPriority.setText("Priority:"+"        "+entityModel.getPriority());

            if(entityModel.getMembers()!=null)
                myViewHolder.txtMembers.setText("Members:"+"    "+entityModel.getMembers().size());

        }

    }

    @Override
    public int getItemCount() {
        return entityArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtEntityName,txtStartDate,txtFinishDate,txtPriority,txtMembers;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtEntityName=itemView.findViewById(R.id.txt_entity_name);
            txtStartDate=itemView.findViewById(R.id.txt_start_date);
            txtFinishDate=itemView.findViewById(R.id.txt_finish_date);
            txtPriority=itemView.findViewById(R.id.txt_priority);
            txtMembers=itemView.findViewById(R.id.txt_members);
        }
    }
}
