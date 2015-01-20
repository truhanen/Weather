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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Remember: always call super.onCreateView(...) when extending this class
 * to properly handle the viewCreated parameter.
 */
public abstract class TabFragment extends Fragment {
	
	private boolean viewCreated = false;
	
	abstract void refresh();
	
	abstract void makeIncomplete();
	
	boolean isViewCreated() {
		return viewCreated;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewCreated = true;
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onDestroyView() {
		viewCreated = false;
		super.onDestroyView();
	}
}
