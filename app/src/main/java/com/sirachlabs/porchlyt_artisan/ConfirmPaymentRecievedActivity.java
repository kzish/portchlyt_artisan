package com.sirachlabs.porchlyt_artisan;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.koushikdutta.ion.Ion;

import org.joda.time.LocalDateTime;
import org.json.JSONObject;

import MainActivityTabs.JobsFragment;
import globals.globals;
import models.mArtisan.mArtisan;
import models.mJobs.JobStatus;
import models.mJobs.mJobs;


public class ConfirmPaymentRecievedActivity extends AppCompatActivity {

    String _job_id;
    LinearLayout linlay;
    String tag = "ConfirmPaymentRecievedActivity";
    Toolbar mtoolbar;
    TextView txt_job_name;
    TextView txt_total_amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_payment_recieved);

        //
        _job_id = getIntent().getStringExtra("_job_id");
        linlay = (LinearLayout) findViewById(R.id.linlay);

        //
        txt_job_name = (TextView) findViewById(R.id.txt_job_name);
        txt_total_amount = (TextView) findViewById(R.id.txt_total_amount);

        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);

        //
        //artisan can also reject the payment by closing this activity
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(getString(R.string.confirm_payment_received));

        //
        get_the_job_details(_job_id);


    }//.oncreate


    //function to get the job detail and display them
    private void get_the_job_details(String _job_id) {
        mJobs job = app.db.mJobsDao().get_job(_job_id);
        txt_job_name.setText(job.description);
        txt_total_amount.setText(globals.formatCurrencyExact(job.get_the_total_price()));
    }

    //
    public void accept_cash_payment(View v) {
        //
        ProgressDialog pd = new ProgressDialog(ConfirmPaymentRecievedActivity.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.show();
        mJobs job = app.db.mJobsDao().get_job(_job_id);
        mArtisan artisan = app.db.mArtisanDao().get_artisan();

        Ion.with(ConfirmPaymentRecievedActivity.this)
                .load(globals.base_url + "/artisan_accept_cash_payment")
                .setBodyParameter("_job_id", _job_id)
                .setBodyParameter("client_app_id", job.client_app_id)
                .setBodyParameter("artisan_app_id", artisan.app_id)
                .asString()
                .setCallback((e, result) -> {
                    Log.e(tag, result + " ");
                    pd.dismiss();
                    if (e != null) {

                        Snackbar.make(linlay, getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();

                    } else {
                        try {

                            JSONObject json = new JSONObject(result);
                            String res = json.getString("res");
                            String msg = json.getString("msg");
                            if (res.equals("ok")) {
                                //indicate that this job is now completed
                                Toast.makeText(ConfirmPaymentRecievedActivity.this, getString(R.string.payment_confirmed), Toast.LENGTH_SHORT).show();
                                if (job.end_time == null) {
                                    //make sure we dont change the date again
                                    job.end_time = LocalDateTime.now().toString();//set the end time
                                }
                                job.job_status = JobStatus.closed.toString();
                                //update the job
                                app.db.mJobsDao().update_one(job);
                                JobsFragment.refreshJobsAdapter();
                                finish();
                            } else {
                                Snackbar.make(linlay, getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                                Log.e(tag, "line 79 " + msg);
                            }
                        } catch (Exception ex) {
                            Log.e(tag, "line 111 " + ex.getMessage());
                            Snackbar.make(linlay, getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                        } finally {
                            pd.dismiss();
                        }
                    }

                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}//.class
