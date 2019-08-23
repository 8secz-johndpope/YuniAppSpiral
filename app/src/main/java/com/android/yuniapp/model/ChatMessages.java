package com.android.yuniapp.model;

public class ChatMessages {
    private String user_id;
    private String name;
    private String gender;
    private String profile_pic;
    private String chat_id;
    private String message;
    private String message_datetime;

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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_datetime() {
        return message_datetime;
    }

    public void setMessage_datetime(String message_datetime) {
        this.message_datetime = message_datetime;
    }
}
