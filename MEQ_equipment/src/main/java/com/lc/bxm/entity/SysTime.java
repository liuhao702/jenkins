package com.lc.bxm.entity;

import com.alibaba.fastjson.JSON;

public class SysTime {

//	private String[] time;

//	public String[] getTime() {
//		return time;
//	}
//
//	public void setTime(String[] time) {
//		this.time = time;
//	}
	
	private String time;
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getSysTime(SysTime sysTime) {
		return JSON.toJSONString(sysTime.time.split("-"));
	}
}
