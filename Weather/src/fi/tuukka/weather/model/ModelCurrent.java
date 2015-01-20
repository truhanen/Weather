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
package fi.tuukka.weather.model;

import fi.tuukka.weather.utils.Station;

public class ModelCurrent implements ModelInterface {

	@Override
	public boolean isFinished() {
		return Station.chosen().hasFreshHtml();
	}

	@Override
	public void downloadNext() {
		Station.chosen().downloadHtml();
	}
	
	public String getHtml() {
		return Station.chosen().getHtml();
	}
	
	public String getStationName() {
		return Station.chosen().getStationName();
	}
}
