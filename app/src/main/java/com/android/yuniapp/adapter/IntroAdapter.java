package com.android.yuniapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.model.IntroPagerModel;
import com.android.yuniapp.utils.CustomTypefaceSpan;

import java.util.ArrayList;

public class IntroAdapter extends PagerAdapter {
    private Context mContext;
    private ArrayList<IntroPagerModel> introPagerModelArrayList;
    private LayoutInflater layoutInflater;


    public IntroAdapter(Context mContext, ArrayList<IntroPagerModel> introPagerModelArrayList) {
        this.mContext = mContext;
        this.introPagerModelArrayList = introPagerModelArrayList;
        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return introPagerModelArrayList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View itemView = layoutInflater.inflate(R.layout.adapter_viewpager_intro, container, false);

        ImageView imageView = itemView.findViewById(R.id.img_page);
        TextView txtTitle=itemView.findViewById(R.id.txt_title);
        TextView txtSubTitle=itemView.findViewById(R.id.txt_sub_title);

        imageView.setImageResource(introPagerModelArrayList.get(position).getImageId());

        txtSubTitle.setText(introPagerModelArrayList.get(position).getSubtitle());

        SpannableStringBuilder spanTxt = new SpannableStringBuilder(introPagerModelArrayList.get(position).getTitle());
        Typeface typeface = ResourcesCompat.getFont(mContext, R.font.teko_medium);

        spanTxt.setSpan (new RelativeSizeSpan(1.2f), 6, introPagerModelArrayList.get(position).getTitle().length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spanTxt.setSpan (new CustomTypefaceSpan("", typeface), 6, introPagerModelArrayList.get(position).getTitle().length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        txtTitle.setText(spanTxt);

        container.addView(itemView);

        return itemView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
