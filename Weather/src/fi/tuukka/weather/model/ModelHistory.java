package fi.tuukka.weather.model;

import android.graphics.Bitmap;
import fi.tuukka.weather.utils.Station;
import fi.tuukka.weather.view.FragmentHistory;

public class ModelHistory implements ModelInterface {

	@Override
	public boolean isFinished() {
		return Station.chosen().isHistoriesFinished();
	}

	@Override
	public void downloadNext() {
		Station.chosen().downloadNextHistory();
	}

	public Bitmap[] getHistoryGraphs() {
		return Station.chosen().getHistoryGraphs();
	}

	public int historiesDownloaded() {
		return Station.chosen().historiesDownloaded();
	}
	
	public int totalHistories() {
		return Station.chosen().getHistoryGraphs().length;
	}

	public String getStationName() {
		return Station.chosen().getStationName();
	}

}
