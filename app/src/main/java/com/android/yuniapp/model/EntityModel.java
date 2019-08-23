package com.android.yuniapp.model;

import java.util.ArrayList;

public class EntityModel {
    private String star_id;
    private String planet_id;
    private String moon_id;
    private String satellite_id;
    private String comet_id;
    private String user_id;
    private String name;
    private String description;
    private String start_date;
    private String start_time;
    private String finish_date;
    private String finish_time;
    private String alarm_date;
    private String alarm_time;
    private String notes;
    private String priority;
    private String is_completed;
    private ArrayList<DocumentResponseModel> documents;
    private ArrayList<MembersResponseModel> members;


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

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getFinish_date() {
        return finish_date;
    }

    public void setFinish_date(String finish_date) {
        this.finish_date = finish_date;
    }

    public String getFinish_time() {
        return finish_time;
    }

    public void setFinish_time(String finish_time) {
        this.finish_time = finish_time;
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

    public String getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(String is_completed) {
        this.is_completed = is_completed;
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


}
