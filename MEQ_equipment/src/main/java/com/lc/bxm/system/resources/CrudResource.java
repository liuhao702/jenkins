package com.lc.bxm.system.resources;

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
import com.lc.bxm.common.helper.DataList;
import com.lc.bxm.common.helper.RedisUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/**
 * 增删改查通用接口
 * @author JF
 * @date 2019年6月3日
 */
@RestController
@RequestMapping("/common")
public class CrudResource{

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	RedisUtil redis;
	@Autowired
	DataList  datalist;
	/**
	 * JF 表格通用接口,分页
	 */
	@RequestMapping(value = "tableJson", method = RequestMethod.GET)
	@ResponseBody
	public String getTableJson(@RequestParam String tableName, @RequestParam int pageSize,
			@RequestParam int currentPage, @RequestParam String inputSearch, @RequestParam String order,
			@RequestParam String prop, @RequestParam String filterString) {
		if (filterString != "") {
			filterString = filterString.replace("'", "''");
			filterString = filterString.replace("\\", "\\\\");
		}
		if (inputSearch != "") {
			inputSearch = inputSearch.replace("\\", "\\\\");
			inputSearch = inputSearch.replace("(","");
			inputSearch = inputSearch.replace(")","");
			inputSearch = inputSearch.replace("*","");
			inputSearch = inputSearch.replace("?","");
			inputSearch = inputSearch.replaceAll("([';])+|(--)+|(%)+|(^)+","");
			
		}
		String userJson = null;
		if (order.equals("descending")) {
			order = "desc";
		}
		if (order.equals("ascending")) {
			order = "asc";
		}
		String sql = "select bxm_get_grid_page_json('" + tableName + "','*','','" + filterString + "','"
				+ inputSearch + "','" + prop + " " + order + "'," + pageSize + "," + currentPage + ")";
		ResultSet rs = dbConn.query(sql);
		System.err.println(sql);
		try {
			if (rs.next()) {
				userJson = rs.getString(1);
			}
		} catch (SQLException e) {}
		return userJson;
	}

