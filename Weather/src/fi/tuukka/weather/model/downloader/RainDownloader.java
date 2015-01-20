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
package fi.tuukka.weather.model.downloader;

import java.util.Date;

import fi.tuukka.weather.utils.Rains;
import android.graphics.Bitmap;
import android.widget.Toast;

public class RainDownloader {

    public static enum RainType {
        MIN15(18, URLRAIN15),
        H1(20, URLRAIN);
        public int steps;
        public String url;

        RainType(int steps, String url) {
            this.steps = steps;
            this.url = url;
        }
    }

    public static final int FIRSTFORECASTIND = 12;
    public static final String FORECASTLABEL = "forecast";
    private final static String URLRAIN = "http://ilmatieteenlaitos.fi/sade-ja-pilvialueet";
    private final static String URLRAIN15 = "http://ilmatieteenlaitos.fi/sade-ja-pilvialueet/suomi?flash=0";
    private static Rains sateet1h = new Rains(RainType.H1);
    private static Rains sateet15min = new Rains(RainType.MIN15);

    public static void downloadNext() {
        try {
            if (!sateet15min.hasFreshHtml()) {
                sateet15min.downloadHtml();
            }
            if (!sateet1h.hasFreshHtml()) {
                sateet1h.downloadHtml();
            }
            if (!sateet15min.hasRain(RainType.MIN15.steps - 1)) { // download the most recent rain15
            // System.out.println("download last 15");
                sateet15min.downloadRain(RainType.MIN15.steps - 1);
            } else if (!sateet1h.hasRains(FIRSTFORECASTIND, RainType.H1.steps - 1)) { // download all forecasts
            // System.out.println("download forecast");
                sateet1h.downloadRain(FIRSTFORECASTIND, RainType.H1.steps - 1);
            } else if (!sateet15min.hasAllRains()) { // download rest of the rain15s in reverse order
            // System.out.println("download rest 15");
                sateet15min.downloadRain(RainType.MIN15.steps - 1, 0);
            } else if (!sateet1h.hasAllRains()) { // download rest of the rains
            // System.out.println("download rest 1");
                sateet1h.downloadRain(0, RainType.H1.steps - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasFreshHtml() {
        return sateet1h.hasFreshHtml() && sateet15min.hasFreshHtml();
    }

    public static boolean isAllDownloaded() {
        // System.out.println(sateet15min.isAllDownloaded() && sateet1h.isAllDownloaded());
        return sateet15min.hasAllRains() && sateet1h.hasAllRains();
    }

    public static int rainsDownloaded() {
        return sateet1h.rainsFinished() + sateet15min.rainsFinished();
    }

    public static boolean hasRain(RainType type, int i) {
        if (type == RainType.MIN15)
            return sateet15min.rains() != null && sateet15min.rains()[i] != null;
        if (type == RainType.H1)
            return sateet1h.rains() != null && sateet1h.rains()[i] != null;
        return false;
    }

    public static Bitmap getRain(RainType type, int i) {
        if (type == RainType.MIN15) {
            // System.out.println(new Date(sateet15min.rainTimes()[i])); // TODO print
            return sateet15min.rains()[i];
        }
        // System.out.println(new Date(sateet1h.rainTimes()[i])); // TODO print
        return sateet1h.rains()[i];
    }

    public static Date getTime(RainType type, int i) {
        if (type == RainType.MIN15)
            return new Date(sateet15min.rainTimes()[i]);
        return new Date(sateet1h.rainTimes()[i]);
    }
}
