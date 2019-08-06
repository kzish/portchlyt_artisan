package com.example.porchlyt_artisan;

import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.beardedhen.androidbootstrap.TypefaceProvider;


import net.danlew.android.joda.JodaTimeAndroid;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.jonasrottmann.realmbrowser.RealmBrowser;
import globals.globals;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import models.appSettings;

public class app extends MultiDexApplication {

    public static Context ctx;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        JodaTimeAndroid.init(this);
        MultiDex.install(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name("porchlyt_artisan.realm")
                .schemaVersion(21)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfig);


        TypefaceProvider.registerDefaultIconSets();

        ctx = getApplicationContext();

        //create and insert the appsettings
        Realm db = Realm.getDefaultInstance();
        db.beginTransaction();
        appSettings s = new appSettings();
        db.insertOrUpdate(s);
        db.commitTransaction();
        db.close();

        //delete all realm data
        //Realm.deleteRealm(realmConfig);


        //show the realm database
        //RealmBrowser.startRealmModelsActivity(this, realmConfig);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
