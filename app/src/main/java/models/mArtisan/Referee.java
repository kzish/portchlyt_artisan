package models.mArtisan;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Referee extends RealmObject
{
    @PrimaryKey
    public String _id = UUID.randomUUID().toString();
    public String refname;
    public String refemail;
    public String refmobile;

    //contructor
    public Referee()
    {
        refname = "";
        refmobile = "";
        refemail = "";
    }

}
