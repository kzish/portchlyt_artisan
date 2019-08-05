package com.example.porchlyt_artisan;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import adapters.mExtra_Jobs_Adapter;
import globals.globals;
import io.realm.Realm;
import models.mArtisan.mArtisan;
import models.mArtisanServiceRequest;

public class ViewExtraJobsActivity extends AppCompatActivity {

    Toolbar mtoolbar;
    static Activity activity;
    static String tag="ViewExtraJobsActivity";

    SwipeRefreshLayout swipeContainer;
    RecyclerView list_extra_jobs;
    LinearLayout rel_jobs;
    RelativeLayout rel_empty;
    RelativeLayout rel_swipe_down_notification;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_extra_jobs);

        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(getString(R.string.extra_jobs));
        activity= this;

        swipeContainer = (SwipeRefreshLayout)findViewById(R.id.swipeContainer);
        list_extra_jobs = (RecyclerView) findViewById(R.id.list_extra_jobs);

        rel_empty=(RelativeLayout)findViewById(R.id.rel_empty);
        rel_swipe_down_notification=(RelativeLayout)findViewById(R.id.rel_swipe_down_notification);
        rel_jobs=(LinearLayout) findViewById(R.id.rel_jobs);


        //remove notification
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                rel_swipe_down_notification.setVisibility(View.GONE);
            }
        },3000);


        fetch_extra_jobs();

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetch_extra_jobs();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);




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



    public void fetch_extra_jobs() {

        List<mArtisanServiceRequest> jobs = new ArrayList<>();

        mExtra_Jobs_Adapter extra_jobs_adapter = new mExtra_Jobs_Adapter(ViewExtraJobsActivity.this);
        extra_jobs_adapter.setHasStableIds(true);

        LinearLayoutManager lm = new LinearLayoutManager(app.ctx, LinearLayoutManager.VERTICAL, false);
        list_extra_jobs.setLayoutManager(lm);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                Realm db = globals.getDB();
                mArtisan m = db.where(mArtisan.class).findFirst();

                Ion.with(app.ctx)
                        .load(globals.base_url + "/fetch_extra_jobs")
                        .setBodyParameter("artisan_skills", TextUtils.join(":", m.skills))
                        .asString()
                        .setCallback((e, result) -> {
                            swipeContainer.setRefreshing(false);
                            if (e == null) {
                                //create and display the jobs
                                try {
                                    JSONArray json_a = new JSONArray(result);
                                    for (int i = 0; i < json_a.length(); i++) {
                                        JSONObject json = json_a.getJSONObject(i);
                                        mArtisanServiceRequest service_request = new Gson().fromJson(json.toString(), mArtisanServiceRequest.class);
                                        jobs.add(service_request);
                                    }
                                    extra_jobs_adapter.jobs = jobs;
                                    if(jobs.size()==0)
                                    {
                                        swipeContainer.setVisibility(View.GONE);
                                        rel_empty.setVisibility(View.VISIBLE);
                                    }
                                    else
                                    {
                                        swipeContainer.setVisibility(View.VISIBLE);
                                        rel_empty.setVisibility(View.GONE);
                                    }
                                    list_extra_jobs.setAdapter(extra_jobs_adapter);


                                } catch (Exception ex) {
                                    Log.e(tag, "line 118 " + ex.getMessage());
                                    Snackbar.make(rel_jobs, app.ctx.getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                                } finally {

                                }

                            } else {
                                Snackbar.make(rel_jobs, app.ctx.getString(R.string.error_fetching_jobs), Snackbar.LENGTH_SHORT).show();
                            }


                        });


            }
        });
    }

}
