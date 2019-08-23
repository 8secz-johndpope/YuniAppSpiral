package com.android.yuniapp.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.yuniapp.R;
import com.android.yuniapp.listener.AddTaskListener;
import com.android.yuniapp.utils.AndroidUtils;

public class AddTaskFragment extends DialogFragment {
    private AddTaskListener addTaskListener;
    private EditText edtTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);

        edtTask=view.findViewById(R.id.edt_task);
        view.findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtTask.getText().toString().trim().isEmpty())
                    AndroidUtils.showToast(getActivity(),"Task can't be empty.");
                else{
                    addTaskListener.onAddTaskClick(edtTask.getText().toString());
                    dismiss();
                }
            }
        });

        return view;
    }

    public void setListener(AddTaskListener listener)
    {
        this.addTaskListener=listener;
    }
}
