package com.android.yuniapp.model;

public class MembersModel {
    private int id;
    private String member_name;
    private String to_email_id;
    private int isSelected;

    public MembersModel(int id, String member_name, String to_email_id, int isSelected) {
        this.id = id;
        this.member_name = member_name;
        this.to_email_id = to_email_id;
        this.isSelected = isSelected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public int getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(int isSelected) {
        this.isSelected = isSelected;
    }
}
