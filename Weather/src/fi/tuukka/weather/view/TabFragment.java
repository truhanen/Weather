package fi.tuukka.weather.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Remember: always call super.onCreateView(...) when extending this class
 * to properly handle the viewCreated parameter.
 */
public abstract class TabFragment extends Fragment {
	
	private boolean viewCreated = false;
	
	abstract void refresh();
	
	abstract void makeIncomplete();
	
	boolean isViewCreated() {
		return viewCreated;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewCreated = true;
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onDestroyView() {
		viewCreated = false;
		super.onDestroyView();
	}
}
