package com.android.yuniapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.adapter.MembersAdapter;
import com.android.yuniapp.listener.AddMembersListener;
import com.android.yuniapp.model.MemberRequestModel;
import com.android.yuniapp.model.MembersModel;
import com.android.yuniapp.model.MembersResponseModel;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConstantUtils;
import com.google.android.gms.common.data.DataHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AddMembersActivity extends AppCompatActivity implements View.OnClickListener, AddMembersListener {
    private RecyclerView recyclerViewMembers;
    private MembersAdapter membersAdapter;
    private ArrayList<MembersModel> membersList = new ArrayList<>();
    private ProgressBar progressBar;
    private ArrayList<MemberRequestModel> memberRequestList=new ArrayList<>();
    private EditText edtSearch;
    private ImageView imgClearSearch;
    private TextView txtNoResultFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        AppUtils.setToolbarWithBothIcon(this, "Members", "", R.drawable.back_icon, 0, 0, R.drawable.green_check);

        progressBar = findViewById(R.id.progress_bar);
        txtNoResultFound=findViewById(R.id.txt_no_result_found);
        edtSearch=findViewById(R.id.edit_search);
        imgClearSearch=findViewById(R.id.img_clear_search);
        imgClearSearch.setVisibility(View.INVISIBLE);
        imgClearSearch.setOnClickListener(this);
        recyclerViewMembers = findViewById(R.id.recycler_members);
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));
        membersAdapter = new MembersAdapter(this, membersList, this);
        recyclerViewMembers.setAdapter(membersAdapter);
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
                if(s.toString().trim().isEmpty())
                imgClearSearch.setVisibility(View.GONE);
                else    imgClearSearch.setVisibility(View.VISIBLE);

            }
        });


        if(getIntent()!=null)
        {
            if(getIntent().getParcelableArrayListExtra(ConstantUtils.MEMBERS_REQUEST_ARRAY_LIST)!=null)
                memberRequestList.addAll(getIntent().<MemberRequestModel>getParcelableArrayListExtra(ConstantUtils.MEMBERS_REQUEST_ARRAY_LIST));
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    15);
        } else {
            new FetchContacts().execute();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 15) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new FetchContacts().execute();
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // user rejected the permission
                if (!ActivityCompat.shouldShowRequestPermissionRationale(AddMembersActivity.this, Manifest.permission.READ_CONTACTS)) {
                    AndroidUtils.showLongToast(AddMembersActivity.this, "Go to Settings and Grant the permission to use this feature.");

                } else {

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                            15);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_lft_most_img:
                onBackPressed();
                break;
            case R.id.toolbar_right_most_img:

                memberRequestList.clear();
                for(int i=0;i<membersList.size();i++)
                {
                    if(membersList.get(i).getIsSelected()>=0)
                    {
                        memberRequestList.add(new MemberRequestModel(membersList.get(i).getMember_name(),membersList.get(i).getTo_email_id()));
                    }
                }

                Intent intent=getIntent();
                intent.putParcelableArrayListExtra(ConstantUtils.MEMBERS_REQUEST_ARRAY_LIST,memberRequestList);
                setResult(RESULT_OK,intent);
                finish();
                break;
            case R.id.img_clear_search:
                edtSearch.setText("");
                break;
        }
    }

    @Override
    public void onChecked(int id) {

        for(int i=0;i<membersList.size();i++)
        {
           if(membersList.get(i).getId()==id)
           {

               if (membersList.get(i).getIsSelected() >= 0) {
                   membersList.get(i).setIsSelected(-1);
               } else {
                   membersList.get(i).setIsSelected(0);
               }
           }
        }

        membersAdapter.notifyDataSetChanged();
    }

    private class FetchContacts extends AsyncTask<Void, Void, ArrayList<MembersModel>> {

        private final String DISPLAY_NAME = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME;

        private final String FILTER = DISPLAY_NAME + " NOT LIKE '%@%'";

        private final String ORDER = String.format("%1$s COLLATE NOCASE", DISPLAY_NAME);

        @SuppressLint("InlinedApi")
        private final String[] PROJECTION = {
                ContactsContract.Contacts._ID,
                DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
        };

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<MembersModel> doInBackground(Void... params) {

            try {
                ArrayList<MembersModel> contacts = new ArrayList<>();
                int counter=0;
                ContentResolver cr = getContentResolver();
                Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, FILTER, null, ORDER);
                if (cursor != null && cursor.moveToFirst()) {

                    do {
                        // get the contact's information
                        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                        Integer hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        // get the user's email address
                        String email = null;
                        Cursor ce = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);

                        if (ce != null && ce.moveToFirst()) {
                            email = ce.getString(ce.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                            ce.close();
                        }

                        /*// get the user's phone number
                        String phone = null;
                        if (hasPhone > 0) {
                            Cursor cp = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                            if (cp != null && cp.moveToFirst()) {
                                phone = cp.getString(cp.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                cp.close();
                            }
                        }*/

                        // if the user user has an email or phone then add it to contacts
                        if ((!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                                && !email.equalsIgnoreCase(name))) {
                            MembersModel contact = new MembersModel(counter,name, email, -1);
                            contacts.add(contact);
                            counter++;
                            /*contact.name = name;
                            contact.email = email;
                            contact.phoneNumber = phone;
                            contacts.add(contact);*/
                        }

                    } while (cursor.moveToNext());

                    // clean up cursor
                    cursor.close();
                }

                return contacts;
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<MembersModel> contacts) {
            progressBar.setVisibility(View.GONE);
            if (contacts != null) {

                // success
                membersList.addAll(contacts);
                if(memberRequestList!=null && memberRequestList.size()>0)
                {
                    for(int i=0;i<membersList.size();i++) {
                        for (int j = 0; j < memberRequestList.size(); j++) {
                            if (membersList.get(i).getTo_email_id().equals(memberRequestList.get(j).getTo_email_id()))
                                membersList.get(i).setIsSelected(0);
                        }
                    }
                }


                membersAdapter.notifyDataSetChanged();
            } else {
                // show failure
                // syncFailed();
            }
        }
    }
    void filter(String text){
        ArrayList<MembersModel> temp = new ArrayList();
        for(MembersModel d: membersList){
            if(d.getMember_name().contains(text)){
                temp.add(d);
            }
        }
        //update recyclerview
        membersAdapter.updateList(temp);

        if(temp.size()>0)
            txtNoResultFound.setVisibility(View.GONE);
        else txtNoResultFound.setVisibility(View.VISIBLE);
    }

}
