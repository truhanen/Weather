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
package fi.tuukka.weather.downloader;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import fi.tuukka.weather.controller.ControllerCurrent;
import fi.tuukka.weather.controller.ControllerHistory;
import fi.tuukka.weather.controller.ControllerInterface;
import fi.tuukka.weather.controller.ControllerRain;
import fi.tuukka.weather.controller.ControllerStations;
import fi.tuukka.weather.controller.ControllerWarnings;
import fi.tuukka.weather.utils.Station;
import fi.tuukka.weather.view.ActivityMain;
import fi.tuukka.weather.view.FragmentStations;
import fi.tuukka.weather.view.FragmentWarnings;
import fi.tuukka.weather.view.ActivityMain.Tab;
import android.app.Service;
import android.content.Context;
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
    private Tab currentTab = ActivityMain.FIRSTTAB;
    private ArrayList<Tab> finishedTabs = new ArrayList<Tab>();
    private DownloaderInterface downloaderInterface;
    private boolean errorAlreadySent = false;
    private boolean downloading = false;

    @Override
    public IBinder onBind(Intent intent) {
        return downloaderBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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

    public void download(Tab tab) {
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
        return finishedTabs.size() == Tab.values().length;
    }

    private void makeIncompleteAll() {
        for (Tab tab : Tab.values())
            makeIncomplete(tab);
    }

    private void makeIncomplete(Tab frag) {
        finishedTabs.remove(frag);
        if (downloaderInterface != null && !frag.frag.getController().isFinished(getApplicationContext()))
            downloaderInterface.makeIncomplete(frag);
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
            Context context = getApplicationContext();
            while (!isFinished()) {
                System.out.println(i++);
                if (i > 60)
                    break;
                for (Tab tab : Tab.values()) {
                    if (!finishedTabs.contains(tab) && (currentTab == tab || finishedTabs.contains(currentTab))) {
//                        System.out.println(tab.name());
                        ControllerInterface controller = tab.frag.getController();
                        if (!controller.isFinished(context)) {
                            controller.downloadNext(context);
                        }
                        handler.sendEmptyMessage(tab.ordinal());
                        if (controller.isFinished(context)) {
                            finishedTabs.add(tab);
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
            if (downloaderInterface != null && !runAlone) {
                downloaderInterface.refresh(Tab.values()[tabN]);
            }
        }
    }

    public class DownloaderBinder extends Binder {
        public void addInterface(DownloaderInterface di) {
            downloaderInterface = di;
        }

        public void queryStations() {
            downloadAll();
        }

        public void changeStation(Station station) {
            Station.setChosen(station);
            Station.saveChosen(getApplicationContext());
            download(Tab.CURRENT);
            download(Tab.HISTORY);
        }

        public void tabChanged(Tab tab2) {
            currentTab = tab2;
        }

        public void refresh(Tab tab) {
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
        void refresh(Tab tab);

        void makeIncomplete(Tab tab);
    }
}
