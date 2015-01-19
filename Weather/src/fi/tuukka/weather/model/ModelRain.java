package fi.tuukka.weather.model;

import java.util.Date;

import android.graphics.Bitmap;
import fi.tuukka.weather.model.downloader.RainDownloader;
import fi.tuukka.weather.model.downloader.RainDownloader.RainType;

public class ModelRain implements ModelInterface {

	@Override
	public boolean isFinished() {
		return RainDownloader.isAllDownloaded();
	}

	@Override
	public void downloadNext() {
		RainDownloader.downloadNext();
	}

	public int rainsDownloaded() {
		return RainDownloader.rainsDownloaded();
	}

	public boolean hasRain(RainType rainType, int index) {
		return RainDownloader.hasRain(rainType, index);
	}

	public Bitmap getRain(RainType rainType, int index) {
		return RainDownloader.getRain(rainType, index);
	}

	public Date getTime(RainType rainType, int index) {
		return RainDownloader.getTime(rainType, index);
	}

}
