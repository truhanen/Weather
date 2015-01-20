/*******************************************************************************
 * Copyright (C) 2015  Tuukka Ruhanen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
