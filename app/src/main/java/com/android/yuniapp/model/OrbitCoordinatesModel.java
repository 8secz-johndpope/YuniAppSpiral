package com.android.yuniapp.model;

import java.util.ArrayList;

public class OrbitCoordinatesModel {
    private int orbit;
    private ArrayList<XyCoordinatesModel> xyCoordinatesArrayList;

    public OrbitCoordinatesModel(int orbit, ArrayList<XyCoordinatesModel> xyCoordinatesArrayList) {
        this.orbit = orbit;
        this.xyCoordinatesArrayList = xyCoordinatesArrayList;
    }

    public int getOrbit() {
        return orbit;
    }

    public void setOrbit(int orbit) {
        this.orbit = orbit;
    }

    public ArrayList<XyCoordinatesModel> getXyCoordinatesArrayList() {
        return xyCoordinatesArrayList;
    }

    public void setXyCoordinatesArrayList(ArrayList<XyCoordinatesModel> xyCoordinatesArrayList) {
        this.xyCoordinatesArrayList = xyCoordinatesArrayList;
    }
}
