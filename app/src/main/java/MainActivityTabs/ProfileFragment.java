package MainActivityTabs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.jackandphantom.circularimageview.CircleImage;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.rilixtech.CountryCodePicker;
import com.sirachlabs.porchlyt_artisan.R;
import com.sirachlabs.porchlyt_artisan.ViewMoreEarningsActivity;
import com.sirachlabs.porchlyt_artisan.app;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import globals.MyMqtt;
import globals.globals;
import models.Account_status;
import models.appSettings;
import models.mArtisan.mArtisan;
import models.mArtisan.mLocation;
import models.mArtisanSearch;
import models.mBank;
import models.mJobs.JobStatus;


//todo ensure google play services is up to date and working
public class ProfileFragment extends Fragment {

    static RelativeLayout account_blocked_remit_cash;
    static RelativeLayout account_blocked;
    static RelativeLayout account_active;


    EditText txt_mobile;
    TextView txt_view_more_earnings;
    EditText txt_name;
    EditText txt_email;
    EditText txt_bank_account_number;
    Spinner spinner_bank_name;

    static TextView lbl_completed_jobs;
    static TextView lbl_cancelled_jobs;
    static TextView lbl_disputed_jobs;

    TextView txt_location;
    EditText txt_hourly_rate;
    Switch switchAvailable;
    static TextView txt_skills;
    LinearLayout content_view;
    CircleImage img_profile;
    public static Context ctx;


    String my_address = "";

    Looper _looper;


    //
    ProgressBar img_progress_bar;
    static RatingBar ratingBar;

    ProgressDialog pd;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    BootstrapButton btn_save_details;
    static TextView lbl_earnings;


    public static ArrayList<String> jobs;//this is the list of jobs to be added by the AddJobActivity
    ImageView img_search; //center image
    public static List<mArtisanSearch> searches;///these are the live searches

    private FusedLocationProviderClient mFusedLocationClient;
    final int LOCATION_REQUEST_CODE = 1;

    public static SlidingUpPanelLayout sliding_layout;


    //
    private static final String IMAGE_DIRECTORY = "/_pictures";
    private int GALLERY = 1, CAMERA = 2;


    String tag = "Profile Fragment";

    private static Activity activity;


    public ProfileFragment() {
    }

    public static ProfileFragment newInstance(String param1, String param2) {

        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getActivity();
        activity = getActivity();
        jobs = new ArrayList<String>();
        searches = new ArrayList<mArtisanSearch>();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    public static void check_account_status() {
        //if active, blocked, remit?
        appSettings aps = app.db.appSettingsDao().get_app_settings();
        if (aps.account_status.equals(Account_status.active.toString())) {
            account_active.setVisibility(View.VISIBLE);
            account_blocked.setVisibility(View.GONE);
            account_blocked_remit_cash.setVisibility(View.GONE);
        }
        if (aps.account_status.equals(Account_status.blocked.toString())) {
            account_active.setVisibility(View.GONE);
            account_blocked.setVisibility(View.VISIBLE);
            account_blocked_remit_cash.setVisibility(View.GONE);
        }
        if (aps.account_status.equals(Account_status.must_remit_cash.toString())) {
            account_active.setVisibility(View.GONE);
            account_blocked.setVisibility(View.GONE);
            account_blocked_remit_cash.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        content_view = (LinearLayout) view.findViewById(R.id.content_view);
        switchAvailable = (Switch) view.findViewById(R.id.switchAvailable);
        pd = new ProgressDialog(getContext());

        //
        txt_mobile = (EditText) view.findViewById(R.id.txt_mobile);
        lbl_earnings = (TextView) view.findViewById(R.id.lbl_earnings);
        txt_name = (EditText) view.findViewById(R.id.txt_name);
        txt_email = (EditText) view.findViewById(R.id.txt_email);
        txt_bank_account_number = (EditText) view.findViewById(R.id.txt_bank_account_number);
        spinner_bank_name = (Spinner) view.findViewById(R.id.spinner_bank_name);
        txt_location = (TextView) view.findViewById(R.id.txt_location);//this will be updated automatically
        txt_view_more_earnings = (TextView) view.findViewById(R.id.txt_view_more_earnings);
        txt_hourly_rate = (EditText) view.findViewById(R.id.txt_hourly_rate);
        txt_skills = (TextView) view.findViewById(R.id.txt_skills);
        img_profile = (CircleImage) view.findViewById(R.id.img_profile);
        img_progress_bar = (ProgressBar) view.findViewById(R.id.img_progress_bar);
        ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        btn_save_details = (BootstrapButton) view.findViewById(R.id.btn_save_details);
        btn_save_details.setVisibility(View.GONE);

        sliding_layout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);

        account_blocked_remit_cash = (RelativeLayout) view.findViewById(R.id.account_blocked_remit_cash);
        account_blocked = (RelativeLayout) view.findViewById(R.id.account_blocked);
        account_active = (RelativeLayout) view.findViewById(R.id.account_active);
        check_account_status();

        lbl_completed_jobs = (TextView) view.findViewById(R.id.lbl_completed_jobs);
        lbl_cancelled_jobs = (TextView) view.findViewById(R.id.lbl_cancelled_jobs);
        lbl_disputed_jobs = (TextView) view.findViewById(R.id.lbl_disputed_jobs);
        //load the number of jobs completed, cancelled, and disputed
        load_jobs();

        populate_spinner_banks();

        //view more
        account_active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent vm = new Intent(activity, ViewMoreEarningsActivity.class);
                vm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(vm);
            }
        });
