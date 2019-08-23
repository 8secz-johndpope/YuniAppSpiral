package com.android.yuniapp.model;

import java.util.ArrayList;

public class GetAllTasksResponseModel {
    private ArrayList<TaskModel> data;

    public ArrayList<TaskModel> getData() {
        return data;
    }

    public void setData(ArrayList<TaskModel> data) {
        this.data = data;
    }
}
