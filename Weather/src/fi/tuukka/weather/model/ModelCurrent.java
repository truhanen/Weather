package fi.tuukka.weather.model;

import fi.tuukka.weather.utils.Station;

public class ModelCurrent implements ModelInterface {

	@Override
	public boolean isFinished() {
		return Station.chosen().hasFreshHtml();
	}

	@Override
	public void downloadNext() {
		Station.chosen().downloadHtml();
	}
	
	public String getHtml() {
		return Station.chosen().getHtml();
	}
	
	public String getStationName() {
		return Station.chosen().getStationName();
	}
}
