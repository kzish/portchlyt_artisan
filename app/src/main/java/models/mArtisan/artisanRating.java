package models.mArtisan;


import org.joda.time.DateTime;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class artisanRating extends RealmObject
{
    @PrimaryKey
    public String _id = UUID.randomUUID().toString();
    public int numStars;//the number of stars given;
}
