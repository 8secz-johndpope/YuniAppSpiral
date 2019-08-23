package com.android.yuniapp.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.listener.ItemClickListener;

public class GenderSelectionFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    private View view;
    private TextView textViewMale, textViewFemale;
    private ItemClickListener itemClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_gender, container, false);
        textViewMale = view.findViewById(R.id.male);
        textViewFemale = view.findViewById(R.id.female);
        textViewMale.setOnClickListener(this);
        textViewFemale.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.male:
                itemClickListener.onClick(getResources().getString(R.string.male),"M");
                dismiss();
                break;
            case R.id.female:
                itemClickListener.onClick(getResources().getString(R.string.female),"F");
                dismiss();
                break;
        }
    }

    public void setListener(ItemClickListener ItemClickListener) {
        this.itemClickListener = ItemClickListener;
    }
}
