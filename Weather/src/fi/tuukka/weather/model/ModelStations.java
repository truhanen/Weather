package fi.tuukka.weather.model;

import fi.tuukka.weather.view.FragmentStations;

public class ModelStations implements ModelInterface {

	@Override
	public boolean isFinished() {
		return FragmentStations.isReady();
	}

	@Override
	public void downloadNext() {
		FragmentStations.downloadStations();
	}

}
