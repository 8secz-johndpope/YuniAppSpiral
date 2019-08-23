package com.android.yuniapp.model;

import java.util.ArrayList;

public class CometRequestModel {
    private String comet_id;
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
    private ArrayList<DocumentModel> documents;
    private ArrayList<MemberRequestModel> members;


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

    public String getComet_id() {
        return comet_id;
    }

    public void setComet_id(String comet_id) {
        this.comet_id = comet_id;
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

    public ArrayList<DocumentModel> getDocuments() {
        return documents;
    }

    public void setDocuments(ArrayList<DocumentModel> documents) {
        this.documents = documents;
    }

    public ArrayList<MemberRequestModel> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<MemberRequestModel> members) {
        this.members = members;
    }
}
