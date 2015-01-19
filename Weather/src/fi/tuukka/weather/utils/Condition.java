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