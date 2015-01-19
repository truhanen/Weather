package fi.tuukka.weather.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import fi.tuukka.weather.R;
import fi.tuukka.weather.model.ModelCurrent;
import fi.tuukka.weather.utils.Station;
import fi.tuukka.weather.utils.Utils;
import fi.tuukka.weather.view.ActivityMain.Frag;
import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class FragmentCurrent extends TabFragment {
	
	private Forecast ennuste;
	private View view;
	private LinearLayout havaintoLayout;
	private TextView havaintoHeader;
	private ModelCurrent model;
	private final String[] searchLines = {
			"L&auml;mp&ouml;tila</span> <span class=\"parameter-value\">",
			"Kosteus</span> <span class=\"parameter-value\">",
			"Kastepiste</span> <span class=\"parameter-value\">",
			"tuulta</span> <span class=\"parameter-value\">",
			"Puuska</span> <span class=\"parameter-value\">",
			"Paine</span> <span class=\"parameter-value\">",
			"Tunnin&nbsp;sadekertym&auml;</span> <span class=\"parameter-value\">",
			"Lumensyvyys</span> <span class=\"parameter-value\">",
			"/8)",
			"N&auml;kyvyys</span> <span class=\"parameter-value\">"
	};
	private final String[] parameterNames = {
			"Lämpötila",
			"Kosteus",
			"Kastepiste",
			"Tuuli",
			"Puuska",
			"Paine",
			"Tunnin sadekertymä",
			"Lumensyvyys",
			"Pilvisyys",
			"Näkyvyys",
	};
	
	private String[] variables = new String[searchLines.length];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.tuorein, container, false);
		model = (ModelCurrent) Frag.CURRENT.model;
		havaintoLayout = (LinearLayout) view.findViewById(R.id.havaintoLayout);
		havaintoHeader = (TextView) view.findViewById(R.id.havaintoHeader);
		LinearLayout ennusteLayout = (LinearLayout) view.findViewById(R.id.ennusteLayout);
		TextView ennusteHeader = (TextView) view.findViewById(R.id.ennusteHeader);
		ennuste = new Forecast(this, ennusteLayout, ennusteHeader);
		makeCurrentIncomplete();
		if (model.getHtml() != null) {
			paivitaHavainto();
			paivitaEnnuste();
		}
		super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }
	
	public void paivitaHavainto() {
		String full = model.getHtml();
		if (full == null) return;
		int start = full.indexOf("Havaintoasema:");
		String range = full.substring(start, full.indexOf("/table", start));
		getVariables(range);
		viewVariables();
		refreshTime();
	}
	
	public void paivitaEnnuste() {
		ennuste.paivita();
	}
	
	public void makeCurrentIncomplete(){
		havaintoHeader.setText("Havainto (ladataan...)");
		havaintoLayout.removeAllViews();
		ennuste.makeIncomplete();
	}
	
	private void refreshTime() {
		String timeString = new SimpleDateFormat("HH:mm").format(new Date());
		TextView textView = (TextView) view.findViewById(R.id.havaintoHeader);
		textView.setText("Havainto (" + timeString + " @ " + model.getStationName() + ")");
	}

	private void viewVariables() {
		Activity activity = getActivity();
		havaintoLayout.removeAllViews();
		int fontsize = Utils.getScaledFont(activity);
		LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		for (int i=0; i<variables.length; i++) {
			if (variables[i] != null) {
				LinearLayout row = new LinearLayout(activity);
				havaintoLayout.addView(row, rowParams);
				
				TextView valueName = new TextView(activity);
				valueName.setText(parameterNames[i]);
				valueName.setTextAppearance(activity, R.style.TextStyle);
				valueName.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontsize);
				row.addView(valueName, nameParams);
				
				TextView value = new TextView(activity);
				value.setText(variables[i]);
				value.setTextAppearance(activity, R.style.TextStyle);
				value.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontsize);
				value.setGravity(Gravity.RIGHT);
				row.addView(value, valueParams);
			}
		}
	}

	public void getVariables(String html) {
		for (int i = 0; i < searchLines.length; ++i) {
			if (i != searchLines.length-2 && html.indexOf(searchLines[i], 0) != -1) {
				int left = html.indexOf(searchLines[i], 0) + searchLines[i].length();
				int right = html.indexOf("<", left);
				String htmlVariable = html.substring(left, right);
				if (htmlVariable.length() > 20)
					variables[i] = "Tyyntä";
				else {
					htmlVariable = htmlVariable.replace("&deg;", "°");
					htmlVariable = htmlVariable.replace("&nbsp;", " ");
					htmlVariable = htmlVariable.trim();
					variables[i] = htmlVariable;
				}
			}
			else if (html.indexOf(searchLines[i], 0) != -1){
				int left = html.indexOf(searchLines[i]) - 1;
				variables[i] = html.substring(left, left + 3);
			}
			else
				variables[i] = null;
		}
	}

	@Override
	public void refresh() {
		paivitaHavainto();
		paivitaEnnuste();
	}

	@Override
	public void makeIncomplete() {
		makeCurrentIncomplete();
	}
}
