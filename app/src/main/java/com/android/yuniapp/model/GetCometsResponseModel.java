package com.android.yuniapp.model;

import java.util.ArrayList;

public class GetCometsResponseModel {
    private ArrayList<CometResponseModel> comets;

    public ArrayList<CometResponseModel> getComets() {
        return comets;
    }

    public void setComets(ArrayList<CometResponseModel> comets) {
        this.comets = comets;
    }
}
