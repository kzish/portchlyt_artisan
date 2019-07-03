package com.example.porchlyt_artisan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import adapters.skillsAdapter;

public class AddSkillsActivity extends AppCompatActivity {

    ListView lst_skills;
    skillsAdapter adp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_skills);

        try {
            lst_skills = (ListView) findViewById(R.id.lst_skills);
            String[] skills = getResources().getStringArray(R.array.job_categories);
            adp = new skillsAdapter(skills);
            lst_skills.setAdapter(adp);
            adp.notifyDataSetChanged();
        }catch (Exception ex)
        {
            Log.e("d",ex.getMessage());
        }



    }

    public void SaveSkills(View v)
    {
        finish();//end this activity
    }

}
