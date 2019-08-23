package com.android.yuniapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MemberRequestModel implements Parcelable {
    private String member_name;
    private String to_email_id;


    public MemberRequestModel(String member_name, String to_email_id) {
        this.member_name = member_name;
        this.to_email_id = to_email_id;
    }

    protected MemberRequestModel(Parcel in) {
        member_name = in.readString();
        to_email_id = in.readString();
    }

    public static final Creator<MemberRequestModel> CREATOR = new Creator<MemberRequestModel>() {
        @Override
        public MemberRequestModel createFromParcel(Parcel in) {
            return new MemberRequestModel(in);
        }

        @Override
        public MemberRequestModel[] newArray(int size) {
            return new MemberRequestModel[size];
        }
    };

    public String getMember_name() {
        return member_name;
    }

    public void setMember_name(String member_name) {
        this.member_name = member_name;
    }

    public String getTo_email_id() {
        return to_email_id;
    }

    public void setTo_email_id(String to_email_id) {
        this.to_email_id = to_email_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(member_name);
        dest.writeString(to_email_id);
    }
}
