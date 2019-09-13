package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import models.mArtisan.artisanRating;

@Dao
public interface mArtisanRatingDao {

    @Insert
    public void insert_one(artisanRating item);

    @Insert
    public void insert_many(artisanRating... item);

    @Update
    public void update_one(artisanRating item);

    @Update
    public void update_many(artisanRating... item);


    @Delete
    public void delete_one(artisanRating item);

    @Delete
    public void delete_many(artisanRating... item);

    @Query("select * from artisanRating")
    public List<artisanRating> get_artisan_ratings();//get my rating


}
