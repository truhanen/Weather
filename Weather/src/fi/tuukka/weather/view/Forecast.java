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

import fi.tuukka.weather.R;
import fi.tuukka.weather.utils.Condition;
import fi.tuukka.weather.utils.GraphData;
import fi.tuukka.weather.utils.Station;
import fi.tuukka.weather.utils.Utils;
import android.app.Activity;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class Forecast {

    private FragmentCurrent tuorein;
    private LinearLayout ennusteLayout;

    private ArrayList<Condition> shortConds;
    private ArrayList<Condition> longConds;
    private ArrayList<Condition> conds;
    private TextView ennusteHeader;
    private static boolean rightSourceConfirmed = false;

    public Forecast(FragmentCurrent fragmentCurrent, LinearLayout lo, TextView tv) {
        this.tuorein = fragmentCurrent;
        this.ennusteLayout = lo;
        this.ennusteHeader = tv;
        makeIncomplete();
    }

    public void paivita() {
        String html = Station.chosen(tuorein.getActivity().getApplicationContext()).getHtml();
        // checkRightSource(lataaja.station.getForecastUrl(), html);
        String shortHtml = html.substring(html.indexOf("short local-weather-forecast meteogram"), html.indexOf("mid local-weather-forecast meteogram"));
        ArrayList<Integer> shortColspans = getColspans(shortHtml);
        shortConds = getConds(shortHtml, shortColspans);
        GraphData graphData = new GraphData(html);
        longConds = graphData.getConds();
        combineForecasts();
        view();
        refreshTime();
    }

    private void combineForecasts() {
        conds = shortConds;

        int lastShortDay = conds.get(conds.size() - 1).day();
        int lastShortTime = conds.get(conds.size() - 1).time();
        for (int i = 0; i < longConds.size(); i++) {
            if (longConds.get(i).day() == lastShortDay) {
                for (; i < longConds.size(); i++) {
                    boolean notInShortConds = (longConds.get(i).day() == lastShortDay
                            && longConds.get(i).time() > lastShortTime)
                            || longConds.get(i).day() != lastShortDay;
                    if (notInShortConds)
                        break;
                }
                for (; i < longConds.size(); i++)
                    conds.add(longConds.get(i));
                break;
            }
        }
    }

    private ArrayList<Condition> getConds(String html, ArrayList<Integer> colspans) {
        ArrayList<Integer> days = getDays(html, colspans);
        ArrayList<Integer> times = getTimes(html);
        ArrayList<Integer> codes = getCodes(html);
        ArrayList<Integer> temps = getTemperatures(html);
        ArrayList<Float> rains = getRains(html);
        ArrayList<Condition> conds = new ArrayList<Condition>();
        int timeSteps = 0;
        for (int i = 0; i < colspans.size(); i++) {
            timeSteps = timeSteps + colspans.get(i);
        }
        for (int i = 0; i < timeSteps; i++) {
            conds.add(new Condition(days.get(i), times.get(i), codes.get(i), temps.get(i), rains.get(i)));
        }
        return conds;
    }

    public void makeIncomplete() {
        ennusteHeader.setText("Ennuste (ladataan...)");
        ennusteLayout.removeAllViews();
    }

    private void refreshTime() {
        String timeString = new SimpleDateFormat("HH:mm").format(new Date());
        ennusteHeader.setText("Ennuste (" + timeString + " @ "
                              + Station.chosen(tuorein.getActivity().getApplicationContext()).getForecastPlaceName() + ")");
    }

    private ArrayList<Integer> getColspans(String html) {
        ArrayList<Integer> spans = new ArrayList<Integer>();
        String range = html.substring(html.indexOf("meteogram-dates"), html.indexOf("meteogram-times"));
        int index = 0;
        while ((index = range.indexOf("colspan", index + 1)) != -1) {
            String span = range.substring(index + 9, range.indexOf("\"", index + 9));
            spans.add(Integer.parseInt(span));
        }
        return spans;
    }

    private ArrayList<Integer> getDays(String html, ArrayList<Integer> colspans) {
        ArrayList<Integer> days = new ArrayList<Integer>();
        String range = html.substring(html.indexOf("meteogram-dates"), html.indexOf("meteogram-times"));
        int index = 0;
        int spanInd = 0;
        while ((index = range.indexOf("kuuta ", index + 1)) != -1) {
            int day = Utils.getDay(range.substring(index + 12, index + 14));
            for (int i = 0; i < colspans.get(spanInd); i++) {
                days.add(day);
            }
            spanInd++;
        }
        return days;
    }

    private ArrayList<Integer> getTimes(String html) {
        ArrayList<Integer> times = new ArrayList<Integer>();
        String range = html.substring(html.indexOf("meteogram-times"), html.indexOf("meteogram-weather-symbols"));
        int index = 0;
        while ((index = range.indexOf(":00", index + 1)) != -1) {
            times.add(Integer.parseInt(range.substring(index - 2, index).trim()));
        }
        return times;
    }

    private ArrayList<Integer> getCodes(String html) {
        ArrayList<Integer> codes = new ArrayList<Integer>();
        String range = html.substring(html.indexOf("meteogram-weather-symbols"), html.indexOf("meteogram-temperatures"));
        int index = 0;
        while ((index = range.indexOf("class=\"w", index + 1)) != -1) {
            String raw = range.substring(index + 29, range.indexOf('"', index + 29));
            int code = Integer.parseInt(raw);
            codes.add(code);
        }
        return codes;
    }

    private ArrayList<Integer> getTemperatures(String html) {
        ArrayList<Integer> temps = new ArrayList<Integer>();
        String range = html.substring(html.indexOf("meteogram-temperatures"), html.indexOf("meteogram-wind-symbols"));
        int index = 0;
        while ((index = range.indexOf("tila", index + 1)) != -1) {
            temps.add(Integer.parseInt(range.substring(index + 5, range.indexOf("°C", index) - 1)));
        }
        return temps;
    }

    private ArrayList<Float> getRains(String html) {
        ArrayList<Float> rains = new ArrayList<Float>();
        String range = html.substring(html.indexOf("meteogram-hourly-precipitation-values"), html.indexOf("</tbody> </table> </div>"));
        int index = 0;
        while ((index = range.indexOf("title", index + 1)) != -1) {
            String rain = range.substring(index + 34, range.indexOf("mm", index) - 1);
            rains.add(Float.parseFloat(rain.replace(',', '.')));
        }
        return rains;
    }

    private void view() {
        Activity activity = tuorein.getActivity();
        ennusteLayout.removeAllViews();
        int fontsize = Utils.getScaledFont(activity);
        int[] widths = Utils.getEnnusteWidths(activity);
        int position = 0;
        int day = 0;
        for (int i = 0; i < conds.size(); i++) {
            if (day == 0 || conds.get(i).day() != day) {
                // tablerow for the weekday
                LinearLayout dayLayout = new LinearLayout(activity);
                ennusteLayout.addView(dayLayout);
                // textview for the weekday
                TextView dayView = new TextView(activity);
                dayView.setText(Utils.getDay(conds.get(i).day()));
                dayView.setTextAppearance(activity, R.style.TextStyle);
                dayView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontsize);
                dayView.setGravity(Gravity.CENTER);
                dayView.setWidth(widths[0]);
                dayLayout.addView(dayView);
                day = conds.get(i).day();
            }
            // horizontal layout for time and conditions
            LinearLayout timeConditionLayout = new LinearLayout(activity);
            ennusteLayout.addView(timeConditionLayout);

            // TextView for the time
            TextView timeView = new TextView(activity);
            timeView.setGravity(Gravity.CENTER);
            timeView.setText(Integer.toString(conds.get(position).time()) + ":00");
            timeView.setTextAppearance(activity, R.style.TextStyle);
            timeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontsize);
            timeView.setWidth(widths[0]);
            timeConditionLayout.addView(timeView);

            // TextView for the temperatures
            TextView tempView = new TextView(activity);
            tempView.setGravity(Gravity.CENTER);
            tempView.setTextAppearance(activity.getApplicationContext(), R.style.TextStyle);
            tempView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontsize);
            tempView.setWidth(widths[1]);
            int color = Utils.getTempColor(conds.get(position).temp());
            tempView.setTextColor(color);
            tempView.setText(conds.get(position).temp() + "°C");
            timeConditionLayout.addView(tempView);

            // TextView for the conditions
            TextView condView = new TextView(activity);
            condView.setGravity(Gravity.CENTER_VERTICAL);
            condView.setTextAppearance(activity, R.style.TextStyle);
            condView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontsize);
            if (Utils.isRainCode(conds.get(position).code())) {
                condView.setTextColor(tuorein.getResources().getColor(R.color.rain));
            } else {
                condView.setTextColor(tuorein.getResources()
                        .getColor(R.color.white));
            }
            condView.setText(Utils.getDescription(conds.get(position).code()));
            // condView.setText("selkeää");
            timeConditionLayout.addView(condView);

            // TextView for the rains
            TextView rainView = new TextView(activity);
            rainView.setText(Float.toString(conds.get(position).rain()).replace('.', ',') + " mm");
            rainView.setTextAppearance(activity, R.style.TextStyle);
            rainView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontsize);
            rainView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            rainView.setGravity(Gravity.CENTER);
            rainView.setGravity(Gravity.RIGHT);
            timeConditionLayout.addView(rainView);
            position++;
        }
    }

    public static void setSourceConfirmed(boolean isTrue) {
        rightSourceConfirmed = isTrue;
    }
}
