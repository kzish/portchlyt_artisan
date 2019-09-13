package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import models.mJobs.mTask;

@Dao
public interface  mTaskDao {

    @Insert
    public void insert_one(mTask task);

    @Insert
    public void insert_many(mTask... task);

    @Update
    public void update_one(mTask task);

    @Update
    public void update_many(mTask... task);


    @Delete
    public void delete_one(mTask task);

    @Delete
    public void delete_many(mTask... task);

    @Query("select * from mTask where _job_id = :job_id")
    public List<mTask> get_tasks(String job_id);


}
