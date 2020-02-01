package com.sirachlabs.porchlyt_artisan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.google.gson.Gson;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.rilixtech.CountryCodePicker;

import org.json.JSONObject;

import globals.globals;
import models.appSettings;
import models.mArtisan.mArtisan;
import models.mArtisan.mLocation;

public class RegisterActivity extends AppCompatActivity {
    ProgressDialog pd;
    EditText txt_mobile;
    Toolbar mtoolbar;
    String sdata = "";//data of the partisan to be posted
    private ArrayAdapter<String> adapter;
    String tag = "RegisterActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        txt_mobile = (EditText) findViewById(R.id.txt_mobile);
        pd = new ProgressDialog(this);
        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);

        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(getString(R.string.registration));
        mtoolbar.setTitle(getResources().getString(R.string.registration));
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

        //clear previous data

        app.db.clearAllTables();
        //seed all db tables here

        //create and insert the appsettings
        appSettings aps = new appSettings();
        app.db.appSettingsDao().insert(aps);

        //create and insert the location table
        mLocation loc = new mLocation();
        loc.last_known_location = getString(R.string.we_need_your_location_to_provide_you_this_service);
        app.db.LocationDao().insert_one(loc);

        String mobile = txt_mobile.getText().toString();
        if (mobile.equals("")) {
            txt_mobile.setError(getResources().getString(R.string.cannot_be_blank));
            return;
        }
        mArtisan artisan = new mArtisan();
        artisan.app_id = aps.app_id;
        artisan.mobile = txt_mobile.getText().toString();
        app.db.mArtisanDao().insert_one(artisan);//insert this artisan into the db
        try {
            sdata = new Gson().toJson((artisan));
        } catch (Exception ex) {
            Log.e("d", ex.getMessage());
        }


        try {
            pd.setMessage(getString(R.string.registration_in_progress));
            pd.show();
            pd.setCanceledOnTouchOutside(false);
            Future<String> data = Ion.with(RegisterActivity.this)
                    .load(globals.base_url + "/artisanRegistration")
                    .setBodyParameter("data", sdata)//add the artisan as a parameter
                    .asString()
                    .setCallback((e, result) -> {
                        pd.hide();
                        if (e != null) {
                            Toast.makeText(RegisterActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (result == null) {
                            Toast.makeText(RegisterActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.e(tag, "result: " + result);
                            try {
                                String res = "";
                                String msg = "";
                                String otp = "";
                                try {
                                    res = new JSONObject(result).getString("res");
                                } catch (Exception ex) {
                                }
                                try {
                                    msg = new JSONObject(result).getString("msg");
                                } catch (Exception ex) {
                                }
                                try {
                                    otp = new JSONObject(result).getString("otp");
                                } catch (Exception ex) {
                                }

                                if (res.equals("ok")) {
                                    // to the next activity to confirm the otp pin
                                    artisan.otp = otp;
                                    artisan.registered = true;
                                    //update artisan
                                    app.db.mArtisanDao().update_one(artisan);
                                    Intent main = new Intent(RegisterActivity.this, MainActivity.class);
                                    main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    main.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(main);
                                    finish();//clear this activity
                                }

                            } catch (Exception ex) {
                                Toast.makeText(RegisterActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                                Log.d("d", ex.getMessage() + " line 161");
                            }

                    });
        } catch (Exception ex) {
            Log.e("d", ex.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
