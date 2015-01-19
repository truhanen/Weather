package fi.tuukka.weather.view;

import fi.tuukka.weather.R;
import fi.tuukka.weather.model.ModelWarnings;
import fi.tuukka.weather.utils.Utils;
import fi.tuukka.weather.view.ActivityMain.Frag;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class FragmentWarnings extends TabFragment {

	LinearLayout layout;
	private View view;
	private ModelWarnings model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.varoitukset, container, false);
		model = (ModelWarnings) Frag.WARNINGS.model;
		layout = (LinearLayout) view.findViewById(R.id.varoitusLayout);
		makeButtonWork();
		if (model.warnings() != null)
			refresh();
		super.onCreateView(inflater, container, savedInstanceState);
		return view;
	}

	private void makeButtonWork() {
		final Activity activity = getActivity();
		layout.setOnClickListener(new View.OnClickListener() {
			public void onClick(View layout) {
				Utils.showImage(activity, layout, BitmapFactory.decodeResource(getResources(), R.drawable.warnings_legend_fi));
			}
		});
	}
	
	@Override
	public void refresh() {
		if (model.warnings() != null)
			Utils.setBackGroundDrawable(getActivity(), layout, model.warnings());
	}
	
	@Override
	public void makeIncomplete() {
		layout.setBackgroundColor(Color.BLACK);
	}
}
