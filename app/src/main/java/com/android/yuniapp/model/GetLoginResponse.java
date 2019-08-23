package com.android.yuniapp.model;

public class GetLoginResponse {
    private String user_id;
    private String name;
    private String email_id;
    private String address;
    private String dob;
    private String gender;
    private String auth_token;
    private String profile_pic;
    private String automatic_quote;
    private String own_quote;
    private String create_datetime;


    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
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

    public String getEmail_id() {
        return email_id;
    }

    public void setEmail_id(String email_id) {
        this.email_id = email_id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAuth_token() {
        return auth_token;
    }

    public void setAuth_token(String auth_token) {
        this.auth_token = auth_token;
    }

    public String getCreate_datetime() {
        return create_datetime;
    }

    public void setCreate_datetime(String create_datetime) {
        this.create_datetime = create_datetime;
    }

    public String getAutomatic_quote() {
        return automatic_quote;
    }

    public void setAutomatic_quote(String automatic_quote) {
        this.automatic_quote = automatic_quote;
    }

    public String getOwn_quote() {
        return own_quote;
    }

    public void setOwn_quote(String own_quote) {
        this.own_quote = own_quote;
    }
}
