package models;

import androidx.annotation.Keep;

import java.util.ArrayList;

///this is a model of  a search item

@Keep
public class mArtisanSearch {
    public String latitude;
    public String longitude;
    public String address;
    public ArrayList<String> job_categories= new ArrayList<String>();
}
