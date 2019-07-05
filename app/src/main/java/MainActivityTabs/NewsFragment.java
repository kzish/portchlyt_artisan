package MainActivityTabs;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.porchlyt_artisan.R;
import com.example.porchlyt_artisan.app;

import adapters.mNotification_Adapter;


public class NewsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    static public RecyclerView list_notifications;

    static String tag = "NewsFragment";

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
        list_notifications = (RecyclerView) view.findViewById(R.id.list_notifications);

        set_notification_adapter();
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
            LinearLayoutManager lm = new LinearLayoutManager(app.ctx, LinearLayoutManager.HORIZONTAL, false);
            list_notifications.setLayoutManager(lm);
            list_notifications.setAdapter(notification_adapter);
        } catch (Exception ex) {
            Log.e(tag, ex.getLocalizedMessage());
        }
    }

}
