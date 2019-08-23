package com.android.yuniapp.model;

import java.util.ArrayList;

public class GetPlanetsResponseModel {
    private ArrayList<PlanetResponseModel> planets;

    public ArrayList<PlanetResponseModel> getPlanets() {
        return planets;
    }

    public void setPlanets(ArrayList<PlanetResponseModel> planets) {
        this.planets = planets;
    }
}
