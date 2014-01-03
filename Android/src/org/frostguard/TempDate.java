package org.frostguard;

import java.util.Date;

public class TempDate {
	private Integer temperature;
	private Date fromTime;
	private Long id;

	public TempDate() {
	}
	@Override
	public String toString() {
		return "TempDate [temperature=" + temperature + ", fromTime="
				+ fromTime + "]";
	}

	public TempDate(Integer temperature, Date fromTime) {
		super();
		this.temperature = temperature;
		this.fromTime = fromTime;
	}

	public Integer getTemperature() {
		return temperature;
	}

	public Date getFromTime() {
		return fromTime;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}