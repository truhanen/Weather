/*******************************************************************************
 * Copyright (C) 2015 Tuukka Ruhanen
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
