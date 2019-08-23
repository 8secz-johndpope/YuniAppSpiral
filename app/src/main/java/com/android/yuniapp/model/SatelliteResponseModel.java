package com.android.yuniapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class SatelliteResponseModel implements Parcelable {

    private String star_id;
    private String planet_id;
    private String moon_id;
    private String satellite_id;
    private String user_id;
    private String name;
    private String description;
    private String start_date;
    private String finish_date;
    private String alarm_date;
    private String alarm_time;
    private String notes;
    private String priority;
    private String start_time;
    private String finish_time;
    private ArrayList<DocumentResponseModel> documents;
    private ArrayList<MembersResponseModel> members;

    protected SatelliteResponseModel(Parcel in) {
        star_id = in.readString();
        planet_id = in.readString();
        moon_id = in.readString();
        satellite_id = in.readString();
        user_id = in.readString();
        name = in.readString();
        description = in.readString();
        start_date = in.readString();
        finish_date = in.readString();
        alarm_date = in.readString();
        alarm_time = in.readString();
        notes = in.readString();
        priority = in.readString();
        start_time = in.readString();
        finish_time = in.readString();
        documents = in.createTypedArrayList(DocumentResponseModel.CREATOR);
        members = in.createTypedArrayList(MembersResponseModel.CREATOR);
    }

    public static final Creator<SatelliteResponseModel> CREATOR = new Creator<SatelliteResponseModel>() {
        @Override
        public SatelliteResponseModel createFromParcel(Parcel in) {
            return new SatelliteResponseModel(in);
        }

        @Override
        public SatelliteResponseModel[] newArray(int size) {
            return new SatelliteResponseModel[size];
        }
    };

    public String getStar_id() {
        return star_id;
    }

    public void setStar_id(String star_id) {
        this.star_id = star_id;
    }

    public String getPlanet_id() {
        return planet_id;
    }

    public void setPlanet_id(String planet_id) {
        this.planet_id = planet_id;
    }

    public String getMoon_id() {
        return moon_id;
    }

    public void setMoon_id(String moon_id) {
        this.moon_id = moon_id;
    }

    public String getSatellite_id() {
        return satellite_id;
    }

    public void setSatellite_id(String satellite_id) {
        this.satellite_id = satellite_id;
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

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getFinish_time() {
        return finish_time;
    }

    public void setFinish_time(String finish_time) {
        this.finish_time = finish_time;
    }

    public ArrayList<DocumentResponseModel> getDocuments() {
        return documents;
    }

    public void setDocuments(ArrayList<DocumentResponseModel> documents) {
        this.documents = documents;
    }

    public ArrayList<MembersResponseModel> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<MembersResponseModel> members) {
        this.members = members;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(star_id);
        dest.writeString(planet_id);
        dest.writeString(moon_id);
        dest.writeString(satellite_id);
        dest.writeString(user_id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(start_date);
        dest.writeString(finish_date);
        dest.writeString(alarm_date);
        dest.writeString(alarm_time);
        dest.writeString(notes);
        dest.writeString(priority);
        dest.writeString(start_time);
        dest.writeString(finish_time);
        dest.writeTypedList(documents);
        dest.writeTypedList(members);
    }
}
