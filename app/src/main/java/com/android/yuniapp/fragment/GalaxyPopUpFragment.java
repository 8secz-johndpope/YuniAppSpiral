package com.android.yuniapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.yuniapp.R;
import com.android.yuniapp.adapter.GalaxyStarsAdapter;

public class GalaxyPopUpFragment extends DialogFragment {
   private View view;
   private RecyclerView recyclerViewGalaxy;
   private GalaxyStarsAdapter galaxyStarsAdapter;

   private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_galaxy_popup,container,false);

        //recyclerViewGalaxy.setLayoutManager(new LinearLayoutManager(getActivity()));



        return view;
    }
}
