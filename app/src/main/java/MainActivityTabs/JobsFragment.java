package MainActivityTabs;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sirachlabs.porchlyt_artisan.R;

import adapters.mjobsAdapter;

public class JobsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    static RecyclerView lst_jobs;
    //static Realm db;
    public static mjobsAdapter jad;
    static Activity ctx;

    static String tag ="jobs fragment";
    public JobsFragment() {
    }

    public static JobsFragment newInstance(String param1, String param2) {
        JobsFragment fragment = new JobsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //db=Realm.getDefaultInstance();
        ctx=getActivity();
        View view = inflater.inflate(R.layout.fragment_jobs, container, false);
        lst_jobs = (RecyclerView)view.findViewById(R.id.lst_jobs);
        refreshJobsAdapter();//show the current jobs
        return  view;
    }

    public void onButtonPressed(Uri uri) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStop() {
        super.onStop();
        //db.close();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //db.close();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
    //refresh the jobs adapter to reflect any changes
    public static void refreshJobsAdapter()
    {

        try {
            jad = new mjobsAdapter(ctx);
            jad.setHasStableIds(true);

            lst_jobs.setDrawingCacheEnabled(true);
            lst_jobs.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

            lst_jobs.setLayoutManager(new LinearLayoutManager(ctx));
            lst_jobs.setAdapter(jad);
            jad.notifyDataSetChanged();
        }catch (Exception e)
        {
            Log.e(tag,e.getLocalizedMessage());
        }
        finally {
        }
    }
}
