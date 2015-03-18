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

import fi.tuukka.weather.downloader.Downloader;
import fi.tuukka.weather.view.ActivityMain;
import fi.tuukka.weather.view.FragmentHistory;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

public class Station {

    public static final String STATIONKUNTAKEY = "stationKuntaKey";
    public static final String STATIONKUNTADEFAULT = "Jyväskylä";
    public static final String STATIONOSAKEY = "stationOsaKey";
    public static final String STATIONOSADEFAULT = "Kuokkala";
    public static final String STATIONNAMEKEY = "stationNameKey";
    public static final String STATIONNAMEDEFAULT = "Jyväskylä lentoasema";
    public static final String STATIONIDKEY = "stationIdKey";
    public static final String STATIONIDDEFAULT = "101339";

    private static Station chosen = null;

    private String kunta = null;
    private String osa = null;
    private String name = null;
    private String id = null;

    private String html = null;
    private long htmlTime = 0l;
    private HistoryGraphs graphs = new HistoryGraphs();

    public Station(String kunta2, String osa2, String name2, String id2) {
        kunta = kunta2;
        osa = osa2;
        name = name2;
        id = id2;
    }

    public Station(String osaJaKunta, String id) {
        setValues(osaJaKunta, id);
    }

    public static void setPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(ActivityMain.PREFS_FILENAME, Context.MODE_PRIVATE);
        String kunta = prefs.getString(STATIONKUNTAKEY, STATIONKUNTADEFAULT);
        String osa = prefs.getString(STATIONOSAKEY, STATIONOSADEFAULT);
        String name = prefs.getString(STATIONNAMEKEY, STATIONNAMEDEFAULT);
        String id = prefs.getString(STATIONIDKEY, STATIONIDDEFAULT);
        chosen = new Station(kunta, osa, name, id);
    }

    public static void saveChosen(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(ActivityMain.PREFS_FILENAME, Context.MODE_PRIVATE);
        prefs.edit().putString(STATIONKUNTAKEY, chosen(context).kunta).commit();
        prefs.edit().putString(STATIONOSAKEY, chosen(context).osa).commit();
        prefs.edit().putString(STATIONNAMEKEY, chosen(context).name).commit();
        prefs.edit().putString(STATIONIDKEY, chosen(context).id).commit();
    }

    public boolean hasFreshHtml() {
        return html != null && System.currentTimeMillis() - htmlTime < Downloader.HTMLEXPIREDTIME;
    }

    public boolean isHistoriesFinished(Context context) {
        if (Downloader.isRunAlone())
            return graphs.hasAllFreshSaved(context);
        else {
            return graphs.hasAllFresh();
        }
    }

    public static Station chosen(Context context) {
        if (chosen == null)
            setPreferences(context);
        return chosen;
    }

    public static void setChosen(Station chosen2) {
        chosen = chosen2;
    }

    public Bitmap[] getHistoryGraphs() {
        if (graphs == null)
            return null;
        return graphs.bmps.clone();
    }

    public void setValues(String osaJaKunta, String idT) {
        if (osaJaKunta.contains(",")) {
            osa = osaJaKunta.substring(0, osaJaKunta.indexOf(','));
            kunta = osaJaKunta.substring(osaJaKunta.indexOf(',') + 2);
        } else {
            osa = null;
            kunta = osaJaKunta;
        }
        id = idT;
    }

    public String getHtml() {
        return html;
    }

    public String getUrl() {
        StringBuilder sb = new StringBuilder("http://ilmatieteenlaitos.fi/saa/" + kunta);
        if (osa != null)
            sb.append("/" + osa);
        if (id != null)
            sb.append("?station=" + id);
        return sb.toString();
    }

    public String getForecastPlaceName() {
        if (osa == null)
            return kunta;
        return osa + ", " + kunta;
    }

    public String getStationName() {
        return name;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id2) {
        id = id2;
    }

    public void downloadHtml() {
        html = Utils.downloadHtml(getUrl());
        htmlTime = System.currentTimeMillis();
        parseNameAndId();
    }

    private void parseNameAndId() {
        int start = html.indexOf("Havaintoasema:");
        String range = html.substring(start, html.indexOf("/option", start));
        start = range.indexOf("value");
        id = range.substring(start + 7, start + 13);
        start = range.indexOf(">", start);
        int end = range.indexOf('<', start);
        name = range.substring(start + 1, end);
    }

    public int historiesDownloaded() {
        int n = 0;
        for (int i = 0; i < graphs.bmps.length; i++)
            if (graphs.bmps[i] != null)
                n++;
        return n;
    }

    public void downloadNextHistory(Context context) {
        if (html == null || !hasFreshHtml())
            downloadHtml();
        if (html == null)
            return;
        for (int i = 0; i < graphs.bmps.length; i++) {
            if (downloadHistory(i, context))
                break;
        }
    }

    private boolean downloadHistory(int i, Context context) {
        boolean downloaded = false;
        try {
            String fileName = graphs.getHistoryFileName(i);
            Bitmap bmp = null;
            if (graphs.isFreshFile(fileName, context))
                bmp = FileUtils.openBitmap(fileName, context);
            if (Downloader.isRunAlone()) {
                if (bmp == null) {
                    if (graphs.bmps[i] != null && graphs.isFresh(i)) {
                        bmp = graphs.bmps[i];
                        // System.out.println("history from ui to file");
                    } else {
                        bmp = downloadHistoryBitmap(i);
                        // System.out.println("history from web to file");
                    }
                    FileUtils.saveBitmap(fileName, bmp, context);
                    downloaded = true;
                }
                if (graphs.bmps[i] != null)
                    Utils.recycle(graphs.bmps, i);
            } else if (graphs.bmps[i] == null || !graphs.isFresh(i)) {
                if (bmp != null) {
                    // System.out.println("history from file to ui");
                    graphs.bmps[i] = bmp;
                } else {
                    // System.out.println("history from web to ui");
                    graphs.bmps[i] = downloadHistoryBitmap(i);
                }
                graphs.times[i] = System.currentTimeMillis();
                downloaded = true;
            }
        } catch (NullPointerException e) {
            System.err.print("Virhe latauksessa.");
            e.printStackTrace();
        }
        return downloaded;
    }

    private Bitmap downloadHistoryBitmap(int i) {
        Bitmap bmp = null;
        if (getHtml().contains(getId() + "-" + FragmentHistory.historyParameterIds[i])) {
            String url = "http://cdn.fmi.fi/weather-observations/products/graphs/observations-" + getId() + "-" + FragmentHistory.historyParameterIds[i] + "-fi.png";
            bmp = Utils.loadBitmapFromUrl(url);
        }
        return bmp;
    }

    public static class HistoryGraphs {
        public Bitmap[] bmps;
        public long[] times;

        public HistoryGraphs() {
            bmps = new Bitmap[FragmentHistory.historyParameterIds.length]; // TODO bugi, pitäisi olla vain niin monta kuin sivulla on graafeja
            times = new long[bmps.length];
        }

        public String getHistoryFileName(int i) {
            return FileUtils.HISTORYSTART + String.format("%02d", i) + "_" + FragmentHistory.historyParameterIds[i];
        }

        public boolean hasAllFresh() {
            for (int i = 0; i < bmps.length; i++) {
                if (bmps[i] == null || !isFresh(i)) {
                    return false;
                }
            }
            return true;
        }

        public boolean hasAllFreshSaved(Context context) {
            for (int i = 0; i < bmps.length; i++) {
                String fileName = getHistoryFileName(i);
                if (!FileUtils.hasFile(fileName, context) || !isFreshFile(fileName, context)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isFresh(int i) {
            return System.currentTimeMillis() - times[i] < Downloader.EXPIREDTIME;
        }

        public boolean isFreshFile(String filename, Context context) {
            return System.currentTimeMillis() - FileUtils.lastModified(filename, context) < Downloader.EXPIREDTIME;
        }
    }

    public void recycle() {
        Utils.recycle(graphs.bmps);
    }
}
