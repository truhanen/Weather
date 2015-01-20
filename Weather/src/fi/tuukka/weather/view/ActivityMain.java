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
package fi.tuukka.weather.view;

import fi.tuukka.weather.R;
import fi.tuukka.weather.model.ModelCurrent;
import fi.tuukka.weather.model.ModelHistory;
import fi.tuukka.weather.model.ModelInterface;
import fi.tuukka.weather.model.ModelRain;
import fi.tuukka.weather.model.ModelStations;
import fi.tuukka.weather.model.ModelWarnings;
import fi.tuukka.weather.model.downloader.Downloader;
import fi.tuukka.weather.model.downloader.Downloader.DownloaderBinder;
import fi.tuukka.weather.model.downloader.Downloader.DownloaderInterface;
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
	public static final Frag FIRSTTAB = Frag.CURRENT;
	private DownloaderBinder downloaderBinder;
	private ViewPager viewPager;
	private WeatherFragmentPagerAdapter pagerAdapter;
	public static enum Frag {
		CURRENT(new ModelCurrent(), new FragmentCurrent(), R.string.tuorein),
		RAIN(new ModelRain(), new FragmentRain(), R.string.sadealueet),
		HISTORY(new ModelHistory(), new FragmentHistory(), R.string.historia),
		WARNINGS(new ModelWarnings(), new FragmentWarnings(), R.string.varoitukset),
		STATIONS(new ModelStations(), new FragmentStations(), R.string.asemat);
		public TabFragment tab;
		public ModelInterface model;
		public int titleId;
		Frag(ModelInterface model, TabFragment tab, int titleId) {
			this.titleId = titleId; this.tab = tab; this.model = model;
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
				public void refresh(Frag tab) {
					if (tab.tab.isViewCreated())
						tab.tab.refresh();
				}
				@Override
				public void makeIncomplete(Frag tab) {
					if (tab.tab.isViewCreated())
						tab.tab.makeIncomplete();
				}
//				@Override
//				public boolean isFinished(Frag tab) {
//					return tab.tab.isFinished();
//				}
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
		Frag tab = Frag.values()[viewPager.getCurrentItem()];
	    switch (item.getItemId()) {
	    case R.id.refresh:
	    	if (downloaderBinder != null)
	    		downloaderBinder.refresh(tab);
	        return true;
	    case R.id.refreshAll:
	    	if (downloaderBinder != null)
	    		downloaderBinder.refresh(tab); // TODO wrong
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
			return (Fragment) Frag.values()[arg0].tab;
		}
		@Override
		public int getCount() {
			return Frag.values().length;
		}
		public String getPageTitle(int i) {
			return getString(Frag.values()[i].titleId);
		}
    }
}
