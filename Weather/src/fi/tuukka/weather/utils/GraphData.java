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
package fi.tuukka.weather.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class GraphData {
	
	ArrayList<Float> temps;
	ArrayList<Integer> codes;
	ArrayList<Float> rains;
	ArrayList<Long> allTimes;
	ArrayList<Long> lessTimes;
	
	public GraphData(String html) {
		parseData(html);
	}
	
	
	private void parseData(String html) {
		int rangeStart = html.indexOf("long forecast graph");
		String range = html.substring(rangeStart, html.indexOf("/script", rangeStart));
		getTemps(range);
		getCodes(range);
		getRains(range);
	}
	
	
	private void getTemps(String html) {
		allTimes = new ArrayList<Long>();
		temps = new ArrayList<Float>();
		int tempStart = html.indexOf("temperature:") + 13;
		int tempEnd = html.indexOf("]]", tempStart) + 1;
		String tempRange = html.substring(tempStart, tempEnd);
		int i = 0;
		while ((i = tempRange.indexOf('[', i+1)) != -1) {
			int j = tempRange.indexOf(',', i);
			int k = tempRange.indexOf(']', i);
			long millis = Long.parseLong(tempRange.substring(i+1, j));
			allTimes.add(millis);
			temps.add(Float.parseFloat(tempRange.substring(j+1, k)));
		}
	}
	
	
	private void getCodes(String html) {
		codes = new ArrayList<Integer>();
		int codeStart = html.indexOf("weather_code:") + 14;
		int codeEnd = html.indexOf("]]", codeStart) + 1;
		String codeRange = html.substring(codeStart, codeEnd);
		int i = 0;
		while ((i = codeRange.indexOf('[', i+1)) != -1) {
			int j = codeRange.indexOf(',', i);
			int k = codeRange.indexOf(']', i);
			codes.add(Integer.parseInt(codeRange.substring(j+1, k)));
		}
	}
	
	
	private void getRains(String html) {
		lessTimes = new ArrayList<Long>();
		rains = new ArrayList<Float>();
		int rainStart = html.indexOf("precipitation:") + 15;
		int rainEnd = html.indexOf("]]", rainStart) + 1;
		String rainRange = html.substring(rainStart, rainEnd);
		int i = 0;
		while ((i = rainRange.indexOf('[', i+1)) != -1) {
			int k = i;
			for (int j=0; j<3; j++) k = rainRange.indexOf(',', k+1);
			int l = rainRange.indexOf(',', k+1);
			long millis = Long.parseLong(rainRange.substring(i+1, rainRange.indexOf(',', i)));
			lessTimes.add(millis);
			rains.add(Float.parseFloat(rainRange.substring(k+1, l)));
		}
	}
	
	
	public ArrayList<Condition> getConds() {
		ArrayList<Condition> conds = new ArrayList<Condition>();
		int j = 0;
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeZone(TimeZone.getTimeZone("GMT"));
		for (int i=0; i<allTimes.size(); i++) {
			gc.setTimeInMillis(allTimes.get(i));
			int hour = gc.get(Calendar.HOUR_OF_DAY);
			if (hour == 3 || hour == 9 || hour == 15 || hour == 21) {
				int day = gc.get(Calendar.DAY_OF_WEEK);
				int code = codes.get(i);
				int temp = Math.round(temps.get(i));
				float rain = rains.get(j++);
				conds.add(new Condition(day, hour, code, temp, rain));
			}
		}
		return conds;
	}
}
