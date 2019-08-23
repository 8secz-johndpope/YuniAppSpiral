package com.android.yuniapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class StarResponseModel implements Parcelable {
    private String id;
    private String user_id;
    private String name;
    private String description;
    private String start_date;
    private String start_time;
    private String finish_time;
    private String finish_date;
    private String alarm_date;
    private String alarm_time;
    private String notes;
    private String priority;
    private String type;
    private ArrayList<DocumentResponseModel> documents;
    private ArrayList<MembersResponseModel> members;
    private ArrayList<OrderingsModel> orderings;
    private float x;
    private float y;

    public StarResponseModel() {
    }


    protected StarResponseModel(Parcel in) {
        id = in.readString();
        user_id = in.readString();
        name = in.readString();
        description = in.readString();
        start_date = in.readString();
        start_time = in.readString();
        finish_time = in.readString();
        finish_date = in.readString();
        alarm_date = in.readString();
        alarm_time = in.readString();
        notes = in.readString();
        priority = in.readString();
        type = in.readString();
        documents = in.createTypedArrayList(DocumentResponseModel.CREATOR);
        members = in.createTypedArrayList(MembersResponseModel.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(user_id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(start_date);
        dest.writeString(start_time);
        dest.writeString(finish_time);
        dest.writeString(finish_date);
        dest.writeString(alarm_date);
        dest.writeString(alarm_time);
        dest.writeString(notes);
        dest.writeString(priority);
        dest.writeString(type);
        dest.writeTypedList(documents);
        dest.writeTypedList(members);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StarResponseModel> CREATOR = new Creator<StarResponseModel>() {
        @Override
        public StarResponseModel createFromParcel(Parcel in) {
            return new StarResponseModel(in);
        }

        @Override
        public StarResponseModel[] newArray(int size) {
            return new StarResponseModel[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getFinish_time() {
        return finish_time;
    }

    public void setFinish_time(String finish_time) {
        this.finish_time = finish_time;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public ArrayList<OrderingsModel> getOrderings() {
        return orderings;
    }

    public void setOrderings(ArrayList<OrderingsModel> orderings) {
        this.orderings = orderings;
    }

    public static Creator<StarResponseModel> getCREATOR() {
        return CREATOR;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
