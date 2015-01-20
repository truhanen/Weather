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
package fi.tuukka.weather.utils;

public class Condition {
    private int day;
    private int time;
    private int code;
    private int temp;
    float rain;

    public Condition(int day, int time, int code, int temp, float rain) {
        this.day = day;
        this.time = time;
        this.code = code;
        this.temp = temp;
        this.rain = rain;
    }

    public int day() {
        return day;
    }

    public int time() {
        return time;
    }

    public int code() {
        return code;
    }

    public int temp() {
        return temp;
    }

    public float rain() {
        return rain;
    }
}