	/**
	 * LJZ 通用表单新增接口
	 */
	@RequestMapping(value = "addJson", method = RequestMethod.POST)
	@ResponseBody
	public String getAddDataJson(HttpServletRequest request, @RequestBody String jsonSaveData) {
		try {
			//连接redis缓存
			//Jedis jedis = redis.init();
			JSONObject jsonObject = JSONObject.fromObject(jsonSaveData);
			StringBuilder columnName = new StringBuilder();
			StringBuilder columnValue = new StringBuilder();
			Iterator<?> iter = null;
			String menuId = jsonObject.getString("menuId");
			String funName = jsonObject.getString("funName");
			String tableName = jsonObject.getString("tableName");
			String saveData = jsonObject.getString("formData");
			String userId = null;
			userId = jsonObject.getString("userId");
			JSONObject jsonDataToAdd = JSONObject.fromObject(saveData);
			Object object = null;
			if (tableName.equals("sys_users")) {//新增用户排重处理
				String userCode = jsonDataToAdd.getString("user_code");
				String  comp_id = jsonDataToAdd.getString("comp_id");
				ResultSet rs = dbConn.query(String.format("SELECT EXISTS (SELECT user_code FROM sys_users WHERE user_code = '%s' and comp_id =%s )", userCode,comp_id));
				if(rs.next()) {
					if(rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,该账号已存在");
					}
				}
			}

			if (tableName.equals("sys_roles")) {//新增角色排重处理
				String role_name = jsonDataToAdd.getString("role_name");
				String sql = null;
				if (userId.equals("1d0e11c1-691f-400b-bf10-f5bc52433bb7")) {
					sql = String.format("SELECT EXISTS (SELECT role_name FROM sys_roles WHERE role_name = '%s')", role_name);
				}else {
					String  comp_id = jsonDataToAdd.getString("comp_id");
					sql = String.format("SELECT EXISTS (SELECT role_name FROM sys_roles WHERE role_name = '%s' and comp_id =%s)", role_name,comp_id);
				}
				ResultSet rs = dbConn.query(sql);
				if(rs.next()) {
					if(rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,该角色已存在");
					}
				}
			}
			if (tableName.equals("cyj_instrument_equipment_maintenance")) {//新增设备保养排重处理
				String m_code = jsonDataToAdd.getString("m_code");
				String  comp_id = jsonDataToAdd.getString("comp_id");
				ResultSet rs = dbConn.query(String.format("SELECT EXISTS (SELECT m_code FROM %s WHERE m_code = '%s' and comp_id =%s)",tableName,m_code,comp_id));
				if(rs.next()) {
					if(rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,保养编号已存在");
					}
				}
			}
			if (tableName.equals("cyj_instrument_equipment_inspection")) {//新增设备巡检排重处理
				String i_oddnumber = jsonDataToAdd.getString("i_oddnumber");
				String  comp_id = jsonDataToAdd.getString("comp_id");
				ResultSet rs = dbConn.query(String.format("SELECT EXISTS (SELECT i_oddnumber FROM %s WHERE i_oddnumber = '%s' and comp_id =%s)",tableName,i_oddnumber,comp_id));
				if(rs.next()) {
					if(rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,巡检单号已存在");
					}
				}
			}
			if (tableName.equals("cyj_instrument_equipment_state")) {//新增设备状态排重处理
				String s_code = jsonDataToAdd.getString("s_code");
				String  comp_id = jsonDataToAdd.getString("comp_id");
				ResultSet rs = dbConn.query(String.format("SELECT EXISTS (SELECT s_code FROM %s WHERE s_code = '%s' and comp_id =%s)",tableName,s_code,comp_id));
				if(rs.next()) {
					if(rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,设备状态码已存在");
					}
				}
			}
			if (tableName.equals("cyj_instrument_equipment_faultalarm")) {//新增设备故障排重处理
				String f_code = jsonDataToAdd.getString("f_code");
				String  comp_id = jsonDataToAdd.getString("comp_id");
				ResultSet rs = dbConn.query(String.format("SELECT EXISTS (SELECT f_code FROM %s WHERE f_code = '%s' and comp_id =%s)",tableName,f_code,comp_id));
				if(rs.next()) {
					if(rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,故障代码已存在");
					}
				}
			}
			if (tableName.equals("cyj_instrument_equipment_repair")) {//新增设备维修单号排重处理
				String r_oddnumber = jsonDataToAdd.getString("r_oddnumber");
				String  f_id = jsonDataToAdd.getString("f_id");
				ResultSet rs = dbConn.query(String.format("SELECT EXISTS (SELECT r_oddnumber FROM %s WHERE r_oddnumber = '%s' and f_id =%s)",tableName,r_oddnumber,f_id));
				if(rs.next()) {
					if(rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,维修单号已存在");
					}
				}
			}
			if (tableName.equals("sys_menus")) {//新增菜单排重处理
				String url = jsonDataToAdd.getString("url");
				ResultSet rs = dbConn.query(String.format("SELECT EXISTS (SELECT url FROM sys_menus WHERE url = '%s')", url));
				if(rs.next()) {
					if(rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,该url已存在");
					}
				}
			}
			if (tableName.equals("meq_employee")) {//员工编码排重
				String empCode = jsonDataToAdd.getString("employee_code");
				String  comp_id = jsonDataToAdd.getString("comp_id");
				ResultSet rs = dbConn.query(String.format("SELECT EXISTS (SELECT employee_code FROM meq_employee WHERE employee_code = '%s' and comp_id =%s)", empCode,comp_id));
				if(rs.next()) {
					if(rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,该员工编码已存在");
					}
				}
			}
			if(tableName.equals("sys_departments")) {//新增部门排重
				String deptCode = jsonDataToAdd.getString("dept_code");
				String  comp_id = jsonDataToAdd.getString("comp_id");
				ResultSet rs = dbConn.query("select exists (select dept_code from sys_departments where dept_code ='"+ deptCode +"' and comp_id ="+comp_id+")");
				if(rs.next()) {
					if(rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,该部门编码已存在");
					}
				}
			}
			if(tableName.equals("meq_production_lines")) {//产线排重
				String lineName = jsonDataToAdd.getString("line_name");
				String deptId = jsonDataToAdd.getString("dept_id");
				String  comp_id = jsonDataToAdd.getString("comp_id");
				ResultSet rs = dbConn.query("select exists (select line_name from meq_production_lines where "
						+ "line_name = '"+ lineName +"' and dept_id = '"+ deptId +"' and comp_id ="+comp_id+")");
				if(rs.next()) {
					if(rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,同车间产线名称不能重复");
					}
				}
			}
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
					// 判断如果有特殊字段的需要改变格,比如说下拉框多选,将["aa","bb"]改为{aa,bb}
					if (key.equals("role_ids") || key.equals("creators") || key.equals("task_cc") || key.equals("person_liable")) {
						String ids = jsonDataToAdd.getString(key).replace("\"", "");
						columnValue.append("'{" + ids.substring(1, ids.length() - 1) + "}'");
					} else {
						columnValue.append("'" + jsonDataToAdd.getString(key).replace("'", "''") + "'");
					}
				}
				if (iter.hasNext()) {
					columnValue.append(",");
				}
			}
			String sqlInsert = String.format("INSERT INTO %s(%s) VALUES(%s)", tableName, columnName, columnValue);
			boolean res = dbConn.queryUpdate(sqlInsert);
			System.err.println(sqlInsert);
			// 捕获用户操作日志
			getLogs.getUserLog(request, menuId, funName, res, "", saveData, userId);
			if (res) {
				return message.getSuccessInfo("新增成功");
			} else {
				return message.getErrorInfo("新增失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("新增失败");
		}
	}

}
