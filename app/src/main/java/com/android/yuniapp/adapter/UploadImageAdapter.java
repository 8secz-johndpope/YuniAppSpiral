package com.android.yuniapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.yuniapp.R;
import com.android.yuniapp.listener.RecyclerItemClickListener;
import com.android.yuniapp.listener.UserImageRecyclerListener;
import com.android.yuniapp.model.UserImages;
import com.android.yuniapp.utils.ConstantUtils;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UploadImageAdapter extends RecyclerView.Adapter<UploadImageAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<UserImages> userImagesArrayList;
    private UserImageRecyclerListener userImageRecyclerListener;

    public UploadImageAdapter(Context context, ArrayList<UserImages> userImagesArrayList, UserImageRecyclerListener userImageRecyclerListener) {
        this.context = context;
        this.userImagesArrayList = userImagesArrayList;
        this.userImageRecyclerListener = userImageRecyclerListener;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_upload_images, viewGroup, false);
        return new UploadImageAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int position) {
        UserImages userImages=userImagesArrayList.get(myViewHolder.getAdapterPosition());
        if(position!=0)
        {
            if(userImages.getImage_url()!=null && !userImages.getImage_url().isEmpty())
                Glide.with(context).load(ConstantUtils.BASE_URL+userImages.getImage_url()).placeholder(R.drawable.img_placeholder).into(myViewHolder.imgPicture);
                //Picasso.with(context).load(ConstantUtils.BASE_URL+userImages.getImage_url()).placeholder(R.drawable.img_placeholder).into(myViewHolder.imgPicture);
            else myViewHolder.imgPicture.setImageResource(R.drawable.profile_placeholder);

            myViewHolder.imgPicture.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    userImageRecyclerListener.onItemLongClick(myViewHolder.getAdapterPosition());
                    return true;
                }
            });
        }
        else if(position==0)
        {
            myViewHolder.imgPicture.setImageResource(R.drawable.add_img_placeholder);
        }

        myViewHolder.imgPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userImageRecyclerListener.onItemClick(myViewHolder.getAdapterPosition());
            }
        });



    }

    @Override
    public int getItemCount() {
        return userImagesArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView imgPicture;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPicture=itemView.findViewById(R.id.img_picture);
        }
    }
}
