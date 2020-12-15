package com.lc.bxm.system.resources;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * 外部数据库接口
 * @author LJZ
 * @date 2019年6月21日
 */
@RestController
@RequestMapping("/common")
public class ForeignResource {

    //此处使用AES-128-ECB加密模式，key需要为16位。
    private String cKey = "Sd9EmIVoRVbkYNxS";

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	
	/**
	 * 外部数据库新增接口 LJZ
	 */
	@RequestMapping(value = "addForeignDbsJson", method = RequestMethod.POST)
	@ResponseBody
	public String getAddDataJson(HttpServletRequest request, @RequestBody String jsonSaveData, HttpSession session) {
		try {		
			JSONObject jsonObject = JSONObject.fromObject(jsonSaveData);
			StringBuilder columnName = new StringBuilder();
			StringBuilder columnValue = new StringBuilder();
			String sqlInsert = null;
			Iterator<?> iter = null;
			String menuId = jsonObject.getString("menuId");
			String funName = jsonObject.getString("funName");
			String tableName = jsonObject.getString("tableName");
			String saveData = jsonObject.getString("formData");
			JSONObject jsonDataToAdd = JSONObject.fromObject(saveData);
			Object object = null;
			String errorMessage = null;

			for (iter = jsonDataToAdd.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				columnName.append(key);
				object = jsonDataToAdd.get(key);
				if (iter.hasNext()) {
					columnName.append(",");
				}
				if (jsonDataToAdd.getString(key).isEmpty() || (object instanceof JSONNull)) {
					columnValue.append("null");
				} else {
					// 密码加密
					if (key.equals("password")) {
						String pwd = jsonDataToAdd.getString(key);
						columnValue.append("'" + Encrypt(pwd, cKey) + "'");
					} else {
						columnValue.append("'" + jsonDataToAdd.getString(key) + "'");
					}
				}
				if (iter.hasNext()) {
					columnValue.append(",");
				}
			}
			sqlInsert = String.format("INSERT INTO %s(%s) VALUES(%s)", tableName, columnName, columnValue);
			boolean res = dbConn.queryUpdate(sqlInsert);
			// 捕获用户操作日志
			getLogs.getUserLog(request, menuId, funName, res, "", saveData, "aa81b6bc-a985-4bd1-90e3-a391cd69b2aa");
			if (res) {
				return message.getSuccessInfo("新增成功");
			} else {
				return message.getErrorInfo(String.format("新增失败,%s", errorMessage));
			}
		} catch (Exception e) {
			return message.getErrorInfo("新增失败");
		}
	}

