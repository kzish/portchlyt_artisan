package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import models.mJobs.mJobs;

@Dao
public interface mJobsDao {

    @Insert
    public void insert_one(mJobs item);

    @Insert
    public void insert_many(mJobs... item);

    @Update
    public void update_one(mJobs item);

    @Update
    public void update_many(mJobs... item);


    @Delete
    public void delete_one(mJobs item);

    @Delete
    public void delete_many(mJobs... item);


    @Query("select * from mJobs where _job_id=:job_id")
    public mJobs get_job(String job_id);


    @Query("select * from mJobs")
    public List<mJobs> get_jobs();

    @Query("select * from mJobs where job_status=:status")
    public List<mJobs>get_jobs_with_status(String status);


}
