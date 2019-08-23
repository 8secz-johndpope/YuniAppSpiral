package com.android.yuniapp.listener;

public interface InfoPopUpEventListener {
    void onEditTaskClick(int position,String source);
    void onDeleteTaskClick(int position,String source);
    void onStorageTaskClick(int position);
    void onMarkDoneClick(int position,String source);
    void onDetailsClick(int position,String source);
}
