package com.lc.bxm.system.resources;

import java.sql.ResultSet;

import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.DataList;
import com.lc.bxm.common.helper.RedisUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/**
 * 增删改查通用接口
 * @author JF
 * @date 2019年6月3日
 */
@RestController
@RequestMapping("/common")
public class Crud2Resource {
	
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
	 * LJZ 通用删除接口
	 */
	@RequestMapping(value = "deleteJson", method = RequestMethod.POST)
	@ResponseBody
	public String getDeleteDataJson(HttpServletRequest request, @RequestBody String deleteInfo) {
		try {
			//连接redis缓存
			//Jedis jedis = redis.init();
			String resultInfo = null;
			String sql = null;
			boolean res = false;
			JSONObject jsonObj = JSONObject.fromObject(deleteInfo);
			String menuId = jsonObj.getString("menuId");
			String funName = jsonObj.getString("funName");
			String tableName = jsonObj.getString("tableName");
			String idName = jsonObj.getString("idName");
			String idValue = jsonObj.getString("idValue");
			String userId = jsonObj.getString("userId");
			if (idValue.contains("[")) {
				JSONArray idValues = jsonObj.getJSONArray("idValue");//
				String idStrValue =idValues.toString();
				// 删除之前获取该信息的JSON,用于日志信息
				ResultSet rsinfo = null;
				for (int i = 0; i < idValues.size(); i++) {
					if(tableName.equals("meq_station_properties")) {
						rsinfo = dbConn.query("select  prop_id from meq_station_properties where prop_code = '"+idValues.get(i)+"'");
						if (rsinfo.next()) {
							rsinfo = dbConn.query("SELECT bxm_get_data_row('" + tableName + "','" + rsinfo.getInt(1) + "')");
						}
					}else {
						rsinfo = dbConn.query("SELECT bxm_get_data_row('" + tableName + "','" + idValues.get(i) + "')");
					}
					//记录日志
					while (rsinfo.next()) {
						resultInfo += rsinfo.getString(1);
					}
					if (tableName.equals("cyj_instrument_equipment_faultalarm")) {// 如果删除的是故障警报,要判断故障警报是否有子集维修记录
						ResultSet rs = dbConn.query(String.format("select exists (select f_id from cyj_instrument_equipment_repair where f_id in(%s) ) ", idValues.get(i)));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该数据已被引用，不可删除");
							}
						}
					}
					if (tableName.equals("sys_roles")) {// 如果删除的是角色,要判断角色是否跟用户关联,有关联则不能删除
						ResultSet rs = dbConn.query(String.format("select exists (select 1 from sys_users where '%s' = any(role_ids))", idValues.get(i)));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("有用户关联到该角色，不可删除");
							}
						}
					}
					if (tableName.equals("sys_menu_groups")) {// 如果删除的是菜单分组,要判断菜单分组是否跟菜单和自身关联,有则否
						ResultSet rs = dbConn.query(String.format("select bxm_sys_menu_groups_delete_valid('%s')", idValues.get(i)));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("有菜单关联到该分组，不可删除");
							}
						}
					}
					if (tableName.equals("sys_menus")) {
						//判断redis缓存是否有当前key
						ResultSet rs = dbConn.query(String.format("select exists (select 1 from sys_menu_functions where menu_uid = '%s')", idValues.get(i)));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("有功能关联到该菜单，不可删除");
							}
						}
					}
					if (tableName.equals("sys_departments")) {// 如果删除的是部门,要判断部门下面是否有子集部门或者员工
						ResultSet rs = dbConn.query(String.format("select exists (select dept_id from v_department_relation where dept_id = '%s') ", idValues.get(i)));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该部门有关联数据，不可删除");
							}
						}
					}
					
					if (tableName.equals("meq_production_lines")) {// 如果删除的是产线,要判断下面是否有工位或工位设置
						ResultSet rs = dbConn.query(String.format("select exists (select line_id from v_meq_production_line_relation where line_id = '%s')", idValues.get(i)));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该产线有关联数据，不可删除");
							}
						}
					}

					if(tableName.equals("meq_employee")) {// 如果删除的是员工,要判断产线设置是否有引用
						ResultSet rs = dbConn.query(String.format("select exists (select 1 from v_meq_employee_relation where employee_id = '%s')", idValues.get(i)));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该员工有关联数据，不可删除");
							}
						}
					}
				}
				if (idStrValue != null && !idStrValue.equals("")) {
					idStrValue = idStrValue.replace("[", "").replace("]", "").replace("\"", "'");
				}
				sql = String.format("DELETE FROM %s WHERE %s", tableName,
						idName + " in" + "(" + idStrValue + ")");
				res = dbConn.queryUpdate(sql);	
			// 捕获日志信息
			getLogs.getUserLog(request, menuId, funName, res, resultInfo, "", userId);
			if (res) {
				return message.getSuccessInfo("删除成功");
			} else {
				return message.getErrorInfo("删除失败");
			}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("删除失败");
		}
		return  message.getErrorInfo("删除失败");
	}
	
	/**
	 * LJZ 通用删除接口
	 */
	@RequestMapping(value = "deleteJson1", method = RequestMethod.POST)
	@ResponseBody
	public String getDeleteDataJson1(HttpServletRequest request, @RequestBody String deleteInfo) {
		try {
			//连接redis缓存
			//Jedis jedis = redis.init();
			String resultInfo = null;
			String sql = null;
			boolean res = false;
			JSONObject jsonObj = JSONObject.fromObject(deleteInfo);
			String menuId = jsonObj.getString("menuId");
			String funName = jsonObj.getString("funName");
			String tableName = jsonObj.getString("tableName");
			String idName = jsonObj.getString("idName");
			String idValue = jsonObj.getString("idValue");
			String userId = jsonObj.getString("userId");
			String idStrValue = null;
			if (idValue.contains("[")) {
				JSONArray idValues = jsonObj.getJSONArray("idValue");//
				idStrValue = idValues.toString();
				// 删除之前获取该信息的JSON,用于日志信息
				ResultSet rsinfo = null;
				for (int i = 0; i < idValues.size(); i++) {
					if(tableName.equals("meq_station_properties")) {
						rsinfo = dbConn.query("select  prop_id from meq_station_properties where prop_code = '"+idValues.get(i)+"'");
						if (rsinfo.next()) {
							rsinfo = dbConn.query("SELECT bxm_get_data_row('" + tableName + "','" + rsinfo.getInt(1) + "')");
						}
					}else {
						rsinfo = dbConn.query("SELECT bxm_get_data_row('" + tableName + "','" + idValues.get(i) + "')");
					}
					//记录日志
					while (rsinfo.next()) {
						resultInfo += rsinfo.getString(1);
					}
					if (tableName.equals("meq_product_cates")) {// 如果删除的是产品类别,要判断下面是否有子集或产品
						if (!idValues.get(i).equals("102")&&!idValues.get(i).equals("113")) {
						ResultSet rs = dbConn.query(String.format("select exists (select 1 from v_meq_product_cate_relation where cate_id = %s)", idValues.get(i)));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该产品类别下面有子类别或产品，不可删除");
							}
						}
						}else {
							return message.getErrorInfo("该类别不能删除");
						}
					}
					
					if (tableName.equals("sys_menu_groups")) {// 如果删除的是菜单分组,要判断菜单分组是否跟菜单和自身关联,有则否
						ResultSet rs = dbConn.query(String.format("select bxm_sys_menu_groups_delete_valid('%s')", idValues.get(i)));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("有菜单关联到该分组，不可删除");
							}
						}
					}
					
					if (tableName.equals("sys_roles")) {// 如果删除的是角色,要判断角色是否跟用户关联,有关联则不能删除
						ResultSet rs = dbConn.query(String.format("select exists (select 1 from sys_users where '%s' = any(role_ids))", idValues.get(i)));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("有用户关联到该角色，不可删除");
							}
						}
					}
				}
				
				if (idStrValue != null && !idStrValue.equals("")) {
					idStrValue = idStrValue.replace("[", "").replace("]", "").replace("\"", "'");
				}
				
					if (tableName.equals("sys_menus")) {
						//判断redis缓存是否有当前key
						ResultSet rs = dbConn.query(String.format("select exists (select 1 from sys_menu_functions where menu_uid in(%s))", idStrValue));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("有功能关联到该菜单，不可删除");
							}
						}
					}
					if (tableName.equals("sys_departments")) {// 如果删除的是部门,要判断部门下面是否有子集部门或者员工
						ResultSet rs = dbConn.query(String.format("select exists (select dept_id from v_department_relation where dept_id in(%s) ) ", idStrValue));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该部门有关联数据，不可删除");
							}
						}
					}
					if (tableName.equals("meq_resource_top_cates")) {// 如果删除的是一级类别,要判断是否有二级类别
						ResultSet rs = dbConn.query(String.format("select exists (select cate_id from meq_resource_cates where top_cate_id in(%s) )" ,idStrValue));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该类别下面有二级类别，不可删除");
							}
						}
					}
					if (tableName.equals("meq_resource_cates")) {// 如果删除的是二级类别,要判断下面是否有设备
						ResultSet rs = dbConn.query(String.format("select exists (select resource_id from meq_resources where cate_id in(%s) )" ,idStrValue));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该类别下面有设备，不可删除");
							}
						}
					}
					if (tableName.equals("meq_production_lines")) {// 如果删除的是产线,要判断下面是否有工位或工位设置
						ResultSet rs = dbConn.query(String.format("select exists (select line_id from v_meq_production_line_relation where line_id in(%s) )", idStrValue));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该产线有关联数据，不可删除");
							}
						}
					}
					if(tableName.equals("meq_employee")) {// 如果删除的是员工,要判断产线设置是否有引用
						ResultSet rs = dbConn.query(String.format("select exists (select 1 from v_meq_employee_relation where employee_id in(%s) )", idStrValue));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该员工有关联数据，不可删除");
							}
						}
					}
					if(tableName.equals("meq_station_properties")) {// 如果删除的是工位属性,要判断是否有工位设置
						ResultSet rs = dbConn.query(String.format("select exists (select 1 from meq_station_settings where prop_code in(%s) )", idStrValue));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该工位属性关联到工位设置，不可删除");
							}
						}
					}
					if(tableName.equals("meq_line_stations")) {// 如果删除的是工位,要判断是否有工序
						ResultSet rs = dbConn.query(String.format("select exists (select 1 from v_line_station_relation where station_id in(%s) )", idStrValue));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该工位有关联数据，不可删除");
							}
						}
					}
					if(tableName.equals("meq_products")) {// 如果删除的是产品,要查询是否有关联的
						ResultSet rs = dbConn.query(String.format("select exists (select 1 from v_meq_product_relation where product_code in(%s) )", idStrValue));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该产品有关联数据，不可删除");
							}
						}
					}
					if(tableName.equals("meq_products_new")) {// 如果删除的是产品,要查询是否有关联的
						ResultSet rs = dbConn.query(String.format("select exists (select 1 from v_meq_product_relation_new where product_code in(%s) )", idStrValue));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该产品有关联数据，不可删除");
							}
						}
					}
					if(tableName.equals("meq_bad_reasons")) {// 如果删除的是不良原因,要查询是否有关联的
						ResultSet rs = dbConn.query(String.format("select exists (select 1 from meq_repairings where reason_id in(%s) )", idStrValue));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该不良原因在返修表有关联数据，不可删除");
							}
						}
					}
					if(tableName.equals("meq_bad_handlings")) {// 如果删除的是不良处理方式,要查询是否有关联的
						ResultSet rs = dbConn.query(String.format("select exists (select 1 from meq_repairings where handling_id in(%s) )", idStrValue));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该不良处理方式在返修表有关联数据，不可删除");
							}
						}
					}
				sql = String.format("DELETE FROM %s WHERE %s", tableName,
						idName + " in" + "(" + idStrValue + ")");
