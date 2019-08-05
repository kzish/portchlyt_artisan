package adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porchlyt_artisan.R;
import com.example.porchlyt_artisan.ViewBlogActivity;

import java.util.List;

import models.Posts.mBlogPost;

public class mBlogPostsAdapter extends RecyclerView.Adapter<mBlogPostsAdapter.myHolder> {

    List<mBlogPost> posts;
    Activity activity;
    View.OnTouchListener tl = null;

    public mBlogPostsAdapter(Activity activity, List<mBlogPost> posts) {
        this.posts = posts;
        this.activity = activity;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_posts_view_item, parent, false);
        return new myHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {
        mBlogPost post = posts.get(position);
        holder.web_view.getSettings().setJavaScriptEnabled(true);
        //holder.web_view.loadUrl(post.link);

        //data == html data which you want to load
        holder.web_view.loadDataWithBaseURL("", post.content.rendered, "text/html", "UTF-8", "");
        holder.txt_post_title.setText(post.title.rendered);

        holder.linlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent vb = new Intent(activity, ViewBlogActivity.class);
                vb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                vb.putExtra("blog_title",post.title.rendered);
                vb.putExtra("blog_content",post.content.rendered);
                activity.startActivity(vb);
            }
        });



    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (posts != null)
            return posts.size();
        return 0;
    }


    class myHolder extends RecyclerView.ViewHolder {

        public WebView web_view;
        public LinearLayout linlay;
        public TextView txt_post_title;

        public myHolder(@NonNull View itemView) {
            super(itemView);
            web_view = (WebView) itemView.findViewById(R.id.web_view);
            linlay = (LinearLayout) itemView.findViewById(R.id.linlay);
            txt_post_title = (TextView) itemView.findViewById(R.id.txt_post_title);
        }
    }


}
