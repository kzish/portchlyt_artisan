package com.sirachlabs.porchlyt_artisan;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import adapters.mNotification_Adapter;
import models.mNotification;

public class ViewNotificationsActivity extends AppCompatActivity {

    String tag="ViewNotificationsActivity";
    RecyclerView list_notifications;
    RelativeLayout rel_empty;
    Toolbar mtoolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notifications);
        list_notifications = (RecyclerView)findViewById(R.id.list_notifications);
        rel_empty = (RelativeLayout) findViewById(R.id.rel_empty);

        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(getString(R.string.notifications));

    }

    @Override
    protected void onStart() {
        super.onStart();
        set_notification_adapter();
    }

    public void set_notification_adapter() {
        try {

            List<mNotification> notifications = app.db.mNotificationDao().get_notifications();
            if(notifications.size()==0)
            {
                rel_empty.setVisibility(View.VISIBLE);
                list_notifications.setVisibility(View.GONE);
            }else
            {
                rel_empty.setVisibility(View.GONE);
                rel_empty.setVisibility(View.GONE);
                list_notifications.setVisibility(View.VISIBLE);
            }
            mNotification_Adapter notification_adapter = new mNotification_Adapter(notifications);
            notification_adapter.setHasStableIds(true);
            LinearLayoutManager lm = new LinearLayoutManager(app.ctx, LinearLayoutManager.VERTICAL, false);
            list_notifications.setLayoutManager(lm);
            list_notifications.setAdapter(notification_adapter);
        } catch (Exception ex) {
            Log.e(tag, ex.getLocalizedMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
