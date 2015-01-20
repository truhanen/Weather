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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import fi.tuukka.weather.R;
import fi.tuukka.weather.model.ModelCurrent;
import fi.tuukka.weather.model.ModelRain;
import fi.tuukka.weather.model.downloader.RainDownloader;
import fi.tuukka.weather.model.downloader.RainDownloader.RainType;
import fi.tuukka.weather.view.ActivityMain.Frag;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FragmentRain extends TabFragment {
	
	private View view;
	TextView textView1;
	TextView textView2;
	private ModelRain model;
	private LinearLayout sadeLayout;
	private boolean firstRainShown = false;
	private boolean limitsInitialized = false;
	private int[] touchLimits;
	private RainType[] rainType;
	private int[] rainInd;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.sade, container, false);
		model = (ModelRain) Frag.RAIN.model;
		sadeLayout = (LinearLayout) view.findViewById(R.id.sadeLayout);
		setTextViewBackgroundColor();
		onTouchProcess();
		makeRainIncomplete();
		paivita();
		super.onCreateView(inflater, container, savedInstanceState);
		return view;
	}
	
	
	public void paivita() { // ks. latausjärjestys @ Lataaja.downloadRains()
		if (!firstRainShown)
			setImage(RainType.MIN15, RainType.MIN15.steps-1);
		refreshTime();
	}
	
	
	private void refreshTime() {
		if (!model.isFinished())
			textView2.setText("Ladataan... " + Integer.toString(model.rainsDownloaded()) + "/" + Integer.toString(RainType.MIN15.steps + RainType.H1.steps));
		else {
			String timeString = new SimpleDateFormat("HH:mm").format(new Date());
			textView2.setText(" Päivitetty klo " + timeString + " ");
		}
	}
	
	public void makeRainIncomplete(){
		textView1 = (TextView) view.findViewById(R.id.textView1);
		textView1.setText("");
		textView2 = (TextView) view.findViewById(R.id.textView2);
		textView2.setText("");
		if (sadeLayout != null)
			sadeLayout.setBackgroundColor(Color.BLACK);
		firstRainShown = false;
		refreshTime();
	}
	

	private void setTextViewBackgroundColor() {
		view.findViewById(R.id.textView1).setBackgroundColor(getResources().getColor(R.color.black));
		view.findViewById(R.id.textView2).setBackgroundColor(getResources().getColor(R.color.black));
	}

	private void setImage(RainType rainType, int index) {
		if (!model.hasRain(rainType, index))
			return;
		sadeLayout.setBackgroundDrawable(new BitmapDrawable(getResources(), model.getRain(rainType, index)));
		firstRainShown = true;
		setLabel(rainType, index);
	}
	
	private void setLabel(RainType rainType, int index) {
		Date endDate = model.getTime(rainType, index);
		SimpleDateFormat formatter = new SimpleDateFormat("d.M. HH:mm");
		SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
		StringBuilder sb = new StringBuilder();
		Date startDate = (Date) endDate.clone();
		if (rainType == RainType.MIN15)
			startDate.setTime(endDate.getTime() - 900000l);
		else
			startDate.setTime(endDate.getTime() - 3600000l);
		sb.append(formatter.format(startDate) + " - " + timeFormatter.format(endDate) + " ");
		if (rainType == RainType.H1 && index > 11)
			sb.append("(Ennuste) ");
		textView1.setText(sb);
	}

	private void onTouchProcess() {
		sadeLayout.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				chooseImage(event.getY());
				return true;
			}
		});
	}
	
	
	private void initializeLimits() {
		Date first15minTime = model.getTime(RainType.MIN15, 0);
		ArrayList<RainType> rainType2 = new ArrayList<RainType>();
		ArrayList<Integer> rainInd2 = new ArrayList<Integer>();
		ArrayList<Integer> mins = new ArrayList<Integer>();
		int summins = 0;
		for (int i=0; first15minTime.after(model.getTime(RainType.H1, i)) && i<RainType.H1.steps; i++) {
			rainType2.add(RainType.H1);
			rainInd2.add(i);
			mins.add(60);
			summins += 60;
		}
		for (int i=0; i<RainType.MIN15.steps; i++) {
			rainType2.add(RainType.MIN15);
			rainInd2.add(i);
			mins.add(15);
			summins += 15;
		}
		for (int i=RainDownloader.FIRSTFORECASTIND; i<RainType.H1.steps; i++) {
			rainType2.add(RainType.H1);
			rainInd2.add(i);
			mins.add(60);
			summins += 60;
		}
		// set touchLimits
		int height = sadeLayout.getHeight();
		ArrayList<Integer> touchLimits2 = new ArrayList<Integer>();
		touchLimits2.add(0);
		for (int i=0; i<mins.size(); i++) {
			touchLimits2.add((int) (touchLimits2.get(i) + height * mins.get(i) * 1.0 / summins));
		}
		touchLimits = new int[touchLimits2.size()];
		touchLimits[0] = 0;
		rainType = new RainType[rainType2.size()];
		rainInd = new int[rainInd2.size()];
		for (int i=0; i<rainType.length; i++) {
			rainType[i] = rainType2.get(i);
			rainInd[i] = rainInd2.get(i);
			touchLimits[i+1] = touchLimits2.get(i+1);
		}
		
		limitsInitialized = true;
	}
	

	protected void chooseImage(float y) {
		if (!limitsInitialized)
			initializeLimits();
		for (int i = 0; i < touchLimits.length-1; i++) {
			if (y >= touchLimits[i] && y <= touchLimits[i+1]) {
				setImage(rainType[i], rainInd[i]);
				break;
			}
		}
	}
	@Override
	public void refresh() {
		paivita();
	}
	
	@Override
	public void makeIncomplete() {
		makeRainIncomplete();
	}
}
