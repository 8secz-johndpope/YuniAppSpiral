package com.android.yuniapp.model;

import java.util.ArrayList;

public class GetSatelliteResponseModel {
    private ArrayList<SatelliteResponseModel> satellites;

    public ArrayList<SatelliteResponseModel> getSatellites() {
        return satellites;
    }

    public void setSatellites(ArrayList<SatelliteResponseModel> satellites) {
        this.satellites = satellites;
    }
}
