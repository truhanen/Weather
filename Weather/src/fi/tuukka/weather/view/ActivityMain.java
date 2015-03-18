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
package fi.tuukka.weather.view;

import fi.tuukka.weather.R;
import fi.tuukka.weather.controller.ControllerCurrent;
import fi.tuukka.weather.controller.ControllerHistory;
import fi.tuukka.weather.controller.ControllerInterface;
import fi.tuukka.weather.controller.ControllerRain;
import fi.tuukka.weather.controller.ControllerStations;
import fi.tuukka.weather.controller.ControllerWarnings;
import fi.tuukka.weather.downloader.Downloader;
import fi.tuukka.weather.downloader.Downloader.DownloaderBinder;
import fi.tuukka.weather.downloader.Downloader.DownloaderInterface;
import fi.tuukka.weather.utils.Station;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ActivityMain extends FragmentActivity {

    public static final String PREFS_FILENAME = "weatherprefs";
    public static final Tab FIRSTTAB = Tab.CURRENT;
    private DownloaderBinder downloaderBinder;
    private ViewPager viewPager;
    private WeatherFragmentPagerAdapter pagerAdapter;

    /**
     * All tabs and their fragments.
     */
    public static enum Tab {
        CURRENT(new FragmentCurrent()),
        RAIN(new FragmentRain()),
        HISTORY(new FragmentHistory()),
        WARNINGS(new FragmentWarnings()),
        STATIONS(new FragmentStations());
        
        public TabFragment frag;

        Tab(TabFragment frag) {
            this.frag = frag;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        pagerAdapter = new WeatherFragmentPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(FIRSTTAB.ordinal());
        bindService(new Intent(this, Downloader.class), downloaderConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection downloaderConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            downloaderBinder = (DownloaderBinder) service;
            downloaderBinder.addInterface(new DownloaderInterface() {
                @Override
                public void refresh(Tab tab) {
                    if (tab.frag.isViewCreated())
                        tab.frag.refresh();
                }

                @Override
                public void makeIncomplete(Tab tab) {
                    if (tab.frag.isViewCreated())
                        tab.frag.makeIncomplete();
                }
                // @Override
                // public boolean isFinished(Frag tab) {
                // return tab.tab.isFinished();
                // }
            });
            downloaderBinder.runDownloaderAlone(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            return;
        }
    };

    public void queryStations() {
        downloaderBinder.queryStations();
    }

    public void changeStation(Station station) {
        downloaderBinder.changeStation(station);
    }

    @Override
    public void onDestroy() {
        if (downloaderBinder != null)
            downloaderBinder.runDownloaderAlone(true);
        unbindService(downloaderConnection);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Tab tab = Tab.values()[viewPager.getCurrentItem()];
        switch (item.getItemId()) {
        case R.id.refresh:
            if (downloaderBinder != null)
                downloaderBinder.refresh(tab);
            return true;
        case R.id.refreshAll:
            if (downloaderBinder != null)
                downloaderBinder.refresh(tab); // TODO wrong, should refresh all
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public class WeatherFragmentPagerAdapter extends FragmentPagerAdapter {
        public WeatherFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {
            return (Fragment) Tab.values()[arg0].frag;
        }

        @Override
        public int getCount() {
            return Tab.values().length;
        }

        public String getPageTitle(int i) {
            return getString(Tab.values()[i].frag.getTitleId());
        }
    }
}
