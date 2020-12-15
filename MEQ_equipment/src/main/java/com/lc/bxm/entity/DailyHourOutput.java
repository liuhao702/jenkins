package com.lc.bxm.entity;

import com.alibaba.fastjson.JSON;

public class DailyHourOutput {

	private String columns;
	
	private String data;

	public String getColumns() {
		return columns;
	}

	public void setColumns(String columns) {
		this.columns = columns;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	public String getDailyHourOutput(DailyHourOutput dailyHourOutput) {
		return JSON.toJSONString(dailyHourOutput);
	}
}
