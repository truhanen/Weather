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
package fi.tuukka.weather.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import fi.tuukka.weather.downloader.Downloader;
import fi.tuukka.weather.downloader.RainDownloader;
import fi.tuukka.weather.downloader.RainDownloader.RainType;
import android.content.Context;
import android.graphics.Bitmap;

public class Rains {

    private RainType type;

    private long[] rainTimes;
    private String[] rainImageUrls;
    private Bitmap[] rains;
    private boolean[] inUi; // to determine whether images are set to ui
    private boolean[] saved; // to determine whether images are saved to file
    private long htmlTime = 0l;

    public Rains(RainType type2) {
        this.type = type2;
        this.rains = new Bitmap[type.steps];
        this.rainTimes = new long[type.steps];
        this.inUi = new boolean[type.steps];
        this.saved = new boolean[type.steps];
    }

    public boolean hasFreshHtml() {
        return System.currentTimeMillis() - htmlTime < Downloader.EXPIREDTIME;
    }

    public long[] rainTimes() {
        return rainTimes.clone(); // TODO
    }

    public Bitmap[] rains() {
        return rains.clone(); // TODO
    }

    public RainType type() {
        return type;
    }

    public boolean hasRain(int i) {
        return (Downloader.isRunAlone() && saved[i]) || (!Downloader.isRunAlone() && inUi[i]);
    }

    public boolean hasRains(int from, int to) {
        for (int i = from; i <= to && i < type.steps; i++) {
            if (!hasRain(i))
                return false;
        }
        return true;
    }

    public boolean hasAllRains() {
        if (!hasFreshHtml())
            return false;
        return hasRains(0, type.steps - 1);
    }

    public List<Integer> rainsDownloaded() {
        ArrayList<Integer> downloaded = new ArrayList<Integer>();
        for (int i = 0; i < type.steps; i++)
            if (hasRain(i))
                downloaded.add(i);
        return downloaded;
    }

    /**
     * Download the next rain from the given range that has not yet been downloaded.
     */
    public void downloadRain(int from, int to, Context context) throws Exception {
        int add = 1;
        if (from > to)
            add = -1;
        for (int i = from; (i <= to || (i >= to && from > to)) && i <= type.steps; i += add) {
            if (!hasRain(i)) {
                downloadRain(i, context);
                break;
            }
        }
    }

    /**
     * Only assign values to inUi[] and saved[] here, do not use them.
     */
    public void downloadRain(int i, Context context) throws Exception {
        String fileName = getRainFileName(i);
        boolean onlyDownload = true; // set this if you do not want to save or load bitmap files
        if (!onlyDownload && Downloader.isRunAlone()) {
            // save new if we do not have a valid file
            if (isObsoleteFile(fileName) || !FileUtils.hasFile(fileName, context)) {
                Bitmap bmp;
                if (rains[i] != null) {
                    // System.out.println("rain from ui to file " + type + " " + Integer.toString(i) + " " + fileName);
                    bmp = rains[i];
                } else {
                    // System.out.println("rain from web to file " + type + " " + Integer.toString(i) + " " + fileName);
                    bmp = Utils.loadBitmapFromUrl(rainImageUrls[i]);
                }
                FileUtils.saveBitmap(fileName, bmp, context);
                Utils.recycle(rains, i);
            }
            if (rains[i] != null)
                Utils.recycle(rains, i);
            inUi[i] = false;
            saved[i] = true;
        } else {
            if (rains[i] == null) {
                if (!onlyDownload && !isObsoleteFile(fileName) && FileUtils.hasFile(fileName, context)) {
//                    System.out.println("rain from file to ui " + type + " " + Integer.toString(i) + " " + fileName);
                    rains[i] = FileUtils.openBitmap(fileName, context);
                }
                if (rains[i] == null) { // also in case the opened file was corrupt and returned null
//                    System.out.println("rain from web to ui " + type + " " + Integer.toString(i));
                    rains[i] = Utils.loadBitmapFromUrl(rainImageUrls[i]);
                }
                if (rains[i] == null)
                    throw new Exception();
            }
            inUi[i] = true;
        }
    }

