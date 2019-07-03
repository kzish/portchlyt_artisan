package MainActivityTabs;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.porchlyt_artisan.R;
import com.example.porchlyt_artisan.app;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.theartofdev.edmodo.cropper.CropImage;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import globals.*;

import javax.annotation.Nullable;


import globals.MyMqtt;
import io.realm.Realm;
import models.mArtisan.mArtisan;
import models.mArtisanSearch;


//todo ensure google play services is up to date and working
public class ProfileFragment extends Fragment {

    TextView txt_mobile;
    EditText txt_name;
    EditText txt_email;
    TextView txt_location;
    EditText txt_hourly_rate;
    Switch switchAvailable;
    TextView txt_skills;
    LinearLayout content_view;
    CircleImageView img_profile;
    public static Context ctx;

    String my_address = "";

    Looper _looper;


    //
    ProgressBar img_progress_bar;
    static RatingBar ratingBar;

    ProgressDialog pd;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    public static ArrayList<String> jobs;//this is the list of jobs to be added by the AddJobActivity
    ImageView img_search; //center image
    public static List<mArtisanSearch> searches;///these are the live searches

    private FusedLocationProviderClient mFusedLocationClient;
    final int LOCATION_REQUEST_CODE = 1;


    //
    private static final String IMAGE_DIRECTORY = "/_pictures";
    private int GALLERY = 1, CAMERA = 2;


    double wayLatitude;
    double wayLongitude;

    String tag = "Profile Fragment";


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
        jobs = new ArrayList<String>();
        searches = new ArrayList<mArtisanSearch>();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        content_view = (LinearLayout) view.findViewById(R.id.content_view);
        switchAvailable = (Switch) view.findViewById(R.id.switchAvailable);
        pd = new ProgressDialog(getContext());

