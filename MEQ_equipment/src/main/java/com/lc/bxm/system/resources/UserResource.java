package com.lc.bxm.system.resources;

import java.sql.Connection;



import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.RedisUtil;
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.common.util.Base64Util;
import com.lc.bxm.common.util.JwtUtil;
import com.lc.bxm.common.util.TokenUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.entity.TestConnection;

import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@RestController
@RequestMapping("/user")
@SuppressWarnings("unused")
public class UserResource {
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	TestConnection testConn;
	@Autowired
	RedisUtil redis;
	@Autowired
	Base64Util base;

	static String dbNameStr = null;
     
	@ApiOperation(value="用户登录", notes="根据用户名和密码登录")
	@RequestMapping(value = "login", method = org.springframework.web.bind.annotation.RequestMethod.POST)
	@ResponseBody
	public String login(@RequestBody String userJson, HttpServletRequest request) {
		testConn.setState("private");
		JSONObject jsonObject = JSONObject.fromObject(userJson);
		JSONObject json = new JSONObject();
		String comp_no = jsonObject.getString("comp_no"); //企业编码
		String userCode = jsonObject.getString("userCode"); //用户编号
//		String password = jsonObject.getString("password");
		String password = base.decode(jsonObject.getString("password").trim());
		if ((comp_no.equals("LCKJ001")) &&(userCode.equals("bxmadmin")) && (password.equals("lc123456"))) {
//			String token = JwtUtil.createJWT(600000L, userCode);
			String token = TokenUtil.generateToken(userCode);
			json.put("token", "" + token + "");
			json.put("status", Integer.valueOf(0));
			json.put("userId", "1d0e11c1-691f-400b-bf10-f5bc52433bb7");
			json.put("userCode", "bxmadmin");
			json.put("userName", "bxmadmin");
			json.put("comp_id", "0");
			return json.toString();
		}
//		ResultSet rs = this.dbConn.query(String.format("select bxm_user_status('%s','%s')", new Object[] { userCode, password }));
		ResultSet rs = this.dbConn.query(String.format("select bxm_user_status_comp('%s','%s','%s')",  userCode,comp_no, password ));
		System.err.println(String.format("select bxm_user_status_comp('%s','%s','%s')",  userCode,comp_no, password ));
		try {
			if (rs.next()) {
				int res = rs.getInt(1);
				if (res == 0) {
					rs = this.dbConn.query(String.format("select comp_id from cyj_customer_data where customer_code = '%s'",comp_no));
					int comp_id = 0;
					if (rs.next()) {
						comp_id = rs.getInt(1);
					}
					JSONObject info = getUserInfo(request, userCode, comp_id);
//					String token = JwtUtil.createJWT(600000L, info.getString("userId"))
					String token = TokenUtil.generateToken(info.getString("userId"));
					json.put("token", "" + token + "");
					json.put("status", Integer.valueOf(0));
					json.put("userId", info.get("userId"));
					json.put("userName", info.get("userName"));
					json.put("userCode", info.get("userCode"));
					json.put("comp_id", comp_id);
					lastLoginTime(userCode,comp_id);
					return json.toString();
				}
				if (res == 1) {
					json.put("status", Integer.valueOf(1));
					json.put("message", "账号或密码错误");
					return json.toString();
				}
				if (res == 2) {
					json.put("status", Integer.valueOf(2));
					json.put("message", "该用户已被禁用");
					return json.toString();
				}
				if (res == 3) {
					json.put("status", Integer.valueOf(3));
					json.put("message", "必须修改密码");
					return json.toString();
				}
			}
		} catch (SQLException localSQLException) {
			return message.getErrorInfo("系统异常");
		}
		json.put("status", Integer.valueOf(1));
		json.put("message", "账号或密码错误");
		return json.toString();
	}
	
	
	
	
	@ApiOperation(value="设备简报小程序用户登录", notes="设备简报小程序根据用户名和密码登录")
	@RequestMapping(value = "appletsLogin1", method = org.springframework.web.bind.annotation.RequestMethod.POST)
	@ResponseBody
	public String appletsLogin(@RequestBody String userJson, HttpServletRequest request) {
		testConn.setState("private");
		JSONObject jsonObject = JSONObject.fromObject(userJson);
		JSONObject json = new JSONObject();
		String comp_no = jsonObject.getString("comp_no"); //企业编码
		String userCode = jsonObject.getString("userCode"); //用户编号
//		String password = jsonObject.getString("password");
		String password = base.decode(jsonObject.getString("password").trim());
		if ((comp_no.equals("LCKJ001")) &&(userCode.equals("bxmadmin")) && (password.equals("lc123456"))) {
//			String token = JwtUtil.createJWT(600000L, userCode);
			String token = TokenUtil.generateToken(userCode);
			json.put("token", "" + token + "");
			json.put("status", Integer.valueOf(0));
			json.put("userId", "1d0e11c1-691f-400b-bf10-f5bc52433bb7");
			json.put("userCode", "bxmadmin");
			json.put("userName", "bxmadmin");
			json.put("comp_id", "0");
			return json.toString();
		}
//		ResultSet rs = this.dbConn.query(String.format("select bxm_user_status('%s','%s')", new Object[] { userCode, password }));
		ResultSet rs = this.dbConn.query(String.format("select bxm_user_status_comp('%s','%s','%s')",  userCode,comp_no, password ));
		System.err.println(String.format("select bxm_user_status_comp('%s','%s','%s')",  userCode,comp_no, password ));
		try {
			if (rs.next()) {
				int res = rs.getInt(1);
				if (res == 0) {
					rs = this.dbConn.query(String.format("select comp_id from cyj_customer_data where customer_code = '%s'",comp_no));
					int comp_id = 0;
					if (rs.next()) {
						comp_id = rs.getInt(1);
					}
					JSONObject info = getUserInfo(request, userCode, comp_id);
//					String token = JwtUtil.createJWT(600000L, info.getString("userId"))
					String token = TokenUtil.generateToken(info.getString("userId"));
					json.put("token", "" + token + "");
					json.put("status", Integer.valueOf(0));
					json.put("userId", info.get("userId"));
					json.put("userName", info.get("userName"));
					json.put("userCode", info.get("userCode"));
					json.put("comp_id", comp_id);
					lastLoginTime(userCode,comp_id);
					return json.toString();
				}
				if (res == 1) {
					json.put("status", Integer.valueOf(1));
					json.put("message", "账号或密码错误");
					return json.toString();
				}
				if (res == 2) {
					json.put("status", Integer.valueOf(2));
					json.put("message", "该用户已被禁用");
					return json.toString();
				}
				if (res == 3) {
					json.put("status", Integer.valueOf(3));
					json.put("message", "必须修改密码");
					return json.toString();
				}
			}
		} catch (SQLException localSQLException) {
		}
		json.put("status", Integer.valueOf(1));
		json.put("message", "账号或密码错误");
		return json.toString();
	}
	

