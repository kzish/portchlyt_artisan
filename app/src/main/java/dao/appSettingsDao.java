package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import models.appSettings;

@Dao
public interface appSettingsDao {

    @Insert
    public void insert(appSettings aps);

    @Update
    public void update(appSettings aps);

    @Delete
    public void delete(appSettings aps);

    @Query("select * from appSettings")
    public appSettings get_app_settings();

}
