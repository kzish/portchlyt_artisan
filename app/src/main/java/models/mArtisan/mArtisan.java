package models.mArtisan;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.sirachlabs.porchlyt_artisan.app;

import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;


@Entity
@Keep
public class mArtisan  {
    @PrimaryKey
    @NonNull
    public String _id = UUID.randomUUID().toString();
    public String dateRegistered = DateTime.now().toString();//the date this artisan registered in case we will need to clear the database
    public String image;
    public String name = "";
    public String surname = "";
    public String password = "";
    public String mobile = "";
    public String mobile_country_code = "";
    public String email = "";
    public double hourlyRate = 0.0;
    public String streetAddress = "";
    public String employmentType = EmploymentType.partTime.toString();
    public String country = "";
    public String stateOrPorvince = "";
    public String cityOrTown = "";
    public String otp;
    public String skills = "";
    //public RealmList<Referee> referees = new RealmList<>();
    //public RealmList<artisanRating> artisanRating = new RealmList<artisanRating>();
    public int numJobs;//the number of jobs that i have done
    public boolean registered;
    public boolean busy=false;//is this artisan currently busy or not, you do not get another work untill you finish your current work
    public boolean on_duty=true;//am i working or am i on leave? on duty

    public boolean synced;
    public String app_id;//the app id of this artisan
    public double earnings_since_last_disbursement;//this is what this person has earned since his/her last dispursment
    public String account_bank;
    public String account_number;


    public int get_rating()//get my rating
    {
        List<artisanRating> artisanRating= app.db.mArtisanRatingDao().get_artisan_ratings();
        try
        {
            int fiveRatings  = 0;
            int fourRatings  = 0;
            int threeRatings = 0;
            int twoRatings   = 0;
            int oneRatings   = 0;

            for(artisanRating i :artisanRating){if(i.numStars==5)fiveRatings++;}
            for(artisanRating i :artisanRating){if(i.numStars==4)fourRatings++;}
            for(artisanRating i :artisanRating){if(i.numStars==3)threeRatings++;}
            for(artisanRating i :artisanRating){if(i.numStars==2)twoRatings++;}
            for(artisanRating i :artisanRating){if(i.numStars==1)oneRatings++;}

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

