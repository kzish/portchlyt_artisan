package com.sirachlabs.porchlyt_artisan;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agik.AGIKSwipeButton.Controller.OnSwipeCompleteListener;
import com.agik.AGIKSwipeButton.View.Swipe_Button_View;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.joda.time.LocalDateTime;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import MainActivityTabs.JobsFragment;
import globals.globals;
import models.mArtisan.mArtisan;
import models.mJobs.mJobs;


//this activity is a dialog activity it is for answering the service request or rejecting it
public class AnswerServiceRequestDialogActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static TextView txt_count_down_timer;
    public static ImageView img_btn_reject;
    public static ImageView img_btn_accept;
    public static TextView txt_client_mobile;
    public static TextView txt_eta;
    public static TextView txt_service_type;
    public static TextView txt_address;


    MapView mMapView;
    private GoogleMap googleMap;


    String lat;
    String lon;
    String services;
    String client_app_id;
    String client_mobile;
    String client_address = "";
    String request_id = "";//the id of the request send by the client

    Swipe_Button_View btn_reject;
    Swipe_Button_View btn_accept;
    CountDownTimer timer;


    LinearLayout linlay_explanation,
            linlay_accept_reject;
            FrameLayout linlay_map;

    MediaPlayer mp;

    String tag = "asrda";

    RadioButton rd_1, rd_2, rd_3, rd_4;
    View contextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_service_request_dialog);



        //wake the phone up, on the screen, when this activity starts

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);



        contextView = findViewById(R.id.context_view);

        linlay_accept_reject = (LinearLayout) findViewById(R.id.linlay_accept_reject);
        linlay_explanation = (LinearLayout) findViewById(R.id.linlay_explanation);
        linlay_map = (FrameLayout) findViewById(R.id.linlay_map);

        rd_1 = (RadioButton) findViewById(R.id.rd_1);
        rd_2 = (RadioButton) findViewById(R.id.rd_2);
        rd_3 = (RadioButton) findViewById(R.id.rd_3);
        rd_4 = (RadioButton) findViewById(R.id.rd_4);

        linlay_explanation.setVisibility(View.GONE);//hide this initially

        txt_count_down_timer = (TextView) findViewById(R.id.txt_count_down_timer);
        btn_accept = (Swipe_Button_View) findViewById(R.id.btn_accept);
        btn_reject = (Swipe_Button_View) findViewById(R.id.btn_reject);
        txt_client_mobile = (TextView) findViewById(R.id.txt_client_mobile);
        txt_eta = (TextView) findViewById(R.id.txt_eta);
        txt_service_type = (TextView) findViewById(R.id.txt_service_type);
        txt_address = (TextView) findViewById(R.id.txt_address);

        //get the data in the intent
        lat = getIntent().getStringExtra("lat");
        lon = getIntent().getStringExtra("lon");
        services = getIntent().getStringExtra("services");
        client_app_id = getIntent().getStringExtra("client_app_id");
        client_mobile = getIntent().getStringExtra("client_mobile");
        request_id = getIntent().getStringExtra("request_id");


        //init map
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();//display map imeediatly

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For dropping a marker at a point on the Map
                LatLng my_position = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
                googleMap.addMarker(new MarkerOptions().position(my_position).title(getString((R.string.your_client_is_here)))
                        .snippet(getString(R.string.call_or_text_your_client_to_begin_communication)));

                //set the map location
                CameraPosition cameraPosition = new CameraPosition.Builder().target(my_position).zoom(13).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        try {
            MapsInitializer.initialize(AnswerServiceRequestDialogActivity.this.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        //do reverse geo code to get the address of the client
        //this should be done async
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                Geocoder geocoder;
                List<Address> addresses;
                try {
                    geocoder = new Geocoder(AnswerServiceRequestDialogActivity.this, Locale.getDefault());
                    addresses = geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lon), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
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
                }

                try {
                    //this will now run on the ui thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txt_address.setText(client_address);//set the address
                        }
                    });
                }catch (Exception ex) {

                }
            }
        });


        //set the data here
        txt_client_mobile.setText(client_mobile);
        txt_service_type.setText(services);


        //start playing the sound clip for the ring tone
        try {
            mp = MediaPlayer.create(this, R.raw.notification_sound);
            mp.setLooping(true);
            mp.start();
        } catch (Exception ex) {
            Log.e(tag, "line 125 " + ex.getLocalizedMessage());
        }


        try {
            //start the count down timer shut off after 20 seconds the show the popup incase the person does not answser the call
            timer = new CountDownTimer(20000, 1000) {

                public void onTick(long millisUntilFinished) {
                    txt_count_down_timer.setText((millisUntilFinished / 1000) + "");//set the time remaining
                }

                public void onFinish() {
                    SwipeRejectRequest();//handle the rejection
                }
            }.start();
        } catch (Exception ex) {
            Log.e(tag, "line 177 " + ex.getLocalizedMessage());
        }//try catch


        //accept
        btn_accept.setOnSwipeCompleteListener_forward_reverse(new OnSwipeCompleteListener() {
            @Override
            public void onSwipe_Forward(Swipe_Button_View swipeView) {

            }

            @Override
            public void onSwipe_Reverse(Swipe_Button_View swipeView) {

                //create a job id
                String _job_id = UUID.randomUUID().toString();
                //stop the timer
                timer.cancel();
                mp.stop();// stop the media player
                //execute web service to accept tjhe request
                ProgressDialog pd = new ProgressDialog(AnswerServiceRequestDialogActivity.this);
                pd.setMessage(getString(R.string.please_wait));
                pd.setCanceledOnTouchOutside(false);
                pd.show();

                String sdata = "";
                try {
                    mArtisan artisan = app.db.mArtisanDao().get_artisan();
                    JSONObject jo = new JSONObject();
                    jo.put("request_id", request_id);
                    jo.put("client_app_id", client_app_id);
                    jo.put("artisan_app_id", artisan.app_id);
                    jo.put("_job_id", _job_id);//add the job id
                    jo.put("artisan_skills", artisan.skills);
                    sdata = jo.toString();
                } catch (Exception ex) {
                    Log.e(tag, "line 243 " + ex.getLocalizedMessage());
                } finally {
                }
                Ion.with(AnswerServiceRequestDialogActivity.this)
                        .load(globals.base_url + "/ArtisanAcceptRequest")
                        .setBodyParameter("data", sdata)
                        .asString()
                        .setCallback((e, result) -> {
                            Log.e(tag, "line 270 " + result);
                            pd.dismiss();
                            if (e == null) {
                                //no error
                                //now save this job in the jobs place and open its activity straight away
                                try {
                                    JSONObject json = new JSONObject(result);
                                    mJobs job = new mJobs();
                                    job._job_id = _job_id;//set the job id
                                    job.client_app_id = json.getString("client_app_id");
                                    job.client_mobile = json.getString("client_mobile");
                                    job.geoLocationLatitude = json.getString("latitude");
                                    job.geoLocationLongitude = json.getString("longitude");
                                    job.start_time = LocalDateTime.now().toString();
                                    job.description = json.getString("requested_skills");//any notes the artian may want to note but initially indicate the skills
                                   //save the job
                                    app.db.mJobsDao().insert_one(job);
                                    JobsFragment.refreshJobsAdapter();//display the job item in the jobs fragment
                                    //immediatly change my status so i cannot recieve any new jobs until this one i completed
                                    mArtisan artisan = app.db.mArtisanDao().get_artisan();
                                    artisan.busy = true;//update this
                                    app.db.mArtisanDao().update_one(artisan);
                                    //auto open the job
                                    Intent vj=  new Intent(AnswerServiceRequestDialogActivity.this,ViewJobActivity.class);
                                    vj.putExtra("_job_id",_job_id);
                                    startActivity(vj);
                                    finish();//end this activity

                                } catch (Exception ex) {
                                    Log.e(tag, "line 279 " + ex.getLocalizedMessage());
                                } finally {
                                    finish();//end this activity
                                }


                            } else {
                                Toast.makeText(AnswerServiceRequestDialogActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                            }

                        });

            }
        });


        //reject
        btn_reject.setOnSwipeCompleteListener_forward_reverse(new OnSwipeCompleteListener() {
            @Override
            public void onSwipe_Forward(Swipe_Button_View swipeView) {
                SwipeRejectRequest();//handle the rejection
            }

            @Override
            public void onSwipe_Reverse(Swipe_Button_View swipeView) {

            }
        });


    }//oncreate


    //ovverride the back pressed button
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }//.onBackpressed

    //submit the reason for not answering the request
    public void SubmitReasonForNotAnswering(View v) {
        mArtisan artisan = app.db.mArtisanDao().get_artisan();

        String response = "";
        if (rd_1.isChecked()) response = "i_am_busy";
        if (rd_2.isChecked()) response = "i_am_slow_to_answer";
        if (rd_3.isChecked()) response = "i_cannot_do_this_job_right_now";
        if (rd_4.isChecked()) response = "the_place_is_too_far";

        String data = "";
        try {
            JSONObject json = new JSONObject();
            json.put("request_id", request_id);
            json.put("response", response);
            json.put("artisan_app_id", artisan.app_id);
            data = json.toString();
        } catch (Exception ex) {
            Snackbar.make(contextView, getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
            Log.e(tag, "line 249 " + ex.getLocalizedMessage());
            return;
        }

        ProgressDialog pd = new ProgressDialog(AnswerServiceRequestDialogActivity.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        Ion.with(AnswerServiceRequestDialogActivity.this)
                .load(globals.base_url + "/SetReasonForNotResponding")
                .setBodyParameter("data", data)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        pd.dismiss();
                        if (e != null) {
                            Snackbar.make(contextView, R.string.error_occured, Snackbar.LENGTH_SHORT).show();
                            Log.e(tag, e + " line 269");
                        } else {
                            Snackbar.make(contextView, R.string.error_occured, Snackbar.LENGTH_SHORT).show();
                            finish();//close this activity now since we have recorded this at the server
                        }
                    }
                });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        //Screen On
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }


    private void clearFlags() {
        //Don't forget to clear the flags at some point in time.
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearFlags();
    }

    //handle reject by swipping
    public void SwipeRejectRequest() {
        //show the panel to answer for why you missed the call
        linlay_explanation.setVisibility(View.VISIBLE);
        linlay_accept_reject.setVisibility(View.GONE);//hide accept/reject
        linlay_map.setVisibility(View.GONE);//hide map too
        mp.stop();//stop the alarm

    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        if (mp != null && mp.isPlaying()) {
            mp.stop();//stop playing the music
        }
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}//activity
