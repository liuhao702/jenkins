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
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/**
 * 工艺卡接口
 * 
 * @author JF
 * @date 2019年7月6日
 */
@RestController
@RequestMapping("/card")
public class CardResources {

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;

	/**
	 * LJZ 表格接口,从表行内编辑
	 */
	@RequestMapping(value = "tableJson", method = RequestMethod.GET)
	@ResponseBody
	public String getTableJson(@RequestParam String tableName, @RequestParam String inputSearch,
			@RequestParam String order, @RequestParam String prop, @RequestParam String filterString) {
		if (filterString != "") {
			filterString = filterString.replace("'", "''");
			filterString = filterString.replace("\\", "\\\\");
		}
		if (inputSearch != "") {
			inputSearch = inputSearch.replace("\\", "\\\\");
			inputSearch = inputSearch.replace("(", "");
			inputSearch = inputSearch.replace(")", "");
			inputSearch = inputSearch.replace("*", "");
			inputSearch = inputSearch.replace("?", "");
			inputSearch = inputSearch.replaceAll("([';])+|(--)+|(%)+|(^)+", "");

		}
		String userJson = null;
		if (order.equals("descending")) {
			order = "desc";
		}
		if (order.equals("ascending")) {
			order = "asc";
		}
		String sql = "select bxm_get_page_data_json('" + tableName + "','*','" + filterString + "','" + inputSearch
				+ "','" + prop + " " + order + "',0,0)";
		ResultSet rs = dbConn.query(sql);
		try {
			if (rs.next()) {
				userJson = rs.getString(1);
			}
		} catch (SQLException e) {
		}
		String dateJson = getFormDataJson(tableName);
		String frameJson = getFormFrameJson(tableName);
		return "{" + userJson + dateJson + frameJson + "}";
	}

	/**
	 * JF 拼接表单控件JSON FORMDATA部分
	 */
	private String getFormDataJson(String tableName) {
		String result = null;
		StringBuilder sb = new StringBuilder();
		// 如果ID为空就是新增,不为空为编辑
		try {
			ResultSet rs = dbConn.query(
					"select column_name,control_type,bxm_get_default_value(default_value),data_type from v_sys_columns "
							+ "where not is_deactive and table_name = '" + tableName + "'");
			while (rs.next()) {
				// 新增的时候判断是否为单选按钮，如果是就给一个默认值
				if (rs.getString(2).equals("radio")) {
					if (rs.getString(3) == null) {
						sb.append("\"" + rs.getString(1) + "\":" + true + ",");
					} else {
						sb.append("\"" + rs.getString(1) + "\":" + rs.getString(3) + ",");
					}
				} else {
					if (rs.getString(3) == null) {
						if (rs.getString(4).startsWith("_")) {
							sb.append("\"" + rs.getString(1) + "\":[],");
						} else {
							sb.append("\"" + rs.getString(1) + "\":null,");
						}
					} else {
						sb.append("\"" + rs.getString(1) + "\":\"" + rs.getString(3) + "\",");
					}
				}
			}
			result = "\"formData\":{" + Str.delComma(sb.toString()) + "}";
		} catch (SQLException e) {
		}
		return result;
	}

