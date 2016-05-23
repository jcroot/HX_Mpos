package com.lk.td.pay.beans;

import java.io.Serializable;

public class CalendarDay implements Serializable{
	
	private CalendarDay[] days;
	/**
	 * 
	 */
	private static final long serialVersionUID = 4900709105315976895L;

	private String year;
	
	private String month;
	
	private String day;
	
	private int total;

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public CalendarDay[] getDays() {
		return days;
	}

	public void setDays(CalendarDay[] days) {
		this.days = days;
	}
	
	

}
