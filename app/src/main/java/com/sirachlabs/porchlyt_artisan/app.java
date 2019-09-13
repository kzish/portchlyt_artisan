package com.sirachlabs.porchlyt_artisan;

import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.room.Room;

import net.danlew.android.joda.JodaTimeAndroid;

import java.lang.reflect.Method;


public class app extends MultiDexApplication {

    public static Context ctx;
    public static AppDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();

        ctx =getApplicationContext();

        JodaTimeAndroid.init(this);
        MultiDex.install(this);
        db = Room.databaseBuilder(this, AppDatabase.class, "porchlyt_artisan")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()//todo fix this for the production version
                .build();

        showDebugDBAddressLogToast(this);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }


    public static void showDebugDBAddressLogToast(Context context) {
        if (BuildConfig.DEBUG) {
            try {
                Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
                Method getAddressLog = debugDB.getMethod("getAddressLog");
                Object value = getAddressLog.invoke(null);
                Log.e("db",value+"");
                // Toast.makeText(context, (String) value, Toast.LENGTH_LONG).show();
            } catch (Exception ignore) {

            }
        }
    }


}
