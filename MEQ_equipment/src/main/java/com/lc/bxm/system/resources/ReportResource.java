package com.lc.bxm.system.resources;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.helper.Str;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/**
 * 报表搜索条件接口
 * @author LJZ
 * @date 2019年8月11日
 */
@RestController
@RequestMapping("/report")
public class ReportResource {
	
	@Autowired
	PostgreSQLConn dbConn;
	
	/**
	 * LJZ 获取动态条件通用接口
	 */
	@RequestMapping(value = "filterControlJson", method = RequestMethod.GET, produces = "text/html;charset=utf-8")
	@ResponseBody
	public String formJson(@RequestParam String menuUid) {
		String dateJson = getFormDataJson(menuUid);
		String frameJson = getFormFrameJson(menuUid);
		return "{" + dateJson + frameJson + "}";
	}
	
	/**
	 * LJZ 拼接动态条件控件JSON FORMDATA部分
	 */
	private String getFormDataJson(String menuUid) {
		String result = null;
		StringBuilder sb = new StringBuilder();
		// 如果ID为空就是新增,不为空为编辑
		try {
			ResultSet rs = dbConn.query(String.format("SELECT column_name,control_type,default_value FROM v_visual_report_filter WHERE menu_uid = '%s'", menuUid));
			while (rs.next()) {
				// 新增的时候判断是否为单选按钮，如果是就给一个默认值
				if (rs.getString("control_type").equals("radio")) {
					if (rs.getString("default_value") == null) {
						sb.append("\"" + rs.getString("column_name") + "\":" + true + ",");
					} else {
						sb.append("\"" + rs.getString("column_name") + "\":" + rs.getString("default_value") + ",");
					}
				} else {
					if (rs.getString("default_value") == null) {
						sb.append("\"" + rs.getString("column_name") + "\":null,");
					} else {
						sb.append("\"" + rs.getString("column_name") + "\":\"" + rs.getString("default_value") + "\",");
					}
				}
			}
			result = "\"formData\":{" + Str.delComma(sb.toString()) + "}";
		} catch (SQLException e) {}
		return result;
	}
	
	/**
	 * LJZ 拼接动态条件控件JSON FORMFRAME部分
	 */
	private String getFormFrameJson(String menuUid) {
		ResultSet rs = dbConn.query(String.format("SELECT * FROM v_visual_report_filter WHERE menu_uid = '%s'", menuUid));
		StringBuilder sb = new StringBuilder();
		try {
			while (rs.next()) {
				sb.append("{");
				sb.append("\"label\":\"" + rs.getString("caption") + "\",");
				sb.append("\"type\":\"" + rs.getString("control_type") + "\",");
				sb.append("\"placeholder\":\"" + rs.getString("place_holder") + "\",");
				sb.append("\"key\":\"" + rs.getString("column_name") + "\",");
				sb.append("\"width\":\"" + rs.getString("width") + "\"");
				// 根据SOURCE_UID判断是否有数据源
				if (rs.getString("source_uid") != null) {
					if (rs.getString("control_type").equals("radio")) {
						sb.append(",\"r_options\":");
					} else if (rs.getString("control_type").equals("checkbox")) {
						sb.append(",\"c_options\":");
					} else if (rs.getString("control_type").equals("select_one") || rs.getString(3).equals("select_many") || rs.getString(3).equals("input_select_one")) {
						sb.append(",\"s_options\":");
					} else if (rs.getString("control_type").equals("select_tree") || rs.getString(3).equals("select_tree_end")) {
						sb.append(",\"tree_options\":");
					} else if(rs.getString("control_type").equals("tablebox_one")){
						sb.append(",\"table_options\":");
					}else {
						sb.append(",\"r_options\":");
					}
					if (rs.getString("control_type").equals("select_tree") || rs.getString("control_type").equals("select_tree_end")) {
						sb.append("[" + getSelectTreeJson(rs.getString("view_name"), rs.getString("value_member"), rs.getString("display_member"),
								rs.getString("filter_string"), "") + "]");
					}else {
						sb.append(rs.getString("source_text"));
					}
				}
				sb.append("},");
			}
		} catch (SQLException e) {}
		return ",\"formFrame\":[" + Str.delComma(sb.toString()) + "]";
	}
	
