package adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porchlyt_artisan.R;
import com.example.porchlyt_artisan.ViewJobActivity;
import com.example.porchlyt_artisan.ViewNotificationActivity;
import com.example.porchlyt_artisan.app;
import com.github.marlonlom.utilities.timeago.TimeAgo;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.List;
import java.util.Locale;

import MainActivityTabs.NewsFragment;
import globals.globals;
import io.realm.Realm;
import io.realm.Sort;
import models.mArtisanServiceRequest;
import models.mJobs.mJobs;
import models.mNotification;

public class mExtra_Jobs_Adapter extends RecyclerView.Adapter<mExtra_Jobs_Adapter.myHolder> {

    public List<mArtisanServiceRequest> jobs;
    String tag = "mExtra_Jobs_Adapter";
    String client_address;
    Activity act;

    public mExtra_Jobs_Adapter(Activity act) {

        this.act = act;


    }


    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.m_extra_jobs_item, parent, false);
        return new myHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {
        mArtisanServiceRequest job = jobs.get(position);

        Drawable picture_icon = null;

        //select the correct icon to display
        if (job.requested_services.contains("Carpentry"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_carpentry);
        if (job.requested_services.contains("Electricals"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_electricals);
        if (job.requested_services.contains("Plumbing"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_plumbing);
        if (job.requested_services.contains("Cleaning"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_cleaning);
        if (job.requested_services.contains("Fumigation"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_fumigation);
        if (job.requested_services.contains("Masonry"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_builder);
        if (job.requested_services.contains("Painting"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_painting);
        if (job.requested_services.contains("Home Inspection"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_home_inspection);
        if (job.requested_services.contains("Air Conditioner Repairs"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_air_condition);
        if (job.requested_services.contains("Refrigerator Repair"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_fridge);
        if (job.requested_services.contains("Home/Office Relocation"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_movers);
        if (job.requested_services.contains("Tilers"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_tiler);
        if (job.requested_services.contains("Swimming Pool Maintenance"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_swiming_pool);
        if (job.requested_services.contains("Gardner"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_gardening);
        if (job.requested_services.contains("Furniture"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_furniture);
        if (job.requested_services.contains("Generator Repairs"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_generator);
        if (job.requested_services.contains("Solar Installation"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_solar);
        if (job.requested_services.contains("Security Systems"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_security);
        if (job.requested_services.contains("Roofing"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_roofing);
        if (job.requested_services.contains("Fencing"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_fencing);
        if (job.requested_services.contains("Laundry services"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_laundry);
        if (job.requested_services.contains("Gas cooker repairs"))
            picture_icon = app.ctx.getResources().getDrawable(R.drawable.ic_gas_cooker);

        holder.img_job_picture.setImageDrawable(picture_icon);

        holder.txt_job_name.setText(TextUtils.join(":", job.requested_services));
        holder.txt_client_mobile.setText(job.client_mobile);

        //set the correct time

        try {
            DateTimeFormatter dtf = ISODateTimeFormat.dateTime();
            long time_in_millis = dtf.parseDateTime(job.time_of_request).toDateTime().getMillis();
            holder.txt_date.setText(TimeAgo.using(time_in_millis));
        } catch (Exception ex) {
            Log.e(tag, ex.getLocalizedMessage());
        }

        //also get the address

        client_address = "";


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                Realm db = globals.getDB();
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(app.ctx, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(Double.parseDouble(job.lat), Double.parseDouble(job.lon), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                    if (city.equals(null)) city = "";
                    if (state.equals(null)) state = "";
                    if (country.equals(null)) country = "";
                    if (knownName.equals(null))
                        knownName = "";//this to ensure that we dont pull null values in the address
                    client_address = country + ", " + city + ", " + state + ", " + knownName;

                } catch (Exception ex) {
                    Log.e(tag, "asrda line 95 " + ex.getMessage());
                } finally {
                    db.close();
                }

                //this will now run on the ui thread
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holder.txt_job_location.setText(client_address);
                    }
                });
            }
        });


    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    class myHolder extends RecyclerView.ViewHolder {

        public ImageView img_job_picture;
        public TextView txt_job_name;
        public TextView txt_date;
        public TextView txt_client_mobile;
        public TextView txt_job_location;
        public LinearLayout linlay;

        public myHolder(View view) {
            super(view);
            txt_job_location = (TextView) view.findViewById(R.id.txt_job_location);
            txt_client_mobile = (TextView) view.findViewById(R.id.txt_client_mobile);
            txt_date = (TextView) view.findViewById(R.id.txt_date);
            txt_job_name = (TextView) view.findViewById(R.id.txt_job_name);
            txt_job_location = (TextView) view.findViewById(R.id.txt_job_location);
            img_job_picture = (ImageView) view.findViewById(R.id.img_job_picture);
            linlay = (LinearLayout) view.findViewById(R.id.linlay);
        }
    }




    // Clean all elements of the recycler
    public void clear() {
        jobs.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<mArtisanServiceRequest> list) {
        jobs.addAll(list);
        notifyDataSetChanged();
    }

    //inside the adapter class
    @Override
    public long getItemId(int position) {
        return position;
    }



}//.adapter
