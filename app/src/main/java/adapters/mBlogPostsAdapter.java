package adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.porchlyt_artisan.R;
import com.example.porchlyt_artisan.ViewBlogActivity;

import java.nio.channels.InterruptedByTimeoutException;
import java.security.PublicKey;
import java.util.List;

import globals.globals;
import models.Posts.mBlogPost;

public class mBlogPostsAdapter extends RecyclerView.Adapter<mBlogPostsAdapter.myHolder> {

    private Activity activity_context;
    List<mBlogPost> posts;
    String tag="mBlogPostsAdapter";

    public mBlogPostsAdapter(Activity activity_context, List<mBlogPost> posts) {
        this.posts = posts;
        this.activity_context = activity_context;
    }//.constructor


    @Override
    public int getItemViewType(int position) {
        return posts.get(position) != null ? 1 : 0;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        try {
            myHolder viewHolder = null;
            if (viewType == 1) {
                View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_posts_view_item, parent, false);
                viewHolder = new myHolder(layoutView);
            } else {
                View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_item, parent, false);
                viewHolder = new ProgressViewHolder(layoutView);
            }
            return viewHolder;
        }catch (Exception ex)
        {
            Log.e(tag,ex+"");
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {

        try {
            mBlogPost post = posts.get(position);

            //if (holder instanceof myHolder) {
            if(post!=null && holder instanceof myHolder){

                holder.txt_post_title.setText(post.title.rendered);
                holder.web_view.getSettings().setJavaScriptEnabled(true);
                holder.web_view.loadDataWithBaseURL("", post.content.rendered, "text/html", "UTF-8", "");
                //long press to open this option
                holder.linlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent vp = new Intent(activity_context, ViewBlogActivity.class);
                        vp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        vp.putExtra("blog_title", post.title.rendered);
                        vp.putExtra("blog_content", post.content.rendered);
                        activity_context.startActivity(vp);

                    }
                });

            } else {
                ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
            }



        }catch (Exception ex)
        {
            Log.e(tag,ex.getMessage());
        }


    }


    @Override
    public int getItemCount() {
        if (posts == null) return 0;
        return posts.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    public class myHolder extends RecyclerView.ViewHolder {
        public WebView web_view;
        public LinearLayout linlay;
        public TextView txt_post_title;
        public myHolder(View v) {
            super(v);
            web_view = (WebView) v.findViewById(R.id.web_view);
            linlay = (LinearLayout) v.findViewById(R.id.linlay);
            txt_post_title = (TextView) v.findViewById(R.id.txt_post_title);
        }
    }//.class


    public class ProgressViewHolder extends mBlogPostsAdapter.myHolder {
        public ProgressBar progressBar;
        public ProgressViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }

}//.adapter
