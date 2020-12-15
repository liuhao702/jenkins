package com.lc.bxm.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.entity.TestConnection;

/**
 * 获取用户操作日志和系统日志
 * @author JF
 * @date 2019年6月11日
 */
@Service
public class GetLogs {
	
	@Autowired
	PostgreSQLConn dbConn;
	
	@Autowired
	TestConnection testConn;
	/**
	 * 获取用户操作日志
	 */
	public void getUserLog(HttpServletRequest request,String id,String opeType,boolean successFully,String fromJson,String toJson,String creator) {
		String ip = getIpAddress(request);
		String sql = null;
		Integer comp_id=0;
		
		ResultSet rs = dbConn.query("select comp_id from sys_users where user_uid = '"+creator+"'");
		try {
			if (rs.next()) {
				comp_id = rs.getInt(1);
			}
		} catch (Exception e) {
		}
		if(creator == null || creator.equals("undefined")) {
			sql = String.format("insert into sys_user_logs(menu_uid,operation_type,successfully,ip,from_json,to_json,creator) values" + 
					"('%s','%s',%s,'%s','%s','%s',null)",id,opeType,successFully,ip,fromJson,toJson);
		}else {
			sql = String.format("insert into sys_user_logs(menu_uid,operation_type,successfully,ip,from_json,to_json,creator,comp_id) values" + 
					"('%s','%s',%s,'%s','%s','%s','%s',%s)",id,opeType,successFully,ip,fromJson,toJson,creator,comp_id);
		}
		dbConn.queryUpdate(sql);
	}
	
	/**
	 * 获取用户登录操作日志
	 */
	public void getLoginLog(HttpServletRequest request,String creator,Integer comp_id) {
		String ip = getIpAddress(request);
		String sql = String.format("insert into sys_user_logs(operation_type,successfully,ip,creator,comp_id) values" + 
				"('%s',%s,'%s','%s',%s)","用户登录",true,ip,creator,comp_id);
//		System.err.println(testConn.getState());
		if (testConn.getState()!=null) {
			dbConn.query(sql);
		}else {
			dbConn.queryUserLogin(sql);
		}
	}
	
	/**
	 * 获取本机IP地址
	 */
	public static String getIpAddress(HttpServletRequest request) {

		String ipAddress = request.getHeader("x-forwarded-for");

		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknow".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();

			if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
				// 根据网卡获取本机配置的IP地址
				InetAddress inetAddress = null;
				try {
					inetAddress = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				ipAddress = inetAddress.getHostAddress();
			}
		}
		// 对于通过多个代理的情况，第一个IP为客户端真实的IP地址，多个IP按照','分割
		if (null != ipAddress && ipAddress.length() > 15) {
			// "***.***.***.***".length() = 15
			if (ipAddress.indexOf(",") > 0) {
				ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
			}
		}
		return ipAddress;
	}
	
	/**
	 * 保存错误日志
	 */
	public void saveErrorMessage(String error) {
		String sql = String.format("insert into sys_logs (message) values ('%s')",error);
		if (testConn.getState()!=null) {
			dbConn.query(sql);
		}else {
			dbConn.queryUserLogin(sql);
		}
	}
	
}
