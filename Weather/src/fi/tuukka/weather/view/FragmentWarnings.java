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
import fi.tuukka.weather.controller.ControllerInterface;
import fi.tuukka.weather.controller.ControllerWarnings;
import fi.tuukka.weather.utils.Utils;
import fi.tuukka.weather.view.ActivityMain.Tab;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class FragmentWarnings extends TabFragment {

    LinearLayout layout;
    private View view;
    private ControllerWarnings controller = new ControllerWarnings();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.varoitukset, container, false);
        layout = (LinearLayout) view.findViewById(R.id.varoitusLayout);
        makeButtonWork();
        if (controller.warnings() != null)
            refresh();
        super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    private void makeButtonWork() {
        final Activity activity = getActivity();
        layout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View layout) {
                Utils.showImage(activity, layout, BitmapFactory.decodeResource(getResources(), R.drawable.warnings_legend_fi));
            }
        });
    }

    @Override
    public void refresh() {
        if (controller.warnings() != null)
            Utils.setBackGroundDrawable(getActivity(), layout, controller.warnings());
    }

    @Override
    public void makeIncomplete() {
        layout.setBackgroundColor(Color.BLACK);
    }
    
    @Override
    public int getTitleId() {
        return R.string.varoitukset;
    }

    @Override
    public ControllerInterface getController() {
        return controller;
    }
}
