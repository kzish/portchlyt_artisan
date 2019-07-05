package adapters;

import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porchlyt_artisan.R;
import com.example.porchlyt_artisan.ViewNotificationActivity;
import com.example.porchlyt_artisan.app;

import java.util.List;

import io.realm.Realm;
import io.realm.Sort;
import models.mNotification;
import globals.*;

public class mNotification_Adapter extends RecyclerView.Adapter<mNotification_Adapter.myHolder> {

    List<mNotification> notifications;

    public mNotification_Adapter()
    {
        Realm db = globals.getDB();
        notifications = db.copyFromRealm( db.where(mNotification.class).sort("date", Sort.DESCENDING).findAll() );
        db.close();
    }


    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.m_notification_item, parent, false);
        return new mNotification_Adapter.myHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {
        mNotification notification =  notifications.get(position);
        holder.txt_notification_text.setText(notification.notification_text);
        if(notification.is_read) {
            holder.img_is_open_icon.setImageDrawable(app.ctx.getResources().getDrawable(R.drawable.ic_mail_read));
        }
        holder.setIsRecyclable(true);
        holder.linlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent vn = new Intent(app.ctx, ViewNotificationActivity.class);
                vn.putExtra("notification_id",notification._id);
                vn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                app.ctx.startActivity(vn);
            }
        });

    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class myHolder extends RecyclerView.ViewHolder {

        public ImageView img_is_open_icon;
        public TextView txt_notification_text;
        public LinearLayout linlay;
        public myHolder(View view) {
            super(view);
            txt_notification_text = (TextView)view.findViewById(R.id.txt_notification_text);
            img_is_open_icon = (ImageView )view.findViewById(R.id.img_is_open_icon);
            linlay = (LinearLayout ) view.findViewById(R.id.linlay);
        }
    }
}
