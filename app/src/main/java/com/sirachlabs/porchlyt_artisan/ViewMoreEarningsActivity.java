package com.sirachlabs.porchlyt_artisan;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.koushikdutta.ion.Ion;

import org.joda.time.DateTime;
import org.json.JSONObject;

import MainActivityTabs.ProfileFragment;
import globals.globals;
import models.mArtisan.mArtisan;

public class ViewMoreEarningsActivity extends AppCompatActivity {

    Toolbar mtoolbar;
    TextView lbl_total_earnings_this_month_label;
    TextView lbl_total_jobs_this_month_label;

    TextView lbl_total_jobs_this_month;
    TextView lbl_total_earnings_this_month;

    TextView lbl_total_earnings;
    TextView lbl_total_jobs;

    LinearLayout linlay;
    LinearLayout grand_layout;
    RelativeLayout grand_layout_2;
    RelativeLayout grand_layout_3;
    String tag = "ViewMoreEarningsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_more_earnings);


        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(getString(R.string.earnings));


        lbl_total_earnings_this_month_label = (TextView) findViewById(R.id.lbl_total_earnings_this_month_label);
        lbl_total_jobs_this_month_label = (TextView) findViewById(R.id.lbl_total_jobs_this_month_label);

        lbl_total_jobs_this_month = (TextView) findViewById(R.id.lbl_total_jobs_this_month);
        lbl_total_earnings_this_month = (TextView) findViewById(R.id.lbl_total_earnings_this_month);

        lbl_total_earnings = (TextView) findViewById(R.id.lbl_total_earnings);
        lbl_total_jobs = (TextView) findViewById(R.id.lbl_total_jobs);

        linlay = (LinearLayout) findViewById(R.id.linlay);
        grand_layout = (LinearLayout) findViewById(R.id.grand_layout);
        grand_layout_2 = (RelativeLayout) findViewById(R.id.grand_layout_2);
        grand_layout_3 = (RelativeLayout) findViewById(R.id.grand_layout_3);

        //initially
        grand_layout_2.setVisibility(View.GONE);
        grand_layout.setVisibility(View.VISIBLE);


        linlay = (LinearLayout) findViewById(R.id.linlay);


        fetch_earnings_data();

    }


    public void fetch_earnings_data() {
        ProgressDialog pd = new ProgressDialog(ViewMoreEarningsActivity.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        mArtisan artisan = app.db.mArtisanDao().get_artisan();

        Ion.with(this)
                .load(globals.base_url + "/fetch_earnings_data")
                .setBodyParameter("artisan_app_id", artisan.app_id)
                .asString()
                .withResponse()
                .setCallback((e, result) -> {
                    pd.hide();
                    grand_layout_3.setVisibility(View.GONE);
                    if (e != null) {
                        Snackbar.make(linlay, getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                        Log.e(tag, "line 69: " + e);
                        grand_layout_2.setVisibility(View.VISIBLE);
                        grand_layout.setVisibility(View.GONE);
                        return;
                    } else {
                        try {
                            JSONObject obj = new JSONObject(result.getResult());
                            String total_earnings = obj.getString("total_earnings");
                            String total_earnings_this_month = obj.getString("total_earnings_this_month");
                            String total_jobs = obj.getString("total_jobs");
                            String total_jobs_this_month = obj.getString("total_jobs_this_month");


                            lbl_total_earnings.setText(globals.formatCurrencyExact(Double.parseDouble(total_earnings)));
                            lbl_total_earnings_this_month.setText(globals.formatCurrencyExact(Double.parseDouble(total_earnings_this_month)));


                            lbl_total_jobs.setText(globals.numberCalculation(Long.parseLong(total_jobs)));
                            lbl_total_jobs_this_month.setText(globals.numberCalculation(Long.parseLong(total_jobs_this_month)));

                            //update the artisans current month earnings
                            artisan.earnings_since_last_disbursement = Double.parseDouble(total_earnings_this_month);
                            app.db.mArtisanDao().update_one(artisan);
                            ProfileFragment.set_my_earning();

                            //get the current month
                            DateTime now = DateTime.now();
                            String month = now.toString("MMMM");

                            lbl_total_jobs_this_month_label.setText(getString(R.string.total_jobs_this) + " " + month);
                            lbl_total_earnings_this_month_label.setText(getString(R.string.total_earnings_this) + " " + month);

                            grand_layout_2.setVisibility(View.GONE);
                            grand_layout.setVisibility(View.VISIBLE);

                        } catch (Exception ex) {
                            Snackbar.make(linlay, getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                            grand_layout_2.setVisibility(View.VISIBLE);
                            grand_layout.setVisibility(View.GONE);
                            Log.e(tag, "line 80: " + ex);
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


}
