package com.lc.bxm.entity;

import com.alibaba.fastjson.JSON;

public class LineName {

	private String lineName;

	public String getLineName() {
		return lineName;
	}

	public void setLineName(String lineName) {
		this.lineName = lineName;
	}
	
	public String getLineName(LineName lineName) {
		return JSON.toJSONString(lineName);
	}
}
