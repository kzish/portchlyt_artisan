package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.porchlyt_artisan.R;
import com.example.porchlyt_artisan.RegisterActivity;
import com.example.porchlyt_artisan.app;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

//this adpater is used by the addskills activity pop up on registering the new artian


public class skillsAdapter extends BaseAdapter {

    String[] skills;

    public skillsAdapter(String[] l) {
        skills = l;
    }

    @Override
    public int getCount() {
        if (skills != null) return skills.length;
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (skills != null) return skills[position];
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.skill_row_item, parent, false);
        LinearLayout linlay = (LinearLayout) v.findViewById(R.id.linlay);
        final String skill = skills[position];

        if (position % 2 == 0) {
            //linlay.setBackgroundColor(app.ctx.getResources().getColor(R.color.primary));
        }
        TextView txt_skill = (TextView) v.findViewById(R.id.txt_skill);
        txt_skill.setText(skill);
        final CheckBox chk_skill = (CheckBox) v.findViewById(R.id.chk_skill);
        if(RegisterActivity.skills.contains(skill)) chk_skill.setChecked(true);//set checked to be true if this skill is already selected

        linlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //either add the skill of remove the skill
                chk_skill.performClick();

                if (chk_skill.isChecked()) {
                    if(RegisterActivity.skills.size()>=3)
                    {
                        //prevent user from  selecting this
                        //and prevent user from entering this skill if 3 or more selected
                        //notify the user of max limit
                        Toast.makeText(app.ctx,app.ctx.getString(R.string.max_3_skills),Toast.LENGTH_SHORT).show();
                        chk_skill.setChecked(false);//this is to uncheck it
                    }
                    else
                    {
                        //add skill otherwise
                        RegisterActivity.skills.add(skill);
                    }

                } else {
                    RegisterActivity.skills.remove(skill);

                }

                RegisterActivity.setSkills();//show the skills
            }
        });

        return v;
    }
}
