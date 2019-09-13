package models.mArtisan;


import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;


@Entity
@Keep
public class artisanRating
{
    @PrimaryKey
    @NonNull
    public String _id = UUID.randomUUID().toString();
    public int numStars;//the number of stars given;
}