	/**
	 * LJZ 下拉框层级
	 */
	private String getSelectTreeJson(String viewName, String valueMember, String displayMember, String filterString,
			String parentUid) {
		ResultSet rs = null;
		if (parentUid == "") {
			rs = dbConn.query(String.format("SELECT * FROM %s WHERE %s parent_uid IS NULL", viewName,
					filterString == null ? "" : filterString + " AND "));
		} else {
			rs = dbConn.query(String.format("SELECT * FROM %S WHERE %s parent_uid = '%s'", viewName,
					filterString == null ? "" : filterString + " AND ", parentUid));
		}
		StringBuilder sb = new StringBuilder();
		try {
			while (rs.next()) {
				sb.append("{");
				sb.append("\"id\":\"" + rs.getString(valueMember) + "\",");
				sb.append("\"label\":\"" + rs.getString(displayMember) + "\"");
				String str = getSelectTreeJson(viewName, valueMember, displayMember, filterString, rs.getString(valueMember));
				if (!str.equals("")) {
					sb.append(",\"children\":[" + str + "]");
				}
				sb.append("},");
			}
		} catch (SQLException e) {}
		return Str.delComma(sb.toString());
	}
	
	/**
	 * 报表参数字段联动
	 */
	@RequestMapping(value = "columnName", method = RequestMethod.GET)
	@ResponseBody
	public String productionLine(@RequestParam String menuId) {
		StringBuilder sb = new StringBuilder();
		ResultSet rs = dbConn.query(String.format("SELECT c.column_name,c.column_name FROM sys_columns c " + 
				"JOIN sys_visual_reports r on r.view_name = c.table_name WHERE r.menu_uid = '%s'", menuId));
		System.err.println(String.format("SELECT c.column_name,c.column_name FROM sys_columns c " + 
				"JOIN sys_visual_reports r on r.view_name = c.table_name WHERE r.menu_uid = '%s'", menuId));
		try {
			while (rs.next()) {
				sb.append("{\"id\":\"" + rs.getString(1) + "\",\"value\":\"" + rs.getString(2) + "\"},");
			}
		} catch (SQLException e) {}
		return "[" + Str.delComma(sb.toString()) + "]";
	}
	
	/**
	 * LJZ 报表动态条件拼接
	 */
	@RequestMapping(value = "reportFilter", method = RequestMethod.POST)
	@ResponseBody
	public String reportFilter(@RequestBody String filterArray) {
		String key = null;
		StringBuilder filter = new StringBuilder();
		String controlTypeString = ""; 
		try {
			if(!filterArray.isEmpty()) {
				JSONObject jsonObject = JSONObject.fromObject(filterArray);
				String menuId = jsonObject.getString("menuId");
				for(Iterator<?> iter = jsonObject.keys(); iter.hasNext();) {
					key = (String) iter.next();
					
					ResultSet controlTypeResult = dbConn.query(String.format("SELECT control_type FROM sys_visual_report_parameters WHERE menu_uid = '%s' AND column_name = '%s' LIMIT 1", menuId,key));
					if(controlTypeResult.next()) {
						controlTypeString = controlTypeResult.getString(1);
					}
					
					if(jsonObject.get(key) instanceof JSONNull || jsonObject.getString(key).isEmpty() || key.equals("menuId")) {
					}else if (controlTypeString.equals("0")) {
						filter.append(key + " ~ '" + jsonObject.getString(key) + "' AND ");
					}else if(jsonObject.get(key) instanceof Integer) {
						filter.append(key + " = " + jsonObject.getInt(key) + " AND ");
					}else if(jsonObject.get(key) instanceof String) {
						filter.append(key + " = '" + jsonObject.getString(key) + "' AND ");
					}else if(jsonObject.get(key) instanceof JSONArray) {
						JSONArray arrayObject = JSONArray.fromObject(jsonObject.getString(key));
						if(arrayObject.size() == 2 && isValidDate(arrayObject.getString(0)) && isValidDate(arrayObject.getString(1))) {
							filter.append(key + " BETWEEN '" + arrayObject.getString(0) + " 00:00:00' AND '" + arrayObject.getString(1) + " 23:59:59.999' AND ");
						}
					}
				}
				return Str.delStringAnd(filter.toString().trim());
			}else {
				return "";
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		return "";
	}
	
	//验证字符串是否符合yyyy-MM-dd的日期格式
	private boolean isValidDate(String str) {
		boolean convertSuccess=true;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			format.setLenient(false);
			format.parse(str);
		} catch (ParseException e) {
			convertSuccess=false;
		} 
		return convertSuccess;
	}
}