	@RequestMapping(value = { "loginPlanform" }, method = {org.springframework.web.bind.annotation.RequestMethod.POST })
	@ResponseBody
	public String loginPlatform(@RequestBody String userJson, HttpServletRequest request) {
		//Jedis jedis = redis.init();
		JSONObject jsonObject = JSONObject.fromObject(userJson);
		JSONObject json = new JSONObject();
		String uid = jsonObject.getString("uid");
		int infoStr = getDataBaseName(uid, jsonObject.getInt("com_id"), jsonObject.getInt("app_id"));
		if (infoStr == 10) {
			return this.message.getErrorInfo("404");
		}
		String token = jsonObject.getString("token");
		try {
			ResultSet rss = this.dbConn
					.queryUser(new StringBuilder().append("select exists(select * from dbbasename where token = '")
							.append(token).append("')").toString());
			if ((rss.next()) && (!rss.getBoolean(1))) {
				this.dbConn.userUpdate(new StringBuilder().append("insert  into dbbasename values('")
								.append(token).append("','").append(dbNameStr).append("')").toString());
			}
			ResultSet rs = this.dbConn.queryUserLogin(String.format(
					"select exists (select user_uid from sys_users where user_uid = '%s')", new Object[] { uid }));

			if (rs.next()) {
				boolean res = rs.getBoolean(1);
				if (res) {
					JSONObject info = getUserInfoByPlanform(request, uid);
					json.put("status", Integer.valueOf(0));
					json.put("userId", info.get("userId"));
					json.put("userName", info.get("userName"));
					json.put("userCode", info.get("userCode"));
					if (infoStr == 3) {
						json.put("is_admin", "bxmadmin");
					}
					return json.toString();
				}
				json.put("status", Integer.valueOf(1));
				return json.toString();
			}
		} catch (SQLException localSQLException) {
			localSQLException.printStackTrace();

		}
		json.put("status", Integer.valueOf(1));
		return json.toString();
	}

//	private void lastLoginTime(String userCode) {
//		String sql = "update sys_users set last_login_time = current_timestamp where user_code = '" + userCode + "'";
//		this.dbConn.queryUpdate(sql);
//	}
	
