package com.lc.bxm.entity;


import java.io.InputStream;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 数据库测试实体类
 * 
 * @author lh
 */
public class TestConnection {
	//功能包上传文件存流
	private InputStream is;
	private String url;
	private String host;
	private String port;
	private String databaseName;
	private String state;
	private String userName;
	private String passWord;
	private Hashtable<String, String> hsatable;
	
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public InputStream getIs() {
		return is;
	}

	public void setIs(InputStream is) {
		this.is = is;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	private String token;
	
	public Hashtable<String, String> getHsatable() {
		return hsatable;
	}

	public void setHsatable(Hashtable<String, String> hsatable) {
		this.hsatable = hsatable;
	}

	HttpServletRequest request;
	HttpSession session;

	public HttpSession getSession() {
		return this.session;
	}

	public void setSession(HttpSession session) {
		this.session = session;
	}

	public HttpServletRequest getRequest() {
		return this.request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return this.port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDatabaseName() {
		return this.databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return this.passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
}
