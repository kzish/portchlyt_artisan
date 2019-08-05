package com.example.porchlyt_artisan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

public class ViewBlogActivity extends AppCompatActivity {

    String blog_title;
    WebView web_view;
    Toolbar mtoolbar;
    String blog_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_blog);
        mtoolbar = (Toolbar) findViewById(R.id.mtoolbar);

        web_view = (WebView) findViewById(R.id.web_view);
        blog_title = getIntent().getStringExtra("blog_title");
        blog_content = getIntent().getStringExtra("blog_content");

        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        getSupportActionBar().setTitle(blog_title);

        web_view.getSettings().setJavaScriptEnabled(true);
        web_view.loadDataWithBaseURL("", blog_content, "text/html", "UTF-8", "");


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


}