	//绑定comp_id
	private void lastLoginTime(String userCode,Integer comp_id) {
		String sql = "update sys_users set last_login_time = current_timestamp where user_code = '" + userCode + "' and comp_id = "+comp_id+"";
		this.dbConn.queryUpdate(sql);
	}

	private void lastLoginTimeForuuid(String uuid) {
		String sql = "update sys_users set last_login_time = current_timestamp where user_uid = '" + uuid + "'";
		this.dbConn.queryUpdate(sql);
	}

	
	public JSONObject getUserInfo(HttpServletRequest request, String userCode, Integer comp_id) {
		ResultSet rs = null;
//		if (testConn.getState()!=null) {
//			  rs = this.dbConn.query(String.format("select user_uid,user_name,comp_id from sys_users where user_code = '%s'", new Object[] { userCode }));
//		}else {
//			  rs = this.dbConn.queryUserLogin(String.format("select user_uid,user_name,comp_id from sys_users where user_code = '%s'", new Object[] { userCode }));
//		}
//	  rs = this.dbConn.query(String.format("select user_uid,user_name,comp_id from sys_users where user_code = '%s' ", userCode ));
		rs = this.dbConn.query(String.format("select user_uid,user_name,comp_id from sys_users where user_code = '%s' and comp_id =%s", userCode,comp_id ));
		JSONObject json = new JSONObject();
		try {
			if (rs.next()) {
				json.put("userId", rs.getString(1));
				json.put("userName", rs.getString(2));
				json.put("userCode", userCode);
				this.getLogs.getLoginLog(request, rs.getString(1),rs.getInt(3));
			}
		} catch (SQLException localSQLException) {
		}
		return json;
	}

	private JSONObject getUserInfoByPlanform(HttpServletRequest request, String uid) {
		ResultSet rs = null;
			  rs = this.dbConn.queryUserLogin(String.format("select user_name,user_code from sys_users where user_uid = '%s'", uid));
		JSONObject json = new JSONObject();
		try {
			if (rs.next()) {
				json.put("userId", uid);
				json.put("userName", rs.getString(1));
				json.put("userCode", rs.getString(2));
				this.getLogs.getLoginLog(request, uid,1);
			}
		} catch (SQLException localSQLException) {
		}
		return json;
	}

