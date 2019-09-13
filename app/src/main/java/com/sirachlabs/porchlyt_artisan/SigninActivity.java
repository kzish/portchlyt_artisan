package com.sirachlabs.porchlyt_artisan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.koushikdutta.ion.Ion;
import com.rilixtech.CountryCodePicker;

import org.json.JSONObject;

import globals.globals;
import models.appSettings;
import models.mArtisan.mArtisan;
import models.mArtisan.mLocation;

public class SigninActivity extends AppCompatActivity {

    EditText txt_mobile;
    Toolbar mtoolbar;
    private ArrayAdapter<String> adapter;
    String tag = "SigninActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        txt_mobile = (EditText) findViewById(R.id.txt_mobile);
        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);


        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(getString(R.string.registration));
        mtoolbar.setTitle(getResources().getString(R.string.sign_in));
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


    //todo provide a method to pull all my existing data from the server
    public void Sign_in(View v) {

        //clear all
        app.db.clearAllTables();

        String mobile = txt_mobile.getText().toString();
        if (mobile.equals("")) {
            txt_mobile.setError(getResources().getString(R.string.cannot_be_blank));
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.show();


        Ion.with(this)
                .load(globals.base_url + "/artisan_login")
                .setBodyParameter("mobile", mobile)//add the artisan as a parameter
                .asString()
                .setCallback((e, result) -> {

                    //first clear the db
                    app.db.clearAllTables();
                    pd.hide();
                    if (e != null) {
                        Log.e(tag, "line 98 " + e.getMessage());
                        Toast.makeText(SigninActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                        Toast.makeText(SigninActivity.this, "line 95: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (result == null) {
                        Log.e(tag, "line 105 ");
                        Toast.makeText(SigninActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                        Toast.makeText(SigninActivity.this, "line 102 "+result, Toast.LENGTH_SHORT).show();
                        return;
                    }


                    String res = "";
                    String msg = "";
                    try {
                        res = new JSONObject(result).getString("res");
                    } catch (Exception ex) {
                    }
                    try {
                        msg = new JSONObject(result).getString("msg");
                    } catch (Exception ex) {
                    }

                    if (res.equals("ok_not_exist")) {
                        Toast.makeText(SigninActivity.this, getString(R.string.this_mobile_does_not_exist), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (res.equals("ok")) {

                        //  redirect to the next activity to confirm the otp pin
                        mArtisan artisan = app.db.mArtisanDao().get_artisan();

                        //add in the new one
                        try {

                            String artisan_data = new JSONObject(result).getString("artisan_data");
                            JSONObject artisan_json_data = new JSONObject(artisan_data);
                            artisan = new mArtisan();
                            //fetch artisan info, and convert skills array to linear string
                            artisan.dateRegistered = artisan_json_data.getString("dateRegistered");
                            //artisan.image=artisan_json_data.getString("image");
                            artisan.name = artisan_json_data.getString("name");
                            artisan.surname = artisan_json_data.getString("surname");
                            artisan.password = artisan_json_data.getString("password");
                            artisan.mobile = artisan_json_data.getString("mobile");
                            artisan.email = artisan_json_data.getString("email");
                            artisan.hourlyRate = Double.parseDouble(artisan_json_data.getString("hourlyRate"));
                            artisan.streetAddress = artisan_json_data.getString("streetAddress");
                            artisan.employmentType = artisan_json_data.getString("employmentType");
                            artisan.country = artisan_json_data.getString("country");
                            artisan.stateOrPorvince = artisan_json_data.getString("stateOrPorvince");
                            artisan.cityOrTown = artisan_json_data.getString("cityOrTown");
                            artisan.otp=artisan_json_data.getString("otp");
                            //parse to string array and convert to linear string
                            try {
                                artisan.skills = TextUtils.join(" . ", new Gson().fromJson(artisan_json_data.getString("skills"), String[].class));
                            }catch (Exception ex){}
                            artisan.numJobs = Integer.parseInt(artisan_json_data.getString("numJobs"));
                            artisan.registered=true;//bypass the otp
                            artisan.busy=Boolean.parseBoolean(artisan_json_data.getString("busy"));
                            artisan.on_duty=Boolean.parseBoolean(artisan_json_data.getString("on_duty"));

                            artisan.synced=true;
                            artisan.app_id=artisan_json_data.getString("app_id");
                            artisan.earnings_since_last_disbursement=0.0;//this is what this person has earned since his/her last dispursment
                            artisan.account_bank=artisan_json_data.getString("account_bank");
                            artisan.account_number=artisan_json_data.getString("account_number");


                            //insert the artisan details into the db
                            app.db.mArtisanDao().insert_one(artisan);

                            //seed the db tables here

                            //appSettings
                            appSettings aps = new appSettings();
                            aps.app_id = artisan.app_id;//set the new app_id from the server
                            app.db.appSettingsDao().insert(aps);

                            //location table
                            mLocation loc = new mLocation();
                            loc.last_known_location=getString(R.string.we_need_your_location_to_provide_you_this_service);
                            app.db.LocationDao().insert_one(loc);


                            Intent main = new Intent(SigninActivity.this, MainActivity.class);
                            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            main.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(main);
                            finish();//clear this activity


                        } catch (Exception ex) {
                            Toast.makeText(SigninActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                            Toast.makeText(SigninActivity.this, "line 182: "+ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(tag, "line 131 " + ex.getMessage());
                        }


                    } else {
                        //show the err measage
                        Toast.makeText(SigninActivity.this, msg + "", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}

