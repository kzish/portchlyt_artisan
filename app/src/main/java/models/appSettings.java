package models;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

//this class is for general settings for the mobile app
public class appSettings extends RealmObject {
    public appSettings()
    {
        _id="app";
        app_id=UUID.randomUUID().toString();//this is the app id for the mobile app for mqtt server
    }
    @PrimaryKey
    public String _id;
    public String app_id;
}
