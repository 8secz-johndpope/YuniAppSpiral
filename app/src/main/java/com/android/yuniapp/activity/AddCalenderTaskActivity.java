package com.android.yuniapp.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.yuniapp.R;
import com.android.yuniapp.adapter.CalendarTaskAdapter;
import com.android.yuniapp.application.BaseActivity;
import com.android.yuniapp.fragment.AddTaskFragment;
import com.android.yuniapp.listener.AddTaskListener;
import com.android.yuniapp.listener.RecyclerItemClickListener;
import com.android.yuniapp.model.GetAllTasksResponseModel;
import com.android.yuniapp.model.TaskModel;
import com.android.yuniapp.rest.ApiClient;
import com.android.yuniapp.rest.ApiInterface;
import com.android.yuniapp.utils.AndroidUtils;
import com.android.yuniapp.utils.AppUtils;
import com.android.yuniapp.utils.ConnectivityController;
import com.android.yuniapp.utils.ConstantUtils;
import com.android.yuniapp.utils.RecyclerItemTouchHelper;
import com.google.gson.JsonElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCalenderTaskActivity extends BaseActivity implements View.OnClickListener, AddTaskListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, RecyclerItemClickListener {
    private ArrayList<TaskModel> taskList = new ArrayList<>();
    private RecyclerView recyclerViewTasks;
    private CalendarTaskAdapter calendarTaskAdapter;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private TextView txtNoTask,txtToolbarTitle;
    private String selectedDate, nextDayDate;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_calender_task);

        AppUtils.setFullScreenWithPadding(this);

        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.fragment_loader);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        sharedPreferences = getSharedPreferences(ConstantUtils.APP_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        txtToolbarTitle=findViewById(R.id.txt_toolbar_title);
        findViewById(R.id.img_back).setOnClickListener(this);
        findViewById(R.id.txt_add_task).setOnClickListener(this);
        txtNoTask = findViewById(R.id.txt_no_task);
        progressBar = findViewById(R.id.progress_bar);
        recyclerViewTasks = findViewById(R.id.recycler_tasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        calendarTaskAdapter = new CalendarTaskAdapter(this, taskList,this);
        recyclerViewTasks.setAdapter(calendarTaskAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT , this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerViewTasks);

        if (getIntent() != null) {
            if (getIntent().getStringExtra(ConstantUtils.SELECTED_DATE) != null) {
                selectedDate = getIntent().getStringExtra(ConstantUtils.SELECTED_DATE);
                nextDayDate = getIntent().getStringExtra(ConstantUtils.NEXT_DAY_DATE);

                try {
                    txtToolbarTitle.setText(new SimpleDateFormat("MMM dd, yyyy").format(  new SimpleDateFormat("yyyy-MM-dd").parse(selectedDate)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (ConnectivityController.isNetworkAvailable(this))
                    callAllTaskApi();
                else AndroidUtils.showToast(this, getResources().getString(R.string.no_internet));
            }

        }


    }

    private void callAllTaskApi() {
        progressBar.setVisibility(View.VISIBLE);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetAllTasksResponseModel> getAllTasksResponseModelCall = apiService.getAllTask(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), selectedDate);
        getAllTasksResponseModelCall.enqueue(new Callback<GetAllTasksResponseModel>() {
            @Override
            public void onResponse(Call<GetAllTasksResponseModel> call, Response<GetAllTasksResponseModel> response) {
                progressBar.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getData() != null && response.body().getData().size() > 0) {
                        recyclerViewTasks.setVisibility(View.VISIBLE);
                        txtNoTask.setVisibility(View.GONE);

                        taskList.clear();
                        taskList.addAll(response.body().getData());
                        calendarTaskAdapter.notifyDataSetChanged();
                    } else {
                        recyclerViewTasks.setVisibility(View.GONE);
                        txtNoTask.setVisibility(View.VISIBLE);

                    }
                } else if (response.code() == 500)
                    AndroidUtils.showToast(AddCalenderTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }

            @Override
            public void onFailure(Call<GetAllTasksResponseModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                AndroidUtils.showToast(AddCalenderTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                onBackPressed();
                break;
            case R.id.txt_add_task:
                AddTaskFragment addTaskFragment = new AddTaskFragment();
                addTaskFragment.setListener(this);
                addTaskFragment.show(getSupportFragmentManager(), addTaskFragment.getTag());
                break;
        }
    }


    @Override
    public void onAddTaskClick(String text) {
        txtNoTask.setVisibility(View.GONE);
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<TaskModel> addTaskResponseCall = apiService.addTask(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), selectedDate, text);
        addTaskResponseCall.enqueue(new Callback<TaskModel>() {
            @Override
            public void onResponse(Call<TaskModel> call, Response<TaskModel> response) {
                progressDialog.dismiss();
                if (response.code() == 200 && response.body() != null) {
                    AndroidUtils.showToast(AddCalenderTaskActivity.this,"Task has been saved successfully!");
                    recyclerViewTasks.setVisibility(View.VISIBLE);
                    taskList.add(response.body());
                    recyclerViewTasks.setAdapter(null);
                    calendarTaskAdapter = new CalendarTaskAdapter(AddCalenderTaskActivity.this, taskList,AddCalenderTaskActivity.this);
                    recyclerViewTasks.setAdapter(calendarTaskAdapter);
               //calendarTaskAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<TaskModel> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, final int position) {

        switch (direction) {
            case ItemTouchHelper.RIGHT:
                if (ConnectivityController.isNetworkAvailable(AddCalenderTaskActivity.this))
                    updateTaskDateApi(position);
                else
                    AndroidUtils.showToast(AddCalenderTaskActivity.this, getResources().getString(R.string.no_internet));
                break;
            case ItemTouchHelper.LEFT:
               /* AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddCalenderTaskActivity.this);
                alertDialog.setMessage(getResources().getString(R.string.dialog_delete_title));

                alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (ConnectivityController.isNetworkAvailable(AddCalenderTaskActivity.this))
                            deleteTaskApi(position);
                        else
                            AndroidUtils.showToast(AddCalenderTaskActivity.this, getResources().getString(R.string.no_internet));
                    }
                });

                alertDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.show();*/

                if (ConnectivityController.isNetworkAvailable(AddCalenderTaskActivity.this))
                    deleteTaskApi(position);
                else
                    AndroidUtils.showToast(AddCalenderTaskActivity.this, getResources().getString(R.string.no_internet));

                break;
        }


    }

    private void deleteTaskApi(final int position) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<JsonElement> deleteTaskResposeCall = apiService.deleteTask(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), taskList.get(position).getTask_id());
        deleteTaskResposeCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                progressDialog.dismiss();
                if (response.code() == 200) {
                    AndroidUtils.showToast(AddCalenderTaskActivity.this, "Task has been deleted successfully!");
                    taskList.remove(position);
                    calendarTaskAdapter.notifyItemRemoved(position);
                    calendarTaskAdapter.notifyItemRangeChanged(taskList.size(),position);

                    if (taskList.isEmpty())
                        txtNoTask.setVisibility(View.VISIBLE);
                    else txtNoTask.setVisibility(View.GONE);
                } else if (response.code() == 500)
                    AndroidUtils.showToast(AddCalenderTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(AddCalenderTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }
        });
    }

    public void updateTaskDateApi(final int position) {
        progressDialog.show();
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<JsonElement> updateTaskDateResponseCall = apiService.updateTask(sharedPreferences.getString(ConstantUtils.AUTH_TOKEN, ""), taskList.get(position).getTask_id(), nextDayDate, taskList.get(position).getName());
        updateTaskDateResponseCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                progressDialog.dismiss();
                if (response.code() == 200) {
                    AndroidUtils.showToast(AddCalenderTaskActivity.this, "Task date has been changed to next day successfully!");
                    taskList.remove(position);
                    calendarTaskAdapter.notifyItemRemoved(position);
                    calendarTaskAdapter.notifyItemRangeChanged(taskList.size(),position);

                    if (taskList.isEmpty())
                        txtNoTask.setVisibility(View.VISIBLE);
                    else txtNoTask.setVisibility(View.GONE);

                } else if (response.code() == 500)
                    AndroidUtils.showToast(AddCalenderTaskActivity.this, getResources().getString(R.string.some_went_wrong));
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                progressDialog.dismiss();
                AndroidUtils.showToast(AddCalenderTaskActivity.this, getResources().getString(R.string.some_went_wrong));

            }
        });


    }

    @Override
    public void onItemClick(int position) {

        if (ConnectivityController.isNetworkAvailable(AddCalenderTaskActivity.this))
            deleteTaskApi(position);
        else
            AndroidUtils.showToast(AddCalenderTaskActivity.this, getResources().getString(R.string.no_internet));
    }
}
