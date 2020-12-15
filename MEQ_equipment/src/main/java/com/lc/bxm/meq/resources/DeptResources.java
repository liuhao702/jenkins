package com.lc.bxm.meq.resources;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.helper.Str;
import com.lc.bxm.common.util.TreeDateUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import net.sf.json.JSONObject;

/**
 * MEQ部门接口
 * @author JF
 * @date 2019年6月18日
 */
@RestController
@RequestMapping("/dept")
public class DeptResources {

	@Autowired
	PostgreSQLConn dbConn;
	
	@Autowired 
	TreeDateUtil tree;
	
	/**
	 * JF 部门树状图表格分页
	 */
	@RequestMapping(value = "deptTableJson", method = RequestMethod.GET)
	@ResponseBody
	public String getTreeTableJson(@RequestParam String tableName, @RequestParam int pageSize,
			@RequestParam int currentPage, @RequestParam String inputSearch, @RequestParam String order,
			@RequestParam String prop, @RequestParam String filterString,@RequestParam String id) {
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
		//根据传进来的树的表名和ID查找出所有的ID
		if(id != null && !id.equals("")) {
		StringBuilder sb = new StringBuilder();
		sb.append("("+id + getId(id) + ")");
		filterString = filterString +" and dept_id in"+sb.toString();
		}
		String sql = "select bxm_get_grid_page_json('" + tableName + "','*','','" + filterString + "','"
				+ inputSearch + "','" + prop + " " + order + "'," + pageSize + "," + currentPage + ")";
		ResultSet rs = dbConn.query(sql);
		try {
			if (rs.next()) {
				userJson = rs.getString(1);
			}
		} catch (SQLException e) {}
		return userJson;
	}
	
	/**
	 * JF 根据父级部门ID递归查询出所有的下级ID
	 */
	private String getId(String id) {
		StringBuilder sb = new StringBuilder();
		String sql = "select dept_id from sys_departments where parent_dept_id = '" + id + "'";
		ResultSet rs = dbConn.query(sql);
		try {
			while(rs.next()) {
				sb.append(","+rs.getString(1));
				sb.append(getId(rs.getString(1)));
			}
		} catch (SQLException e) {}
		return sb.toString();
	}
	
	/**
	 * JF 获取部门树状图JSON前台调用
	 */
	@RequestMapping(value = "deptJson", method = RequestMethod.GET, produces = "text/html;charset=utf-8")
	@ResponseBody
	public String getDeptJson(Integer comp_id) {
		String deptJson = getGroupJson("",comp_id).replace("null", "");
		return "{\"data\":[" + deptJson + "]" + getTitleJson() + "}";
	}

	/**
	 * JF 获取部门下拉框JSON前台调用
	 */
	@RequestMapping(value = "deptXlJson", method = RequestMethod.GET)
	@ResponseBody
	public String getDeptXlJson() {
		String deptJson = getDeptGroupJson("");
		return "{\"choice\":null,\"tree_options\":[" + deptJson + "]}";
	}
	
	/**
	 * JF 获取列名和列
	 */
	private String getTitleJson() {
		StringBuilder sb = new StringBuilder();
        JSONObject json = new JSONObject();
        String str =null;
		ResultSet rs = dbConn.query(
				"select caption,column_name from sys_columns where table_name = 'v_sys_departments' and is_visible = 'true' order by idx");
		try {
			//sb.append(",\"column\": [");
			while (rs.next()) {
				json.put("title", rs.getString(1));
				json.put("key", rs.getString(2));
				//sb.append("{\"title\": \"" + rs.getString(1) + "\", \"key\": \"" + rs.getString(2) + "\"},");
				sb.append(json.toString()+",");
				//sb.deleteCharAt(sb.length() -1);
		}
			str=sb.deleteCharAt(sb.length()-1).toString();
		} catch (SQLException e) {
		}
		return ","+"\"column\":["+str+"]";
		//return Str.delComma(sb.toString()) + "]";
	}
	