	/**
	 * JF 拼接表单控件JSON FORMFRAME部分
	 */
	private String getFormFrameJson(String tableName) {
		ResultSet rs = dbConn.query(
				"select column_name,caption,control_type,place_holder,edit_mode,source_uid,is_visible,valid_reg,valid_prompt,required,source_uid,source_text,view_name,filter_string,value_member,display_member"
						+ " from v_sys_columns where is_visible = true and control_type is not null and table_name = '"
						+ tableName + "' order by column_idx");
		StringBuilder sb = new StringBuilder();
		Boolean isProcess = false;
		try {
			while (rs.next()) {
				// 查询是否严格按照工艺,meq_line_cards中的part_count不能修改
				if (tableName.equals("v_meq_process_cards") && rs.getString(1).equals("part_count")) {
					ResultSet rsProcess = dbConn.query(
							"SELECT setting_value = 'true' FROM sys_settings WHERE setting_code = 'MustProducedAccordProcess'");
					if (rsProcess.next()) {
						isProcess = rsProcess.getBoolean(1);
					}
				}

				// 修改是否静止必填的状态
				boolean editIsornot = false;
				if (rs.getInt(5) == 0 || rs.getInt(5) == 3) {
					editIsornot = true;
				}
				// 判断控件类型是否为空,为空就不显示
				if (rs.getString(3) != null && !"".equals(rs.getString(3))) {
					sb.append("{");
					sb.append("\"title\":\"" + rs.getString(2) + "\",");
					sb.append("\"type\":\"" + rs.getString(3) + "\",");
					sb.append("\"placeholder\":\"" + rs.getString(4) + "\",");
					if (rs.getString(1).equals("part_count")) {
						sb.append("\"disabled\":" + isProcess + ",");
					} else {
						sb.append("\"disabled\":" + editIsornot + ",");
					}
					sb.append("\"sortable\":\"custom\",");
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
						} else if (rs.getString(3).equals("select_one") || rs.getString(3).equals("select_many")
								|| rs.getString(3).equals("input_select_one")) {
							sb.append(",\"s_options\":");
						} else if (rs.getString(3).equals("select_tree") || rs.getString(3).equals("select_tree_end")) {
							sb.append(",\"tree_options\":");
						} else if (rs.getString(3).equals("tablebox_one")) {
							sb.append(",\"table_options\":");
						} else {
							sb.append(",\"r_options\":");
						}
						if (rs.getString(3).equals("select_tree") || rs.getString(3).equals("select_tree_end")) {
							sb.append("[" + getSelectTreeJson(rs.getString(13), rs.getString(15), rs.getString(16),
									rs.getString(14), "") + "]");
						} else {
							sb.append(rs.getString(12));
							if (rs.getString(3).equals("tablebox_one")) {
								try {
									ResultSet rsDatasource = dbConn.query(String.format(
											"SELECT source_text FROM v_sys_datasources WHERE is_value_member_visible = '是' and view_name = (SELECT d.view_name FROM v_sys_datasources d LEFT JOIN sys_columns c ON d.source_uid = c.source_uid WHERE c.table_name = '%s' AND c.control_type = 70)",
											tableName));
									if (rsDatasource.next()) {
										sb.append(String.format(",\"s_options\":%s", rsDatasource.getString(1)));
									}
								} catch (Exception e) {

								}
							}
						}
					}
					sb.append("},");
				}
			}
		} catch (SQLException e) {
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
				String str = getSelectTreeJson(viewName, valueMember, displayMember, filterString,
						rs.getString(valueMember));
				if (!str.equals("")) {
					sb.append(",\"children\":[" + str + "]");
				}
				sb.append("},");
			}
		} catch (SQLException e) {
		}
		return Str.delComma(sb.toString());
	}

	/**
	 * 工艺卡通用表单新增接口原来版本
	 */
	@RequestMapping(value = "addJsonOld", method = RequestMethod.POST)
	@ResponseBody
	public String getAddDataJsonOld(HttpServletRequest request, @RequestBody String jsonSaveData) {
		try {
			JSONObject jsonObject = JSONObject.fromObject(jsonSaveData);
			StringBuilder columnName = new StringBuilder();
			StringBuilder columnValue = new StringBuilder();
			String userId = jsonObject.getString("userId");
			String menuId = jsonObject.getString("menuId");
			String funName = jsonObject.getString("funName");
			String tableName = jsonObject.getString("tableName");
			JSONObject jsonDataToAdd = JSONObject.fromObject(jsonObject.getString("formData"));
			Object object = null;

			if (tableName.equals("meq_sops")) {// 工艺路线名称排重
				String sopName = jsonDataToAdd.getString("sop_name");
				String productCode = jsonDataToAdd.getString("product_code");
				String lintId = jsonDataToAdd.getString("line_id");
				ResultSet rs = null;
				rs = dbConn.query("select exists (select sop_name from meq_sops where sop_name ='" + sopName + "')");
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,该工艺模板名称已存在");
					}
				}

				rs = dbConn.query(String.format(
						"SELECT EXISTS (SELECT 1 FROM meq_sops WHERE product_code = '%s' AND line_id = %s) ",
						productCode, lintId));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,该产线下该产品已存在一个工艺模板");
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
			String sqlInsert = String.format("INSERT INTO %s(%s) VALUES(%s)", tableName, columnName, columnValue);
			boolean res = dbConn.queryUpdate(sqlInsert);
			// 捕获用户操作日志
			getLogs.getUserLog(request, menuId, funName, res, "", jsonObject.getString("formData"), userId);
			if (res) {
				return message.getSuccessInfo("新增成功");
			} else {
				return message.getErrorInfo("新增失败");
			}
		} catch (Exception e) {
			return message.getErrorInfo("新增失败");
		}
	}
	
	
	/**
	 * LH 工艺卡通用表单新增接口新版本
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
			Object object = null;
			if (tableName.equals("meq_sops")) {// 工艺路线名称排重
				String sopName = jsonDataToAdd.getString("sop_name");
				ResultSet rs = null;
				rs = dbConn.query("select exists (select sop_name from meq_sops where sop_name ='" + sopName + "')");
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,该工艺模板名称已存在");
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
			String sqlInsert = String.format("INSERT INTO %s(%s) VALUES(%s)", tableName, columnName, columnValue);
			boolean res = dbConn.queryUpdate(sqlInsert);
			// 捕获用户操作日志
			getLogs.getUserLog(request, menuId, funName, res, "", jsonObject.getString("formData"), userId);
			if (res) {
				return message.getSuccessInfo("新增成功");
			} else {
				return message.getErrorInfo("新增失败");
			}
		} catch (Exception e) {
			return message.getErrorInfo("新增失败");
		}
	}

	/**
	 * 行内编辑工艺卡批量新增编辑接口
	 */
	@RequestMapping(value = "addOrUpdateJson", method = RequestMethod.POST)
	@ResponseBody
	public String getAddOrUpdateJson(HttpServletRequest request, @RequestBody String jsonSaveData) {
		JSONObject jsonObject = JSONObject.fromObject(jsonSaveData);
		StringBuilder allAddValue = new StringBuilder();// 所有新增value
		StringBuilder allUpdateValue = new StringBuilder();// 拼接所有sql语句
		StringBuilder deleteStr = new StringBuilder();// 需要删除的id集合
		StringBuilder fileUpdate = new StringBuilder();// 修改文件
		StringBuilder columnName = new StringBuilder();// 列名
		JSONArray array = JSONArray.fromObject(jsonObject.getString("data"));// 数组对象

		boolean ok = false;
		// 第一层循环，将对象循环出来
		for (int i = 0; i < array.size(); i++) {
			StringBuilder addValue = new StringBuilder();// 用来拼接新增的对象
			StringBuilder updateStr = new StringBuilder();// 用来拼接编辑的对象
			Object object = null;
			String cardId = null;
			String fileName = null;
			fileName = array.getJSONObject(i).getString("file_name");

			if (array.getJSONObject(i).getString("card_name").equals("")
					|| array.getJSONObject(i).getString("card_name").equals("null")) {
				return message.getErrorInfo("工序名称不允许为空");
			}
			// 如果isDelete为true的话就是删除,其余则是新增修改
			if (array.getJSONObject(i).getString("isDelete").equals("true")) {
				deleteStr.append(array.getJSONObject(i).getString("card_id"));
				deleteStr.append(",");
			} else {
				// 第二层循环，循环出对象里面的每一个字段
				for (Iterator<?> iter = array.getJSONObject(i).keys(); iter.hasNext();) {
					// 获取当前对象的ID根据ID是否为空来控制当前对象的操作
					cardId = array.getJSONObject(i).getString("card_id");
					// 如果ID不为空的话就是编辑
					if (!cardId.equals("null")) {
						String key = (String) iter.next();
						if (!key.equals("isEdit") && !key.equals("create_date") && !key.equals("index")
								&& !key.equals("isDelete")) {// 过滤掉isEdit字段
							object = array.getJSONObject(i).get(key);
							if (array.getJSONObject(i).getString(key).isEmpty() || (object instanceof JSONNull)) {
								updateStr.append(key + " = " + "null");
							} else {
								updateStr.append(key + " = " + "'" + array.getJSONObject(i).getString(key) + "'");
							}
							updateStr.append(",");
						}
					} else {// ID为空新增数据
						String key = (String) iter.next();
						if (!key.equals("isEdit") && !key.equals("card_id") && !key.equals("create_date")
								&& !key.equals("index") && !key.equals("isDelete")) {
							if (!ok) {
								columnName.append(key);
							}
							object = array.getJSONObject(i).get(key);
							if (array.getJSONObject(i).getString(key).isEmpty() || (object instanceof JSONNull)) {
								addValue.append("null");
							} else {
								addValue.append("'" + array.getJSONObject(i).getString(key) + "'");
							}
							if (!ok)
								columnName.append(",");
							addValue.append(",");

						}
					}
				}
				ok = true;
				// 组装所有的value值
				if (!cardId.equals("null")) {
					allUpdateValue.append(String.format("update meq_process_cards set %s where card_id = %s;",
							Str.delComma(updateStr.toString()), cardId));
				} else {
					allAddValue.append("(" + Str.delComma(addValue.toString()) + "),");
				}
				// 将文件导入进工序
				fileUpdate.append(String.format(
						"update meq_process_cards set file_data = (select distinct(file_content) from meq_process_cards cc left join sys_files ss "
								+ "on (cc.file_name = ss.file_name) where cc.file_name = '%s') where file_name = '%s';",
						fileName, fileName));
			}
		}
		String sqlInsert = String.format("INSERT INTO meq_process_cards (%s) VALUES %s",
				Str.delComma(columnName.toString()), Str.delComma(allAddValue.toString()));
		String sqlDelete = String.format("delete from meq_process_cards where card_id in (%s)",
				Str.delComma(deleteStr.toString()));
		boolean addRes = dbConn.queryUpdate(sqlInsert);
		boolean deleteRes = dbConn.queryUpdate(sqlDelete);
		boolean updateRes = dbConn.queryUpdate(allUpdateValue.toString());
		dbConn.queryUpdate(fileUpdate.toString());
		if (addRes || updateRes || deleteRes) {
			return message.getSuccessInfo("保存成功");
		}
		return message.getErrorInfo("保存失败");
	}

	/**
	 * 工艺卡通用删除接口
	 */
	@RequestMapping(value = "deleteJson", method = RequestMethod.POST)
	@ResponseBody
	public String getDeleteDataJson(HttpServletRequest request, @RequestBody String deleteInfo) {
		try {
			JSONObject jsonObj = JSONObject.fromObject(deleteInfo);
			String userId = jsonObj.getString("userId");
			String menuId = jsonObj.getString("menuId");
			String funName = jsonObj.getString("funName");
			String tableName = jsonObj.getString("tableName");
			String idValue = jsonObj.getString("idValue");
			String resultInfo = null;
			boolean res = false;
			String sql =  null;
			if (idValue.contains("[")) {
				JSONArray jsonArray = JSONArray.fromObject(idValue);
				for (int i = 0; i < jsonArray.size(); i++) {
					ResultSet rsinfo = dbConn
							.query("select bxm_get_data_row('" + tableName + "','" + jsonArray.get(i) + "')");
					while (rsinfo.next()) {
						resultInfo += rsinfo.getString(1);
					}
					if (tableName.equals("meq_sops")) {// 如果删除的是工艺路线,要判断下面是否有工艺卡
						ResultSet rs = dbConn.query(String.format(
								"select exists (select card_id from meq_process_cards where sop_id = '%s')",
								jsonArray.get(i)));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("该工艺模板下面有工序，不可删除");
							}
						}
					}
				}
				if (idValue != null && !idValue.equals("")) {
					idValue = idValue.replace("[", "").replace("]", "").replace("\"", "'");
				}
				sql = String.format("DELETE FROM %s WHERE %s", tableName,
						jsonObj.getString("idName") +" in" + "(" + idValue + ")");
			}
			// 删除之前获取该信息的JSON,用于日志信息
			 res = dbConn.queryUpdate(sql);
			// 捕获日志信息
			getLogs.getUserLog(request, menuId, funName, res, resultInfo, "", userId);
			if (res) {
				return message.getSuccessInfo("删除成功");
			} else {
				return message.getErrorInfo("删除失败");
			}
		} catch (Exception e) {
			return message.getErrorInfo("删除失败");
		}
	}

	/**
	 * 工艺卡通用编辑接口
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
			ResultSet rsinfo = dbConn
					.query("select bxm_get_data_json('" + tableName + "','" + jsonObj.getString("id") + "')");
			while (rsinfo.next()) {
				resultInfo = rsinfo.getString(1);
			}
			ResultSet rs = dbConn.query(String
					.format("select column_name from v_sys_columns where table_name = '%s' and is_key", tableName));
			if (rs.next()) {
				keyColumn = rs.getString(1);
			}

			if (tableName.equals("meq_sops")) {// 工艺路线修改排重
				String sopName = jsonData.getString("sop_name");
				String sopId = jsonData.getString("sop_id");
				ResultSet cc = dbConn.query(String.format(
						"select exists (select sop_id from meq_sops where sop_name = '%s' and sop_id != '%s')", sopName,
						sopId));
				if (cc.next()) {
					if (cc.getBoolean(1)) {
						return message.getErrorInfo("修改失败,该工艺模板已存在");
					}
				}
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
			} else {
				return message.getErrorInfo("修改失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("修改失败");
		}
	}

	/**
	 * JF 工艺卡审核撤审功能
	 */
	@RequestMapping(value = "auditJson", method = RequestMethod.POST)
	@ResponseBody
	public String cardAudit(@RequestBody String jsonData) {
		JSONObject jsonObj = JSONObject.fromObject(jsonData);
		String userId = jsonObj.getString("userId");
		String sopId = jsonObj.getString("sopId");
		String status = jsonObj.getString("status");// 审核撤审状态
		ResultSet cc = dbConn
				.query("select exists (select card_id from meq_process_cards where sop_id = '" + sopId + "')");
		ResultSet rs = dbConn.query("select exists(select 1 from meq_process_cards c"
				+ " inner join meq_sops s on c.sop_id = s.sop_id inner join meq_line_stations l on c.station_id = l.station_id"
				+ " where s.line_id <> l.line_id and s.sop_id = '" + sopId + "')");
		try {
			if (rs.next()) {
				if (rs.getBoolean(1)) {
					return message.getErrorInfo("产线和工位不一致，审核失败");
				}
			}
			if (cc.next()) {
				if (!cc.getBoolean(1)) {
					return message.getErrorInfo("该工艺模板无工序，审核失败");
				}
			}
		} catch (SQLException e) {
		}
		// 状态10为审核,状态20为撤审
		if (status.equals("10")) {
			String sql = "update meq_sops set auditor = '" + userId
					+ "',audited_date = current_timestamp where sop_id = '" + sopId + "'";
			boolean res = dbConn.queryUpdate(sql);
			if (res) {
				return message.getSuccessInfo("审核成功");
			} else {
				return message.getErrorInfo("审核失败");
			}
		}
		if (status.equals("20")) {
			
			ResultSet rsprocess = dbConn.query(String.format("select meq_process(%s)",sopId));
			try {
				if (rsprocess.next()) {
					if (rsprocess.getBoolean(1)) {
						return message.getErrorInfo("该工艺模板已被引用无法撤审");
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			String sql = "update meq_sops set auditor = null,audited_date = null where sop_id = '" + sopId + "'";
			boolean res = dbConn.queryUpdate(sql);
			if (res) {
				return message.getSuccessInfo("撤审成功");
			} else {
				return message.getErrorInfo("撤审失败");
			}
		}
		return null;
	}

	/**
	 * JF 根据产线ID获取下面的工位
	 */
	@RequestMapping(value = "stationJson", method = RequestMethod.GET)
	@ResponseBody
	public String getStation(@RequestParam String lineId) {
		StringBuilder sb = new StringBuilder();
		String sql = "select station_id,station_name from meq_line_stations where line_id = '" + lineId + "'";
		ResultSet rs = dbConn.query(sql);
		try {
			while (rs.next()) {
				sb.append("{\"id\":" + rs.getInt(1) + ",\"value\":\"" + rs.getString(2) + "\"},");
			}
		} catch (SQLException e) {
		}
		return "[" + Str.delComma(sb.toString()) + "]";
	}

	/**
	 * JF 默认工艺卡表格分页
	 */
	@RequestMapping(value = "cardTableJson", method = RequestMethod.POST)
	@ResponseBody
	public String getCardTableJson(@RequestBody String data) {
		JSONObject parameter = JSONObject.fromObject(data);
		String pageSize = parameter.getString("pageSize");
		String currentPage = parameter.getString("currentPage");
		String order = parameter.getString("order");
		String prop = parameter.getString("prop");
		String filterString = parameter.getString("filterString");
		String inputSearch = parameter.getString("inputSearch");
		if (filterString != "") {
			// product_code=["z0002","z0001"] and sop_id=[88,89,90] and card_id=[204,190]
			// and line_id=[64,40,62,67]
			filterString = filterString.replace("[", "(");
			filterString = filterString.replace("]", ")");
			filterString = filterString.replace("\"", "''");
			filterString = filterString.replace("\\", "\\\\");
		}
		if (inputSearch != "") {
			inputSearch = inputSearch.replace("\\", "\\\\");
			inputSearch = inputSearch.replace("(", "");
			inputSearch = inputSearch.replace(")", "");
			inputSearch = inputSearch.replace("*", "");
			inputSearch = inputSearch.replace("?", "");
			inputSearch = inputSearch.replaceAll("([';])+|(--)+|(%)+|(^)+", "");

		}
		String userJson = null;
		if (order.equals("descending")) {
			order = "desc";
		}
		if (order.equals("ascending")) {
			order = "asc";
		}
		String sql = "select bxm_get_grid_page_json('v_meq_product_processes','*','','" + filterString + "','"
				+ inputSearch + "','" + prop + " " + order + "'," + pageSize + "," + currentPage + ")";
		ResultSet rs = dbConn.query(sql);
		try {
			if (rs.next()) {
				userJson = rs.getString(1);
			}
		} catch (SQLException e) {
		}
		return userJson;
	}

}
