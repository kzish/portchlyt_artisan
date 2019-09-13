package models.mArtisan;


import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
@Keep
public class mLocation {
    @PrimaryKey
    @NonNull
    public String type  = "Point";
    public double lat;
    public double lng;
    public String last_known_location="";
}