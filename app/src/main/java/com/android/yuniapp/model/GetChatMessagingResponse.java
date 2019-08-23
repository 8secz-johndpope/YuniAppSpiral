package com.android.yuniapp.model;

import java.util.ArrayList;

public class GetChatMessagingResponse {
    private ArrayList<ChatMessages> chat_messages;

    public ArrayList<ChatMessages> getChat_messages() {
        return chat_messages;
    }

    public void setChat_messages(ArrayList<ChatMessages> chat_messages) {
        this.chat_messages = chat_messages;
    }
}