//				System.err.println(sql);
				res = dbConn.queryUpdate(sql);	
			// 捕获日志信息
			getLogs.getUserLog(request, menuId, funName, res, resultInfo, "", userId);
			if (res) {
				return message.getSuccessInfo("删除成功");
			} else {
				return message.getErrorInfo("删除失败");
			}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("删除失败");
		}
		return  message.getErrorInfo("删除失败");
	}

	/**
	 * LJZ 通用编辑接口
	 */
	@RequestMapping(value = "updateJson", method = RequestMethod.POST)
	@ResponseBody
	public String getUpdateDataJson(HttpServletRequest request, @RequestBody String jsonUpdateData) {
		try {
			//连接redis缓存
			//Jedis jedis = redis.init();
			JSONObject jsonObject = JSONObject.fromObject(jsonUpdateData);
			StringBuilder filterString = new StringBuilder();
			StringBuilder updateString = new StringBuilder();
			String menuId = jsonObject.getString("menuId");
			String funName = jsonObject.getString("funName");
			String id = jsonObject.getString("id"); // 当前编辑的ID,如果是用户就用用户ID,角色就是角色ID
			String tableName = jsonObject.getString("tableName");
			String jsonUpdate = jsonObject.getString("formData");
			String userId = null;
			userId = jsonObject.getString("userId");
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
			
//			if (tableName.equals("cyj_instrument_equipment_maintenance")) {//修改设备保养去重
//				String m_id = jsonDataToUpdate.getString("m_id");
//				String role_name = jsonDataToUpdate.getString("m_code");
//				ResultSet cc = dbConn.query(String.format("SELECT EXISTS (SELECT m_code FROM %s WHERE m_code = '%s' and m_id != %s )",tableName,role_name,m_id));
//				if(cc.next()) {
//					if(cc.getBoolean(1)) {
//						return message.getErrorInfo("修改失败,保养编号不能重复");
//					}
//				}
//			}
			
//			if (tableName.equals("cyj_instrument_equipment_inspection")) {//修改设备巡检去重
//				String i_id = jsonDataToUpdate.getString("i_id");
//				String i_oddnumber = jsonDataToUpdate.getString("i_oddnumber");
//				ResultSet cc = dbConn.query(String.format("SELECT EXISTS (SELECT i_oddnumber FROM %s WHERE i_oddnumber = '%s' and i_id != %s )",tableName,i_oddnumber,i_id));
//				if(cc.next()) {
//					if(cc.getBoolean(1)) {
//						return message.getErrorInfo("修改失败,保养编号不能重复");
//					}
//				}
//			}
			if (tableName.equals("meq_production_lines")) {
				String lineName = jsonDataToUpdate.getString("line_name");
				String lineId = jsonDataToUpdate.getString("line_id");
				String deptId = jsonDataToUpdate.getString("dept_id");
				ResultSet cc = dbConn.query(String.format(
						"SELECT EXISTS (SELECT * FROM meq_production_lines WHERE line_name = '%s' AND line_id != '%s' AND dept_id = '%s')", lineName, lineId,deptId));
				if (cc.next()) {
					if (cc.getBoolean(1)) {
						return message.getErrorInfo("修改失败,该产线名称已存在");
					}
				}
			}
			if (tableName.equals("sys_roles")) {
				String role_name = jsonDataToUpdate.getString("role_name");
				String role_uid = jsonDataToUpdate.getString("role_uid");
				ResultSet cc = dbConn.query(String.format(
						"SELECT EXISTS (SELECT * FROM sys_roles WHERE role_name = '%s' AND role_uid != '%s')", role_name, role_uid));
				if (cc.next()) {
					if (cc.getBoolean(1)) {
						return message.getErrorInfo("修改失败,该角色已存在");
					}
				}
			}
			
			if(tableName.equals("sys_menus")) {//菜单url修改排重
				//判断redis缓存是否有当前key
				String url = jsonDataToUpdate.getString("url");
				String menuUid = jsonDataToUpdate.getString("menu_uid");
				ResultSet cc = dbConn.query(String.format("SELECT EXISTS (SELECT 1 FROM sys_menus WHERE url = '%s' AND menu_uid != '%s')", url, menuUid));
				if(cc.next()) {
					if(cc.getBoolean(1)) {
						return message.getErrorInfo("修改失败,该url已存在");
					}
				}
			}
			if(tableName.equals("meq_line_stations")) {//工位管理排重
				String stationId = jsonDataToUpdate.getString("station_id");
				String stationName = jsonDataToUpdate.getString("station_name");
				String lineId = jsonDataToUpdate.getString("line_id");
				ResultSet cc = dbConn.query("select exists (select station_code from meq_line_stations where "
						+ "station_id != '" + stationId + "' and station_name = '" + stationName + "' and line_id = '" + lineId + "')");
				if(cc.next()) {
					if(cc.getBoolean(1)) {
						return message.getErrorInfo("修改失败,同产线的工位名称不能重复");
					}
				}
			}			
			if(tableName.equals("sys_departments")) {//修改部门排重
				String deptId = jsonDataToUpdate.getString("dept_id");
				String parentDeptId = jsonDataToUpdate.getString("parent_dept_id");
				Boolean is_workshop = jsonDataToUpdate.getBoolean("is_workshop");
				if(deptId.equals(parentDeptId)) {
					return message.getErrorInfo("修改失败,上级部门不能选择本部门");
				}
				ResultSet loop = dbConn.query(String.format("SELECT EXISTS (WITH RECURSIVE r AS (SELECT * FROM sys_departments WHERE dept_id = %s union ALL " + 
															"SELECT sys_departments.* FROM sys_departments, r WHERE sys_departments.parent_dept_id = r.dept_id) " + 
															"SELECT * FROM r WHERE dept_id <> %s AND dept_id = %s)", deptId,deptId,parentDeptId));
				if(loop.next()) {
					if(loop.getBoolean(1)) {
						return message.getErrorInfo("修改失败,该部门的上级部门不能选择其下级部门");
					}
				}
				if (!is_workshop) {
					loop = dbConn.query("select exists (select dept_id from meq_production_lines where dept_id ="+deptId+" )");
					if (loop.next()) {
						if (loop.getBoolean(1)) {
							return message.getErrorInfo("该部门\""+jsonDataToUpdate.getString("dept_name")+"\"已关联产线数据，请将产线关联\""+jsonDataToUpdate.getString("dept_name")+"\"指向其他车间");
						}
					}
				}
			}
			if(tableName.equals("meq_employee")) {//工位管理排重
				Boolean is_onduty = jsonDataToUpdate.getBoolean("is_onduty");
				Integer employee_id = jsonDataToUpdate.getInt("employee_id");
				String employee_name = jsonDataToUpdate.getString("employee_name");
				ResultSet rSet = null;
				if (!is_onduty) {
					rSet=dbConn.query("select exists (select person_liable from meq_production_lines where "+employee_id+" =  ANY (person_liable))");
					
					if (rSet.next()) {
						if (rSet.getBoolean(1)) {
							return message.getErrorInfo("该员工\""+employee_name+"\"已关联产线数据，请将产线关联\""+employee_name+"\"指向其他员工");
						}
					}
					rSet=dbConn.query("select exists (select m_id from cyj_instrument_equipment_maintenance where staff_id = "+employee_id+")");
					if (rSet.next()) {
						if (rSet.getBoolean(1)) {
							return message.getErrorInfo("该员工\""+employee_name+"\"已关联设备养护数据，请将设备养护关联\""+employee_name+"\"指向其他员工");
						}
					}
					rSet=dbConn.query("select exists (select i_id from cyj_instrument_equipment_inspection where  staff_id = "+employee_id+")");
					if (rSet.next()) {
						if (rSet.getBoolean(1)) {
							return message.getErrorInfo("该员工\""+employee_name+"\"已关联设备巡检数据，请将设备巡检关联\""+employee_name+"\"指向其他员工");
						}
					}
					rSet=dbConn.query("select exists (select f_id from cyj_instrument_equipment_faultalarm where  f_find_userid = "+employee_id+" OR f_charge_userid ="+employee_id+")");
					if (rSet.next()) {
						if (rSet.getBoolean(1)) {
							return message.getErrorInfo("该员工\""+employee_name+"\"已关联设备故障数据，请将设备故障关联\""+employee_name+"\"指向其他员工");
						}
					}
					rSet=dbConn.query("select exists (select r_id from cyj_instrument_equipment_repair where  r_maintenance_userid = "+employee_id+")");
					if (rSet.next()) {
						if (rSet.getBoolean(1)) {
							return message.getErrorInfo("该员工\""+employee_name+"\"已关联设备维修数据，请将设备维修关联\""+employee_name+"\"指向其他员工");
						}
					}
				}
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
						//判断是否有特殊字段需要改变格式,将["aa","bb"]改为{aa,bb}
						if (key.equals("role_ids") || key.equals("creators") || key.equals("task_cc") || key.equals("person_liable")) {
							String ids = jsonDataToUpdate.getString(key).replace("\"", "");
							updateString.append(key + " = " + "'{" + ids.substring(1, ids.length() - 1) + "}'");
						} else {
							updateString.append(key + " = " + "'" + jsonDataToUpdate.getString(key).replace("'", "''") + "'");
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
			String sqlUpdate = String.format("UPDATE %s SET %s WHERE %s", tableName, updateString, filterString);
			boolean res = dbConn.queryUpdate(sqlUpdate);
			// 捕获日志信息
			getLogs.getUserLog(request, menuId, funName, res, resultInfo, jsonUpdate,userId);
			if (res) {
				return message.getSuccessInfo("修改成功");
			} else {
				return message.getErrorInfo("修改失败");
			}
		} catch (Exception e) {
			//保存错误日志
			e.printStackTrace();
			getLogs.saveErrorMessage(e.toString());
			return message.getErrorInfo("修改失败");
		}
	}
}