//also view more
        txt_view_more_earnings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent vm = new Intent(activity, ViewMoreEarningsActivity.class);
                vm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(vm);
            }
        });

        //save details on clicking the button
        btn_save_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_my_details();
            }
        });

        mArtisan artisan = app.db.mArtisanDao().get_artisan();

        //get artisan details first time load
        switchAvailable.setChecked(artisan.on_duty);
        txt_mobile.setText(artisan.mobile);
        txt_name.setText(artisan.name);
        txt_email.setText(artisan.email);
        txt_bank_account_number.setText(artisan.account_number);

        txt_skills.setText(artisan.skills);
        txt_hourly_rate.setText(artisan.hourlyRate + "");
        lbl_earnings.setText(globals.formatCurrency(artisan.earnings_since_last_disbursement));

        if (!artisan.synced) {
            btn_save_details.setVisibility(View.VISIBLE);
        }

        //
        set_profile_pic();
        //
        get_my_rating();

        //set the last known location if it is there
        mLocation loc = app.db.LocationDao().get_location();
        if (loc != null) {
            txt_location.setText(loc.last_known_location);//set the address
        }

        //
        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //check permission first
                if (
                        ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED
                                ||
                                ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED
                                ||
                                ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED

                ) {
                    // Permission is not granted
                    requestPermissions(new String[]
                            {
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }, 2);
                } else {
                    //permission granted
                    showPictureDialog();

                }


            }
        });


        //
        txt_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btn_save_details.setVisibility(View.VISIBLE);
            }
        });

        //
        txt_mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btn_save_details.setVisibility(View.VISIBLE);
            }
        });


        //
        txt_bank_account_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btn_save_details.setVisibility(View.VISIBLE);
            }
        });


        txt_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btn_save_details.setVisibility(View.VISIBLE);
            }
        });


        txt_hourly_rate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btn_save_details.setVisibility(View.VISIBLE);
            }
        });

        //
        //set unset the availablility
        switchAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                String sdata = "";//data to be sent to the server
                try {
                    mArtisan artisan = app.db.mArtisanDao().get_artisan();
                    JSONObject json = new JSONObject();
                    json.put("artisan_app_id", artisan.app_id);
                    json.put("available", switchAvailable.isChecked());
                    sdata = json.toString();
                } catch (Exception ex) {
                    Log.e("d", ex.getMessage());
                } finally {
                }
                pd.setMessage(getString(R.string.please_wait));
                pd.show();
                //this one needs to be changed at the server directly
                Ion.with(getContext())
                        .load(globals.base_url + "/artisanSetUnSetAvailability")
                        .setBodyParameter("data", sdata)
                        .asString()
                        .withResponse()
                        .setCallback(new FutureCallback<Response<String>>() {
                            @Override
                            public void onCompleted(Exception e, Response<String> result) {
                                pd.hide();
                                if (e == null) {
                                    try {
                                        String res = new JSONObject(result.getResult()).getString("res");
                                        String msg = new JSONObject(result.getResult()).getString("msg");

                                        if (res.equals("err")) {
                                            Toast.makeText(ctx, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                                            return;
                                        } else {
                                            //notify the user
                                            if (!switchAvailable.isChecked()) {
                                                Toast.makeText(getContext(), getString(R.string.you_will_not_recieve_any_job_notifications), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getContext(), getString(R.string.you_will_recieve_job_notifications), Toast.LENGTH_SHORT).show();
                                            }


                                            //now update the local copy
                                            artisan.on_duty = switchAvailable.isChecked();
                                            app.db.mArtisanDao().update_one(artisan);
                                        }

                                    } catch (
                                            Exception ex) {
                                        Log.e("d", ex.getMessage());
                                    }
                                } else {
                                    Toast.makeText(ctx, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


            }
        });


        //init location listners
        //todo change the request updates time to something more than 1 second
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(app.ctx);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(10 * 1000);

        //attempt getting the last known location
        mFusedLocationClient.getLastLocation().

                addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {

                            mLocation loc = app.db.LocationDao().get_location();
                            loc.lat = location.getLatitude();
                            loc.lng = location.getLongitude();
                            set_my_address(loc.lat, loc.lng);
                        }
                    }
                });

        //
        init_location_listner();

        txt_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //

                //check location permision
                if (ContextCompat.checkSelfPermission(ctx,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    Log.e("l", "requested permission ");
                } else {
                    //permission granted
                    init_location_listner();
                }


            }
        });
        spinner_bank_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                btn_save_details.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //check location settings first time
        if (ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Log.e("l", "requested permission ");
        } else {
            //permission granted
            init_location_listner();
        }

        return view;
    }


    //
    @SuppressLint("MissingPermission")
    private void init_location_listner() {
        //
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    //Log.e("l", "location result=null");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {

                        mLocation loc = app.db.LocationDao().get_location();

                        loc.lat = location.getLatitude();
                        loc.lng = location.getLongitude();
                        //
                        set_my_address(loc.lat, loc.lng);
                        update_my_location(loc.lat, loc.lng);//send my location to the server
                        //Log.e("l", wayLatitude + " " + wayLongitude);
                    }
                }
            }
        };//location callback

        //
        if (_looper == null) {
            _looper = Looper.myLooper();
        }
        //
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, _looper);

    }


    public static void get_my_rating() {
        try {
            mArtisan artisan = app.db.mArtisanDao().get_artisan();
            ratingBar.setRating(artisan.get_rating());
        } catch (Exception ex) {
            //
        } finally {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        //this is for fine location
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
                init_location_listner();
            } else {
                //
                //inform artisan why we need his location
                //permission denied
                new AlertDialog.Builder(ctx)
                        .setMessage(getString(R.string.we_need_your_location_to_provide_you_this_service))
                        .setTitle(getString(R.string.information))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing since no permission was granted
                            }
                        })
                        //.setNegativeButton(android.R.string.cancel, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }


        //permissions for profile pic
        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
                showPictureDialog();
            } else {
                //
            }
        }


    }//.onrequest results


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);

    }

    //tbis method will update the server, periodically my details incase any hve changed
    public void update_my_details() {

        if (txt_mobile.getText().toString().equals("")) {
            txt_mobile.setError(getString(R.string.cannot_be_blank));
            return;
        }

        if (txt_name.getText().toString().equals("")) {
            txt_name.setError(getString(R.string.cannot_be_blank));
            return;
        }

        /*if (txt_email.getText().equals("")) {
            txt_email.setError(getString(R.string.cannot_be_blank));
            return;
        }*/

        if (txt_bank_account_number.getText().toString().equals("")) {
            txt_bank_account_number.setError(getString(R.string.cannot_be_blank));
            return;
        }

        //effect changes
        mArtisan artisan = app.db.mArtisanDao().get_artisan();
        artisan.hourlyRate = Double.parseDouble(txt_hourly_rate.getText().toString());
        artisan.mobile = txt_mobile.getText().toString();
        artisan.name = txt_name.getText().toString();
        artisan.email = txt_email.getText().toString();
        artisan.account_number = txt_bank_account_number.getText().toString();
        artisan.account_bank = spinner_bank_name.getSelectedItem().toString();
        artisan.synced = false;

        //
        ProgressDialog pd = new ProgressDialog(ctx);
        pd.setMessage(getString(R.string.please_wait));
        pd.setCanceledOnTouchOutside(false);
        pd.show();


        if (!artisan.synced) {
            String sdata = "";
            try {
                JSONObject json = new JSONObject();
                json.put("artisan_app_id", artisan.app_id);
                json.put("name", artisan.name);
                json.put("email", artisan.email);
                json.put("hourlyRate", artisan.hourlyRate);
                json.put("account_bank", artisan.account_bank);
                json.put("account_number", artisan.account_number);
                json.put("mobile", artisan.mobile);

                sdata = json.toString();
                Log.e("d", sdata);
                //now update the server
                Ion.with(getContext())
                        .load(globals.base_url + "/update_artisan_details")
                        .setBodyParameter("data", sdata)
                        .asString()
                        .setCallback((e, result) -> {
                            pd.dismiss();
                            Log.e("updateMydetails()", result + "");
                            if (e == null) {
                                try {
                                    String res = new JSONObject(result).getString("res");
                                    String msg = new JSONObject(result).getString("msg");

                                    if (res.equals("ok")) {
                                        artisan.synced = true;//indicate that no more updates needed
                                        btn_save_details.setVisibility(View.GONE);
                                        Snackbar.make(content_view, getString(R.string.saved), Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), msg + " ", Toast.LENGTH_LONG).show();
                                        Snackbar.make(content_view, getString(R.string.error_updating_details), Snackbar.LENGTH_SHORT).show();
                                    }
                                    //update the artisan
                                    app.db.mArtisanDao().update_one(artisan);
                                } catch (Exception ex) {
                                    Log.e(tag, ex.getMessage());
                                }//try catch
                            }//.if e==null

                        });
            } catch (Exception ex) {
                Log.e(tag, ex.getMessage());
            } finally {
            }
        }//if !m.synced


    }//.update my details

    //set profile picture onto the imageView
    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(ctx);
        pictureDialog.setTitle(getString(R.string.change_profile_picture));
        String[] pictureDialogItems = {
                getString(R.string.select_photo_from_gallery),
                getString(R.string.capture_photo_from_camera),
                getString(R.string.clear_photo)
        };

        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                            case 2:
                                clearPhoto();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    //select a profile pic from the gallary
    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    //take a profile pic from the camera
    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }


    //
