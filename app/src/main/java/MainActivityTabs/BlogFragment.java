package MainActivityTabs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.porchlyt_artisan.R;
import com.example.porchlyt_artisan.ViewExtraJobsActivity;
import com.example.porchlyt_artisan.ViewNotificationsActivity;
import com.example.porchlyt_artisan.app;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import adapters.mBlogPostsAdapter;
import adapters.mExtra_Jobs_Adapter;
import adapters.mNotification_Adapter;

import globals.*;
import io.realm.Realm;
import models.Posts.mBlogPost;
import models.mArtisan.mArtisan;
import models.mArtisanServiceRequest;
import models.mNotification;

public class BlogFragment extends Fragment {

    private OnFragmentInteractionListener mListener;


    static TextView lbl_notifications;

    static String tag = "BlogFragment";
    static Activity activity;

    RelativeLayout rel_job;
    RelativeLayout rel_notifications;
    RecyclerView blogs;

    LinearLayout linlay;
    SwipeRefreshLayout swipeContainer;



    public BlogFragment() {
    }

    public static BlogFragment newInstance(String param1, String param2) {
        BlogFragment fragment = new BlogFragment();
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
        View view = inflater.inflate(R.layout.fragment_blog, container, false);
        //
        activity = getActivity();


        //
        lbl_notifications=(TextView)view.findViewById(R.id.lbl_notifications);

        blogs=(RecyclerView) view.findViewById(R.id.blogs);
        linlay=(LinearLayout) view.findViewById(R.id.linlay);

        get_number_of_notifications();


        //
        rel_job = (RelativeLayout) view.findViewById(R.id.rel_jobs);
        rel_notifications = (RelativeLayout) view.findViewById(R.id.rel_notifications);

        rel_job.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent j = new Intent(getActivity(), ViewExtraJobsActivity.class);
                getActivity().startActivity(j);
            }
        });

        rel_notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent n = new Intent(getActivity(), ViewNotificationsActivity.class);
                getActivity().startActivity(n);
            }
        });


        swipeContainer = (SwipeRefreshLayout)view.findViewById(R.id.swipeContainer);

        //fetch the blogs
        fetch_posts();


        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetch_posts();
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



    void fetch_posts()
    {
        Ion.with(getContext())
                .load(globals.artisan_blog_base_url+"/posts?categories[]=2")//category 2 is for artisans
                .asString()
                .setCallback((e, result) -> {

                    swipeContainer.setRefreshing(false);
                    if(e==null)
                        {
                            try {
                                List<mBlogPost>posts= new ArrayList<>();
                                JSONArray json_a = new JSONArray(result);
                                for(int i =0;i<json_a.length();i++)
                                {
                                    JSONObject json=json_a.getJSONObject(i);
                                    mBlogPost post = new Gson().fromJson(json.toString(),mBlogPost.class);
                                    posts.add(post);
                                }
                                set_blogs_adapter(getActivity(),posts);
                            }catch (Exception ex)
                            {
                                Log.e(tag,ex.getMessage());
                            }
                        }else
                        {
                            Snackbar.make(linlay,getActivity().getString(R.string.error_occured),Snackbar.LENGTH_LONG).show();
                        }


                });
    }

    public void set_blogs_adapter(Activity activity,List<mBlogPost>posts) {
        try {
            mBlogPostsAdapter posts_adapter = new mBlogPostsAdapter(activity,posts);
            posts_adapter.setHasStableIds(true);
            LinearLayoutManager lm = new LinearLayoutManager(app.ctx, LinearLayoutManager.VERTICAL, false);
            blogs.setLayoutManager(lm);
            blogs.setAdapter(posts_adapter);
        } catch (Exception ex) {
            Log.e(tag, ex.getLocalizedMessage());
        }
    }










    //non read notifications
    public static void get_number_of_notifications() {
        Realm db = globals.getDB();
        int num_notifications = (int) db.where(mNotification.class).equalTo("is_read", false).count();
        if(num_notifications>10)
        {
            lbl_notifications.setText("10+");
        }
        else
        {
            lbl_notifications.setText(num_notifications+"");
        }
        db.close();
    }

}