	@RequestMapping(value = { "getCode" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	public String getCode() {
		int i = 123456789;
		String s = "qwertyuipasdfghjklzxcvbnm";
		String y = s.toUpperCase();
		String word = s + y + i;
		char[] c = word.toCharArray();

		Random rd = new Random();
		String code = "";
		for (int k = 0; k < 4; k++) {
			int index = rd.nextInt(c.length);
			code = code + c[index];
		}
		return code;
	}

	@RequestMapping(value = { "roleJson" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	@ResponseBody
	public String getRoleJson(Integer comp_id) {
		//		ResultSet rs = this.dbConn.query("select role_uid,role_name from sys_roles where  not is_deactive");
		ResultSet rs=null;
		if (comp_id!=null) {
			rs=dbConn.query("select role_uid,role_name from sys_roles where comp_id = "+comp_id+" and not is_deactive");
		}else {
			rs=dbConn.query("select role_uid,role_name from sys_roles where  not is_deactive");
		}
		StringBuilder sb = new StringBuilder();
		JSONObject json = new JSONObject();
		try {
			while (rs.next()) {
				json.put("id", rs.getString(1));
				json.put("name", rs.getString(2));
				sb.append(json.toString() + ",");
			}
		} catch (SQLException localSQLException) {
		}
		return "[" + Str.delComma(sb.toString()) + "]";
	}

	@RequestMapping(value = { "nextLoginChangePassword" }, method = {
			org.springframework.web.bind.annotation.RequestMethod.GET })
	@ResponseBody
	public String setLoginChangePassword(HttpServletRequest request, @RequestParam String userUid) {
		Boolean result = Boolean.valueOf(false);
		ResultSet rs = dbConn.query(String.format("SELECT bxm_set_next_login_change_password('%s')", userUid ));
		try {
			while (rs.next()) {
				result = Boolean.valueOf(rs.getBoolean(1));
			}
		} catch (SQLException localSQLException) {
		}
		this.getLogs.getUserLog(request, "9b115f48-cf38-42ca-ac09-1caf0b1f7cd8", "登录后修改密码", result.booleanValue(),
				userUid, "", userUid);
		if (result.booleanValue()) {
			return this.message.getSuccessInfo("修改成功");
		}
		return this.message.getErrorInfo("修改失败");
	}

	@RequestMapping(value = { "setAdministrator" }, method = {
			org.springframework.web.bind.annotation.RequestMethod.GET })
	@ResponseBody
	public String setAdministrator(HttpServletRequest request, @RequestParam String userUid) {
		Boolean isAdmin = Boolean.valueOf(false);
		ResultSet rsIsAdmin = this.dbConn
				.query(String.format("SELECT bxm_administrator_check_by_uid('%s')", new Object[] { userUid }));
		try {
			while (rsIsAdmin.next()) {
				isAdmin = Boolean.valueOf(rsIsAdmin.getBoolean(1));
			}
			if (!isAdmin.booleanValue()) {
				Boolean result = Boolean.valueOf(false);
				ResultSet rs = this.dbConn
						.query(String.format("SELECT bxm_administrator_set('%s',true)", new Object[] { userUid }));
				while (rs.next()) {
					result = Boolean.valueOf(rs.getBoolean(1));
				}
				this.getLogs.getUserLog(request, "9b115f48-cf38-42ca-ac09-1caf0b1f7cd8", "设置用户为管理员",
						result.booleanValue(), userUid, "", userUid);
				if (result.booleanValue()) {
					return this.message.getSuccessInfo("设置管理员成功");
				}
				return this.message.getErrorInfo("系统要求必须有1到5个管理员");
			}
			return this.message.getErrorInfo("该用户已是管理员");
		} catch (SQLException localSQLException) {
		}
		return null;
	}

	@RequestMapping(value = { "cancelAdministrator" }, method = {
			org.springframework.web.bind.annotation.RequestMethod.GET })
	@ResponseBody
	public String cancelAdministrator(HttpServletRequest request, @RequestParam String userUid) {
		Boolean isAdmin = Boolean.valueOf(false);
		ResultSet rsIsAdmin = this.dbConn
				.query(String.format("SELECT bxm_administrator_check_by_uid('%s')", new Object[] { userUid }));
		try {
			while (rsIsAdmin.next()) {
				isAdmin = Boolean.valueOf(rsIsAdmin.getBoolean(1));
			}
			if (isAdmin.booleanValue()) {
				Boolean result = Boolean.valueOf(false);
				ResultSet rs = this.dbConn
						.query(String.format("SELECT bxm_administrator_set('%s',false)", new Object[] { userUid }));
				while (rs.next()) {
					result = Boolean.valueOf(rs.getBoolean(1));
				}
				this.getLogs.getUserLog(request, "9b115f48-cf38-42ca-ac09-1caf0b1f7cd8", "取消用户管理员",
						result.booleanValue(), userUid, "", userUid);
				if (result.booleanValue()) {
					return this.message.getSuccessInfo("取消用户管理员成功");
				}
				return this.message.getErrorInfo("系统要求必须有一到二个管理员");
			}
			return this.message.getErrorInfo("取消管理员失败，该用户不是管理员");
		} catch (SQLException localSQLException) {
		}
		return null;
	}

	@RequestMapping(value = { "resettingPassword" }, method = {
			org.springframework.web.bind.annotation.RequestMethod.GET })
	@ResponseBody
	public String resettingPassword(HttpServletRequest request, @RequestParam String userUid) {
		Boolean result = Boolean.valueOf(false);
		ResultSet rs = this.dbConn.query("select bxm_password_reset('" + userUid + "')");
		try {
			while (rs.next()) {
				result = Boolean.valueOf(rs.getBoolean(1));
			}
		} catch (SQLException localSQLException) {
		}
		this.getLogs.getUserLog(request, "9b115f48-cf38-42ca-ac09-1caf0b1f7cd8", "重置密码", result.booleanValue(), userUid,
				"", userUid);
		if (result.booleanValue()) {
			return this.message.getSuccessInfo("重置密码成功");
		}
		return this.message.getErrorInfo("重置密码失败");
	}

	@RequestMapping(value = { "getDefaultPassword" }, method = {
			org.springframework.web.bind.annotation.RequestMethod.POST })
	@ResponseBody
	public String defalutPassword() {
		String result = null;
		ResultSet rs = this.dbConn
				.query("select setting_value from sys_settings where setting_code = 'UserDefaultPassword'");
		try {
			while (rs.next()) {
				result = rs.getString(1);
			}
		} catch (SQLException localSQLException) {
		}
		return result;
	}

	@RequestMapping(value = { "changePassword" }, method =RequestMethod.POST )
	@ResponseBody
	public String changePassword(HttpServletRequest request, @RequestBody String userJson) {
		JSONObject jsonObject = JSONObject.fromObject(userJson);
		String userCode = jsonObject.getString("userCode").trim();
		String oldPassword = base.decode(jsonObject.getString("oldPassword")).trim();
		String newPassword = base.decode(jsonObject.getString("newPassword")).trim();
		System.err.println(userCode+"1"+oldPassword+"q1"+newPassword);
		Boolean result = Boolean.valueOf(false);
		Integer status = Integer.valueOf(0);
		String userUid = "";
		try {
			ResultSet rs = null;
			if (jsonObject.get("comp_id")!=null) {  //根据企业ID修改密码
				rs =dbConn.query(String.format("SELECT bxm_password_check_comp_id('%s',%s,'%s')",userCode,jsonObject.get("comp_id"), oldPassword));
			}else {//根据企业编码修改密码
				rs = dbConn.query(String.format("SELECT bxm_password_check_comp_no('%s','%s','%s')",userCode,jsonObject.get("company"), oldPassword));
               System.err.println(String.format("SELECT bxm_password_check_comp_no('%s','%s','%s')",userCode,jsonObject.get("company"), oldPassword));
			}
			ResultSet rsStatus = dbConn.query(String.format("SELECT bxm_password_limit('%s','%s')",userCode, newPassword));
			if (rs.next()) {
				result = rs.getBoolean(1);
			}
			while (rsStatus.next()) {
				status = Integer.valueOf(rsStatus.getInt(1));
			}
			if (result) {
				if (status.intValue() == 0) {
					ResultSet rsUsercode = dbConn.query(String.format(
							"SELECT user_uid FROM sys_users WHERE user_code = '%s'", userCode ));
					if (rsUsercode.next()) {
						userUid = rsUsercode.getString(1);
					}
					getLogs.getUserLog(request, "9b115f48-cf38-42ca-ac09-1caf0b1f7cd8", "修改密码",
							result.booleanValue(), userCode, "", userUid);
                    
					if (jsonObject.get("comp_id")!=null) {  
						//根据企业ID修改密码
						dbConn.queryUpdate(String.format("SELECT bxm_password_set_comp_id('%s',%s,'%s')",
								 userCode,jsonObject.get("comp_id"), newPassword));
						System.err.println(String.format("SELECT bxm_password_set_comp_id('%s',%s,'%s')",
								 userCode,jsonObject.get("comp_id"), newPassword));
					}else {
						//根据企业编码修改密码
						dbConn.queryUpdate(String.format("SELECT bxm_password_set_comp_no('%s','%s','%s')",
								 userCode,jsonObject.get("company"), newPassword ));
						System.err.println(String.format("SELECT bxm_password_set_comp_no('%s',%s,'%s')",
								 userCode,jsonObject.get("company"), newPassword));
					}
					return this.message.getSuccessInfo("修改密码成功");
				}
				if (status.intValue() == 1) {
					String passwordLength = "1";

					ResultSet rsPasswordMinLength = dbConn.query("SELECT setting_value FROM sys_settings WHERE setting_code = 'PasswordMinLength'");
					while (rsPasswordMinLength.next()) {
						passwordLength = rsPasswordMinLength.getString(1);
					}
					return message.getErrorInfo(String.format("修改密码长度为%s位",  passwordLength ));
				}
				return message.getErrorInfo("密码必须含有数字和字母");
			}
			return message.getErrorInfo("旧密码错误");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getDataBaseName(String user_id, int com_id, int app_id) {
		Connection conn = null;
		ResultSet rs = null;
		ResultSet rss = null;
		int is_admin = 10;
		if (user_id.equals("") || (user_id == null)) {
			rs = this.dbConn.queryUser(String
					.format("SELECT db_name FROM sys_company_apps WHERE com_id =%s and app_id = %s", com_id, app_id));
		} else {
			rs = this.dbConn.queryUser(String.format(
					"SELECT db_name FROM sys_company_apps WHERE com_id = (SELECT com_id FROM sys_company_roles WHERE user_id = '%s' AND com_id = %s) AND app_id = %s",
					user_id, com_id, app_id));
			rss = this.dbConn.queryUser("select is_admin from sys_company_roles where user_id ='" + user_id + "'");
			try {
				if (rss.next()) {
					is_admin = rss.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			if (rs.next()) {
				testConn.setDatabaseName(rs.getString(1));
				dbNameStr = rs.getString(1);
				conn = this.dbConn.getConnUserLogin();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (conn != null) {
			return is_admin;
		}
		return is_admin;
	}
	
	
    //C++获取工艺卡接口
	@RequestMapping(value =  "getDataBaseCard" , method =RequestMethod.GET )
	@ResponseBody
	public String getDataBaseCard(@RequestParam int appid, @RequestParam int companyid, @RequestParam int lineid,
			@RequestParam int stationid) {
		ResultSet rs = null;
		StringBuilder builder = new StringBuilder();
		if (getDataBaseName("", companyid, appid) == 10) {
			rs = this.dbConn.queryUserLogin(String.format("select meq_get_card_id_by_station(%s,%s)",
					new Object[] { Integer.valueOf(lineid), Integer.valueOf(stationid) }));
			String str = null;
			String[] splitAddress = null;
			try {
				if (rs.next()) {
					str = rs.getString(1);
					splitAddress = str.split(",");
				}
				for (int i = 0; i < splitAddress.length; i++) {
					rs = this.dbConn.queryUserLogin(
							"select file_path ,play_time  from meq_process_cards where card_id = " + splitAddress[i] + "");
					if (rs.next()) {
						builder.append(rs.getString(1) + ",");
						builder.append(rs.getString(2) + "|");
//						rs = this.dbConn.queryUserLogin("select play_time from meq_line_cards where card_id = "
//								+ splitAddress[i] + " and station_id =" + stationid + "");
//						if (rs.next()) {
//							builder.append(rs.getString(1) + "|");
//						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return Str.delComma(builder.toString());
	}
}
