package com.android.yuniapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class DocumentResponseModel implements Parcelable {
    private String document_id;
    private String document_url;
    private String  document_type;

    public DocumentResponseModel(String document_id, String document_url, String document_type) {
        this.document_id = document_id;
        this.document_url = document_url;
        this.document_type = document_type;
    }

    protected DocumentResponseModel(Parcel in) {
        document_id = in.readString();
        document_url = in.readString();
        document_type = in.readString();
    }

    public static final Creator<DocumentResponseModel> CREATOR = new Creator<DocumentResponseModel>() {
        @Override
        public DocumentResponseModel createFromParcel(Parcel in) {
            return new DocumentResponseModel(in);
        }

        @Override
        public DocumentResponseModel[] newArray(int size) {
            return new DocumentResponseModel[size];
        }
    };

    public String getDocument_id() {
        return document_id;
    }

    public void setDocument_id(String document_id) {
        this.document_id = document_id;
    }

    public String getDocument_url() {
        return document_url;
    }

    public void setDocument_url(String document_url) {
        this.document_url = document_url;
    }

    public String getDocument_type() {
        return document_type;
    }

    public void setDocument_type(String document_type) {
        this.document_type = document_type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(document_id);
        dest.writeString(document_url);
        dest.writeString(document_type);
    }
}
