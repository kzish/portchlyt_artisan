package com.example.porchlyt_artisan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.rilixtech.CountryCodePicker;

import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.regex.MatchResult;

import globals.globals;
import io.realm.Realm;
import models.appSettings;
import models.mArtisan.mArtisan;

public class SigninActivity extends AppCompatActivity {

    BootstrapEditText txt_mobile;
    CountryCodePicker ccp;
    Toolbar mtoolbar;
    private ArrayAdapter<String> adapter;
    String tag = "SigninActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        txt_mobile = (BootstrapEditText) findViewById(R.id.txt_mobile);
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
        String mobile = txt_mobile.getText().toString();
        if (mobile.equals("")) {
            txt_mobile.setError(getResources().getString(R.string.cannot_be_blank));
            return;
        }

        mobile = ccp.getSelectedCountryCodeWithPlus() + mobile;//set the country code to the mobile


        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.show();


        Ion.with(this)
                .load(globals.base_url + "/artisan_login")
                .setBodyParameter("mobile", mobile)//add the artisan as a parameter
                .asString()
                .withResponse()
                .setCallback((e, result) -> {

                    pd.hide();
                    if(e!=null)
                    {
                        Log.e(tag,"line 98 "+e.getMessage());
                        Toast.makeText(SigninActivity.this,getString(R.string.error_occured),Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(result==null)
                    {
                        Log.e(tag,"line 105 ");
                        Toast.makeText(SigninActivity.this,getString(R.string.error_occured),Toast.LENGTH_SHORT).show();
                        return;
                    }




                    String res = "";
                    String msg = "";
                    try {
                        res = new JSONObject(result.getResult()).getString("res");
                    } catch (Exception ex) {
                    }
                    try {
                        msg = new JSONObject(result.getResult()).getString("msg");
                    } catch (Exception ex) {
                    }

                    if(res.equals("ok_not_exist"))
                    {
                        Toast.makeText(SigninActivity.this,getString(R.string.this_mobile_does_not_exist),Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (res.equals("ok")) {
                        // to the next activity to confirm the otp pin
                        Realm db = globals.getDB();
                        db.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                mArtisan m = db.where(mArtisan.class).findFirst();
                                //delete the existing
                                if(m!=null) {
                                    m.deleteFromRealm();
                                }

                                //add in the new one
                                String artisan_data = "";
                                try
                                {
                                    artisan_data = new JSONObject(result.getResult()).getString("artisan_data");
                                    m = new Gson().fromJson(artisan_data, mArtisan.class);
                                    db.insertOrUpdate(m);
                                    //also set the appSettings
                                    appSettings aps = new appSettings();
                                    aps.app_id = m.app_id;//set the new app_id
                                    db.insertOrUpdate(aps);
                                    startActivity(new Intent(SigninActivity.this, ConfirmOTPActivity.class));

                                }
                                catch (Exception ex)
                                {
                                    Toast.makeText(SigninActivity.this,getString(R.string.error_occured),Toast.LENGTH_SHORT).show();
                                    Log.e(tag,"line 131 "+ex.getMessage());
                                }




                            }
                        });
                        db.close();

                    } else {
                        //show the err measage
                        Toast.makeText(SigninActivity.this, msg + "", Toast.LENGTH_SHORT).show();
                    }
                });//.//ion witb


    }//.Sign_in


}//.activity
