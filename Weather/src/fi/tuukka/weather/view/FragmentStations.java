package fi.tuukka.weather.view;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import fi.tuukka.weather.R;
import fi.tuukka.weather.utils.Station;
import fi.tuukka.weather.utils.Utils;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;

public class FragmentStations extends TabFragment {
	
//	private ArrayList<Station> stations;
	private View view;
	private RadioGroup rg;
	private boolean listenCheck = true;
	private TextView header;
	private static String query = null;
	private static String[] stationStrings = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.asemat, container, false);
		rg = (RadioGroup) view.findViewById(R.id.radioGroup1);
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup rg, int id) {
				if (listenCheck) {
					for (int i = 0; i < rg.getChildCount(); i++) {
						RadioButton btn = (RadioButton) rg.getChildAt(i);
						if (btn.getId() == id) {
							String text = (String) btn.getText();
							Station station = new Station(text, null);
							changeStation(station);
							refreshStations();
							return;
						}
					}
				}
			}
		});
		EditText searchBox = (EditText) view.findViewById(R.id.stationSearchBox);
		searchBox.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String string = s.toString();
				if (query == null || !query.equals(string)) {
					query = s.toString();
					stationStrings = null; // mark as not finished
					((ActivityMain) getActivity()).queryStations();
				}
			}
		});
		Activity activity = getActivity();
		header = (TextView) view.findViewById(R.id.stationsHeader);
		int fontsize = Utils.getScaledFont(activity);
		int pad = Utils.dpToPx(5, activity);
		header.setTextAppearance(activity, R.style.TextStyle);
		header.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontsize);
		header.setPadding(0, pad, 0, pad);
		header.setText("Syötä kunta/kunnanosa:");
		super.onCreateView(inflater, container, savedInstanceState);
		return view;
	}
    
    public static boolean isReady() {
    	return stationStrings != null || query == null || query.length() == 0;
    }
    
    private void changeStation(Station station) {
    	((ActivityMain) getActivity()).changeStation(station);
    }
	
	public static void downloadStations() {
		if (query == null || query.length() == 0) {
			return;
		}
		String ss = query.toString().toLowerCase();
		HttpResponse response;
        HttpClient myClient = new DefaultHttpClient();
        HttpPost myConnection = new HttpPost("http://ilmatieteenlaitos.fi/paikallissaa?p_p_id=locationmenuportlet_WAR_fmiwwwweatherportlets&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_cacheability=cacheLevelFull&timestamp=0&place="
        								     + ss);
        String str = "";
        try {
        	response = myClient.execute(myConnection);
            str = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if ((int) str.charAt(0) == 0)
        	stationStrings = new String[0];
        else
        	stationStrings = str.split("\n");
	}
	
	public void refreshStations() {
		Station chosen = Station.chosen();
		listenCheck = false;
		((ScrollView) view.findViewById(R.id.scrollView1)).scrollTo(0, 0);
		rg.clearCheck();
		rg.removeAllViews();
		if (stationStrings != null)
			for (String s : stationStrings) {
				Station station = new Station(s, null);
				RadioButton rb = new RadioButton(getActivity());
				rb.setText(station.getForecastPlaceName());
				rg.addView(rb);
				if (chosen != null && station.getForecastPlaceName().equals(chosen.getForecastPlaceName()))
					rg.check(rb.getId());
			}
		listenCheck = true;
	}
	
	@Override
	public void refresh() {
		refreshStations();
	}
	
	@Override
	public void makeIncomplete() {
		refreshStations();
	}
}
