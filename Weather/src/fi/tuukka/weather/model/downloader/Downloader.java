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

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import fi.tuukka.weather.model.ModelCurrent;
import fi.tuukka.weather.model.ModelHistory;
import fi.tuukka.weather.model.ModelInterface;
import fi.tuukka.weather.model.ModelRain;
import fi.tuukka.weather.model.ModelStations;
import fi.tuukka.weather.model.ModelWarnings;
import fi.tuukka.weather.utils.Station;
import fi.tuukka.weather.view.ActivityMain;
import fi.tuukka.weather.view.FragmentStations;
import fi.tuukka.weather.view.FragmentWarnings;
import fi.tuukka.weather.view.ActivityMain.Frag;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

public class Downloader extends Service {

    public static final long EXPIREDTIME = 5 * 60 * 1000; // 5 minutes
    public static final long HTMLEXPIREDTIME = 1 * 60 * 1000; // one minute
    public static final int ERROR = -1;
    private static boolean runAlone = false;
    private final DownloaderRunnable downloaderRunnable = new DownloaderRunnable(new DownloaderHandler());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final DownloaderBinder downloaderBinder = new DownloaderBinder();
    private Frag currentTab = ActivityMain.FIRSTTAB;
    private ArrayList<Frag> finishedTabs = new ArrayList<Frag>();
    private DownloaderInterface downloaderIterface;
    private boolean errorAlreadySent = false;
    private boolean downloading = false;

    @Override
    public IBinder onBind(Intent intent) {
        return downloaderBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Station.chosen() == null)
            Station.setPreferences(this);
        downloadAll();
        // final ScheduledFuture downloaderHandle = scheduler.scheduleAtFixedRate(downloader, 0, 15, TimeUnit.SECONDS);
        // Runnable cancel = new Runnable() {
        // public void run() {
        // downloaderHandle.cancel(true);
        // }
        // };
        // scheduler.schedule(cancel, 15, TimeUnit.SECONDS);
        // SÄÄNNÖLLINEN LATAUS TOIMIMAAN
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        return START_NOT_STICKY;
    }

    public static boolean isRunAlone() {
        return runAlone;
    }

    public void download(Frag tab) {
        makeIncomplete(tab);
        scheduler.execute(downloaderRunnable);
    }

    public void downloadAll() {
        makeIncompleteAll();
        scheduler.execute(downloaderRunnable);
    }

    public void showError(String text) {
        if (errorAlreadySent)
            return;
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        errorAlreadySent = true;
    }

    private boolean isFinished() {
        return finishedTabs.size() == Frag.values().length;
    }

    private void makeIncompleteAll() {
        for (Frag tab : Frag.values())
            makeIncomplete(tab);
    }

    private void makeIncomplete(Frag tab) {
        finishedTabs.remove(tab);
        if (downloaderIterface != null && !tab.model.isFinished())
            downloaderIterface.makeIncomplete(tab);
        errorAlreadySent = false;
    }

    private class DownloaderRunnable implements Runnable {
        DownloaderHandler handler;

        public DownloaderRunnable(DownloaderHandler handler2) {
            this.handler = handler2;
        }

        public void run() {
            downloading = true;
            int i = 0;
            while (!isFinished()) {
                System.out.println(i++);
                if (i > 60)
                    break;
                for (Frag frag : Frag.values()) {
                    if (!finishedTabs.contains(frag) && (currentTab == frag || finishedTabs.contains(currentTab))) {
                        System.out.println(frag.name());
                        if (!frag.model.isFinished()) {
                            frag.model.downloadNext();
                        }
                        handler.sendEmptyMessage(frag.ordinal());
                        if (frag.model.isFinished()) {
                            finishedTabs.add(frag);
                        }
                    }
                }
            }
            downloading = false;
            System.out.println("ready");
        }
    };

    private class DownloaderHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int tabN = msg.what;
            if (tabN == ERROR) {
                showError("Virhe latauksessa.");
                errorAlreadySent = true;
                return;
            }
            if (downloaderIterface != null && !runAlone) {
                downloaderIterface.refresh(Frag.values()[tabN]);
            }
        }
    }

    public class DownloaderBinder extends Binder {
        public void addInterface(DownloaderInterface di) {
            downloaderIterface = di;
        }

        public void queryStations() {
            downloadAll();
        }

        public void changeStation(Station station) {
            Station.setChosen(station);
            Station.saveChosen();
            download(Frag.CURRENT);
            download(Frag.HISTORY);
        }

        public void tabChanged(Frag tab2) {
            currentTab = tab2;
        }

        public void refresh(Frag tab) {
            download(tab);
        }

        public void refreshAll() {
            downloadAll();
        }

        public void runDownloaderAlone(boolean b) {
            if (b != runAlone) {
                runAlone = b;
                if (downloading)
                    finishedTabs.clear();
                else
                    downloadAll();
            }
        }
    }

    public static interface DownloaderInterface {
        void refresh(Frag tab);

        void makeIncomplete(Frag tab);
    }
}
