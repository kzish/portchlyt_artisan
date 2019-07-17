package com.example.porchlyt_artisan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.poovam.pinedittextfield.LinePinField;
import com.poovam.pinedittextfield.PinField;

import org.json.JSONObject;

import globals.globals;
import io.realm.Realm;
import models.mArtisan.mArtisan;


public class ConfirmOTPActivity extends AppCompatActivity {

    Realm db;
    Toolbar mtoolbar;
    BootstrapButton btnOtp;
    ProgressDialog pd;
    mArtisan m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_otp);
        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);
        pd = new ProgressDialog(this);

        db = Realm.getDefaultInstance();
        btnOtp = (BootstrapButton) findViewById(R.id.btnOtp);

        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(getString(R.string.confirm_5_digit_pin));

        //confirm the opt pin
        final LinePinField txtOtp = findViewById(R.id.txtOtp);
        txtOtp.setOnTextCompleteListener(new PinField.OnTextCompleteListener() {
            @Override
            public boolean onTextComplete(String enteredText) {
                m = db.where(mArtisan.class).findFirst();//get the first and only martisan
                if (enteredText.equals(m.otp)) {

                    pd.setMessage(getString(R.string.please_wait));
                    pd.show();

                    String sdata = "";//the data to post
                    try {
                        sdata = new Gson().toJson(db.copyFromRealm(m));
                        Log.e("d",sdata);
                    } catch (Exception ex) {
                        Log.e("d", ex.getMessage());
                    }

                    try {
                        //now update the server that this person has got the right opt
                        Ion.with(ConfirmOTPActivity.this)
                                .load(globals.base_url + "/artisanConfirmRegistration")
                                .setBodyParameter("data", sdata)
                                .asString()
                                .withResponse()
                                .setCallback(new FutureCallback<Response<String>>() {
                                    @Override
                                    public void onCompleted(Exception e, Response<String> result) {
                                        //pd.hide();
                                        if (e == null) {
                                            try {
                                                String res = new JSONObject(result.getResult()).getString("res");
                                                String msg = new JSONObject(result.getResult()).getString("msg");
                                                if (res.equals("ok"))//then save the local version and start the next activity
                                                {
                                                    db.beginTransaction();
                                                    m.registered = true;
                                                    db.commitTransaction();//save the new status

                                                    Intent main = new Intent(ConfirmOTPActivity.this, MainActivity.class);
                                                    main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    main.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                    main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(main);
                                                    finish();//clear this activity
                                                } else {
                                                    Toast.makeText(ConfirmOTPActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (Exception ex) {
                                                Log.d("d", ex.getMessage());
                                            }
                                        } else {
                                            Toast.makeText(ConfirmOTPActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }catch (Exception ex)
                    {
                        Log.e("d",ex.getMessage());
                    }

                } else {
                    //Toast.makeText(ConfirmOTPActivity.this, getString(R.string.invalid_otp), Toast.LENGTH_SHORT).show();
                    btnOtp.setText(getString(R.string.invalid_otp));
                }

                return true; // Return true to keep the keyboard open else return false to close the keyboard
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
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
