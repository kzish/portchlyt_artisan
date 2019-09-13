package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import models.mArtisan.mArtisan;

@Dao
public interface mArtisanDao {

    @Insert
    public void insert_one(mArtisan item);

    @Insert
    public void insert_many(mArtisan... item);

    @Update
    public void update_one(mArtisan item);

    @Update
    public void update_many(mArtisan... item);


    @Delete
    public void delete_one(mArtisan item);

    @Delete
    public void delete_many(mArtisan... item);

    @Query("select * from mArtisan")
    public mArtisan get_artisan();

    @Query("select * from mArtisan")
    public List<mArtisan> get_artisans();


}
