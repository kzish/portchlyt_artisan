package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porchlyt_artisan.R;
import com.example.porchlyt_artisan.ViewJobActivity;
import com.example.porchlyt_artisan.app;

import org.w3c.dom.Text;

import java.util.List;

import globals.globals;
import io.realm.Realm;
import models.mJobs.mJobs;
import models.mJobs.mTask;

//this adapter is used by ViewJobActivity to diplay amd handle the tasks for that job
public class mTasksAdapter extends RecyclerView.Adapter<mTasksAdapter.myViewHolder> {

    mJobs job;
    public mTasksAdapter(String _job_id)
    {
        Realm db = globals.getDB();
        job      = db.copyFromRealm(   db.where(mJobs.class).equalTo("_job_id",_job_id).findFirst()  );
        db.close();
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
        mTask task = job.tasks.get(position);
        //
        vh.txt_task_description.setText(task.description);
        vh.txt_task_price.setText(task.price+"");
        //
        vh.img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tasks_list.remove(position);
                Realm db = globals.getDB();
                db.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mTask t = job.tasks.get(position);
                        //remove the task from the job
                        db.where(mJobs.class).equalTo("_job_id",job._job_id).findFirst().tasks.remove(position);
                        //now delete the task from realm also
                        db.where( mTask.class ).equalTo("_id",t._id).findFirst().deleteFromRealm();

                        ViewJobActivity.set_tasks_adapter();
                        ViewJobActivity.setTotalPrice();
                    }
                });
                db.close();
            }
        });




        if(job.end_time!=null)
        {
            //hide this delete button since the job is competed and no more chnages can be made
            vh.img_delete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return job.tasks.size();
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        public TextView txt_task_description;
        public TextView txt_task_price;
        public ImageView img_delete;
        public LinearLayout linlay;
        public myViewHolder(View view)
        {
            super(view);
            linlay= (LinearLayout)view.findViewById(R.id.linlay);
            txt_task_description=(TextView)view.findViewById(R.id.txt_task_description);
            txt_task_price=(TextView)view.findViewById(R.id.txt_task_price);
            img_delete=(ImageView) view.findViewById(R.id.img_delete);
        }
    }
}