	/**
	 * JF 获取产品类别树状图JSON
	 */
	@RequestMapping(value = "deptMenuJson", method = RequestMethod.GET)
 	@ResponseBody
 	public String prodMenuJson(Integer comp_id) {
		ResultSet rs = null;
		if (comp_id!=null) {
			rs = dbConn.query("select dept_id,dept_name, parent_dept_id from sys_departments where comp_id ="+comp_id+"");
		}else {
			rs = dbConn.query("select dept_id,dept_name, parent_dept_id from sys_departments");
		}
	
		List<Map<Object, Object>> deptJson= tree.getResultSet(rs);
//		String deptJson = getDeptMenuGroupJson("");
		JSONObject json = new JSONObject();
		json.put("id", "0");
		json.put("name", "全部");
		json.put("children", deptJson);
		return "["+json+"]";
		//return "[{\"id\":\"0\",\"name\":\"全部\",\"children\":[" + deptJson + "]}]";
 	}
	
//	/**
// 	 * JF 拼接产品类别分组JSON
// 	 */
// 	private String getDeptMenuGroupJson(String parentId) {
//		ResultSet rs = null;
//		if (parentId == "") {
//			rs = dbConn.query("select dept_id,dept_name from sys_departments where parent_dept_id is null");
//		} else {
//			rs = dbConn.query(String.format(
//					"select dept_id,dept_name from sys_departments where parent_dept_id = '%s'", parentId));
//		}
// 		StringBuilder sb = new StringBuilder();
// 		try {
// 			while (rs.next()) {
//				sb.append("{\"id\":\"");
//				sb.append(rs.getString(1));
//				sb.append("\",\"name\":\"");
//				sb.append(rs.getString(2));
//				sb.append("\"");
//				String str = getDeptMenuGroupJson(rs.getString(1));
// 				if (!str.equals("")) {
//					sb.append(",\"children\":[" + str + "]");
// 				}
// 				sb.append("},");
// 			}
//		} catch (SQLException e) {}
// 		return Str.delComma(sb.toString());
// 	}
//	
	/**
	 * JF 拼接部门分组JSON
	 */
	private String getGroupJson(String parentId,Integer comp_id) {
		StringBuilder sb = new StringBuilder();
 		try {
			ResultSet rs = null;
			if (parentId == "") {
				rs = dbConn.query("select * from v_sys_departments where comp_id ="+comp_id+" and parent_dept_id is null");
			} else {
				rs = dbConn.query(String
						.format("select * from v_sys_departments where comp_id ="+comp_id+" and parent_dept_id = '%s'", parentId));
			}
			int column = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				sb.append("{");
				for (int i = 1; i <= column; i++) {
					String names = rs.getMetaData().getColumnName(i);
					sb.append("\"" + names + "\":\"" + rs.getString(i) + "\",");
				}
				String str = getGroupJson(rs.getString("dept_id"),comp_id);
				if (!str.equals("")) {
					sb.append("\"children\":[" + str + "]");
				} else {
					 //去掉最后面多余的逗号
					sb.deleteCharAt(sb.length() - 1);
				}
				sb.append("},");
			}
		} catch (SQLException e) {}
		return Str.delComma(sb.toString());
	}
	
	/**
	 * JF 拼接部门下拉框分组JSON
	 */
	private String getDeptGroupJson(String parentId) {
		ResultSet rs = null;
		if (parentId == "") {
			rs = dbConn.query("select dept_id,dept_code,dept_name from sys_departments where  parent_dept_id is null");
		} else {
			rs = dbConn.query(String.format(
					"select dept_id,dept_code,dept_name from sys_departments where  parent_dept_id = '%s'", parentId));
		}
		StringBuilder sb = new StringBuilder();
		try {
			while (rs.next()) {
				sb.append("{\"id\":\"");
				sb.append(rs.getString(1));
				sb.append("\",\"name\":\"");
				sb.append(rs.getString(3));
				sb.append("\"");
				String str = getDeptGroupJson(rs.getString(1));
				if (!str.equals("")) {
					sb.append(",\"children\":[" + str + "]");
				}
				sb.append("},");
			}
		} catch (SQLException e) {}
		return Str.delComma(sb.toString());
	}
	
	/**
	 * JF 获取车间下拉框JSON前台调用
	 */
	@RequestMapping(value = "workShop", method = RequestMethod.GET)
	@ResponseBody
	public String getWorkShop() {
		String deptJson = getWorkShopJson("");
		return "{\"filterData\":{\"workshop\":\"\"},\"filterFrame\":[{\"label\":\"车间\",\"key\":\"workshop\",\"multiple\":false,\"s_options\":[" + deptJson + "]}]}";
	}
	
	/**
	 * JF 拼接部门分组JSON
	 */
	private String getWorkShopJson(String parentId) {
		ResultSet rs = null;
		rs = dbConn.query("select dept_id,dept_name from sys_departments where is_workshop");
		StringBuilder sb = new StringBuilder();
		JSONObject json = new JSONObject();
		try {
			while (rs.next()) {
				json.put("id", rs.getString(1));
				json.put("value", rs.getString(2));
				sb.append(json.toString()+",");
//				sb.append("{\"id\":\"");
//				sb.append(rs.getString(1));
//				sb.append("\",\"value\":\"");
//				sb.append(rs.getString(2));
//				sb.append("\"");
//				sb.append("},");
			}
		} catch (SQLException e) {}
		return Str.delComma(sb.toString());
	}
	
	/**
	 * JF 获取班次List
	 */
	@RequestMapping(value = "banciJson", method = RequestMethod.POST)
	@ResponseBody
	public String getBanciJson(@RequestBody String data) {
		JSONObject ojb = JSONObject.fromObject(data);
		String pageSize = ojb.getString("pageSize");
		String currentPage = ojb.getString("currentPage");
		String prop = ojb.getString("prop");
		String order = ojb.getString("order");
		if (order.equals("descending")) {
			order = "desc";
		}
		if (order.equals("ascending")) {
			order = "asc";
		}
		String filterString = getFilterString(ojb);
		if (filterString != "") {
			filterString = filterString.replace("'", "''");
		}
		String result = null;
		ResultSet rs = dbConn.query("select bxm_get_grid_page_json('meq_work_shifts','*','','" + filterString + "','','" + prop + " " + order + "'," + pageSize + "," + currentPage + ")");
		try {
			if (rs.next()) {
				result = rs.getString(1);
			}
		} catch (SQLException e) {}
		return result;
	}

	/**
	 * JF 根据传进来的参数组装filterString
	 */
	private String getFilterString(JSONObject ojb) {
		StringBuilder sb = new StringBuilder();
		sb.append(" 1=1 ");
		//获取班次参数
		String banci = ojb.getString("shift");
		if(banci != null && !banci.equals("")) {
			sb.append(" and shift_name like '%" + banci + "%'");
		}
		// 拼接日期参数
		String date = ojb.getString("dateRange");
		String[] dateArray = date.substring(1, date.length() - 1).replace("\"", "").split(",");
		if (dateArray.length > 1) {
			sb.append(" and time_begin >= '" + dateArray[0] + "' and  time_end <= '" + dateArray[1] + "'");
		}
		return sb.toString();
	}
	
}