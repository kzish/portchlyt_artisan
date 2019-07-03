package models;

import io.realm.RealmObject;

public class mUser extends RealmObject {
    public String mobile;//this is the primary key
    public String name;
    public String surname;
    public String country;
    public String city_or_state;
}
