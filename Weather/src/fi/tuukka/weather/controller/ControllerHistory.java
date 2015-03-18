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

import android.content.Context;
import android.graphics.Bitmap;
import fi.tuukka.weather.utils.Station;
import fi.tuukka.weather.view.FragmentHistory;

public class ControllerHistory implements ControllerInterface {

    @Override
    public boolean isFinished(Context context) {
        return Station.chosen(context).isHistoriesFinished(context);
    }

    @Override
    public void downloadNext(Context context) {
        Station.chosen(context).downloadNextHistory(context);
    }

    public Bitmap[] getHistoryGraphs(Context context) {
        return Station.chosen(context).getHistoryGraphs();
    }

    public int historiesDownloaded(Context context) {
        return Station.chosen(context).historiesDownloaded();
    }

    public int totalHistories(Context context) {
        return Station.chosen(context).getHistoryGraphs().length;
    }

    public String getStationName(Context context) {
        return Station.chosen(context).getStationName();
    }

}
