package com.example.porchlyt_artisan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import io.realm.Realm;
import models.mArtisan.mArtisan;

public class SplashActivity extends AppCompatActivity {

    Realm db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        db= Realm .getDefaultInstance();
        //check if this user is already registered yes or not
        mArtisan m = db.where(mArtisan.class).findFirst();
        if(m!=null)//this means the user is most probably registerd already
        {
            if(m.registered)
            {
                //since already regirted we can skip this
                startActivity(new Intent(SplashActivity.this,MainActivity.class));
                finish();//clear this activity
            }
        }//else just continue and allow the user to register
}

    public void Register(View v){
        startActivity(new Intent(SplashActivity.this,RegisterActivity.class));
        Log.e("d","clicked");
    }
    public void Signin(View v) {

        startActivity(new Intent(SplashActivity.this,SigninActivity.class));
        Log.e("d","clicked");
    }
}
