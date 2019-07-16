package com.example.porchlyt_artisan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.beardedhen.androidbootstrap.BootstrapLabel;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.NavigationMenu;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.koushikdutta.ion.Ion;

import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;


import java.util.List;
import java.util.Locale;

import adapters.mTasksAdapter;
import globals.globals;
import io.github.kobakei.materialfabspeeddial.FabSpeedDial;
import io.realm.Realm;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;
import models.mArtisan.mArtisan;
import models.mJobs.JobStatus;
import models.mJobs.mJobs;

public class ViewJobActivity extends AppCompatActivity {

    Toolbar mtoolbar;

    static String _job_id;//local id in the database
    static TextView lbl_total_price;
    TextView txt_total_time;
    TextView txt_start_time;
    TextView txt_end_time;
    TextView txt_address;
    TextView txt_service_type;
    TextView txt_client_mobile;
    static RecyclerView list_tasks;
    static LinearLayout tbl_lay;
    LinearLayout content_view;


    //map
    MapView mMapView;
    private GoogleMap googleMap;
    String client_address;

    //
    String tag = "ViewJobActivity";
    static Activity activity;

    public static mTasksAdapter tasks_adapter;

    public static int request_code_for_cancel_job = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_job);
        content_view = (LinearLayout) findViewById(R.id.content_view);
        _job_id = getIntent().getStringExtra("_job_id");

        activity = this;

        FabSpeedDial fab = (FabSpeedDial) findViewById(R.id.fab);
        fab.addOnMenuItemClickListener(new Function3<FloatingActionButton, TextView, Integer, Unit>() {
            @Override
            public Unit invoke(FloatingActionButton floatingActionButton, TextView textView, Integer integer) {
                Realm db = globals.getDB();
                mJobs job = db.where(mJobs.class).equalTo("_job_id", _job_id).findFirst();//get the job
                String mobile = job.client_mobile;
                db.close();

                if(integer==R.id.m_call) {
                    Intent call = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mobile, null));
                    ViewJobActivity.this.startActivity(call);
                }

                if(integer==R.id.m_sms)
                {
                    Intent sms = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", mobile, null));
                    ViewJobActivity.this.startActivity(sms);
                }


                return null;
            }
        });

        //
        Realm db=globals.getDB();
        mJobs job= db.where(mJobs  .class).equalTo("_job_id",_job_id).findFirst();
        if (job.job_status.equals(JobStatus.closed.toString()) ||  job.job_status.equals(JobStatus.cancelled.toString()) ) {
            fab.setVisibility(View.GONE);
        }
        db.close();





        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(getString(R.string.job_details));

        //
        lbl_total_price = (TextView) findViewById(R.id.lbl_total_price);
        txt_total_time = (TextView) findViewById(R.id.txt_total_time);
        txt_start_time = (TextView) findViewById(R.id.txt_start_time);
        txt_end_time = (TextView) findViewById(R.id.txt_end_time);
        txt_address = (TextView) findViewById(R.id.txt_address);
        txt_service_type = (TextView) findViewById(R.id.txt_service_type);
        txt_client_mobile = (TextView) findViewById(R.id.txt_client_mobile);
        list_tasks = (RecyclerView) findViewById(R.id.list_tasks);
        tbl_lay = (LinearLayout) findViewById(R.id.tbl_lay);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        list_tasks.setLayoutManager(layoutManager);

        //
        getTheJob();
        set_tasks_adapter();

        //map
        //init map
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();//display map imeediatly

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For dropping a marker at a point on the Map
                Realm db = globals.getDB();
                mJobs job = db.where(mJobs.class).equalTo("_job_id", _job_id).findFirst();
                LatLng my_position = new LatLng(Double.parseDouble(job.geoLocationLatitude), Double.parseDouble(job.geoLocationLongitude));
                db.close();
                googleMap.addMarker(new MarkerOptions().position(my_position).title(getString((R.string.your_client_is_here)))
                        .snippet(getString(R.string.call_or_text_your_client_to_begin_communication)));

                //set the map location
                CameraPosition cameraPosition = new CameraPosition.Builder().target(my_position).zoom(15).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        try {
            MapsInitializer.initialize(ViewJobActivity.this.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //do reverse geo code to get the address of the client
        //this should be done async
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                Realm db = globals.getDB();
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(ViewJobActivity.this, Locale.getDefault());
                try {
                    mJobs job = db.where(mJobs.class).equalTo("_job_id", _job_id).findFirst();
                    addresses = geocoder.getFromLocation(Double.parseDouble(job.geoLocationLatitude), Double.parseDouble(job.geoLocationLongitude), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txt_address.setText(client_address);//set the address
                    }
                });
            }
        });


        //show that this job is currently closed
        db = globals.getDB();
        job = db.where(mJobs.class).equalTo("_job_id", _job_id).findFirst();
        if (job.job_status.equals(JobStatus.closed.toString())) {
            Snackbar.make(content_view, getString(R.string.this_job_is_closed), Snackbar.LENGTH_INDEFINITE).show();
        }
        if (job.job_status.equals(JobStatus.cancelled.toString())) {
            Snackbar.make(content_view, getString(R.string.this_job_was_cancelled), Snackbar.LENGTH_INDEFINITE).show();
        }
        db.close();


    }


    //get the job from the database and display it
    private void getTheJob() {
        DateTimeFormatter dtf = ISODateTimeFormat.localDateOptionalTimeParser();
        DateTimeFormatter dtf2 = DateTimeFormat.forPattern("d MMM,yyyy HH:mm");
        Realm db = globals.getDB();
        mJobs job = db.where(mJobs.class).equalTo("_job_id", _job_id).findFirst();//get the job

        txt_start_time.setText(dtf2.print(dtf.parseLocalDateTime(job.start_time)));
        if (job.end_time != null && !job.end_time.equals("")) {
            txt_end_time.setText(dtf2.print(dtf.parseLocalDateTime(job.end_time)));
        }
        set_the_total_time();//set the time in a pretty format
        txt_service_type.setText(job.description);
        txt_client_mobile.setText(job.client_mobile);
        db.close();
        setTotalPrice();
    }

    public static void setTotalPrice() {
        Realm db = globals.getDB();
        mJobs job = db.where(mJobs.class).equalTo("_job_id", _job_id).findFirst();
        lbl_total_price.setText(app.ctx.getString(R.string.total_price) + ": " + globals.formatCurrency(job.getTheTotalPrice()));
        if (job.getTheTotalPrice() > 0) {
            tbl_lay.setVisibility(View.VISIBLE);
        } else {
            tbl_lay.setVisibility(View.INVISIBLE);
        }
        db.close();
    }

    //set the total time of this job if running or complete
    private void set_the_total_time() {
        Realm db = globals.getDB();
        mJobs job = db.where(mJobs.class).equalTo("_job_id", _job_id).findFirst();
        DateTimeFormatter dtf = ISODateTimeFormat.localDateOptionalTimeParser();
        LocalDateTime start_time = dtf.parseLocalDateTime(job.start_time);
        LocalDateTime end_time;

        if (job.end_time != null) {
            end_time = dtf.parseLocalDateTime(job.end_time);
        } else {
            end_time = LocalDateTime.now();
        }

        Period p = new Period(start_time, end_time);
        int days = p.getDays();
        int hours = p.getHours();
        int mins = p.getMinutes();
        txt_total_time.setText(
                getString(R.string.total_time) + " " + days + " " + getString(R.string.days)
                        + " " + hours + " " + getString(R.string.hrs)
                        + " " + mins + " " + getString(R.string.mins)
        );
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTotalPrice();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.m_add_task:
                Intent at = new Intent(ViewJobActivity.this, AddTaskActivity.class);
                at.putExtra("_job_id", _job_id);
                startActivity(at);
                break;
            case R.id.m_send_bill_to_client:
                forward_bill_to_client();
                break;

            case R.id.m_cancel:
                Intent cancel = new Intent(ViewJobActivity.this, CancelJobActivity.class);
                Realm db = globals.getDB();
                mArtisan artisan = db.where(mArtisan.class).findFirst();
                mJobs job = db.where(mJobs.class).equalTo("_job_id", _job_id).findFirst();
                cancel.putExtra("_job_id", _job_id);
                cancel.putExtra("artisan_app_id", artisan.app_id);
                cancel.putExtra("client_app_id", job.client_app_id);
                db.close();
                startActivityForResult(cancel, request_code_for_cancel_job);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Realm db = globals.getDB();
        mJobs job = db.where(mJobs.class).equalTo("_job_id", _job_id).findFirst();
        if (job.job_status.equals(JobStatus.opened.toString())) {//show the menu if this job is still open
            getMenuInflater().inflate(R.menu.view_job_menu, menu);
        }//only show when job i still pending completion
        return true;
    }


    @Override
    protected void onStop() {
        super.onStop();
    }


    //function to send this job task to the client
    public void forward_bill_to_client() {

        Realm db = globals.getDB();
        mJobs job = db.where(mJobs.class).equalTo("_job_id", _job_id).findFirst();
        if (job.getTheTotalPrice() == 0) {
            Snackbar.make(content_view, getString(R.string.add_some_bills), Snackbar.LENGTH_SHORT).show();
            return;//dont send anything unless some bills have been added
        }
        String sdata = "";
        ProgressDialog pd = new ProgressDialog(ViewJobActivity.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        try {

            sdata = new Gson().toJson(db.copyFromRealm(job));
            db.close();
            Ion.with(ViewJobActivity.this)
                    .load(globals.base_url + "/forward_bill_to_client")
                    .setBodyParameter("data", sdata)
                    .asString()
                    .setCallback((e, result) -> {
                        pd.dismiss();//hide the pd
                        Log.e(tag, result + " result");
                        if (e != null) {
                            //handle error
                            Snackbar.make(content_view, getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        //proceed if no error
                        try {
                            JSONObject json = new JSONObject(result);
                            String res = json.getString("res");
                            String msg = json.getString("msg");
                            if (res.equals("ok")) {
                                Snackbar.make(content_view, getString(R.string.sent), Snackbar.LENGTH_SHORT).show();
                                return;
                            } else {
                                Log.e(tag, msg);
                                Snackbar.make(content_view, getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (Exception ex) {
                            Log.e(tag, ex.getMessage());
                        }
                    });
        } catch (Exception ex) {
            Log.e(tag, ex.getMessage());
            pd.dismiss();
        } finally {
            //
        }
    }


    public static void set_tasks_adapter() {
        tasks_adapter = new mTasksAdapter(_job_id);//this automatically pulls the tasks of this job
        tasks_adapter.setHasStableIds(true);
        list_tasks.setAdapter(tasks_adapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == request_code_for_cancel_job) {
            finish();
            if (resultCode == RESULT_OK) {
                finish();//finish this activity too
            }
        }
    }

    public static void close_activity() {
        activity.finish();
    }


}
