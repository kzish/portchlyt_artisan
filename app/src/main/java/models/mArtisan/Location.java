package models.mArtisan;

import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
public class Location extends RealmObject {
    public Location (){}
    public Location(double lat,double lon)
    {
        coordinates.add(lat);
        coordinates.add(lon);
    }
    @PrimaryKey
    public String type  = "Point";
    public RealmList<Double> coordinates  = new RealmList<>();//lat long
}