package com.android.yuniapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class DataResponseModel  implements Parcelable {
    private String star_id;
    private String comet_id;
    private String user_id;
    private String name;
    private String description;
    private String start_date;
    private String finish_date;
    private String alarm_date;
    private String alarm_time;
    private String notes;
    private String priority;

    protected DataResponseModel(Parcel in) {
        star_id = in.readString();
        comet_id = in.readString();
        user_id = in.readString();
        name = in.readString();
        description = in.readString();
        start_date = in.readString();
        finish_date = in.readString();
        alarm_date = in.readString();
        alarm_time = in.readString();
        notes = in.readString();
        priority = in.readString();
    }

    public static final Creator<DataResponseModel> CREATOR = new Creator<DataResponseModel>() {
        @Override
        public DataResponseModel createFromParcel(Parcel in) {
            return new DataResponseModel(in);
        }

        @Override
        public DataResponseModel[] newArray(int size) {
            return new DataResponseModel[size];
        }
    };

    public String getStar_id() {
        return star_id;
    }

    public void setStar_id(String star_id) {
        this.star_id = star_id;
    }

    public String getComet_id() {
        return comet_id;
    }

    public void setComet_id(String comet_id) {
        this.comet_id = comet_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getFinish_date() {
        return finish_date;
    }

    public void setFinish_date(String finish_date) {
        this.finish_date = finish_date;
    }

    public String getAlarm_date() {
        return alarm_date;
    }

    public void setAlarm_date(String alarm_date) {
        this.alarm_date = alarm_date;
    }

    public String getAlarm_time() {
        return alarm_time;
    }

    public void setAlarm_time(String alarm_time) {
        this.alarm_time = alarm_time;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(star_id);
        dest.writeString(comet_id);
        dest.writeString(user_id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(start_date);
        dest.writeString(finish_date);
        dest.writeString(alarm_date);
        dest.writeString(alarm_time);
        dest.writeString(notes);
        dest.writeString(priority);
    }
}
