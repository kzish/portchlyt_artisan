package com.sirachlabs.porchlyt_artisan;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.marlonlom.utilities.timeago.TimeAgo;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import MainActivityTabs.BlogFragment;
import models.mNotification;

public class ViewNotificationActivity extends AppCompatActivity {

    String notification_id;
    TextView txt_notification_date;
    TextView txt_notification_text;
    ImageView img_notification_read;
    Toolbar mtoolbar;
    String tag = "ViewNotificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notification);

        notification_id = getIntent().getStringExtra("notification_id");

        mNotification notification = app.db.mNotificationDao().get_notification(notification_id);

        txt_notification_date = (TextView) findViewById(R.id.txt_notification_date);
        txt_notification_text = (TextView) findViewById(R.id.txt_notification_text);
        img_notification_read = (ImageView) findViewById(R.id.img_notification_read);

        DateTimeFormatter dtf = ISODateTimeFormat.localDateOptionalTimeParser();
        long time_in_millis = dtf.parseLocalDateTime(notification.date).toDateTime().getMillis();

        txt_notification_date.setText(TimeAgo.using(time_in_millis));
        txt_notification_text.setText(notification.notification_text);

        //show read icon if this notification is read
        if (notification.is_read) {
            img_notification_read.setImageDrawable(getResources().getDrawable(R.drawable.ic_mail_read));
        }

        //indicate this notification is now read and update
        notification.is_read = true;
        app.db.mNotificationDao().update_one(notification);
        BlogFragment.get_number_of_notifications();//refresh it
        //
        //artisan can also reject the payment by closing this activity
        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(getString(R.string.view_notification));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_notification, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void delete_notification() {
        try {
            mNotification notification = app.db.mNotificationDao().get_notification(notification_id);
            app.db.mNotificationDao().delete_one(notification);
            BlogFragment.get_number_of_notifications();
            finish();
        }catch (Exception ex)
        {
            Log.e   (tag,ex.getMessage());
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.m_delete:
                delete_notification();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
