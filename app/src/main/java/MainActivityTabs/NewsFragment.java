package MainActivityTabs;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.porchlyt_artisan.R;
import com.example.porchlyt_artisan.app;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import adapters.mExtra_Jobs_Adapter;
import adapters.mNotification_Adapter;

import globals.*;
import io.realm.Realm;
import models.mArtisan.mArtisan;
import models.mArtisanServiceRequest;

public class NewsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    static public RecyclerView list_notifications;
    static RecyclerView list_extra_jobs ;
    public static LinearLayout lin_notifications;
    public static LinearLayout lin_extra_jobs;

    static String tag = "NewsFragment";
    static Activity activity;
    private static SwipeRefreshLayout swipeContainer;


    public NewsFragment() {
    }

    public static NewsFragment newInstance(String param1, String param2) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        //
        activity = getActivity();
        swipeContainer = (SwipeRefreshLayout)view. findViewById(R.id.swipeContainer);

        //
        list_notifications = (RecyclerView) view.findViewById(R.id.list_notifications);
        list_extra_jobs = (RecyclerView) view.findViewById(R.id.list_extra_jobs);

        //
        lin_notifications = (LinearLayout)view.findViewById(R.id.lin_notifications);
        lin_extra_jobs = (LinearLayout)view.findViewById(R.id.lin_extra_jobs);

        //
        set_notification_adapter();
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



        return view;
    }

    public void onButtonPressed(Uri uri) {
    }

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


    public static void set_notification_adapter() {
        try {
            mNotification_Adapter notification_adapter = new mNotification_Adapter();
            notification_adapter.setHasStableIds(true);
            LinearLayoutManager lm = new LinearLayoutManager(app.ctx, LinearLayoutManager.HORIZONTAL, false);
            list_notifications.setLayoutManager(lm);
            list_notifications.setAdapter(notification_adapter);
        } catch (Exception ex) {
            Log.e(tag, ex.getLocalizedMessage());
        }
    }


    public static void fetch_extra_jobs() {

        List<mArtisanServiceRequest>jobs=new ArrayList<>();

        mExtra_Jobs_Adapter extra_jobs_adapter=new mExtra_Jobs_Adapter(activity);
        extra_jobs_adapter.setHasStableIds(true);

        LinearLayoutManager lm =  new LinearLayoutManager(app.ctx,LinearLayoutManager.VERTICAL,false);
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
                                        mArtisanServiceRequest service_request =  new Gson().fromJson(json.toString(),mArtisanServiceRequest.class);
                                        jobs.add(service_request);
                                    }
                                    extra_jobs_adapter.jobs=jobs;
                                    list_extra_jobs.setAdapter(extra_jobs_adapter);



                                } catch (Exception ex) {
                                    Log.e(tag, "line 118 " + ex.getMessage());
                                    //Toast.makeText(app.ctx, app.ctx.getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                                }
                                finally {
                                    if(jobs.size()<=0)
                                    {
                                        NewsFragment.lin_extra_jobs.setVisibility(View.GONE);//hide
                                    }
                                    else
                                    {
                                        NewsFragment.lin_extra_jobs.setVisibility(View.VISIBLE);//show
                                    }
                                }

                            } else {
                                Toast.makeText(app.ctx, app.ctx.getString(R.string.error_fetching_jobs), Toast.LENGTH_SHORT).show();
                            }


                        });


            }
        });
    }

}
