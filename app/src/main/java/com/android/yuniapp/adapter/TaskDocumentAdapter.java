package com.android.yuniapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.listener.DocumentClickListener;
import com.android.yuniapp.listener.RecyclerItemClickListener;
import com.android.yuniapp.model.DocumentRequestModel;
import com.android.yuniapp.model.DocumentResponseModel;

import java.util.ArrayList;

public class TaskDocumentAdapter extends RecyclerView.Adapter<TaskDocumentAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<DocumentResponseModel> documentList;
    private DocumentClickListener documentClickListener;

    public TaskDocumentAdapter(Context context, ArrayList<DocumentResponseModel> documentList, DocumentClickListener documentClickListener) {
        this.context = context;
        this.documentList = documentList;
        this.documentClickListener = documentClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_task_document, parent, false);
        return new TaskDocumentAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
        DocumentResponseModel documentResponseModel=documentList.get(myViewHolder.getAdapterPosition());
        if(documentResponseModel!=null)
        {
            if(documentResponseModel.getDocument_type()!=null && !documentResponseModel.getDocument_type().isEmpty() && documentResponseModel.getDocument_type().equalsIgnoreCase("I"))
            {
                myViewHolder.imgIcon.setImageResource(R.drawable.img_icon);
                myViewHolder.txtDocDesc.setText("IMAGE");
            }
            else {
                myViewHolder.imgIcon.setImageResource(R.drawable.pdf_icon);
                myViewHolder.txtDocDesc.setText("PDF");
            }

            myViewHolder.txtRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    documentClickListener.onRemoveClick(myViewHolder.getAdapterPosition());
                }
            });

            myViewHolder.layoutDocument.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    documentClickListener.onDocumentClick(myViewHolder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgIcon;
        private TextView txtDocDesc,txtRemove;
        private RelativeLayout layoutDocument;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgIcon=itemView.findViewById(R.id.img_icon);
            txtDocDesc=itemView.findViewById(R.id.txt_document_desc);
            txtRemove=itemView.findViewById(R.id.txt_remove);
            layoutDocument=itemView.findViewById(R.id.layout_document);
         }
    }
}
