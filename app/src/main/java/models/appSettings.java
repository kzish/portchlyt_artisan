package models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;


//this class is for general settings for the mobile app
@Entity
@Keep
public class appSettings {
    @PrimaryKey
    @NonNull
    public String _id="app";
    public String app_id=UUID.randomUUID().toString();//this is the app id for the mobile app for mqtt server;
    public String account_status=Account_status.active.toString();
}


