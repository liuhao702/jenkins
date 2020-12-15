package com.lc.bxm.common.helper;
import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lc.bxm.common.GetLogs;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/**
 * 数据库帮助类
 * 
 * @author LJZ
 * @date 2019年8月16日
 */
@Service
public class PostgresqlHelper {

	@Autowired
	PostgreSQLConn dbConn;

	@Autowired
	GetLogs getLogs;
    
	public String dataQuery(HttpServletRequest request,String tableName , String pageSize , String currentPage , String order ,
			                      String prop , String filterString , String inputSearch) {
		if (filterString != "") {
			filterString = filterString.replace("[", "(");
			filterString = filterString.replace("]", ")");
			filterString = filterString.replace("'", "''");
			filterString = filterString.replace("\\", "\\\\");
		}
		if (inputSearch != "") {
			inputSearch = inputSearch.replaceAll(" ", "");
			inputSearch = inputSearch.replace("\\", "\\\\");
			inputSearch = inputSearch.replace("(", "");
			inputSearch = inputSearch.replace(")", "");
			inputSearch = inputSearch.replace("*", "");
			inputSearch = inputSearch.replace("?", "");
			inputSearch = inputSearch.replaceAll("([';])+|(--)+|(%)+|(^)+", "");
		}	
		if (order.equals("descending")) {
			order = "desc";
		}
		if (order.equals("ascending")) {
			order = "asc";
		}
		String sql = "select bxm_get_grid_page_json('" + tableName + "','*','" + filterString + "','" + inputSearch
				+ "','" + prop + "','" + order + "'," + pageSize + "," + currentPage + ")";
		ResultSet rs = dbConn.query(sql);
		String messageJson = null;
		try {
			if (rs.next()) {
				messageJson = rs.getString(1);
		  }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return messageJson;
	}
		
	
	// 通用单行新增
	public boolean dataInsert(HttpServletRequest request, String menuId, String funName, String userId,
			String tableName, JSONObject jsonInsert) {
		StringBuilder columnName = new StringBuilder();
		StringBuilder columnValue = new StringBuilder();
		String sqlInsert = null;
		boolean res = false;
		try {
			for (Iterator<?> iter = jsonInsert.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				columnName.append(key);
				if (jsonInsert.get(key) instanceof JSONNull) {
					columnValue.append("null");
				} else if (jsonInsert.get(key) instanceof Integer || jsonInsert.get(key) instanceof Boolean) {
					columnValue.append(jsonInsert.getString(key));
				} else if (jsonInsert.get(key) instanceof JSONArray) {
					columnValue.append(jsonInsert.getString(key).toString().replace("\"", "").replace("[", "'{")
							.replace("]", "}'"));
				} else {
					columnValue.append("'" + jsonInsert.getString(key).replace("'", "''") + "'");
				}
				columnName.append(",");
				columnValue.append(",");
			}
			sqlInsert = String.format("INSERT INTO %s(%s) VALUES (%s)", tableName, Str.delComma(columnName.toString()),
					Str.delComma(columnValue.toString()));
			res = dbConn.queryUpdate(sqlInsert);
			// 捕获日志信息
			getLogs.getUserLog(request, menuId, funName, res, "", jsonInsert.toString(), userId);
		} catch (Exception e) {
			// 保存错误日志
			getLogs.saveErrorMessage(e.toString());
		}
		return res;
	}

	// 通用单行删除
	public boolean dataDelete(HttpServletRequest request, String menuId, String funName, String userId,
			String tableName, String keyColumnName, String keyColumnValue) {
		boolean res = false;
		String resultInfo = null;
		String sql = null;
		ResultSet rsinfo = null;
		try {
			if (keyColumnValue.contains("[")) {
				JSONArray idValues =JSONArray.fromObject(keyColumnValue);
				String idStrValue =idValues.toString();
				// 删除之前获取该信息的JSON,用于日志信息
				for (int i = 0; i < idValues.size(); i++) {
					rsinfo = dbConn.query("SELECT bxm_get_data_row('" + tableName + "','" + idValues.get(i) + "')");
					//记录日志
					while (rsinfo.next()) {
						resultInfo += rsinfo.getString(1);
					}
				}
				if (idStrValue != null && !idStrValue.equals("")) {
					idStrValue = idStrValue.replace("[", "").replace("]", "").replace("\"", "'");
				}
				sql = String.format("DELETE FROM %s WHERE %s", tableName,
						keyColumnName + " in" + "(" + idStrValue + ")");
				
			}else {
				rsinfo = dbConn.query("SELECT bxm_get_data_row('" + tableName + "','" + keyColumnValue + "')");
				while (rsinfo.next()) {
					resultInfo = rsinfo.getString(1);
				}
				 sql = String.format("DELETE FROM %s WHERE %s", tableName,
							keyColumnName + "=" + "'" + keyColumnValue + "'");
			}	
			res = dbConn.queryUpdate(sql);
			// 捕获日志信息
			getLogs.getUserLog(request, menuId, funName, res, resultInfo, "", userId);
		} catch (Exception e) {
			e.printStackTrace();
			// 保存错误日志
			getLogs.saveErrorMessage(e.toString());
		}
		return res;
	}

	// 通用单行编辑
	public boolean dataUpdate(HttpServletRequest request, String menuId, String funName, String userId, String tableName,
			JSONObject jsonUpdate, String editedId) {
		boolean res = false ;
		try {
			StringBuilder updateString = new StringBuilder();
			StringBuilder filterStr = new StringBuilder();
			String resultInfo = null;// 存放操作前的数据
			ResultSet rs =null;
			String keyColumn=null;
			// 查询记录日志
			rs = dbConn.query(String.format("SELECT bxm_get_from_json('%s','','%s','')", tableName, editedId));
			while (rs.next()) {
				resultInfo = rs.getString(1);
			}	
			//获取所有表的字段
			rs = dbConn.query(String.format("SELECT column_name FROM v_sys_columns WHERE table_name = '%s' and is_key", tableName));
			if (rs.next()) {
				keyColumn = rs.getString(1);
			}
			for (Iterator<?> iter = jsonUpdate.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				if (key.equals(keyColumn)) {
					filterStr.append(key + " = " + "'" + jsonUpdate.getString(key) + "'");
				} else {
					if (jsonUpdate.get(key) instanceof JSONNull) {
						updateString.append(key + " = " + "null");
					} else if (jsonUpdate.get(key) instanceof Integer || jsonUpdate.get(key) instanceof Boolean) {
						updateString.append(key + " = " + jsonUpdate.getString(key));
					} else if (jsonUpdate.get(key) instanceof JSONArray) {
						updateString.append(key + " = " + jsonUpdate.getString(key).toString().replace("\"", "")
								.replace("[", "'{").replace("]", "}'"));
					} else {
						updateString.append(key + " = " + "'" + jsonUpdate.getString(key).replace("'", "''") + "'");
					}
					updateString.append(",");
				}
			}
			if (updateString.substring(updateString.length() - 1).equals(",")) {
				updateString.deleteCharAt(updateString.length() - 1);
			}
			String sqlUpdate = String.format("UPDATE %s SET %s WHERE %s", tableName, updateString, filterStr);
			res = dbConn.queryUpdate(sqlUpdate);
			getLogs.getUserLog(request, menuId, funName, res, resultInfo, jsonUpdate.toString(),userId);
		} catch (Exception e) {
			// 保存错误日志
			getLogs.saveErrorMessage(e.toString());
			e.printStackTrace();
		}
		return res;
	}

	// 通用多行添加
	public String dataMultiInsert(PostgreSQLConn dbConn, String tableName, JSONArray jsonArray) {
		StringBuilder columnName = new StringBuilder();
		StringBuilder columnValue = new StringBuilder();
		String sqlInsert = null;
		String columnString = null;
		try {
			ResultSet resultSet = dbConn.query(String.format(
					"SELECT string_agg(column_name,',') FROM v_sys_columns WHERE table_name = '%s'", tableName));
			if (resultSet.next()) {
				columnString = resultSet.getString(1);
			}
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonInsert = jsonArray.getJSONObject(i);
				for (Iterator<?> iter = jsonInsert.keys(); iter.hasNext();) {
					String key = (String) iter.next();
					if (columnString.contains(key)) {
						if (i == 0) {
							columnName.append(key);
							columnName.append(",");
						}
						columnValue.append("(");
						if (jsonInsert.get(key) instanceof JSONNull) {
							columnValue.append("null");
						} else if (jsonInsert.get(key) instanceof Integer || jsonInsert.get(key) instanceof Boolean) {
							columnValue.append(jsonInsert.getString(key));
						} else if (jsonInsert.get(key) instanceof JSONArray) {
							columnValue.append(jsonInsert.getString(key).toString().replace("\"", "").replace("[", "'{")
									.replace("]", "}'"));
						} else {
							columnValue.append("'" + jsonInsert.getString(key).replace("'", "''") + "'");
						}
						columnValue.append("),");
					}
				}
			}
			sqlInsert = String.format("INSERT INTO %s(%s) VALUES %s", tableName, Str.delComma(columnName.toString()),
					Str.delComma(columnValue.toString()));
		} catch (Exception e) {
		}
		return sqlInsert;
	}

