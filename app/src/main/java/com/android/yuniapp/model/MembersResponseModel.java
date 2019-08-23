package com.android.yuniapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MembersResponseModel implements Parcelable {
    private String member_id;
    private String to_email_id;
    private String  member_name;


    protected MembersResponseModel(Parcel in) {
        member_id = in.readString();
        to_email_id = in.readString();
        member_name = in.readString();
    }

    public static final Creator<MembersResponseModel> CREATOR = new Creator<MembersResponseModel>() {
        @Override
        public MembersResponseModel createFromParcel(Parcel in) {
            return new MembersResponseModel(in);
        }

        @Override
        public MembersResponseModel[] newArray(int size) {
            return new MembersResponseModel[size];
        }
    };

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public String getTo_email_id() {
        return to_email_id;
    }

    public void setTo_email_id(String to_email_id) {
        this.to_email_id = to_email_id;
    }

    public String getMember_name() {
        return member_name;
    }

    public void setMember_name(String member_name) {
        this.member_name = member_name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(member_id);
        dest.writeString(to_email_id);
        dest.writeString(member_name);
    }
}
