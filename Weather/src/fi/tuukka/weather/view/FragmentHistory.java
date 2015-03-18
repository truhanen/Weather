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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import fi.tuukka.weather.R;
import fi.tuukka.weather.controller.ControllerCurrent;
import fi.tuukka.weather.controller.ControllerHistory;
import fi.tuukka.weather.controller.ControllerInterface;
import fi.tuukka.weather.utils.Station;
import fi.tuukka.weather.utils.Utils;
import fi.tuukka.weather.view.ActivityMain.Tab;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class FragmentHistory extends TabFragment {

    private TextView textView;
    private LinearLayout root;
    private View view;
    private ControllerHistory controller = new ControllerHistory();
    public static final String[] historyParameterIds = {
            "4", // L�mp�tila
            "13", // Kosteus
            "21", // Tuuli
            "1", // Paine
            "353", // Sade
            "79" }; // Pilvisyys
    public static final String[] parameterNames = {
            " Lämpötila ",
            " Kosteus ",
            " Tuuli ",
            " Paine ",
            " Sade ",
            " Pilvisyys "
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.grid, container, false);
        textView = (TextView) view.findViewById(R.id.TextView08);
        root = (LinearLayout) view.findViewById(R.id.linearLayout12);
        makeHistoryIncomplete();
        if (controller.getHistoryGraphs(getActivity().getApplicationContext()) != null)
            refreshGraphs();
        super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    public void refreshGraphs() {
        final Activity activity = getActivity();
        final Bitmap[] graphs = controller.getHistoryGraphs(getActivity().getApplicationContext());
        if (graphs == null) {
            refreshTime();
            return;
        }
        LinearLayout[] graphLayouts = new LinearLayout[root.getChildCount()];
        for (int i = 0; i < root.getChildCount(); i++) {
            graphLayouts[i] = (LinearLayout) root.getChildAt(i);
        }
        for (int i = 0; i < graphs.length; i++) {
            if (graphs[i] == null) {
                break;
            }
            // do not add the same graph twice (in case refreshGraphs has been called twice)
            if (i < graphLayouts.length / 2 && graphLayouts[i * 2 + 1].getId() == i) {
                continue;
            }
            LinearLayout empty = new LinearLayout(activity);
            empty.setMinimumHeight(5);
            root.addView(empty);
            // add graphs to new LinearLayouts
            LinearLayout graph = new LinearLayout(activity);
            int heightDp = 120;
            int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp, getResources().getDisplayMetrics());
            graph.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, heightPx));
            graph.setGravity(Gravity.CENTER);
            graph.setId(i);
            graph.setBackgroundDrawable(new BitmapDrawable(getResources(), graphs[i]));
            graph.setOnClickListener(new View.OnClickListener() {
                public void onClick(View layout) {
                    Utils.showImage(activity, layout, graphs[layout.getId()]);
                }
            });
            root.addView(graph);
            // add textfield
            TextView text = new TextView(activity);
            text.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            text.setBackgroundColor(getResources().getColor(R.color.black));
            text.setTextColor(Color.WHITE);
            text.setText(parameterNames[i]);
            graph.addView(text);
        }

        refreshTime();
    }

    public void makeHistoryIncomplete() {
        root.removeAllViews();
        refreshTime();
    }

    private void refreshTime() {
        if (!controller.isFinished(getActivity().getApplicationContext()))
            textView.setText("Ladataan... " + Integer.toString(controller.historiesDownloaded(getActivity().getApplicationContext())) + "/" + Integer.toString(controller.totalHistories(getActivity().getApplicationContext())));
        else {
            String timeString = new SimpleDateFormat("HH:mm").format(new Date());
            textView.setText(timeString + " @ " + controller.getStationName(getActivity().getApplicationContext()));
        }
    }

    public Object fetch(String address) throws MalformedURLException,
            IOException {
        URL url = new URL(address);
        Object content = url.getContent();
        return content;
    }

    @Override
    public void refresh() {
        refreshGraphs();
    }

    @Override
    public void makeIncomplete() {
        makeHistoryIncomplete();
    }
    
    @Override
    public int getTitleId() {
        return R.string.historia;
    }

    @Override
    public ControllerInterface getController() {
        return controller;
    }
}