	// 通用多行编辑
	public boolean dataMultiUpdate(HttpServletRequest request, String menuId, String funName, String userId,
		String tableName, JSONArray jsonArray) {
		StringBuilder columnValue = new StringBuilder();
		StringBuilder sqlUpdate = new StringBuilder();
		String keyColumnName = null;
		String keyString = null;
		String[] columns = null;
		try {
			ResultSet resultSet = dbConn.query(String.format(
					"SELECT string_agg(column_name,',') FROM v_sys_columns WHERE table_name = '%s'", tableName));
			if (resultSet.next()) {
				columns = resultSet.getString(1).split(",");
			}

			ResultSet keyColumnResultSet = dbConn.query(String
					.format("SELECT column_name FROM v_sys_columns WHERE table_name = '%s' AND is_key", tableName));
			if (keyColumnResultSet.next()) {
				keyColumnName = keyColumnResultSet.getString(1);
			}
			for (int i = 0; i < jsonArray.size(); i++) {
				columnValue.setLength(0);
				JSONObject jsonUpdate = jsonArray.getJSONObject(i);
				for (Iterator<?> iter = jsonUpdate.keys(); iter.hasNext();) {
					String key = (String) iter.next();
					if (key.equals(keyColumnName)) {
						keyString = String.format("%s = '%s'", key, jsonUpdate.getString(key));
					} else if (Arrays.asList(columns).contains(key)) {
						if (jsonUpdate.get(key) instanceof JSONNull) {
							columnValue.append(key + " = " + "null");
						} else if (jsonUpdate.get(key) instanceof Integer || jsonUpdate.get(key) instanceof Boolean) {
							columnValue.append(key + " = " + jsonUpdate.getString(key));
						} else if (jsonUpdate.get(key) instanceof JSONArray) {
							columnValue.append(key + " = " + jsonUpdate.getString(key).toString().replace("\"", "")
									.replace("[", "'{").replace("]", "}'"));
						} else {
							columnValue.append(key + " = " + "'" + jsonUpdate.getString(key).replace("'", "''") + "'");
						}
						columnValue.append(",");
					}
				}
				sqlUpdate.append(String.format("UPDATE %s SET %s WHERE %s;", tableName,
						Str.delComma(columnValue.toString()), keyString));
			}
			boolean res = dbConn.queryUpdate(sqlUpdate.toString());
			// 捕获日志信息
			getLogs.getUserLog(request, menuId, funName, res, "", jsonArray.toString(), userId);
			return res;
		} catch (Exception e) {
			// 保存错误日志
			getLogs.saveErrorMessage(e.toString());
			return false;
		}
	}
	
	
	// 通用单行删除Cyjq
	public Object dataDeleteCyjq(HttpServletRequest request, String menuId, String funName, String userId,
			String tableName, String keyColumnName, String keyColumnValue) {
		Object res = null;
		String resultInfo = null;
		String sql = null;
		ResultSet rsinfo = null;
		try {
			if (keyColumnValue.contains("[")) {
				JSONArray idValues =JSONArray.fromObject(keyColumnValue);
				String idStrValue =idValues.toString();
				// 删除之前获取该信息的JSON,用于日志信息
				for (int i = 0; i < idValues.size(); i++) {
					rsinfo = dbConn.query("SELECT bxm_get_data_row('" + tableName + "','" + idValues.get(i) + "')");
					//记录日志
					while (rsinfo.next()) {
						resultInfo += rsinfo.getString(1);
					}
				}
				if (idStrValue != null && !idStrValue.equals("")) {
					idStrValue = idStrValue.replace("[", "").replace("]", "").replace("\"", "'");
				}
				sql = String.format("DELETE FROM %s WHERE %s", tableName,
						keyColumnName + " in" + "(" + idStrValue + ")");
				
			}else {
				rsinfo = dbConn.query("SELECT bxm_get_data_row('" + tableName + "','" + keyColumnValue + "')");
				while (rsinfo.next()) {
					resultInfo = rsinfo.getString(1);
				}
				 sql = String.format("DELETE FROM %s WHERE %s", tableName,
							keyColumnName + "=" + "'" + keyColumnValue + "'");
			}	
			res = dbConn.queryUpdateCyjq(sql);
			if (res instanceof Boolean) {
				getLogs.getUserLog(request, menuId, funName, (boolean) res, resultInfo, "", userId);
			}
			// 捕获日志信息
		} catch (Exception e) {
			e.printStackTrace();
			// 保存错误日志
			getLogs.saveErrorMessage(e.toString());
		}
		return res;
	}
	
	
	// 通用单行新增Cyjq
		public Object dataInsertCyjq(HttpServletRequest request, String menuId, String funName, String userId,
				String tableName, JSONObject jsonInsert) {
			StringBuilder columnName = new StringBuilder();
			StringBuilder columnValue = new StringBuilder();
			String sqlInsert = null;
			Object res = false;
			try {
				if (tableName.equals("meq_line_commands_new")) {
					String inspect_id =null;
					String card_process_id =null;
						ResultSet rs = dbConn.query("select inspect_id,card_process_id from meq_products_new"
								+ " where product_code = '"+jsonInsert.getString("product_code")+"'");
					    if (rs.next()) {
					    	inspect_id = rs.getString(1);
					    	card_process_id = rs.getString(2);
						}
					jsonInsert.put("inspect_id", inspect_id);
					jsonInsert.put("process_id", card_process_id);
				}
				for (Iterator<?> iter = jsonInsert.keys(); iter.hasNext();) {
					String key = (String) iter.next();
					columnName.append(key);
					if (jsonInsert.get(key) instanceof JSONNull) {
						columnValue.append("null");
					} else if (jsonInsert.get(key) instanceof Integer || jsonInsert.get(key) instanceof Boolean) {
						columnValue.append(jsonInsert.getString(key));
					} else if (jsonInsert.get(key) instanceof JSONArray) {
						columnValue.append(jsonInsert.getString(key).toString().replace("\"", "").replace("[", "'{")
								.replace("]", "}'"));
					} else {
						columnValue.append("'" + jsonInsert.getString(key).replace("'", "''") + "'");
					}
					columnName.append(",");
					columnValue.append(",");
				}
				sqlInsert = String.format("INSERT INTO %s(%s) VALUES (%s)", tableName, Str.delComma(columnName.toString()),
						Str.delComma(columnValue.toString()));
				res = dbConn.queryUpdateCyjq(sqlInsert);
				System.err.println(sqlInsert);
				if (res instanceof Boolean) {
					getLogs.getUserLog(request, menuId, funName, (boolean) res, "", jsonInsert.toString(), userId);
				}
				// 捕获日志信息
			} catch (Exception e) {
				// 保存错误日志
				getLogs.saveErrorMessage(e.toString());
			}
			return res;
		}
		
		
		// 通用单行编辑Cyjq
		public Object dataUpdateCyjq(HttpServletRequest request, String menuId, String funName, String userId, String tableName,
				JSONObject jsonUpdate, String editedId) {
			Object res = false ;
			try {
				StringBuilder updateString = new StringBuilder();
				StringBuilder filterStr = new StringBuilder();
				@SuppressWarnings("unused")
				String resultInfo = null;// 存放操作前的数据
				ResultSet rs =null;
				String keyColumn=null;
				// 查询记录日志
				rs = dbConn.query(String.format("SELECT bxm_get_from_json('%s','','%s','')", tableName, editedId));
				while (rs.next()) {
					resultInfo = rs.getString(1);
				}	
				//获取所有表的字段
				rs = dbConn.query(String.format("SELECT column_name FROM v_sys_columns WHERE table_name = '%s' and is_key", tableName));
				if (rs.next()) {
					keyColumn = rs.getString(1);
				}
				for (Iterator<?> iter = jsonUpdate.keys(); iter.hasNext();) {
					String key = (String) iter.next();
					if (key.equals(keyColumn)) {
						filterStr.append(key + " = " + "'" + jsonUpdate.getString(key) + "'");
					} else {
						if (jsonUpdate.get(key) instanceof JSONNull) {
							updateString.append(key + " = " + "null");
						} else if (jsonUpdate.get(key) instanceof Integer || jsonUpdate.get(key) instanceof Boolean) {
							updateString.append(key + " = " + jsonUpdate.getString(key));
						} else if (jsonUpdate.get(key) instanceof JSONArray) {
							updateString.append(key + " = " + jsonUpdate.getString(key).toString().replace("\"", "")
									.replace("[", "'{").replace("]", "}'"));
						} else {
							updateString.append(key + " = " + "'" + jsonUpdate.getString(key).replace("'", "''") + "'");
						}
						updateString.append(",");
					}
				}
				if (updateString.substring(updateString.length() - 1).equals(",")) {
					updateString.deleteCharAt(updateString.length() - 1);
				}
				String sqlUpdate = String.format("UPDATE %s SET %s WHERE %s", tableName, updateString, filterStr);
				res = dbConn.queryUpdateCyjq(sqlUpdate);
				if (res instanceof Boolean) {
					getLogs.getUserLog(request, menuId, funName, (boolean) res, "", jsonUpdate.toString(), userId);
				}
			} catch (Exception e) {
				// 保存错误日志
				getLogs.saveErrorMessage(e.toString());
				e.printStackTrace();
			}
			return res;
		}
}
