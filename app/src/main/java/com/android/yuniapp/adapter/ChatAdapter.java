package com.android.yuniapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.model.ChatMessages;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

public class ChatAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;


    private Context context;
    private ArrayList<ChatMessages> chatMessagesArrayList;
    private String userId;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public ChatAdapter(Context context, ArrayList<ChatMessages> chatMessagesArrayList, String userId) {
        this.context = context;
        this.chatMessagesArrayList = chatMessagesArrayList;
        this.userId = userId;
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_message_sent, viewGroup, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_message_received, viewGroup, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessages chatMessages = chatMessagesArrayList.get(position);
        if (chatMessages.getUser_id().equals(userId))
            return VIEW_TYPE_MESSAGE_SENT;
        else return VIEW_TYPE_MESSAGE_RECEIVED;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        ChatMessages chatMessages = chatMessagesArrayList.get(viewHolder.getAdapterPosition());
        switch (viewHolder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) viewHolder).bind(chatMessages);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) viewHolder).bind(chatMessages);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessagesArrayList.size();
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.txt_sender_message);
            timeText = itemView.findViewById(R.id.txt_sent_timestamp);
        }

        void bind(ChatMessages chatMessages) {
            messageText.setText(chatMessages.getMessage());
            try {
                timeText.setText(new SimpleDateFormat("HH:mm a").format(sdf.parse(chatMessages.getMessage_datetime())));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        private TextView txtMessage, txtTimeStamp, txtReceiverName;
        private RoundedImageView imgPicture;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            txtMessage = itemView.findViewById(R.id.txt_message);
            txtTimeStamp = itemView.findViewById(R.id.txt_timestamp);
            txtReceiverName = itemView.findViewById(R.id.txt_receiver_name);
            imgPicture = itemView.findViewById(R.id.img_picture);


        }

        void bind(ChatMessages chatMessages) {
            txtMessage.setText(chatMessages.getMessage());
            try {
                txtTimeStamp.setText(new SimpleDateFormat("hh:mm a").format(sdf.parse(chatMessages.getMessage_datetime())));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            txtReceiverName.setText(chatMessages.getName());
            if (chatMessages.getProfile_pic() != null && !chatMessages.getProfile_pic().equalsIgnoreCase(""))
                Picasso.with(context).load(chatMessages.getProfile_pic()).placeholder(R.drawable.profile_placeholder).into(imgPicture);
        }
    }

}
