package com.android.yuniapp.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by prashantkumar on 8/1/18.
 */

public class ErrorResponse {
    @SerializedName("message")
    private String message;


    public String getMessage() {
        return message;
    }


}
