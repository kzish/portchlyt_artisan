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
import android.content.pm.ActivityInfo;
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

import adapters.mBlogPostsAdapter;
import adapters.mExtra_Jobs_Adapter;
import globals.globals;
import io.realm.Realm;
import models.mArtisan.mArtisan;
import models.mArtisanServiceRequest;

public class ViewExtraJobsActivity extends AppCompatActivity {

    Toolbar mtoolbar;
    static Activity activity;
    static String tag = "ViewExtraJobsActivity";

    SwipeRefreshLayout swipeContainer;
    RecyclerView extra_jobs_recycler_view;
    LinearLayout rel_jobs;
    RelativeLayout rel_empty;
    RelativeLayout rel_swipe_down_notification;

    List<mArtisanServiceRequest> extra_jobs = new ArrayList<>();
    mExtra_Jobs_Adapter extra_jobs_adapter;

    int page = 0;//start at zero
    int per_page = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_extra_jobs);

        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(getString(R.string.extra_jobs));
        activity = this;

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        extra_jobs_recycler_view = (RecyclerView) findViewById(R.id.extra_jobs_recycler_view);
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        extra_jobs_recycler_view.setLayoutManager(lm);
        extra_jobs_recycler_view.setHasFixedSize(true);

        rel_empty = (RelativeLayout) findViewById(R.id.rel_empty);
        rel_swipe_down_notification = (RelativeLayout) findViewById(R.id.rel_swipe_down_notification);
        rel_jobs = (LinearLayout) findViewById(R.id.rel_jobs);


        //remove notification
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                rel_swipe_down_notification.setVisibility(View.GONE);
            }
        }, 3000);


        //
        extra_jobs_adapter = new mExtra_Jobs_Adapter(this, extra_jobs);

        extra_jobs_recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) extra_jobs_recycler_view.getLayoutManager();
                int totalItemCount = extra_jobs_adapter.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (totalItemCount <= (lastVisibleItem + 1)) {

                    //dont insert yet another if already loading
                    mArtisanServiceRequest job = extra_jobs.get(extra_jobs.size()-1);
                    if(job==null)return;//if the last insertion was already a null then dont execute this again

                    extra_jobs.add(null);//add the loading dialog view
                    extra_jobs_adapter.notifyItemInserted(extra_jobs.size() - 1);//notify your insert
                    fetch_extra_jobs();
                }
            }
        });

        extra_jobs_adapter.setHasStableIds(true);
        extra_jobs_recycler_view.setAdapter(extra_jobs_adapter);



        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                //reset the page to 1 and clear the current list
                try {
                    page = 1;
                    extra_jobs = new ArrayList<>();
                    extra_jobs_adapter = new mExtra_Jobs_Adapter(ViewExtraJobsActivity.this, extra_jobs);
                    extra_jobs_adapter.setHasStableIds(true);
                    extra_jobs_recycler_view.setAdapter(extra_jobs_adapter);
                    fetch_extra_jobs();
                } catch (Exception ex) {
                    Log.e(tag, ex + "");
                }
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        //initial pull
        fetch_extra_jobs();


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

        Realm db = globals.getDB();
        mArtisan m = db.where(mArtisan.class).findFirst();
        db.close();

        Ion.with(app.ctx)
                .load(globals.base_url + "/fetch_extra_jobs")
                .setBodyParameter("artisan_skills", TextUtils.join(":", m.skills))
                .setBodyParameter("page", page + "")
                .setBodyParameter("per_page", per_page + "")
                .asString()
                .setCallback((e, result) -> {

                    //delay dismissing the progress bar for a second
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeContainer.setRefreshing(false);
                        }
                    }, 1000);

                    if (extra_jobs.size() > 0) {
                        mArtisanServiceRequest last_inserted_job = extra_jobs.get(extra_jobs.size() - 1);
                        if (last_inserted_job == null) {
                            extra_jobs.remove(last_inserted_job);//remove that null item that was inserted
                            extra_jobs_adapter.notifyItemRemoved(extra_jobs.size() - 1);//notify it
                        }
                    }

                    if (e != null) {
                        Log.e(tag, e + "");
                        Snackbar.make(rel_jobs, app.ctx.getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    if (result == null) {
                        Log.e(tag, "result is null");
                        Snackbar.make(rel_jobs, app.ctx.getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    Log.e(tag, "result: " + result);

                    //create and display the jobs
                    try {
                        JSONArray json_a = new JSONArray(result);
                        for (int i = 0; i < json_a.length(); i++) {
                            JSONObject json = json_a.getJSONObject(i);
                            mArtisanServiceRequest service_request = new Gson().fromJson(json.toString(), mArtisanServiceRequest.class);
                            if(!extra_jobs.contains(service_request)) {//only insert it once
                                extra_jobs.add(service_request);//insert it
                                extra_jobs_adapter.notifyItemInserted(extra_jobs.size() - 1);//notify
                            }
                        }
                        page++;//increment the page

                    } catch (Exception ex) {
                        Log.e(tag, "line 118 " + ex.getMessage());
                        Snackbar.make(rel_jobs, app.ctx.getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();

                    } finally {
                        if (extra_jobs.size() > 0) {
                            mArtisanServiceRequest last_inserted_job = extra_jobs.get(extra_jobs.size() - 1);
                            if (last_inserted_job == null) {
                                extra_jobs.remove(last_inserted_job);//remove that null item that was inserted
                                extra_jobs_adapter.notifyItemRemoved(extra_jobs.size() - 1);//notify it
                            }
                        }

                        if (extra_jobs.size() == 0) {
                            swipeContainer.setVisibility(View.GONE);
                            rel_empty.setVisibility(View.VISIBLE);
                        } else {
                            swipeContainer.setVisibility(View.VISIBLE);
                            rel_empty.setVisibility(View.GONE);
                        }
                    }

                });
    }//fetch_extra_jobs


}//class
