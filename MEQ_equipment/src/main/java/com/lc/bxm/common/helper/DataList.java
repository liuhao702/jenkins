package com.lc.bxm.common.helper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONObject;

/**
 * 数据下拉列表通用接口
 * 
 * @author ljz
 *
 */
@Service
public class DataList {

	@Autowired
	PostgreSQLConn dbConn;
	
	/**
	 * 通用单层数据列表
	 * 
	 * @param tableName
	 * @param keyColumnName
	 * @param valueColumnName
	 * @return
	 */
	public String getDataSingleListJson(String tableName,String keyColumnName, String valueColumnName) throws Exception {
		ResultSet rs = dbConn.query(String.format("SELECT %s,%s FROM %s", keyColumnName,valueColumnName,tableName));
		StringBuilder sb = new StringBuilder();
		JSONObject json = new JSONObject();
		JSONObject jsons = new JSONObject();
		while (rs.next()) {
			json.put("id", rs.getString(1));
			json.put("name", rs.getString(2));
			json.put("level", "1");
			sb.append(json.toString()+",");
			jsons.put("id", "0");
			jsons.put("name", "分类");
			jsons.put("children", "["+Str.delComma(sb.toString())+"]");
		}
		return "["+jsons.toString()+"]";
 	}
	
	/**
	 * 通用获取当前db所有用户id
	 * 
	 * @param tableName
	 * @param keyColumnName
	 * @param valueColumnName
	 * @return
	 */
	public List<Object> getDBUserId(String tableName){
		ResultSet rs = dbConn.query(String.format("SELECT user_uid FROM %s",tableName));
		List<Object> userList= new ArrayList<Object>();
		try {
			while (rs.next()) {
				userList.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userList;
	}
	
}
