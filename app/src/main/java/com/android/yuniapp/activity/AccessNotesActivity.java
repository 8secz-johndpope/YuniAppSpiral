package com.android.yuniapp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.yuniapp.R;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConstantUtils;

public class AccessNotesActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtAccessNote;
    private Button btnDone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_notes);

        AppUtils.setToolbarWithBothIcon(this, getResources().getString(R.string.notes), "", R.drawable.back_icon, 0, 0,0);



        edtAccessNote=findViewById(R.id.edt_access_note);
        btnDone=findViewById(R.id.btn_done);
        btnDone.setOnClickListener(this);
        if(getIntent().getStringExtra("accessNote")!=null)
            edtAccessNote.setText(getIntent().getStringExtra("accessNote"));

    }

    @Override
    public void onClick(View v) {
     switch (v.getId())
     {
         case R.id.toolbar_lft_most_img:
             onBackPressed();
             break;
         case R.id.btn_done:
             Intent intent=getIntent();
             intent.putExtra("accessNote",edtAccessNote.getText().toString().trim());
             setResult(RESULT_OK,intent);
             finish();
             break;
     }
    }
}
