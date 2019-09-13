package models.mJobs;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

//each job may have many sub tasks inside
@Entity
@Keep
public class mTask{
    public mTask(){}
    @PrimaryKey
    @NonNull
    public String _id= UUID.randomUUID().toString();
    public String description;//add a description to this task, eg light fitting
    public double price;//the price of this item of the task
    public String _job_id;//job id associated with this task
}
