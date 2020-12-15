package com.lc.bxm.entity;

import java.util.Date;

import com.alibaba.fastjson.JSON;

public class SysUsers {

	private String userUid;
	private String userCode;
	private String userName;
	private Date createdDate;

	public String getUserUid() {
		return userUid;
	}

	public void setUserUid(String userUid) {
		this.userUid = userUid;
	}

	public String getUserCode() {
		return userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String jsonString(SysUsers sysUsers) {
		return JSON.toJSONString(sysUsers);
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
}
