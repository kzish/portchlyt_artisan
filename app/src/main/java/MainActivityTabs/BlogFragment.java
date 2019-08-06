package MainActivityTabs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
    static int page = 1;//blogs page
    int per_page = 5;//posts per page to retreive
    List<mBlogPost> posts = new ArrayList<>();
    mBlogPostsAdapter posts_adapter;


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
        lbl_notifications = (TextView) view.findViewById(R.id.lbl_notifications);

        blogs = (RecyclerView) view.findViewById(R.id.blogs);
        linlay = (LinearLayout) view.findViewById(R.id.linlay);

        //
        RecyclerView.LayoutManager lm = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        blogs.setLayoutManager(lm);
        blogs.setHasFixedSize(true);

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





        //
        posts_adapter = new mBlogPostsAdapter(getActivity(), posts);
        posts_adapter.setHasStableIds(true);

        blogs.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) blogs.getLayoutManager();
                int totalItemCount = posts_adapter.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                //if (totalItemCount <= (lastVisibleItem + 1))
                //direction integers: -1 for up, 1 for down, 0 will always return false.
                if (!blogs.canScrollVertically(1))
                {
                    //dont insert yet another if already loading
                    mBlogPost post = posts.get(posts.size()-1);
                    if(post==null)return;//if the last insertion was already a null then dont execute this again

                    posts.add(null);//add the loading dialog view
                    posts_adapter.notifyItemInserted(posts.size() - 1);//notify your insert
                    get_more_data();
                }
            }
        });

        blogs.setAdapter(posts_adapter);
        get_more_data();//initial call



        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

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
                    posts = new ArrayList<>();
                    posts_adapter = new mBlogPostsAdapter(getActivity(), posts);
                    posts_adapter.setHasStableIds(true);
                    blogs.setAdapter(posts_adapter);
                    get_more_data();
                }catch (Exception ex)
                {
                    Log.e(tag,ex+"");
                }
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


    //fetch more posts data from the server
    private void get_more_data() {




        Ion.with(getActivity())
                .load(globals.artisan_blog_base_url + "/posts?categories[]=2&page=" + page + "&orderby=date&per_page=" + per_page)//category 2 is for artisans
                .asString()
                .setCallback((e, result) -> {

                    if(posts.size()>0) {
                        mBlogPost last_inserted_post = posts.get(posts.size() - 1);
                        if (last_inserted_post == null) {
                            posts.remove(last_inserted_post);//remove that null item that was inserted
                            posts_adapter.notifyItemRemoved(posts.size() - 1);//notify it
                        }
                    }

                    //delay dismissing the progress bar for a second
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeContainer.setRefreshing(false);
                        }
                    }, 1000);

                    if (e != null) {
                        Log.e(tag, "line 138 " + e);
                        //Snackbar.make(linlay,"error 1",Snackbar.LENGTH_SHORT).show();
                        if(posts.size()>0) {
                            mBlogPost last_inserted_post = posts.get(posts.size() - 1);
                            if (last_inserted_post == null) {
                                posts.remove(last_inserted_post);//remove that null item that was inserted
                                posts_adapter.notifyItemRemoved(posts.size() - 1);//notify it
                            }
                        }
                        return;
                    }

                    if (result == null) {
                        Log.e(tag, "line 136 result is null");
                        //Snackbar.make(linlay,"error 2",Snackbar.LENGTH_SHORT).show();
                        if(posts.size()>0) {
                            mBlogPost last_inserted_post = posts.get(posts.size() - 1);
                            if (last_inserted_post == null) {
                                posts.remove(last_inserted_post);//remove that null item that was inserted
                                posts_adapter.notifyItemRemoved(posts.size() - 1);//notify it
                            }
                        }
                        return;
                    }

                    Log.e(tag, "result " + result);
                    if(result.contains("rest_post_invalid_page_number"))return;

                    try {
                        JSONArray json_a = new JSONArray(result);
                        for (int i = 0; i < json_a.length(); i++) {
                            mBlogPost post = new Gson().fromJson(json_a.get(i).toString(), mBlogPost.class);
                            if(!posts.contains(post)) {//dont repeat posts
                                posts.add(post);//add to data set
                            }
                            posts_adapter.notifyItemInserted(posts.size() - 1);//notify the adapter
                        }
                        page++;
                    } catch (Exception ex) {
                        Log.e(tag, "line 148 " + ex);
                        Snackbar.make(linlay,getActivity().getString(R.string.error_occured),Snackbar.LENGTH_SHORT).show();
                        if(posts.size()>0) {
                            mBlogPost last_inserted_post = posts.get(posts.size() - 1);
                            if (last_inserted_post == null) {
                                posts.remove(last_inserted_post);//remove that null item that was inserted
                                posts_adapter.notifyItemRemoved(posts.size() - 1);//notify it
                            }
                        }
                    }
                });
    }//.get_more_data


    //non read notifications
    public static void get_number_of_notifications() {
        Realm db = globals.getDB();
        int num_notifications = (int) db.where(mNotification.class).equalTo("is_read", false).count();
        if (num_notifications > 10) {
            lbl_notifications.setText("10+");
        } else {
            lbl_notifications.setText(num_notifications + "");
        }
        db.close();
    }

}
