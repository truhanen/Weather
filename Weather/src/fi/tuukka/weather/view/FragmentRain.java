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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fi.tuukka.weather.R;
import fi.tuukka.weather.controller.ControllerCurrent;
import fi.tuukka.weather.controller.ControllerInterface;
import fi.tuukka.weather.controller.ControllerRain;
import fi.tuukka.weather.downloader.RainDownloader;
import fi.tuukka.weather.downloader.RainDownloader.RainType;
import fi.tuukka.weather.view.ActivityMain.Tab;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FragmentRain extends TabFragment {

    private View view;
    TextView textView1;
    TextView textView2;
    private RainLayoutBackground sadeLayout;
    private ControllerRain controller = new ControllerRain();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.sade, container, false);
        sadeLayout = (RainLayoutBackground) view.findViewById(R.id.rain_background);
        sadeLayout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                sadeLayout.chooseImage(event.getY(), controller);
                return true;
            }
        });
        sadeLayout.setFragmentRainInterface(new FragmentRainInterface() {
            @Override
            public void setLabel(RainType type, int index) {
                setTimeLabel(type, index);
            }
        });
        super.onCreateView(inflater, container, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                makeRainIncomplete();
                paivita();
            }
        });
        return view;
    }

    public void paivita() { // ks. latausjärjestys @ Lataaja.downloadRains()
        if (!controller.hasRain(RainType.MIN15, RainType.MIN15.steps - 1)) {
            System.out.println("no rainmap");
            return;
        }
        if (!controller.hasFreshHtml()) {
            System.out.println("no html");
            return;
        }
        if (!sadeLayout.firstRainShown())
            sadeLayout.setImage(RainType.MIN15, RainType.MIN15.steps - 1, controller);
        List<Integer>[] rainsDownloaded = controller.rainsDownloaded();
        refreshTime(rainsDownloaded);
        sadeLayout.setFinishedBars(rainsDownloaded, controller);
    }

    private void refreshTime(List<Integer>[] rainsDownloaded) {
        if (!controller.isFinished(getActivity().getApplicationContext()))
            textView2.setText("Ladataan... " + Integer.toString(rainsDownloaded[0].size() + rainsDownloaded[1].size())
                    + "/" + Integer.toString(RainType.MIN15.steps + RainType.H1.steps));
        else {
            String timeString = new SimpleDateFormat("HH:mm").format(new Date());
            textView2.setText(" Päivitetty klo " + timeString + " ");
        }
    }

    public void makeRainIncomplete() {
        textView1 = (TextView) view.findViewById(R.id.textView1);
        textView1.setText("");
        textView2 = (TextView) view.findViewById(R.id.textView2);
        textView2.setText("");
        if (sadeLayout != null)
            sadeLayout.setBackgroundColor(Color.BLACK);
        sadeLayout.setFirstRainShown(false);
        paivita();
    }

    public void setTimeLabel(RainType rainType, int index) {
        Date endDate = controller.getTime(rainType, index);
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

    @Override
    public void refresh() {
        paivita();
    }

    @Override
    public void makeIncomplete() {
        makeRainIncomplete();
    }
    
    @Override
    public int getTitleId() {
        return R.string.sadealueet;
    }

    @Override
    public ControllerInterface getController() {
        return controller;
    }
    
    public interface FragmentRainInterface {
        public void setLabel(RainType type, int index);
    }
}
