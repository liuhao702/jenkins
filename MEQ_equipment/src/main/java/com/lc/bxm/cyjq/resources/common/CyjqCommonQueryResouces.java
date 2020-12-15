package com.lc.bxm.cyjq.resources.common;

import java.sql.ResultSet;


import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONObject;

/**
 * cyjq通用查询接口
 * @author LH
 * @date 2020年5月25日
 */
@RestController
@RequestMapping("/cyjqcommon")
public class CyjqCommonQueryResouces {
	
	private static Logger logger = Logger.getLogger(CyjqCommonQueryResouces.class);

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;
	
	/**
	 * LH 表主键名称必须id才能使用该接口树状图表格分页
	  *    查询全部filterString可以不传
	 */
	@RequestMapping(value = "tableJson", method = RequestMethod.GET)
	@ResponseBody
	public String getProdTableJson(@RequestParam String tableName, @RequestParam int pageSize,
			@RequestParam int currentPage, @RequestParam String inputSearch, @RequestParam String order,
			@RequestParam String prop, @RequestParam String filterString, @RequestParam String id) {
		if (filterString != "") {
			filterString = filterString.replace("'", "''");
		}
		String userJson = null;
		if (order.equals("descending")) {
			order = "desc";
		}
		if (order.equals("ascending")) {
			order = "asc";
		}
		// 根据传进来的树的ID查找出所有的下级ID
		 if(id != null && !id.equals("")) {
			 StringBuilder sb = new StringBuilder();
					sb.append("("+id + getId(id,tableName,filterString) + ")");
					filterString = " 1=1 and "+filterString+" in"+sb.toString();
				} else {
					filterString = "1=1" ;
			}
		String sql = "select bxm_get_grid_page_json('" + tableName + "','*','','" + filterString + "','"
				+ inputSearch + "','" + prop + " " + order + "'," + pageSize + "," + currentPage + ")";
		System.err.println(sql);
		ResultSet rs = dbConn.query(sql);
		try {
			if (rs.next()) {
				userJson = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return message.getErrorInfo("修改失败,系统内部问题"+e.getMessage());
		}
		return userJson;
	}
	
	/**
	 * LH 根据父级ID递归查询所有的子ID
	 */
	private String getId(String id, String tableName, String filterString) {
		String sql = null;
		if (tableName.equals("v_cyj_product_quality_planning")) {
			 sql = "select p.id from "+tableName+" p "
			 		+ "left join meq_products m on p.product_code = m.product_code  where  m.cate_id ="+id+"";
		}else {
			 sql = "select id from "+tableName+" where "+filterString+" = '" + id + "'";
		}
		StringBuilder sb = new StringBuilder();
		ResultSet rs = dbConn.query(sql);
		try {
			while(rs.next()) { 
				sb.append(","+rs.getString(1));
				sb.append(getId(rs.getString(1),tableName,filterString));
			}
		} catch (SQLException e) {}
		return sb.toString();
	}
	
	
	
	/**
	 * JF 获取表单信息通用接口
	 */
	@RequestMapping(value = "formJson", method = RequestMethod.GET, produces = "text/html;charset=utf-8")
	@ResponseBody
	public String formJson(@RequestParam String id, @RequestParam String tableName) {
		logger.info("formJson start");
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
		logger.info("formJson start");
		String dateJson = getViewFormDataJson(tableName,keyColumnName, id);
		String frameJson = getFormFrameJson(tableName, id);
		return "{" + dateJson + frameJson + "}";
	}

	/**
	 * JF 拼接表单控件JSON FORMDATA部分
	 */
	private String getFormDataJson(String tableName, String id) {
		String result = null;
		//StringBuilder sb = new StringBuilder();
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
		} catch (SQLException e) {}
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
							} else {
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
							sb.append("[" + getSelectTreeJson(rs.getString(13), rs.getString(15), rs.getString(16),
									rs.getString(14), "") + "]");
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
		} catch (SQLException e) {}
		return ",\"formFrame\":[" + Str.delComma(sb.toString()) + "]";
	}

	/**
	 * LJZ 下拉框层级
	 */
	private String getSelectTreeJson(String viewName, String valueMember, String displayMember, String filterString,
			String parentUid) {
		ResultSet rs = null;
		String cloumnName=cloumnName(viewName);
		if (parentUid == "") {
			rs = dbConn.query(String.format("SELECT * FROM %s WHERE %s %s IS NULL", viewName,
					filterString == null ? "" : filterString + " AND ", cloumnName));
		} else {
			rs = dbConn.query(String.format("SELECT * FROM %S WHERE %s %s = '%s'", viewName,
					filterString == null ? "" : filterString + " AND ",cloumnName, parentUid));
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

	//判断树结构数据父级字段
	public String cloumnName(String tableName) {
		switch (tableName) {
		case "v_cyj_instrument_equipment_species": //仪器设备种类设定
			return "species_id";
        case "v_cyj_product_inspection_setting":  //产品检测层别设定
        	return "up_product_pro_ids";
        case "v_cyj_product_inspection_level":    //产品检测属性设定
        	return "up_product_level_id";
		}
		return null;
	}
}
