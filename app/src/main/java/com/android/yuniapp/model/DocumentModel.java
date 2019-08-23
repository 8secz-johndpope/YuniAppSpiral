package com.android.yuniapp.model;

public class DocumentModel {
    private String document_url;
    private String document_type;

    public DocumentModel(String document_url, String document_type) {
        this.document_url = document_url;
        this.document_type = document_type;
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
}
