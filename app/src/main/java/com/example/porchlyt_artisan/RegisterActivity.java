package com.example.porchlyt_artisan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.beardedhen.androidbootstrap.BootstrapLabel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.builder.Builders;
import com.rilixtech.CountryCodePicker;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import globals.globals;
import io.realm.ImportFlag;
import io.realm.Realm;
import io.realm.RealmList;
import models.appSettings;
import models.mArtisan.mArtisan;

public class RegisterActivity extends AppCompatActivity {
    ProgressDialog pd;
    BootstrapEditText txt_mobile;
    CountryCodePicker ccp;
    Toolbar mtoolbar;
    private ArrayAdapter<String> adapter;
    Realm db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        txt_mobile = (BootstrapEditText) findViewById(R.id.txt_mobile);
        pd = new ProgressDialog(this);
        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);

        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(getString(R.string.registration));
        mtoolbar.setTitle(getResources().getString(R.string.registration));
        db = Realm.getDefaultInstance();
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

    public void Register(View v) {
        String mobile = txt_mobile.getText().toString();
        if (mobile.equals("")) {
            txt_mobile.setError(getResources().getString(R.string.cannot_be_blank));
            return;
        }


        db.beginTransaction();
        mArtisan m = new mArtisan();
        m.mobile = ccp.getSelectedCountryCodeWithPlus() + mobile;
        appSettings aps=db.where(appSettings.class).findFirst();
        m.app_id =aps.app_id;
        db.insertOrUpdate(m);//insert this artisan into the db
        db.commitTransaction();

        String sdata = "";//data of the partisan to be posted
        try {
            sdata = new Gson().toJson((m));
        } catch (Exception ex) {
            Log.e("d", ex.getMessage());
        }

        try {
            pd.setMessage(getString(R.string.registration_in_progress));
            pd.show();
            pd.setCanceledOnTouchOutside(false);
            Ion.with(RegisterActivity.this)
                    .load(globals.base_url + "/artisanRegistration")
                    .setBodyParameter("data", sdata)//add the artisan as a parameter
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            pd.hide();
                            if (e == null) {
                                try {
                                    if (result == null) return;//return if the result is null
                                    String res = "";
                                    String msg = "";
                                    String otp = "";
                                    try{res= new JSONObject(result.getResult()).getString("res");}catch (Exception ex){}
                                    try{msg= new JSONObject(result.getResult()).getString("msg");}catch (Exception ex){}
                                    try{otp= new JSONObject(result.getResult()).getString("otp");}catch (Exception ex){}

                                    if (res.equals("ok")) {
                                        // to the next activity to confirm the otp pin
                                        mArtisan m = db.where(mArtisan.class).findFirst();
                                        db.beginTransaction();
                                        m.otp = otp;
                                        db.commitTransaction();//this auto saves the thing
                                        startActivity(new Intent(RegisterActivity.this, ConfirmOTPActivity.class));
                                    } else {
                                        //show the err measage
                                        Toast.makeText(RegisterActivity.this, msg+"", Toast.LENGTH_SHORT).show();
                                    }

                                } catch (Exception ex) {
                                    Toast.makeText(RegisterActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                                    Log.d("d", ex.getMessage() + " line 161");
                                }
                            } else {
                                    Toast.makeText(RegisterActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception ex) {
            Log.e("d", ex.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
