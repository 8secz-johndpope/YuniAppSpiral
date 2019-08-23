package com.android.yuniapp.model;

import java.io.Serializable;

public class VideoCallModel implements Serializable {
    private String profile_pic;
    private String name;
    private String id;
    private String channelId;

    public String getProfile_pic() {
        if (profile_pic == null)
            return "";
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getName() {
        if (name == null)
            return "";
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        if (id == null)
            return "";
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelId() {
        if (channelId == null)
            return "";
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
