package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import models.mNotification;

@Dao
public interface mNotificationDao {

    @Insert
    public void insert_one(mNotification item);

    @Insert
    public void insert_many(mNotification... item);

    @Update
    public void update_one(mNotification item);

    @Update
    public void update_many(mNotification... item);


    @Delete
    public void delete_one(mNotification item);

    @Delete
    public void delete_many(mNotification... item);

    @Query("select * from mNotification where _id=:id order by date desc")
    public mNotification get_notification(String id);

    @Query("select * from mNotification order by date desc")
    public List<mNotification> get_notifications();

    @Query("select * from mNotification where is_read=:state order by date desc")
    public List<mNotification>get_notifications(boolean state);


}
