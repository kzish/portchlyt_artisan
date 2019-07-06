package adapters;

import globals.globals;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.Sort;
import models.mArtisan.mArtisan;
import models.mJobs.mJobs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.beardedhen.androidbootstrap.BootstrapLabel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.porchlyt_artisan.R;
import com.example.porchlyt_artisan.ViewJobActivity;
import com.example.porchlyt_artisan.app;
import com.github.marlonlom.utilities.timeago.TimeAgo;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Collections;
import java.util.List;


//this is just an adapter used to display the running jobs that this artisan has on the job fragment
public class mjobsAdapter extends RecyclerView.Adapter<mjobsAdapter.myViewHolder> {

    public static List<mJobs> jobs;
    public Activity act;
    String artisan_app_id;

    public mjobsAdapter(Activity act) {
        Realm db = globals.getDB();
        List<mJobs> _jobs = db.where(mJobs.class).findAll();
        jobs = db.copyFromRealm(_jobs);
        Collections.reverse(jobs);
        db.close();
        this.act = act;
        this.artisan_app_id = artisan_app_id;
    }

    @Override
    public myViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mjobs_row_item, parent, false);
        return new myViewHolder(itemView, i);
    }

    @Override
    public int getItemCount() {
        if (jobs == null) return 0;
        return jobs.size();
    }


    @Override
    public void onBindViewHolder(myViewHolder viewHolder, int i) {
        myViewHolder vh = (myViewHolder) viewHolder;
        mJobs job = jobs.get(i);
        DateTimeFormatter dtf = ISODateTimeFormat.localDateOptionalTimeParser();
        long time_in_millis = dtf.parseLocalDateTime(job.start_time).toDateTime().getMillis();
        vh.txt_date_time.setText(TimeAgo.using(time_in_millis));//set date in a pritty format
        vh.txt_description.setText(job.description);
        if (i % 2 == 0) {//alternate the background color
            vh.cardView.setCardBackgroundColor(act.getResources().getColor(R.color.light_grey_bg));
        } else {
            vh.cardView.setCardBackgroundColor(act.getResources().getColor(R.color.white));
        }

        //open the activty to view the job on  clicking this item
        vh.rel_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vj = new Intent(act, ViewJobActivity.class);
                vj.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                vj.putExtra("_job_id", job._job_id);//using the primary key
                act.startActivity(vj);
            }
        });


        //indicate that this job is completed
        if (job.end_time != null) {
            vh.img_status.setImageResource((R.drawable.ic_verified_user_black_24dp));
        }

        //todo improve performance so we can ue the following code to set the profile image of the artisan
        //set the profile pic if one exists
        /*Realm db = globals.getDB();
        mArtisan m = db.where(mArtisan.class).findFirst();
        if(m.image!=null) {
            //set_profile_pic(vh.img_artisan_icon);
        }
        db.close();*/



    }

    //inside the adapter class
    @Override
    public long getItemId(int position) {
        return position;
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_date_time;
        public TextView txt_description;
        public ImageView img_artisan_icon;
        public CardView cardView;
        public RelativeLayout rel_lay;
        public ImageView img_status;

        public myViewHolder(View itemView, int pos) {
            super(itemView);
            txt_date_time = (TextView) itemView.findViewById(R.id.txt_date_time);
            txt_description = (TextView) itemView.findViewById(R.id.txt_description);
            img_artisan_icon = (ImageView) itemView.findViewById(R.id.img_artisan_icon);
            cardView = (CardView) itemView.findViewById(R.id.cardView);
            rel_lay = (RelativeLayout) itemView.findViewById(R.id.rel_lay);
            img_status = (ImageView) itemView.findViewById(R.id.img_status);
        }

    }

    //set profile pic from db
    private void set_profile_pic(ImageView img_profile) {
        Realm db = globals.getDB();
        mArtisan m = db.where(mArtisan.class).findFirst();
        if (m.image != null) {
            //set the profile if one exists
            try {
                Log.e("pp", m.image);
                Uri contentURI = Uri.parse(m.image);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(act.getContentResolver(), contentURI);
                img_profile.setImageBitmap(bitmap);
            } catch (Exception ex) {
                Log.e("pp", ex + " line 178");
            } finally {
                db.close();
            }
        }
    }


}
