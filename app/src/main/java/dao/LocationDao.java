package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import models.mArtisan.mLocation;


@Dao
public interface LocationDao {

    @Insert
    public void insert_one(mLocation item);

    @Insert
    public void insert_many(mLocation... item);

    @Update
    public void update_one(mLocation item);

    @Update
    public void update_many(mLocation... item);


    @Delete
    public void delete_one(mLocation item);

    @Delete
    public void delete_many(mLocation... item);

    @Query("select * from mLocation")
    public mLocation get_location();


}
