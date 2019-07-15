package com.example.porchlyt_artisan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import globals.globals;

public class CancelJobActivity extends AppCompatActivity {
    String tag = "CancelJobActivity";
    Toolbar mtoolbar;
    String _job_id;
    String client_app_id;
    String artisan_app_id;
    RadioButton rd_1, rd_2, rd_3, rd_4;
    View contextView;
    LinearLayout lay_other;

    EditText txt_other;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_job);
        contextView = (LinearLayout) findViewById(R.id.contextView);

        lay_other = (LinearLayout) findViewById(R.id.lay_other);
        lay_other.setVisibility(View.GONE);

        _job_id = getIntent().getStringExtra("_job_id");
        client_app_id = getIntent().getStringExtra("client_app_id");
        artisan_app_id = getIntent().getStringExtra("artisan_app_id");

        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(getString(R.string.job_details));

        rd_1 = (RadioButton) findViewById(R.id.rd_1);
        rd_2 = (RadioButton) findViewById(R.id.rd_2);
        rd_3 = (RadioButton) findViewById(R.id.rd_3);
        rd_4 = (RadioButton) findViewById(R.id.rd_4);

        txt_other = (EditText) findViewById(R.id.txt_other);


        rd_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_set_lay_other();
            }
        });

        rd_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_set_lay_other();
            }
        });

        rd_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_set_lay_other();
            }
        });


        rd_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_set_lay_other();
            }
        });


    }

    private void get_set_lay_other() {
        if (rd_4.isChecked()) {
            lay_other.setVisibility(View.VISIBLE);
        } else {
            lay_other.setVisibility(View.GONE);
        }
    }

    public void submit_reason_for_cancelling(View v) {
        String reason = "";
        if (rd_1.isChecked()) reason = "took_too_long_to_arrive";
        if (rd_2.isChecked()) reason = "bad_service";
        if (rd_3.isChecked()) reason = "too_expensive";
        if (rd_4.isChecked()) reason = txt_other.getText().toString();


        //dont send empty reaon, this applies only to the txt_other
        if (TextUtils.isEmpty(reason)) {
            txt_other.setError(getString(R.string.cannot_be_blank));
            return;
        }


        ProgressDialog pd = new ProgressDialog(CancelJobActivity.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        Ion.with(CancelJobActivity.this)
                .load(globals.base_url + "/artisan_cancel_job")
                .setBodyParameter("_job_id", _job_id)
                .setBodyParameter("reason", reason)
                .setBodyParameter("artisan_app_id", artisan_app_id)
                .setBodyParameter("client_app_id", client_app_id)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        pd.dismiss();

                        if (e != null) {
                            Snackbar.make(contextView, R.string.error_occured, Snackbar.LENGTH_SHORT).show();
                            Log.e(tag, e + " line 269");
                        } else {

                            try {
                                JSONObject json = new JSONObject(result);
                                String res = json.getString("res");
                                if (res.equals("ok")) {
                                    Toast.makeText(CancelJobActivity.this,getString(R.string.job_cancelled),Toast.LENGTH_SHORT).show();
                                    finishActivity(ViewJobActivity.request_code_for_cancel_job);
                                } else {
                                    Snackbar.make(contextView, R.string.error_occured, Snackbar.LENGTH_SHORT).show();
                                }
                            } catch (Exception ex) {
                                Log.e(tag, ex.getMessage());
                            }


                        }
                    }
                });
    }


}