    /**
	 * 外部数据库编辑接口 LJZ
	 */
	@RequestMapping(value = "updateForeignDbsJson", method = RequestMethod.POST)
	@ResponseBody
	public String getUpdateDataJson(HttpServletRequest request, @RequestBody String jsonUpdateData) {
		try {
			JSONObject jsonObject = JSONObject.fromObject(jsonUpdateData);
			StringBuilder filterString = new StringBuilder();
			StringBuilder updateString = new StringBuilder();
			String sqlUpdate = null;
			String menuId = jsonObject.getString("menuId");
			String funName = jsonObject.getString("funName");
			String id = jsonObject.getString("id"); // 当前编辑的ID,如果是用户就用用户ID,角色就是角色ID
			String tableName = jsonObject.getString("tableName");
			String jsonUpdate = jsonObject.getString("formData");
			JSONObject jsonDataToUpdate = JSONObject.fromObject(jsonUpdate);
			String keyColumn = null;
			Object object = null;
			String resultInfo = null;
			ResultSet rsinfo = dbConn.query("select bxm_get_data_json('" + tableName + "','" + id + "')");
			while (rsinfo.next()) {
				resultInfo = rsinfo.getString(1);
			}

			ResultSet rs = dbConn.query(String
					.format("select column_name from v_sys_columns where table_name = '%s' and is_key", tableName));
			if (rs.next()) {
				keyColumn = rs.getString(1);
			}
			for (Iterator<?> iter = jsonDataToUpdate.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				object = jsonDataToUpdate.get(key);

				if (key.equals(keyColumn)) {
					filterString.append(key + " = " + "'" + jsonDataToUpdate.getString(key) + "'");
				} else {
					if (jsonDataToUpdate.getString(key).isEmpty() || (object instanceof JSONNull)) {
						updateString.append(key + " = " + "null");
					} else {
						// 密码加密
						if (key.equals("password")) {
							String pwd = jsonDataToUpdate.getString(key);
							updateString.append(key + " = " + "'" + Encrypt(pwd, cKey) + "'");
						} else {
							updateString.append(key + " = " + "'" + jsonDataToUpdate.getString(key) + "'");
						}
					}
					if (iter.hasNext()) {
						updateString.append(",");
					}
				}
			}

			if (updateString.substring(updateString.length() - 1).equals(",")) {
				updateString.deleteCharAt(updateString.length() - 1);
			}
			sqlUpdate = String.format("UPDATE %s SET %s WHERE %s", tableName, updateString, filterString);
			boolean res = dbConn.queryUpdate(sqlUpdate);
			// 捕获日志信息
			getLogs.getUserLog(request, menuId, funName, res, resultInfo, jsonUpdate,
					"aa81b6bc-a985-4bd1-90e3-a391cd69b2aa");
			if (res) {
				return message.getSuccessInfo("修改成功");
			} else {
				return message.getErrorInfo("修改失败");
			}
		} catch (Exception e) {
			return message.getErrorInfo("修改失败");
		}
	}
	
	/**
	 * 测试连接接口 LJZ
	 */
	@RequestMapping(value = "validConnectJson", method = RequestMethod.POST)
	@ResponseBody
	public String getValidDatabaseConnect() {
		String resutl = null;
		try {
			ResultSet rs = query("if exists (select 1 from line) select 1 as result else select 0");
			while (rs.next()) {
				resutl = rs.getString(1);		
			}
		}catch (Exception e) {
			resutl = "连接失败";
		}
		return resutl;
	}
	
	/**
	 * JF 数据库查询
	 */
	public ResultSet query(String sql) {
		Connection conn = getConn();
		PreparedStatement pStatement = null;
		ResultSet rs = null;
		try {
			pStatement = conn.prepareStatement(sql);
			rs = pStatement.executeQuery();
		} catch (SQLException e) {
		} finally {
			try {
				if (conn != null) {
					conn.close();// 释放资源
				}
			} catch (SQLException e) {
			}
		}
		return rs;
	}
	
	/**
	 * JF CONN连接
	 */
	private Connection getConn() {
		int databaseType = 0;
		String dbUrl = null; 
		String userName = null; //默认用户名
		String userPwd = null; //密码
        String driverName= null;//数据驱动
		Connection connection = null;
		try {
			if(databaseType == 0) {
				dbUrl = String.format("\"jdbc:sqlserver://%s; DatabaseName=%s\"","192.168.123.139","ceshi");
				userPwd = "sa";
				driverName = "kllhyy1120";
				// 加载数据驱动
				driverName="com.microsoft.sqlserver.jdbc.SQLServerDriver";
				Class.forName(driverName);
				connection = DriverManager.getConnection(dbUrl, userName, userPwd);
			}
		} catch (ClassNotFoundException e) {
		} catch (SQLException e) {
		}finally {
			if (connection!=null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return connection;
	}
	
	// AES加密
    private static String Encrypt(String sSrc, String sKey) throws Exception {
        if (sKey == null) {
            return null;
        }
        // 判断Key是否为16位
        if (sKey.length() != 16) {
            return null;
        }
        byte[] raw = sKey.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        //"算法/模式/补码方式"
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));

        //此处使用BASE64做转码功能，同时能起到2次加密的作用。
        return new Base64().encodeToString(encrypted);
    }
	
}
