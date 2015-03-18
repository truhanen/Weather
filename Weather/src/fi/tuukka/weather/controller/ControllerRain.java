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
package fi.tuukka.weather.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import fi.tuukka.weather.downloader.RainDownloader;
import fi.tuukka.weather.downloader.RainDownloader.RainType;

public class ControllerRain implements ControllerInterface {

    @Override
    public boolean isFinished(Context context) {
        return RainDownloader.isAllDownloaded();
    }

    @Override
    public void downloadNext(Context context) {
        RainDownloader.downloadNext(context);
    }

    public List<Integer>[] rainsDownloaded() {
        return RainDownloader.rainsDownloaded();
    }
    
    public boolean hasFreshHtml() {
        return RainDownloader.hasFreshHtml();
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
