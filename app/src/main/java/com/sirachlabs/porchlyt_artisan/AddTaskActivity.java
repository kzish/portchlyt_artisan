package com.sirachlabs.porchlyt_artisan;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import models.mJobs.mJobs;
import models.mJobs.mTask;

//this activity will add a task for the view job activity
public class AddTaskActivity extends AppCompatActivity {

    EditText txt_task_name;
    EditText txt_task_bill;

    String _job_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        txt_task_name = (EditText) findViewById(R.id.txt_task_name);
        txt_task_bill = (EditText) findViewById(R.id.txt_task_bill);
        _job_id = getIntent().getStringExtra("_job_id");
    }


    //add the task bill to the job
    public void AddTaskBill(View v) {

        if (txt_task_bill.getText().toString().equals("")) {
            txt_task_bill.setError(getString(R.string.cannot_be_blank));
            return;
        }

        if (txt_task_name.getText().toString().equals("")) {
            txt_task_name.setError(getString(R.string.cannot_be_blank));
            return;
        }

        mJobs job = app.db.mJobsDao().get_job(_job_id);

        mTask task = new mTask();
        task.description = txt_task_name.getText().toString();
        task.price = Double.parseDouble(txt_task_bill.getText().toString());
        task._job_id = _job_id;
        //save the task to db
        app.db.taskDao().insert_one(task);
        //reverse the list so new tasks go on top
        //todo reverse the tasks so new ones go ontop

        //refresh
        ViewJobActivity.set_tasks_adapter();
        //end activity
        finish();
    }
}
