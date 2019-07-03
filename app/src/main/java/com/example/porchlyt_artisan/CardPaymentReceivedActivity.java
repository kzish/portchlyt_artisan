package com.example.porchlyt_artisan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.koushikdutta.ion.Ion;

import org.joda.time.LocalDateTime;
import org.json.JSONObject;

import MainActivityTabs.JobsFragment;
import globals.globals;
import io.realm.Realm;
import models.mArtisan.mArtisan;
import models.mJobs.mJobs;

public class CardPaymentReceivedActivity extends AppCompatActivity {

    String _job_id;
    Double amount_payed;
    LinearLayout linlay;
    String tag = "CardPaymentReceivedActivity";
    TextView txt_job_name;
    TextView txt_total_amount;


    Toolbar mtoolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_payment_received);

        //
        _job_id = getIntent().getStringExtra("_job_id");
        amount_payed = getIntent().getDoubleExtra("amount_payed",0);

        //
        linlay = (LinearLayout) findViewById(R.id.linlay);

        //
        txt_job_name = (TextView) findViewById(R.id.txt_job_name);
        txt_total_amount = (TextView) findViewById(R.id.txt_total_amount);


        //artisan can also reject the payment by closing this activity
        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(getString(R.string.confirm_payment_received));

        get_the_job_details(_job_id);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        return;
    }

    //function to get the job detail and display them
    private void get_the_job_details(String _job_id) {
        Realm db = globals.getDB();
        mJobs job = db.where(mJobs.class).equalTo("_job_id", _job_id).findFirst();
        txt_job_name.setText(job.description);
        txt_total_amount.setText(job.getTheTotalPrice() + "");
        db.close();
    }



    //
    public void accept_card_payment(View v) {
        //
        ProgressDialog pd = new ProgressDialog(CardPaymentReceivedActivity.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.show();
        Realm db = globals.getDB();
        mJobs jobs = db.where(mJobs.class).equalTo("_job_id", _job_id).findFirst();
        mArtisan artisan = db.where(mArtisan.class).findFirst();

        Ion.with(CardPaymentReceivedActivity.this)
                .load(globals.base_url + "/artisan_accept_cash_payment")
                .setBodyParameter("_job_id", _job_id)
                .setBodyParameter("client_app_id", jobs.client_app_id)
                .setBodyParameter("artisan_app_id", artisan.app_id)
                .asString()
                .setCallback((e, result) -> {
                    Log.e(tag, result + " ");
                    db.close();
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
                                Toast.makeText(CardPaymentReceivedActivity.this, getString(R.string.payment_confirmed), Toast.LENGTH_SHORT).show();
                                Realm db2 = globals.getDB();
                                mJobs job = db2.where(mJobs.class).equalTo("_job_id",_job_id).findFirst();
                                db2.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        if(job.end_time==null) {//make sure we dont change the date again
                                            job.end_time = LocalDateTime.now().toString();//set the end time
                                        }
                                    }
                                });
                                db2.close();
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
                            db.close();
                            pd.dismiss();
                        }
                    }

                });
    }






}