//handle data from camera or from gallary
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == activity.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();

                //crop image
                CropImage.activity(contentURI)
                        .start(getContext(), this);


            }

        } else if (requestCode == CAMERA) {

            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            //get uri from bitmap
            String path = MediaStore.Images.Media.insertImage(ctx.getContentResolver(), thumbnail, "Title", null);
            Uri imageUri = Uri.parse(path);
            //crop image
            CropImage.activity(imageUri)
                    .start(getContext(), this);

        }


        //crop  and save cropped image
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == activity.RESULT_OK) {

                //handle cropped image
                try {
                    Uri resultUri = result.getUri();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), resultUri);
                    Uri path = saveImage(bitmap);//saved to file
                    //upload save and display picture
                    upload_profile_picture(path);
                } catch (Exception ex) {
                    Log.e(tag, ex.getMessage() + " line 669");
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();//log error
                Log.e(tag, error + " line 673");
            }
        }


    }//onActivityResult

    //save image to the file
    public Uri saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, "profile_picture" + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(ctx,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            //
            //return f.getAbsolutePath();

            //crop this image just before saving it
            Uri imageUri = Uri.fromFile(new File(f.getAbsolutePath()));

            return imageUri;
        } catch (IOException e1) {
            e1.printStackTrace();
            //Toast.makeText(getActivity(),"line 698 "+e1,Toast.LENGTH_SHORT).show();
        }
        return null;
    }


    //remove photo from img_profile and database and delete from file
    private void clearPhoto() {

        mArtisan artisan = app.db.mArtisanDao().get_artisan();

        img_progress_bar.setVisibility(View.VISIBLE);
        Ion.with(ctx)
                .load(globals.base_url + "/remove_profile_picture")
                .setBodyParameter("artisan_app_id", artisan.app_id)
                .asString()
                .setCallback((e, result) -> {
                    img_progress_bar.setVisibility(View.INVISIBLE);
                    if (e == null) {
                        if (result.contains("ok")) {
                            try {
                                File file = new File(Uri.parse(artisan.image).getPath());
                                file.delete();
                                artisan.image = null;
                            } catch (Exception ex) {
                                //
                            }
                        }
                        img_profile.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_worker));
                    } else {
                        Snackbar.make(content_view, getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    //upload to server and save into the database
    private void upload_profile_picture(Uri path) {


        String artisan_app_id = "";
        mArtisan artisan = app.db.mArtisanDao().get_artisan();
        artisan_app_id = artisan.app_id;

        img_progress_bar.setVisibility(View.VISIBLE);
        Ion.with(ctx)
                .load(globals.base_url + "/upload_image_from_mobile")
                //.uploadProgressBar(img_progress_bar)
                .setMultipartParameter("artisan_app_id", artisan_app_id)
                .setMultipartFile("file", "image/jpeg", new File(path.getPath()))
                .asString()
                .setCallback((e, result) -> {
                    img_progress_bar.setVisibility(View.INVISIBLE);
                    if (e != null)//error occured in uploading
                    {
                        Snackbar.make(content_view, getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                        Log.e("upload_profile_picture", e + "");
                    } else {
                        if (result.contains("ok"))//uploaded successfully
                        {
                            //save the image into the database
                            artisan.image = path.toString();//now update the artisan class in db
                            app.db.mArtisanDao().update_one(artisan);
                            set_profile_pic();//set the profile pic now
                        } else//error occured in server
                        {
                            Snackbar.make(content_view, getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                        }
                    }


                });

    }


    //set profile pic from db
    private void set_profile_pic() {
        mArtisan artisan = app.db.mArtisanDao().get_artisan();
        if (artisan.image != null) {
            //set the profile if one exists
            try {
                Log.e(tag, artisan.image);
                Uri contentURI = Uri.parse(artisan.image);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), contentURI);
                img_profile.setImageBitmap(bitmap);
            } catch (Exception ex) {
                Log.e(tag, ex + " line 178");
            } finally {
            }
        }
    }

    //set my address on the txt_address
    private void set_my_address(double lat, double lng) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder;
                List<Address> addresses;
                try {


                    my_address = getString(R.string.unknown_location);
                    geocoder = new Geocoder(ctx, Locale.getDefault());
                    addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
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
                    my_address = country + ", " + city + ", " + state + ", " + knownName;
                    if (my_address.equals("") || my_address.equals(" ") || TextUtils.isEmpty(my_address))
                        my_address = getString(R.string.unknown_location);

                    //update my last location
                    mLocation loc = app.db.LocationDao().get_location();
                    loc.last_known_location = my_address;
                    app.db.LocationDao().update_one(loc);

                } catch (Exception ex) {
                    Log.e(tag, "line 456 " + ex.getMessage());
                }

                try {
                    //this will now run on the ui thread
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLocation loc = app.db.LocationDao().get_location();
                            txt_location.setText(loc.last_known_location);//set the address
                            txt_location.setSelected(true);
                        }
                    });
                } catch (Exception ex) {

                }
            }

        });
    }//.set_address


    //update my location
    private void update_my_location(double lat, double lng) {
        try {
            //
            mArtisan m = app.db.mArtisanDao().get_artisan();
            //
            JSONObject json = new JSONObject();
            json.put("lat", lat);
            json.put("lng", lng);
            json.put("artisan_app_id", m.app_id);
            json.put("msg_type", "artisan_location_update");

            MyMqtt.publishStringMessage(json.toString(), MyMqtt.mqtt_server);


        } catch (Exception ex) {
            Log.e(tag, "line 936 " + ex.getMessage());
        } finally {
        }
    }


    public static void set_artisan_skills() {
        mArtisan artisan = app.db.mArtisanDao().get_artisan();
        txt_skills.setText(artisan.skills);
    }

    private int get_number_of_completed_jobs() {
        int num_jobs = (int) app.db.mJobsDao().get_jobs_with_status(JobStatus.closed.toString()).size();
        return num_jobs;
    }


    public static void set_my_earning() {
        mArtisan artisan = app.db.mArtisanDao().get_artisan();
        lbl_earnings.setText(globals.formatCurrency(artisan.earnings_since_last_disbursement));
    }


    private void populate_spinner_banks() {
        InputStream is = getResources().openRawResource(R.raw.list_of_banks_in_nigeria);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            String jsonString = writer.toString();
            JSONObject json = new JSONObject(jsonString);
            JSONArray json_a = json.getJSONArray("Banks");

            ArrayList<mBank> banks = new ArrayList<>();
            for (int i = 0; i < json_a.length(); i++) {
                JSONObject j = json_a.getJSONObject(i);
                mBank bank = new Gson().fromJson(j.toString(), mBank.class);
                banks.add(bank);

            }

            List<String> spinnerArray = new ArrayList<String>();
            int index = 0;
            int selected_index = 0;
            mArtisan artisan = app.db.mArtisanDao().get_artisan();
            for (mBank bank : banks) {

                spinnerArray.add(bank.Name);
                if (artisan.account_bank != null && artisan.account_bank.equals(bank.Name)) {
                    selected_index = index;
                }
                index++;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    getActivity(), android.R.layout.simple_spinner_item, spinnerArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_bank_name.setAdapter(adapter);
            spinner_bank_name.setSelection(selected_index, true);


        } catch (Exception ex) {
            Toast.makeText(getActivity(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            Log.e(tag, ex.getMessage());
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
        }


    }


    //load the number of jobs for the artisan
    public static void load_jobs() {

        long num_completed_jobs = 0;
        long num_cancelled_jobs = 0;
        long num_disputed_jobs = 0;

        num_completed_jobs = app.db.mJobsDao().get_jobs_with_status(JobStatus.closed.toString()).size();
        num_cancelled_jobs = app.db.mJobsDao().get_jobs_with_status(JobStatus.cancelled.toString()).size();
        num_disputed_jobs = app.db.mJobsDao().get_jobs_with_status(JobStatus.disputed.toString()).size();


        lbl_completed_jobs.setText(globals.numberCalculation(num_completed_jobs));
        lbl_cancelled_jobs.setText(globals.numberCalculation(num_cancelled_jobs));
        lbl_disputed_jobs.setText(globals.numberCalculation(num_disputed_jobs));

    }


}//.class
