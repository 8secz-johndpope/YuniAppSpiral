package com.android.yuniapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.yuniapp.R;
import com.android.yuniapp.model.UserImages;
import com.android.yuniapp.utils.ConstantUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class UserImagePagerAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<UserImages> userImagesArrayList;
    LayoutInflater mLayoutInflater;

    public UserImagePagerAdapter(Context context, ArrayList<UserImages> userImagesArrayList) {
        this.context = context;
        this.userImagesArrayList = userImagesArrayList;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.adapter_user_images_pager, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.img_user);


        if (userImagesArrayList.get(position).getImage_url() != null && !userImagesArrayList.get(position).getImage_url().isEmpty())
            Glide.with(context).load(ConstantUtils.BASE_URL + userImagesArrayList.get(position).getImage_url()).placeholder(R.drawable.img_placeholder).into(imageView);
        else imageView.setImageResource(R.drawable.img_placeholder);
        container.addView(itemView);

        return itemView;
    }

    @Override
    public int getCount() {
        return userImagesArrayList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
