package com.lc.bxm.system.resources;

import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.common.util.TreeDateUtil;

/**
 * 通用接口
 * @author JF
 * @date 2019年5月11日
 */
@RestController
@RequestMapping("/common")
public class CommonResource {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CommonResource.class);

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	//获取下拉框数据方法
	@Autowired
	TreeDateUtil treeData;

	/**
	 * JF 获取表单信息通用接口
	 */
	@RequestMapping(value = "formJson", method = RequestMethod.GET, produces = "text/html;charset=utf-8")
	@ResponseBody
	public String formJson(@RequestParam String id, @RequestParam String tableName) {
//		logger.info("formJson start");
		String dateJson = getFormDataJson(tableName, id);
		String frameJson = getFormFrameJson(tableName, id);
		return "{" + dateJson + frameJson + "}";
	}
	
	/**
	 * LJZ 获取视图表单信息通用接口
	 */
	@RequestMapping(value = "formViewJson", method = RequestMethod.GET)
	@ResponseBody
	public String formViewJson(@RequestParam String id, @RequestParam String keyColumnName, @RequestParam String tableName) {
//		logger.info("formJson start");
		String dateJson = getViewFormDataJson(tableName,keyColumnName, id);
		String frameJson = getFormFrameJson(tableName, id);
		return "{" + dateJson + frameJson + "}";
	}

	/**
	 * JF 拼接表单控件JSON FORMDATA部分
	 */
	private String getFormDataJson(String tableName, String id) {
		String result = null;
		JSONObject json = new JSONObject();
		// 如果ID为空就是新增,不为空为编辑
		try {
			if (id != "") {
				ResultSet rs = dbConn.query("select bxm_get_data_json('" + tableName + "','" + id + "')");
				while (rs.next()) {
					result = rs.getString(1);
				}
			} else {
				ResultSet rs = dbConn.query(
						"select column_name,control_type,bxm_get_default_value(default_value),data_type from v_sys_columns "
						+ "where not is_deactive and control_type is not null and is_visible and table_name = '"
								+ tableName + "'");
				while (rs.next()) {
					// 新增的时候判断是否为单选按钮，如果是就给一个默认值
					if (rs.getString(2).equals("radio")) {
						if (rs.getString(3) == null) {
							json.put(rs.getString(1), true);
						} else {
							json.put(rs.getString(1), rs.getBoolean(3));
						}
					} else {
						if (rs.getString(3) == null) {
							if (rs.getString(4).startsWith("_")) {
								json.put( rs.getString(1), "[]");
							} else {
								json.put(rs.getString(1), "null");
							}
						} else {
							json.put(rs.getString(1), rs.getString(3));
						}
					}
				}
				result = "\"formData\":" + json.toString() + "";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * JF 拼接表单控件JSON FORMDATA部分
	 */
	private String getViewFormDataJson(String tableName,String keyColumnName, String id) {
		String result = null;
		StringBuilder sb = new StringBuilder();
		JSONObject json = new JSONObject();
		// 如果ID为空就是新增,不为空为编辑
		try {
			if (id != "") {
				ResultSet rs = dbConn.query("select bxm_get_data_json_by_key_column('" + tableName + "','" + keyColumnName + "','" + id + "')");
				while (rs.next()) {
					result = rs.getString(1);
				}
			} else {
				ResultSet rs = dbConn.query(
						"select column_name,control_type,bxm_get_default_value(default_value),data_type from v_sys_columns "
						+ "where not is_deactive and control_type is not null and is_visible and table_name = '"
								+ tableName + "'");
				while (rs.next()) {
					// 新增的时候判断是否为单选按钮，如果是就给一个默认值
					if (rs.getString(2).equals("radio")) {
						if (rs.getString(3) == null) {
							json.put(rs.getString(1), true);
						} else {
							json.put(rs.getString(1), rs.getString(3));
						}
					} else {
						if (rs.getString(3) == null) {
							if (rs.getString(4).startsWith("_")) {
								json.put(rs.getString(1), "[]");
								json.put(rs.getString(1), "null");
							}
						} else {
							json.put(rs.getString(1), rs.getString(3));
						}
					}
					sb.append(json.toString()+",");
				}
				result = "\"formData\":{" + Str.delComma(sb.toString()) + "}";
			}
		} catch (SQLException e) {}
		return result;
	}
	
	/**
	 * JF 拼接表单控件JSON FORMFRAME部分
	 */
	private String getFormFrameJson(String tableName, String id) {
		ResultSet rs = dbConn.query(
				"select column_name,caption,control_type,place_holder,edit_mode,source_uid,is_visible,valid_reg,valid_prompt,required,source_uid,source_text,view_name,filter_string,value_member,display_member"
						+ " from v_sys_columns where is_visible = true and control_type is not null and table_name = '" + tableName + "' order by column_idx");
		StringBuilder sb = new StringBuilder();
		try {
			while (rs.next()) {
				// 修改是否静止必填的状态
				boolean isornot = false;
				if (rs.getInt(5) == 0) {
					isornot = true;
				} else if (rs.getInt(5) == 2 && (!id.isEmpty())) {
					isornot = true;
				}
				// 判断控件类型是否为空,为空就不显示
				if (rs.getString(3) != null && !"".equals(rs.getString(3))) {
					sb.append("{");
					sb.append("\"label\":\"" + rs.getString(2) + "\",");
					sb.append("\"type\":\"" + rs.getString(3) + "\",");
					sb.append("\"placeholder\":\"" + rs.getString(4) + "\",");
					sb.append("\"disabled\":" + isornot + ",");
					sb.append("\"key\":\"" + rs.getString(1) + "\"");
					// 拼接RULES
					sb.append(",\"rules\":[{");
					sb.append("\"required\":" + rs.getBoolean(10) + ",");
					sb.append("\"message\":\"" + rs.getString(9) + "\"");
					// 判断是否有验证规则,如果有就添加
					if (rs.getString(8) != null) {
						sb.append(",\"reg\":\"" + rs.getString(8) + "\",");
						sb.append("\"validator\": \"validator\",\"trigger\": \"blur\"");
					}
					sb.append("}]");
					// 根据SOURCE_UID判断是否有数据源
					if (rs.getString(11) != null) {
						if (rs.getString(3).equals("radio")) {
							sb.append(",\"r_options\":");
							//str = "r_options";
						} else if (rs.getString(3).equals("checkbox")) {
							sb.append(",\"c_options\":");
						} else if (rs.getString(3).equals("select_one") || rs.getString(3).equals("select_many") || rs.getString(3).equals("input_select_one")) {
							sb.append(",\"s_options\":");
						} else if (rs.getString(3).equals("select_tree") || rs.getString(3).equals("select_tree_end") || rs.getString(3).equals("select_tree_end_many")) {
							sb.append(",\"tree_options\":");
						} else if(rs.getString(3).equals("tablebox_one")){
							sb.append(",\"table_options\":");
						}else {
							sb.append(",\"r_options\":");
						}
						if (rs.getString(3).equals("select_tree") || rs.getString(3).equals("select_tree_end") || rs.getString(3).equals("select_tree_end_many")) {
							if (rs.getString(13).equals("v_active_menu_tree")) {
								sb.append(getSelectTreeJsonNew(rs.getString(13), rs.getString(15), rs.getString(16),
										rs.getString(14), ""));
							}else {
								sb.append("[" + getSelectTreeJson(rs.getString(13), rs.getString(15), rs.getString(16),
								rs.getString(14), "") + "]");
							}
							
						} else {
						
							sb.append(rs.getString(12));
							if(rs.getString(3).equals("tablebox_one")) {
								ResultSet rsDatasource = dbConn.query(String.format("SELECT source_text FROM v_sys_datasources WHERE is_value_member_visible = '是' and view_name = (SELECT d.view_name FROM v_sys_datasources d LEFT JOIN sys_columns c ON d.source_uid = c.source_uid WHERE c.table_name = '%s' AND c.control_type = 70)", tableName));
								if(rsDatasource.next()) {
									sb.append(String.format(",\"s_options\":%s", rsDatasource.getString(1)));
								}
							}
						}
					}
					sb.append("},");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
	 *  LH下拉框层级new
	 */
	private Object getSelectTreeJsonNew(String viewName, String valueMember, String displayMember, String filterString,
			String parentUid) {
			String sql  = String.format("SELECT uid, name, parent_uid FROM %s WHERE %s",viewName,filterString == null || filterString == "" ? "1=1" : filterString);
			Object treeStr =treeData.getTree(sql);
	       return treeStr;
	}

	/**
	 * JF 获取下拉框通用接口
	 */
	@RequestMapping(value = "datasourceJson", method = RequestMethod.POST)
	@ResponseBody
	public String filterJson(@RequestBody String filterArray) {
		JSONObject jsonObject = JSONObject.fromObject(filterArray);
		//获取JSON数组,然后根据它去组装2部分的数据
		JSONArray json = JSONArray.fromObject(jsonObject.getString("filterArray"));
		return "{" + getFilterDataJson(json) + getFilterFrameJson(json.toString()) + "}";
	}
	
	/**
	 * JF 获取下拉框JSON DATA部分
	 */
	public String getFilterDataJson(JSONArray jsonArray) {
		JSONObject json = new JSONObject();
		for(int i=0;i<jsonArray.size();i++) {
			json.put(jsonArray.getString(i), "");;
		}
		return "\"filterData\":"+json.toString();
		
	}

	/**
	 * JF 获取下拉框JSON FRAME部分
	 */
	public String getFilterFrameJson(String code) {
		code = code.substring(1, code.length() - 1).replace("\"", "'");
		ResultSet rs = dbConn.query("select source_code,source_name,source_text,multiselect from v_sys_datasources where source_code in (" + code + ") "
							+ "order by array_position(array[" + code + "],source_code::text)");
		StringBuilder sb = new StringBuilder();
		JSONObject json = new JSONObject();
		try {
			while (rs.next()) {
				json.put("key", rs.getString(1));
				json.put("label", rs.getString(2));
				json.put("multiple", rs.getBoolean(4));
				json.put("s_options", rs.getString(3));
				sb.append(json.toString()+",");
			}
		} catch (SQLException e) {}
		String res = Str.delComma(sb.toString());
		return ",\"filterFrame\":[" + res + "]";
	}
	
	@RequestMapping(value = "accessdb", method = RequestMethod.GET)
	@ResponseBody
	public String  AccessDb(HttpServletRequest request, @RequestParam String employee_code ) {
		ResultSet rs = dbConn.query("select * from v_meq_employee where employee_code = '"+employee_code+"'");
		String  str = null;
		try {
			while(rs.next()) {
				str = "employee_code:"+rs.getObject("employee_code")+",employee_name:"+rs.getObject("employee_name")+",is_onduty:"+rs.getObject("is_onduty")+"";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return str;
	}
	/**
	   *  通用的下拉框数据接口 （根据comp_id）
	 * @param request
	 * @param alueField, displayField,tableName, comp_id,condition 值字段，显示字段，表，所属ID,条件
	 * @return
	 */
	@RequestMapping(value = "dropDownBox", method = RequestMethod.GET)
	@ResponseBody
	public List<Map<Object, Object>>  dropDownBox(@RequestParam String tableName,@RequestParam String valueField,
			@RequestParam String displayField, @RequestParam Integer comp_id ,@RequestParam String condition) {
		ResultSet rs = null;
		if (tableName.contains("v_sys_departments_tree")||tableName.contains("v_equipment_species_tree")) {
			if (condition.length()>0) {
				rs=dbConn.query(String.format("select * from %s where comp_id = %s and %s",tableName, comp_id,condition));
			}else {
				rs=dbConn.query(String.format("select * from %s where comp_id = %s",tableName, comp_id));
			}
			List<Map<Object, Object>> deptJson= treeData.getResultSet(rs);
			return deptJson;
		}else {
		if (condition.length()>0) {
			rs=dbConn.query(String.format("select %s,%s from %s where comp_id = %s and %s",valueField, displayField,tableName, comp_id,condition));
		}else {
			rs=dbConn.query(String.format("select %s,%s from %s where comp_id = %s",valueField, displayField,tableName, comp_id));
		}
		List<Map<Object, Object>> list = new ArrayList<Map<Object,Object>>();
		try {
			while (rs.next()) {
				Map<Object, Object> map = new HashMap<Object, Object>();
				map.put("id", rs.getObject(1));
				map.put("value", rs.getObject(2));
			    list.add(map);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	 }
		
	}
}
	
