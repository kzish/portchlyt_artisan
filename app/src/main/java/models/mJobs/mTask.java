package models.mJobs;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

//each job may have many sub tasks inside
public class mTask  extends RealmObject{
    public mTask(){}
    @PrimaryKey
    public String _id= UUID.randomUUID().toString();
    public String description;//add a description to this task, eg light fitting
    public double price;//the price of this item of the task
}
