package models.mArtisan;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;


@Entity
@Keep
public class Referee
{
    @PrimaryKey
    @NonNull
    public String _id = UUID.randomUUID().toString();
    public String refname;
    public String refemail;
    public String refmobile;

}