    public void downloadHtml(Context context) throws Exception {
        String rainHtml = Utils.downloadHtml(type.url);
        if (rainHtml == null) {
            throw new Exception();
        }
        long[] oldTimes = rainTimes;
        rainTimes = findRainTimeStamps(rainHtml);
        rainImageUrls = getRainAddresses(rainHtml);
        syncBitmaps(rainTimes, oldTimes);
        htmlTime = System.currentTimeMillis();
        removeOldFiles(context);
        for (int i = 0; i < type.steps; i++) {
            inUi[i] = false;
            saved[i] = false;
        }
    }

    private void syncBitmaps(long[] newTimes, long[] oldTimes) {
        int j = 0;
        for (int i = 0; i < type.steps; i++) {
            boolean isObservation = type == RainType.MIN15 || i < RainDownloader.FIRSTFORECASTIND;
            if (oldTimes[i] == newTimes[j] && isObservation) {
                rains[j++] = rains[i];
            }
        }
        for (; j < type.steps; j++)
            rains[j] = null;
    }

    private long[] findRainTimeStamps(String source) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("d.M. HH:mm");
            long[] tempTimeStamps = new long[type.steps];
            String searchStringLeft = "1)\">";
            String searchStringRight = "</b>";
            int left = 0;
            int right = 0;
            for (int i = 0; i < type.steps; i++) {
                left = source.indexOf(searchStringLeft,
                        left + searchStringLeft.length())
                        + searchStringLeft.length();
                right = source.indexOf(searchStringRight, left);
                String timeStampRaw = source.substring(left, right);
                timeStampRaw = timeStampRaw.replace("&nbsp;", " ");
                timeStampRaw = timeStampRaw.replace("<b>", "");
                timeStampRaw = timeStampRaw.substring(timeStampRaw.indexOf(' ') + 1);
                tempTimeStamps[i] = formatter.parse(timeStampRaw).getTime();
                // System.out.println(timeStampRaw);
            }
            return tempTimeStamps;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String[] getRainAddresses(String html) {
        String[] imageAddresses = new String[type.steps];

        String searchStringLeft = "anim_images_anim_anim = new Array(\"";
        String searchStringRight = "\");var imagecache = new Array()";
        int left = html.indexOf(searchStringLeft, 0)
                + searchStringLeft.length();
        int right = html.indexOf(searchStringRight, 0);
        String addressArray = html.substring(left, right);
        StringTokenizer tokens = new StringTokenizer(addressArray, "\",\"");

        for (int i = 0; i < type.steps; ++i) {
            imageAddresses[i] = tokens.nextToken();
        }

        return imageAddresses;
    }

    public String getRainFileName(int i) {
        if (type == RainType.H1 && i >= RainDownloader.FIRSTFORECASTIND) {
            return FileUtils.RAINSTART + "_" + type.name() + "_" + rainTimes[i] + "_" + RainDownloader.FORECASTLABEL + "_" + i;
        }
        return FileUtils.RAINSTART + "_" + type.name() + "_" + rainTimes[i];
    }

    public static long parseTime(String fileName) {
        return Long.parseLong(fileName.split("_")[2]);
    }

    private void removeOldFiles(Context context) {
        for (String fileName : FileUtils.getFileNames(FileUtils.RAINSTART, context)) {
            if (isObsoleteFile(fileName)) {
                FileUtils.deleteFile(fileName, context);
            }
        }
    }

    private boolean isObsoleteFile(String fileName) {
        if (!fileName.contains(type.name()))
            return false;
        if (fileName.contains(RainDownloader.FORECASTLABEL)) {
            return !isFreshForecastFile(fileName);
        }
        else
            return parseTime(fileName) < rainTimes[0];
    }

    public boolean isFreshForecastFile(String fileName) {
        if (!fileName.contains(RainDownloader.FORECASTLABEL))
            return true;
        int i = Integer.parseInt(fileName.substring(fileName.lastIndexOf('_') + 1));
        return parseTime(fileName) == rainTimes[i];
    }

    public void recycle() {
        Utils.recycle(rains);
    }
}
