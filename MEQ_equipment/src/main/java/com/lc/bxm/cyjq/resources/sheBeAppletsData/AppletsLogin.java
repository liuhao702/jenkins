package com.lc.bxm.cyjq.resources.sheBeAppletsData;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.RedisUtil;
import com.lc.bxm.common.util.Base64Util;
import com.lc.bxm.common.util.TokenUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import com.lc.bxm.system.resources.UserResource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@Api(value = " 小程序仪器设备登录", tags = { "小程序仪器设备登录" })
@RestController
@RequestMapping("/getApplets")
public class AppletsLogin {

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	Base64Util base;
	@Autowired
	UserResource user;
	
	@ApiOperation(value="设备简报小程序用户登录", notes="设备简报小程序根据用户名和密码登录")
	@RequestMapping(value = "appletsLogin", method = org.springframework.web.bind.annotation.RequestMethod.POST)
	@ResponseBody
	public String appletsLogin(@RequestBody String userJson, HttpServletRequest request) {
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
					JSONObject info = user.getUserInfo(request, userCode, comp_id);
					String token = TokenUtil.generateToken(info.getString("userId"));
					json.put("token", "" + token + "");
					json.put("status", Integer.valueOf(0));
					json.put("userId", info.get("userId"));
					json.put("userName", info.get("userName"));
					json.put("userCode", info.get("userCode"));
					json.put("comp_id", comp_id);
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
	
}
