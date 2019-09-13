package adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.sirachlabs.porchlyt_artisan.R;
import com.sirachlabs.porchlyt_artisan.ViewJobActivity;
import com.sirachlabs.porchlyt_artisan.app;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Collections;
import java.util.List;

import models.mArtisan.mArtisan;
import models.mJobs.JobStatus;
import models.mJobs.mJobs;


//this is just an adapter used to display the running jobs that this artisan has on the job fragment
public class mjobsAdapter extends RecyclerView.Adapter<mjobsAdapter.myViewHolder> {

    public static List<mJobs> jobs;
    public Activity act;
    String artisan_app_id;

    public mjobsAdapter(Activity act) {
        jobs = app.db.mJobsDao().get_jobs();
        Collections.reverse(jobs);
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
        if (job.job_status.equals(JobStatus.closed.toString())) {
            vh.img_status.setImageResource((R.drawable.ic_verified_user_black_24dp));
        }

        if (job.job_status.equals(JobStatus.cancelled.toString())) {
            vh.img_status.setImageResource((R.drawable.ic_cancel_black_24dp));
        }

        if (job.job_status.equals(JobStatus.disputed.toString())) {
            vh.img_status.setImageResource((R.drawable.ic_announcement_black_24dp));
        }





    }

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
        mArtisan m = app.db.mArtisanDao().get_artisan();
        if (m.image != null) {
            //set the profile if one exists
            try {
                Log.e("pp", m.image);
                Uri contentURI = Uri.parse(m.image);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(act.getContentResolver(), contentURI);
                img_profile.setImageBitmap(bitmap);
            } catch (Exception ex) {
                Log.e("pp", ex + " line 178");
            }
        }
    }


}
