package models;


import androidx.annotation.Keep;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
@Keep
public class mUser  {
    @PrimaryKey
    public String mobile;//this is the primary key
    public String name;
    public String surname;
    public String country;
    public String city_or_state;
}
