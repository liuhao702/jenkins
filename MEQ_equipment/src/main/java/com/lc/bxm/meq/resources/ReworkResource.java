package com.lc.bxm.meq.resources;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

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
import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/**
 * MEQ返修功能接口
 * @author JF
 * @date 2019年7月2日
 */
@RestController
@RequestMapping("/rework")
public class ReworkResource {

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;

	/**
	 * 添加返修记录接口
	 */
	@RequestMapping(value = "addJson", method = RequestMethod.POST)
	@ResponseBody
	public String getAddDataJson(HttpServletRequest request, @RequestBody String jsonSaveData) {
		try {
			JSONObject jsonObject = JSONObject.fromObject(jsonSaveData);
			StringBuilder columnName = new StringBuilder();
			StringBuilder columnValue = new StringBuilder();
			String userId = jsonObject.getString("userId");
			String menuId = jsonObject.getString("menuId");
			String funName = jsonObject.getString("funName");
			String tableName = jsonObject.getString("tableName");
			JSONObject jsonDataToAdd = JSONObject.fromObject(jsonObject.getString("formData"));
			String barcode = jsonDataToAdd.getString("barcode").trim();
			Object object = null;

			 if(tableName.equals("meq_repairings")) {
				 ResultSet rs = dbConn.query(String.format("SELECT EXISTS (SELECT 1 FROM meq_barcodes WHERE barcode = '%s')", barcode));
				 if(rs.next()) {
					 if(!rs.getBoolean(1)) {
						 return message.getErrorInfo("新增失败,该条码不存在条码库中");
					 }
				 }
			 }
			
			for (Iterator<?> iter = jsonDataToAdd.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				columnName.append(key);
				object = jsonDataToAdd.get(key);
				if (jsonDataToAdd.getString(key).isEmpty() || (object instanceof JSONNull)) {
					columnValue.append("null");
				} else {
					columnValue.append("'" + jsonDataToAdd.getString(key).replace("'", "''") + "'");
				}
				if (iter.hasNext()) {
					columnName.append(",");
					columnValue.append(",");
				}
			}
			dbConn.queryUpdate(String.format("UPDATE meq_barcodes SET is_repairing = true,repair_times = repair_times + 1 WHERE barcode = '%s'", barcode));
			String sqlInsert = String.format("INSERT INTO %s(%s) VALUES(%s)", tableName, columnName, columnValue);
			boolean res = dbConn.queryUpdate(sqlInsert);
			// 捕获用户操作日志
			getLogs.getUserLog(request, menuId, funName, res, "", jsonObject.getString("formData"), userId);
			if (res) {
				dbConn.queryUpdate(String.format("UPDATE meq_repairings SET barcode = regexp_replace(barcode, E'[\\n\\r]+', '', 'g' ) "
						+ "WHERE regexp_replace(barcode, E'[\\n\\r]+', '', 'g' ) = '%s'", barcode));
				return message.getSuccessInfo("新增成功");
			} else {
				return message.getErrorInfo("新增失败");
			}
		} catch (Exception e) {
			return message.getErrorInfo("新增失败");
		}
	}
	
	/**
	 * 返修编辑接口
	 */
	@RequestMapping(value = "updateJson", method = RequestMethod.POST)
	@ResponseBody
	public String getUpdateDataJson(HttpServletRequest request, @RequestBody String jsonUpdateData) {
		try {
			StringBuilder filterStr = new StringBuilder();
			StringBuilder updateStr = new StringBuilder();
			JSONObject jsonObj = JSONObject.fromObject(jsonUpdateData);
			String userId = jsonObj.getString("userId");
			String menuId = jsonObj.getString("menuId");
			String funName = jsonObj.getString("funName");
			String tableName = jsonObj.getString("tableName");
			JSONObject jsonData = JSONObject.fromObject(jsonObj.getString("formData"));
			String keyColumn = null;
			Object object = null;
			String resultInfo = null;
			ResultSet rsinfo = dbConn.query("select bxm_get_data_json('" + tableName + "','" + jsonObj.getString("id") + "')");
			while (rsinfo.next()) {
				resultInfo = rsinfo.getString(1);
			}
			ResultSet rs = dbConn.query(String.format("select column_name from v_sys_columns where table_name = '%s' and is_key", tableName));
			if (rs.next()) {
				keyColumn = rs.getString(1);
			}
			
			for (Iterator<?> iter = jsonData.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				object = jsonData.get(key);
				if (key.equals(keyColumn)) {
					filterStr.append(key + " = " + "'" + jsonData.getString(key) + "'");
				} else {
					if (jsonData.getString(key).isEmpty() || (object instanceof JSONNull)) {
						updateStr.append(key + " = " + "null");
					} else {
						updateStr.append(key + " = " + "'" + jsonData.getString(key) + "'");
					}
					if (iter.hasNext()) {
						updateStr.append(",");
					}
				}
			}
			if (updateStr.substring(updateStr.length() - 1).equals(",")) {
				updateStr.deleteCharAt(updateStr.length() - 1);
			}
			String sqlUpdate = String.format("UPDATE %s SET %s WHERE %s", tableName, updateStr, filterStr);
			boolean res = dbConn.queryUpdate(sqlUpdate);
			// 捕获日志信息
			getLogs.getUserLog(request, menuId, funName, res, resultInfo, jsonObj.getString("formData"), userId);
			if (res) {
				return message.getSuccessInfo("修改成功");
			}
		} catch (Exception e) {}
		return message.getErrorInfo("修改失败");
	}
	
	/**
	 * 返修处理接口
	 */
	@RequestMapping(value = "handleJson", method = RequestMethod.POST)
	@ResponseBody
	public String getHandleJson(@RequestBody String jsonData) {
			JSONObject jsonObj = JSONObject.fromObject(jsonData);
			String repId = jsonObj.getString("rep_id");
			String reasonId = jsonObj.getString("reason_id");
			String handlingId = jsonObj.getString("handling_id");
			String solvedId = jsonObj.getString("solved_user_id");
			String sqlUpdate = String.format("UPDATE meq_repairings SET reason_id = %s,handling_id = %s,solved_user_id = %s,solved_date = current_timestamp "
														+ "WHERE rep_id = %s",reasonId,handlingId,solvedId,repId);
			boolean res = dbConn.queryUpdate(sqlUpdate);
			if (res) {
				return message.getSuccessInfo("返修处理成功");
			}
		return message.getErrorInfo("修改处理失败");
	}
	
	/**
	 * 返修根据条形码获取产线
	 */
	@RequestMapping(value = "lineIdJson", method = RequestMethod.GET)
	public String getLineId(@RequestParam String barcode) {
		barcode = barcode.trim();
		String lineId = null;
		ResultSet rs = dbConn.query("select pro.line_id from meq_barcodes bar " + 
				"left join meq_line_plan_details lpd on (bar.detail_id = lpd.detail_id) " + 
				"left join meq_production_lines pro on (lpd.line_id = pro.line_id) " + 
				"where barcode = '"+ barcode +"'");
		try {
			if(rs.next()) {
				lineId = rs.getString(1);
			}
		} catch (SQLException e) {}
		return lineId;
	}
	
}
