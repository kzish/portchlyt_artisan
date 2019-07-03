package models.mJobs;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

//this is a single job per visit
//this job may have many sub tasks in the job
//this class must match with the artian app class of mjobs
public class mJobs extends RealmObject {
    @PrimaryKey
    public String _id = UUID.randomUUID().toString();
    public String _job_id;//this is commmon between both the client and the artisan
    public String client_mobile;//mobile number for the cleient
    public String client_app_id;//app id fo rmqtt identfy the client for this job
    public String start_time;//this is also the start time of the job
    public String end_time;//the time this job was ended or officially cleared
    public String country;
    public String category;
    public String city_or_state;
    public String geoLocationLatitude;//coordinates of this job
    public String geoLocationLongitude;
    public String address;//the address of the client requested from will aquire through reverse geo coding
    public double price;
    public String description;//any notes the artsian may want to note
    public RealmList<mTask> tasks;//these are te bills or break down of the job

    public mJobs() {
        start_time = LocalDateTime.now().toString();
        _job_id = UUID.randomUUID().toString();//this artisan must create the job id then send it to the client
    }

    public double getTheTotalPrice() {
        double total = 0;
        for (mTask t : tasks) {
            total += t.price;
        }
        return total;
    }
}
