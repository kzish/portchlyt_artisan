package com.sirachlabs.porchlyt_artisan;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

import MainActivityTabs.BlogFragment;
import MainActivityTabs.JobsFragment;
import MainActivityTabs.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    //private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //toolbar = (Toolbar) findViewById(R.id.mtoolbar);
        //setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setTabIcons();


        //start the mqtt service
        Intent mqtt_service =  new Intent(this,globals.MyMqtt.class);
        startService(mqtt_service);

    }


    private void setTabIcons() {
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_time);//jobs
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_find);//search
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_comment);//blog
        tabLayout.getTabAt(1).select();//select the default tab
    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new JobsFragment(), getString(R.string.jobs));
        adapter.addFragment(new ProfileFragment(), getString(R.string.profile));
        adapter.addFragment(new BlogFragment(), getString(R.string.blog));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        //close panel if open before closing the app
        if(ProfileFragment.sliding_layout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED)
        {
            ProfileFragment.sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }
        else
        {
            super.onBackPressed();
        }
    }
}


class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

}

