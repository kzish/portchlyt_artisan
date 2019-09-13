package models;


import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.List;

//these are the extra jobs that are can be done by the artisan, these pending jobs were not done when artians were not available
//artisn will get the jobs onlye that he is eligible for
//this object is not stored in the db but is fetched at run time
@Keep
public class mArtisanServiceRequest {
    public String _id;
    public String client_request_id;
    public List<String> requested_services = new ArrayList<>();
    public String client_app_id;
    public String client_mobile;
    public String lat;
    public String lon;
    public String time_of_request;
    public boolean notify_client_in_the_future;
}
