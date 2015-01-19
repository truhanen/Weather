package fi.tuukka.weather.model;

import android.graphics.Bitmap;
import fi.tuukka.weather.model.downloader.Downloader;
import fi.tuukka.weather.utils.Utils;

public class ModelWarnings implements ModelInterface {

	private static final String URLWARNIGNS = "http://cdn.fmi.fi/weather-warnings/products/weather-warning-map-1d-fi.gif";
	private static Bitmap warnings = null;
	private static long warningsTime = 0l;
	
	public Bitmap warnings() {
		return warnings;
	}

	@Override
	public boolean isFinished() {
    	return warnings != null && System.currentTimeMillis() - warningsTime < Downloader.EXPIREDTIME;
	}

	@Override
	public void downloadNext() {
		warnings = Utils.loadBitmapFromUrl(URLWARNIGNS);
	}

}
