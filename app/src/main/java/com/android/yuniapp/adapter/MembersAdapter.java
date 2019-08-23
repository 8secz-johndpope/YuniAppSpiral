package com.android.yuniapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.listener.AddMembersListener;
import com.android.yuniapp.model.MembersModel;
import com.google.android.gms.common.data.DataHolder;

import java.util.ArrayList;
import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MyViewHolder> {
     private Context context;
     private ArrayList<MembersModel> membersArrayList;
     private AddMembersListener  addMembersListener;

    public MembersAdapter(Context context, ArrayList<MembersModel> membersArrayList, AddMembersListener addMembersListener) {
        this.context = context;
        this.membersArrayList = membersArrayList;
        this.addMembersListener = addMembersListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_members, parent, false);
        return new MembersAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int position) {
        final MembersModel membersModel=membersArrayList.get(myViewHolder.getAdapterPosition());
        if(membersModel!=null)
        {

            myViewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  addMembersListener.onChecked(membersModel.getId());
                }
            });

            if(membersModel.getMember_name()!=null && !membersModel.getMember_name().isEmpty())
                myViewHolder.txtName.setText(membersModel.getMember_name());
            else myViewHolder.txtName.setText("NA");

            if(membersModel.getTo_email_id()!=null && !membersModel.getTo_email_id().isEmpty())
                myViewHolder.txtEmail.setText(membersModel.getTo_email_id());
            else myViewHolder.txtEmail.setText("NA");

            if(membersModel.getIsSelected()>=0)
                myViewHolder.checkBox.setChecked(true);
            else myViewHolder.checkBox.setChecked(false);


        }

    }

    @Override
    public int getItemCount() {
        return membersArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtName,txtEmail;
        private CheckBox checkBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName=itemView.findViewById(R.id.txt_name);
            txtEmail=itemView.findViewById(R.id.txt_email);
            checkBox=itemView.findViewById(R.id.checkbox);
        }
    }
    public void updateList(ArrayList<MembersModel> list){
        membersArrayList = list;
        notifyDataSetChanged();
    }

}