        //
        txt_mobile = (TextView) view.findViewById(R.id.txt_mobile);
        txt_name = (EditText) view.findViewById(R.id.txt_name);
        txt_email = (EditText) view.findViewById(R.id.txt_email);
        txt_location = (TextView) view.findViewById(R.id.txt_location);//this will be updated automatically
        txt_hourly_rate = (EditText) view.findViewById(R.id.txt_hourly_rate);
        txt_skills = (TextView) view.findViewById(R.id.txt_skills);
        img_profile = (CircleImageView) view.findViewById(R.id.img_profile);
        img_progress_bar = (ProgressBar) view.findViewById(R.id.img_progress_bar);
        ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);


        Realm db = globals.getDB();
        mArtisan m = db.where(mArtisan.class).findFirst();
        //
        switchAvailable.setChecked(m.on_duty);
        txt_mobile.setText(m.mobile);
        txt_name.setText(m.name);
        txt_email.setText(m.email);
        txt_skills.setText(TextUtils.join(" ", m.skills));
        txt_hourly_rate.setText(m.hourlyRate + "");
        //
        db.close();

        //
        set_profile_pic();
        //
        get_my_rating();

        //
        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //check permission first
                if (
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED
                                ||
                                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED
                                ||
                                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                Realm db = globals.getDB();
                db.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mArtisan m = db.where(mArtisan.class).findFirst();
                        m.email = txt_email.getText().toString();
                        m.synced = false;
                    }
                });
                db.close();

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
                Realm db = globals.getDB();
                db.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mArtisan m = db.where(mArtisan.class).findFirst();
                        m.name = txt_name.getText().toString();
                        m.synced = false;
                    }
                });
                db.close();

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
                Realm db = globals.getDB();
                db.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        try {
                            mArtisan m = db.where(mArtisan.class).findFirst();
                            m.hourlyRate = Double.parseDouble(txt_hourly_rate.getText().toString());
                            m.synced = false;
                        } catch (Exception ex) {
                            db.close();//
                        }
                    }
                });
                db.close();

            }
        });

        //
        //set unset the availablility
        switchAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                String sdata = "";//data to be sent to the server
                Realm db = globals.getDB();
                try {
                    mArtisan m = db.where(mArtisan.class).findFirst();
                    JSONObject json = new JSONObject();
                    json.put("artisan_app_id", m.app_id);
                    json.put("available", switchAvailable.isChecked());
                    sdata = json.toString();
                } catch (Exception ex) {
                    Log.e("d", ex.getMessage());
                } finally {
                    db.close();
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
                                            Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                                            return;
                                        } else {
                                            //notify the user
                                            if (!switchAvailable.isChecked()) {
                                                Toast.makeText(getContext(), getString(R.string.you_will_not_recieve_any_job_notifications), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getContext(), getString(R.string.you_will_recieve_job_notifications), Toast.LENGTH_SHORT).show();
                                            }


                                            //now update the local copy
                                            Realm db = globals.getDB();
                                            db.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    mArtisan m = db.where(mArtisan.class).findFirst();
                                                    m.on_duty = switchAvailable.isChecked();
                                                }
                                            });
                                            db.close();
                                        }

                                    } catch (Exception ex) {
                                        Log.e("d", ex.getMessage());
                                    }
                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
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
        locationRequest.setInterval(1 * 1000);
        locationRequest.setFastestInterval(1 * 1000);

        //attempt getting the last known location
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    wayLatitude = location.getLatitude();
                    wayLongitude = location.getLongitude();
                    set_my_address(wayLatitude, wayLongitude);
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
                if (ContextCompat.checkSelfPermission(getActivity(),
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


        //check location settings first time
        if (ContextCompat.checkSelfPermission(getActivity(),
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
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        //
                        set_my_address(wayLatitude, wayLongitude);
                        update_my_location(wayLatitude, wayLongitude);//send my location to the server
                        update_my_details();
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
        Realm db = globals.getDB();
        try {
            mArtisan m = db.where(mArtisan.class).findFirst();
            ratingBar.setRating(m.getRating());

        } catch (Exception ex) {
            //
        } finally {
            db.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        //this is for fine location
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
                init_location_listner();
            } else {
                //
                //inform artisan why we need his location
                //permission denied
                new AlertDialog.Builder(getActivity())
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

        Realm db = globals.getDB();
        mArtisan m = db.where(mArtisan.class).findFirst();
        if (!m.synced) {
            String sdata = "";
            try {
                JSONObject json = new JSONObject();
                json.put("artisan_app_id", m.app_id);
                json.put("name", m.name);
                json.put("email", m.email);
                json.put("hourlyRate", m.hourlyRate);

                sdata = json.toString();
                Log.e("d", sdata);
                //now update the server
                Ion.with(getContext())
                        .load(globals.base_url + "/update_artisan_details")
                        .setBodyParameter("data", sdata)
                        .asString()
                        .setCallback((e, result) -> {
                            Log.e("updateMydetails()", result + "");
                            if (e == null) {
                                try {
                                    String res = new JSONObject(result).getString("res");
                                    db.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            if (res.equals("ok")) {
                                                m.synced = true;//indicate that no more updates needed
                                            } else {
                                                Snackbar.make(content_view, getString(R.string.error_updating_details), Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } catch (Exception ex) {

                                }//try catch
                            }//.if e==null

                        });
            } catch (Exception ex) {
                Log.e("d", ex.getMessage());
            } finally {
                db.close();
            }
        }//if !m.synced


    }//.update my details

    //set profile picture onto the imageView
    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getActivity());
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
        if (resultCode == getActivity().RESULT_CANCELED) {
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
            String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), thumbnail, "Title", null);
            Uri imageUri = Uri.parse(path);
            //crop image
            CropImage.activity(imageUri)
                    .start(getContext(), this);

        }


        //crop  and save cropped image
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == getActivity().RESULT_OK) {

                //handle cropped image
                try {
                    Uri resultUri = result.getUri();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
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
            MediaScannerConnection.scanFile(getActivity(),
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

        Realm db = globals.getDB();
        mArtisan m = db.where(mArtisan.class).findFirst();

        img_progress_bar.setVisibility(View.VISIBLE);
        Ion.with(getContext())
                .load(globals.base_url + "/remove_profile_picture")
                .setBodyParameter("artisan_app_id", m.app_id)
                .asString()
                .setCallback((e, result) -> {
                    img_progress_bar.setVisibility(View.INVISIBLE);
                    if (e == null) {
                        if (result.contains("ok")) {
                            db.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    try {
                                        File file = new File(Uri.parse(m.image).getPath());
                                        file.delete();
                                        m.image = null;
                                    } catch (Exception ex) {
                                        //
                                    }
                                }
                            });
                            db.close();
                            img_profile.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_worker));
                        } else {
                            db.close();
                            Snackbar.make(content_view, getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        db.close();
                        Snackbar.make(content_view, getString(R.string.error_occured), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }


    //upload to server and save into the database
    private void upload_profile_picture(Uri path) {


        String artisan_app_id = "";
        Realm db = globals.getDB();
        mArtisan m = db.where(mArtisan.class).findFirst();
        artisan_app_id = m.app_id;
        db.close();

        img_progress_bar.setVisibility(View.VISIBLE);
        Ion.with(getContext())
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
                            Realm db_ = globals.getDB();
                            mArtisan m_ = db.where(mArtisan.class).findFirst();
                            db_.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    m_.image = path.toString();//now update the artisan class in db
                                }
                            });
                            db_.close();
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
        Realm db = globals.getDB();
        mArtisan m = db.where(mArtisan.class).findFirst();
        if (m.image != null) {
            //set the profile if one exists
            try {
                Log.e("pp", m.image);
                Uri contentURI = Uri.parse(m.image);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), contentURI);
                img_profile.setImageBitmap(bitmap);
            } catch (Exception ex) {
                Log.e("pp", ex + " line 178");
            } finally {
                db.close();
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
                geocoder = new Geocoder(getActivity(), Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(wayLatitude, wayLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
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

                } catch (Exception ex) {
                    Log.e(tag, "line 456 " + ex.getMessage());
                }

                //this will now run on the ui thread
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txt_location.setText(my_address);//set the address
                    }
                });
            }

        });
    }//.set_address


    //update my location
    private void update_my_location(double lat, double lng) {
        Realm db = globals.getDB();

        try {
            //
            mArtisan m = db.where(mArtisan.class).findFirst();

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
            db.close();
        }
    }


}//.class
