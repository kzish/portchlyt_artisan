package com.example.porchlyt_artisan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.beardedhen.androidbootstrap.BootstrapEditText;

import de.jonasrottmann.realmbrowser.helper.Utils;
import io.realm.Realm;
import models.mJobs.mJobs;
import models.mJobs.mTask;
import globals.*;

//this activity will add a task for the view job activity
public class AddTaskActivity extends AppCompatActivity {

    EditText txt_task_name;
    EditText txt_task_bill;

    String _job_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        txt_task_name = (EditText)findViewById(R.id.txt_task_name);
        txt_task_bill = (EditText)findViewById(R.id.txt_task_bill);
        _job_id = getIntent().getStringExtra("_job_id");
    }


    //add the task bill to the job
    public void AddTaskBill(View v) {

        if(txt_task_bill.getText().toString().equals(""))
        {
            txt_task_bill.setError(getString(R.string.cannot_be_blank));
            return;
        }

        if(txt_task_name.getText().toString().equals(""))
        {
            txt_task_name.setError(getString(R.string.cannot_be_blank));
            return;
        }

        Realm db = globals.getDB();
        mJobs job = db.where(mJobs.class).equalTo("_job_id",_job_id).findFirst();
        db.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                //
                mTask task = new mTask();
                task.description=txt_task_name.getText().toString();
                task.price = Double.parseDouble(txt_task_bill.getText().toString());

                //ad the task to the job
                job.tasks.add(task);
                //reverse the list so new tasks go on top
                java.util.Collections.reverse(job.tasks);

            }
        });
        db.close();
        //refresh
        ViewJobActivity.set_tasks_adapter();
        //end activity
        finish();
    }
}
