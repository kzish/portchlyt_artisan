package models.mArtisan;


import org.joda.time.DateTime;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class artisanRating extends RealmObject
{
    @PrimaryKey
    public String _id = UUID.randomUUID().toString();
    public String clientID;//the client who did the rating
    public int numStars;//the number of stars given;
    public String date = DateTime.now().toString();//date this was done
    public String assignmentID;//the id of the assingment this rating was given
}
