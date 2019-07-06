package models.mArtisan;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

public class mArtisan extends RealmObject {
    @PrimaryKey
    public String _id = UUID.randomUUID().toString();
    public String dateRegistered = DateTime.now().toString();//the date this artisan registered in case we will need to clear the database
    public String image;
    public String name = "";
    public String surname = "";
    public String password = "";
    public String mobile = "";//this is also the primary key
    public String email = "";
    public double hourlyRate = 0.0;
    public String streetAddress = "";
    public String employmentType = EmploymentType.partTime.toString();
    public String country = "";
    public String stateOrPorvince = "";
    public String cityOrTown = "";
    public String otp;
    public RealmList<String> skills = new RealmList<>();
    public RealmList<Referee> referees = new RealmList<>();
    public RealmList<Integer> artisanRating = new RealmList<Integer>();
    public int numJobs;//the number of jobs that i have done
    public boolean registered;
    public boolean busy=false;//is this artisan currently busy or not, you do not get another work untill you finish your current work
    public boolean on_duty=true;//am i working or am i on leave? on duty
    public Location location = new Location(0.0,0.0);
    public boolean synced;
    public String app_id;//the app id of this artisan
    public double earnings_since_last_disbursement;//this is what this person has earned since his/her last dispursment



    public int getRating()//get my rating
    {
        try
        {
            int fiveRatings  = 0;
            int fourRatings  = 0;
            int threeRatings = 0;
            int twoRatings   = 0;
            int oneRatings   = 0;

            for(int i :artisanRating){if(i==5)fiveRatings++;}
            for(int i :artisanRating){if(i==4)fourRatings++;}
            for(int i :artisanRating){if(i==3)threeRatings++;}
            for(int i :artisanRating){if(i==2)twoRatings++;}
            for(int i :artisanRating){if(i==1)oneRatings++;}

            int rating = (
                    5 * fiveRatings +
                            4 * fourRatings +
                            3 * threeRatings +
                            2 * twoRatings +
                            1 * oneRatings
            ) /
                    (
                            fiveRatings + fourRatings + threeRatings + twoRatings + oneRatings
                    );
            return (int)rating;
        }
        catch(Exception ex)
        {
            return 0;
        }

    }//.getRating

}

