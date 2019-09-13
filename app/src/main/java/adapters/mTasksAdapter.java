package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sirachlabs.porchlyt_artisan.R;
import com.sirachlabs.porchlyt_artisan.ViewJobActivity;
import com.sirachlabs.porchlyt_artisan.app;

import java.util.List;

import globals.globals;
import models.mJobs.JobStatus;
import models.mJobs.mJobs;
import models.mJobs.mTask;

//this adapter is used by ViewJobActivity to diplay amd handle the tasks for that job
public class mTasksAdapter extends RecyclerView.Adapter<mTasksAdapter.myViewHolder> {

    mJobs job;

    List<mTask> tasks;

    public mTasksAdapter(String _job_id) {
        job = app.db.mJobsDao().get_job(_job_id);
        tasks = app.db.taskDao().get_tasks(_job_id);
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.m_task_item, parent, false);
        return new mTasksAdapter.myViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        mTasksAdapter.myViewHolder vh = (mTasksAdapter.myViewHolder) holder;
        //
        mTask task = tasks.get(position);
        //
        vh.txt_task_description.setText(task.description);
        vh.txt_task_price.setText(globals.formatCurrencyExact(task.price));
        //
        vh.img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // delete the task
                app.db.taskDao().delete_one(task);
                ViewJobActivity.set_tasks_adapter();
                ViewJobActivity.setTotalPrice();
            }
        });


        if ( !job.job_status.equals(JobStatus.opened.toString()) )
        {
            //hide this delete button since the job is competed and no more chnages can be made
            vh.img_delete.setVisibility(View.INVISIBLE);
        }

        //set the background odd number colors
        if (position % 2 == 0) {
            vh.linlay.setBackgroundColor(app.ctx.getResources().getColor(R.color.light_grey_bg));
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_task_description;
        public TextView txt_task_price;
        public ImageView img_delete;
        public LinearLayout linlay;

        public myViewHolder(View view) {
            super(view);
            linlay = (LinearLayout) view.findViewById(R.id.linlay);
            txt_task_description = (TextView) view.findViewById(R.id.txt_task_description);
            txt_task_price = (TextView) view.findViewById(R.id.txt_task_price);
            img_delete = (ImageView) view.findViewById(R.id.img_delete);
            linlay = (LinearLayout) view.findViewById(R.id.linlay);
        }
    }

    //inside the adapter class
    @Override
    public long getItemId(int position) {
        return position;
    }
}
