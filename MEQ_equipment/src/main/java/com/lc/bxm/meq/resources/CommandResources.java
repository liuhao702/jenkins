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
 * 投产功能接口
 * @author JF
 * @date 2019年7月19日
 */
@RestController
@RequestMapping("/command")
public class CommandResources {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	
	/**
	 * JF 表格通用接口,分页
	 */
	@RequestMapping(value = "tableJson", method = RequestMethod.GET)
	@ResponseBody
	public String getTableJson(@RequestParam String tableName,@RequestParam int pageSize,
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
		String sql = "select bxm_get_line_commands_page_json(" + "'*','','" + filterString + "','"
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
	 * JF 获取投产表单信息接口
	 */
	@RequestMapping(value = "formJson", method = RequestMethod.GET, produces = "text/html;charset=utf-8")
	@ResponseBody
	public String formJson(@RequestParam String id, @RequestParam String tableName) {
		String dateJson = getFormDataJson(tableName, id);
		String frameJson = getFormFrameJson(tableName, id);
		return "{" + dateJson + frameJson + "}";
	}

	/**
	 * JF 拼接表单控件JSON FORMDATA部分
	 */
	private String getFormDataJson(String tableName, String id) {
		String result = null;
		StringBuilder sb = new StringBuilder();
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
		Boolean isRythm = false;
		Boolean isHead = false;
		try {
			
			while (rs.next()) {		
				if(rs.getString(1).equals("production_beat")) {
					//查询生产节拍是否显示
					ResultSet rsRythm = dbConn.query("SELECT setting_value = 'false' FROM sys_settings WHERE setting_code = 'EnablingProductionRhythm'");
					if(rsRythm.next()) {
						isRythm = rsRythm.getBoolean(1);
					}
				}
				
				if(rs.getString(1).equals("barcode_head")) {
					//查询条码前缀是否必填
					ResultSet rsHead = dbConn.query("SELECT setting_value = 'true' FROM sys_settings WHERE setting_code = 'BarcodeHeadLineCommand'");
					if(rsHead.next()) {
						isHead = rsHead.getBoolean(1);
					}
				}
				
				// 修改是否静止必填的状态
				boolean isornot = false;
				if (rs.getInt(5) == 0) {
					isornot = true;
				} else if (rs.getInt(5) == 2 && (!id.isEmpty())) {
					isornot = true;
				}
				// 判断控件类型是否为空,为空就不显示
				if (rs.getString(3) != null && !"".equals(rs.getString(3)) && !(isRythm && rs.getString(1).equals("production_beat"))) {
					sb.append("{");
					sb.append("\"label\":\"" + rs.getString(2) + "\",");
					sb.append("\"type\":\"" + rs.getString(3) + "\",");
					sb.append("\"placeholder\":\"" + rs.getString(4) + "\",");
					sb.append("\"disabled\":" + isornot + ",");
					sb.append("\"key\":\"" + rs.getString(1) + "\"");
					// 拼接RULES
					sb.append(",\"rules\":[{");
					if(rs.getString(1).equals("barcode_head")) {
						sb.append("\"required\":" + isHead + ",");
					}else {
						sb.append("\"required\":" + rs.getBoolean(10) + ",");
					}
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
						} else if (rs.getString(3).equals("select_tree") || rs.getString(3).equals("select_tree_end")) {
							sb.append(",\"tree_options\":");
						} else if(rs.getString(3).equals("tablebox_one")){
							sb.append(",\"table_options\":");
						}else {
							sb.append(",\"r_options\":");
						}
						if (rs.getString(3).equals("select_tree") || rs.getString(3).equals("select_tree_end")) {
							sb.append("[" + getSelectTreeJson(rs.getString(13), rs.getString(15), rs.getString(16),
									rs.getString(14), "") + "]");
						} else {
							sb.append(rs.getString(12));
							if(rs.getString(3).equals("tablebox_one")) {
								try {
									ResultSet rsDatasource = dbConn.query(String.format("SELECT source_text FROM v_sys_datasources WHERE is_value_member_visible = '是' and view_name = (SELECT d.view_name FROM v_sys_datasources d LEFT JOIN sys_columns c ON d.source_uid = c.source_uid WHERE c.table_name = '%s' AND c.control_type = 70)", tableName));
									if(rsDatasource.next()) {
										sb.append(String.format(",\"s_options\":%s", rsDatasource.getString(1)));
									}
								}catch (Exception e) {
									
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
	 * 投产通用表单新增接口
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
	 * 行内编辑投产工艺卡批量新增编辑接口
	 */
	@RequestMapping(value = "addOrUpdateJson", method = RequestMethod.POST)
	@ResponseBody
	public String getAddOrUpdateJson(HttpServletRequest request, @RequestBody String jsonSaveData) {
		JSONObject jsonObject = JSONObject.fromObject(jsonSaveData);
		StringBuilder allAddValue = new StringBuilder();// 所有新增value
		StringBuilder allUpdateValue = new StringBuilder();// 拼接所有sql语句
		StringBuilder columnName = new StringBuilder();// 列名
		JSONArray array = JSONArray.fromObject(jsonObject.getString("data"));// 数组对象

		boolean ok = false;
		// 第一层循环，将对象循环出来
		for (int i = 0; i < array.size(); i++) {
			StringBuilder addValue = new StringBuilder();// 用来拼接新增的对象
			StringBuilder updateStr = new StringBuilder();// 用来拼接编辑的对象
			Object object = null;
			String autoId = null;
			// 第二层循环，循环出对象里面的每一个字段
			for (Iterator<?> iter = array.getJSONObject(i).keys(); iter.hasNext();) {
				// 获取当前对象的ID根据ID是否为空来控制当前对象的操作
				autoId = array.getJSONObject(i).getString("auto_id");
				// 如果ID不为空的话就是编辑
				if (!autoId.equals("null")) {
					String key = (String) iter.next();
					if(!key.equals("isEdit") && !key.equals("index")) {// 过滤掉isEdit字段
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
					if(!key.equals("isEdit") && !key.equals("auto_id") && !key.equals("index")) {
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
			if (!autoId.equals("null")) {
				allUpdateValue.append(String.format("update meq_line_cards set %s where auto_id = %s;", Str.delComma(updateStr.toString()), autoId));
			}else {
				allAddValue.append("(" + Str.delComma(addValue.toString()) + "),");
			}
		}
		String sqlInsert = String.format("INSERT INTO meq_line_cards (%s) VALUES %s", Str.delComma(columnName.toString()), Str.delComma(allAddValue.toString()));
		boolean addRes = dbConn.queryUpdate(sqlInsert);
		boolean updateRes = dbConn.queryUpdate(allUpdateValue.toString());
		if (addRes || updateRes) {
			return message.getSuccessInfo("保存成功");
		}
		return message.getErrorInfo("保存失败");
	}

	/**
	 * 投产通用删除接口
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
			String idName = jsonObj.getString("idName");
			String resultInfo = null;
			String sql = null;
			boolean res = false;
			if (idValue.contains("[")) {
				JSONArray idValues = jsonObj.getJSONArray("idValue");//
				String idStrValue =idValues.toString();
				// 删除之前获取该信息的JSON,用于日志信息
				ResultSet rsinfo = null;
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
				sql = String.format("DELETE FROM %s WHERE %s", tableName,idName + " in" + "(" + idStrValue + ")");
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
		}
		return  message.getErrorInfo("删除失败");
	}
	
	
	/**
	 * 投产通用编辑接口
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
			
			if(tableName.equals("meq_line_commands")) {
				String prior = jsonData.getString("prior");
				String lineId = jsonData.getString("line_id");
				String barcodeHead = jsonData.getString("barcode_head");
				String rec_id = jsonObj.getString("id");
				ResultSet cc = dbConn.query(String.format("SELECT EXISTS (SELECT 1 FROM meq_line_commands WHERE prior = '%s' AND line_id = '%s' AND barcode_head = '%s' AND rec_id != '%s' AND NOT deleted)", prior, lineId,barcodeHead,rec_id));
				if(cc.next()) {
					if(cc.getBoolean(1)) {
						return message.getErrorInfo("修改失败,该产线已有相同排序");
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
			}
		} catch (Exception e) {}
		return message.getErrorInfo("修改失败");
	}
	
	/**
	 * 导入投产工艺卡
	 */
	@RequestMapping(value = "importCardJson", method = RequestMethod.GET)
	@ResponseBody
	public String importCard(@RequestParam String lineId, @RequestParam String productCode,
			@RequestParam String recId) {
		ResultSet rs = null;
		try {
			// 根据产品和产线去找默认工艺卡，如果产品产线和默认工艺卡一致，将数据一致的导入到投产工艺卡
			rs = dbConn.query(String.format(
					"select exists (select pp_id from meq_product_processes where line_id = %s and product_code = '%s')", lineId, productCode));
			if (rs.next()) {
				if (rs.getBoolean(1)) {
					// 批量插入数据
					dbConn.query(String.format("call insert_meq_line_cards(%s,%s,'%s')", recId, lineId, productCode));
					return message.getSuccessInfo("导入成功");
				}
			}
			// 如果默认工艺卡没有，找工艺卡数据
			rs = dbConn.query(String.format("select sop_id from meq_sops where line_id = %s and product_code = '%s'", lineId, productCode));
			if (rs.next()) {
				// 判断工艺模板里面是否有数据,有匹配的就批量添加模板下面的工序
				if (rs.getString(1) != null && !rs.getString(1).equals("")) {
					// 批量插入数据
					boolean res = dbConn.queryUpdate(String.format(
							"insert into meq_line_cards(station_id,card_id,idx,part_count,cate_id,file_name,rec_id)"
									+ " select station_id,card_id,idx,part_count,cate_id,file_name,%s as rec_id"
									+ " from meq_process_cards where sop_id = %s", recId, rs.getString(1)));
					if (res) {
						return message.getSuccessInfo("导入成功");
					}
					return message.getErrorInfo("导入失败");
				}
			}
		} catch (SQLException e) {}
		// 如果默认工艺卡和工艺卡都找不到对应的数据,返回一个状态弹框手动选择
		return "selectSop";
	}
	
	/**
	 * 根据传进来的sop_id将所属的工艺卡导入投产工艺卡
	 */
	@RequestMapping(value = "addCardBySopJson", method = RequestMethod.GET)
	@ResponseBody
	public String addCardBySop(@RequestParam String sopId, @RequestParam String recId) {
		boolean res = dbConn.queryUpdate(
				String.format("insert into meq_line_cards(station_id,card_id,idx,part_count,cate_id,file_name,rec_id)"
						+ " select station_id,card_id,idx,part_count,cate_id,file_name,%s as rec_id"
						+ " from meq_process_cards where sop_id = %s", recId, sopId));
		if (res) {
			return message.getSuccessInfo("导入成功");
		}
		return message.getErrorInfo("导入失败");
	}
	
	/**
	 * 强制完成数据接口
	 */
	@RequestMapping(value = "forceSucessJson", method = RequestMethod.GET)
	@ResponseBody
	public String forceSucess(@RequestParam String recId) {
		boolean res = dbConn.queryUpdate("update meq_line_commands set deleted = true where rec_id = '"+ recId +"'");
		if(res) {
			return message.getSuccessInfo("强制完成成功");
		}
		return message.getErrorInfo("强制完成失败");
	}
	
	/**
	 * 审核
	 */
	@RequestMapping(value = "examineJson", method = RequestMethod.GET)
	@ResponseBody
	public String commandExamine(@RequestParam String recId,@RequestParam String lineId,@RequestParam String productCode) {
		//投产下面没工艺卡不能审核
		ResultSet result = dbConn.query("select exists (select auto_id from meq_line_cards where rec_id = '"+ recId +"')");
		try {
			if(result.next()) {
				ResultSet count = dbConn.query("select count(1) from meq_line_cards where station_id is null and rec_id = '"+ recId +"'");
				if(count.next()) {
					if(count.getInt(1)>0) {
						return message.getErrorInfo("审核失败，工序中工位不能为空");
					}
				}
				
				ResultSet proir = dbConn.query("SELECT EXISTS (SELECT 1 FROM meq_line_commands WHERE prior is null and rec_id = '"+ recId +"')");
				if(proir.next()) {
					if(proir.getBoolean(1)) {
						return message.getErrorInfo("审核失败，优先排序不能为空");
					}else {
						ResultSet repeatPrior = dbConn.query(String.format("SELECT COUNT(*) FROM meq_line_commands c \n" + 
								"WHERE c.audited AND not c.deleted AND line_id = %s AND prior = (SELECT prior FROM meq_line_commands WHERE rec_id = %s)", lineId,recId));
						if(repeatPrior.next()) {
							if(repeatPrior.getInt(1) > 0) {
								return message.getErrorInfo("审核失败,优先排序不能重复");
							}
						}
					}
				}
				
				ResultSet barcodeHead = dbConn.query("SELECT setting_value = 'true' FROM sys_settings WHERE setting_code = 'BarcodeHeadLineCommand'");
				if(barcodeHead.next()) {
					if(barcodeHead.getBoolean(1)) {
						ResultSet haveBarcode = dbConn.query("SELECT EXISTS (SELECT 1 FROM meq_line_commands WHERE barcode_head is null and rec_id = '" + recId + "')");
						if(haveBarcode.next()) {
							if(haveBarcode.getBoolean(1)) {
								return message.getErrorInfo("审核失败，条码前缀不能为空");
							}
						}
					}
				}
				
				ResultSet colckInType = dbConn.query("SELECT setting_value = '2' FROM sys_settings WHERE setting_code = 'ClockInType'");
				if(colckInType.next()) {
					if(colckInType.getBoolean(1)) {
						ResultSet operator = dbConn.query("SELECT EXISTS (SELECT 1 FROM meq_line_cards WHERE operator is null and rec_id = '" + recId + "')");
						if(operator.next()) {
							if(operator.getBoolean(1)) {
								return message.getErrorInfo("审核失败，工艺卡中操作人不能为空");
							}
						}
					}
				}
				if(result.getBoolean(1)) {
					boolean res = dbConn.queryUpdate("update meq_line_commands set audited = true where rec_id = '"+ recId +"'");
					if(res) {
						//审核成功之后将投产工艺卡导入默认工艺卡,如果默认工艺卡没有该产线和产品的导入，有则导入
						ResultSet isornot = dbConn.query(String.format("select exists (select pp_id from meq_product_processes where line_id = %s and product_code = '%s')",lineId,productCode));
						if(isornot.next()) {
							dbConn.queryUpdate(String.format("DELETE FROM meq_product_processes WHERE line_id = %s AND product_code = '%s", lineId,productCode));
							dbConn.queryUpdate("insert into meq_product_processes(station_id,idx,play_time,part_count,cate_id,card_id,line_id,product_code,sop_id) " + 
									"select cd.station_id,cd.idx,cd.play_time,cd.part_count,cd.cate_id,cd.card_id,com.line_id,com.product_code," + 
									"mpc.sop_id from meq_line_cards cd left join meq_line_commands com on (cd.rec_id = com.rec_id) " + 
									"left join meq_process_cards mpc on (cd.card_id = mpc.card_id) where cd.rec_id = " + recId);
						}
						return message.getSuccessInfo("审核成功");
					}
					return message.getErrorInfo("审核失败");
				}
				
			}
		} catch (SQLException e) {
			return message.getErrorInfo("审核失败");
		}
		return message.getErrorInfo("该投产无工艺卡，不能审核");
	}
	
	/**
	 * 撤审
	 */
	@RequestMapping(value = "withdrawalJson", method = RequestMethod.GET)
	@ResponseBody
	public String commandWithdrawal(@RequestParam String recId) {
		ResultSet result = dbConn.query("select online_qty from meq_line_commands where rec_id = " + recId);
		try {
			if (result.next()) {
				if (result.getInt(1) > 0) {
					return message.getErrorInfo("已有上线数量，不能撤审");
				}
			}
		} catch (SQLException e) {}
		boolean res = dbConn.queryUpdate("update meq_line_commands set audited = false where rec_id = " + recId);
		if (res) {
			return message.getSuccessInfo("撤审成功");
		}
		return message.getErrorInfo("撤审失败");
	}
	
	/**
	 * 是否允许超单投产
	 */
	@RequestMapping(value = "chaoDanJson", method = RequestMethod.GET)
	@ResponseBody
	public String chaoDan(@RequestParam String detailId,@RequestParam String recId,@RequestParam Integer num) {
		ResultSet isornot = dbConn.query("select setting_value from sys_settings where setting_code = 'ExceedPlan'");
		try {
			if(isornot.next()) {
				//为ture允许超单投产
				if(isornot.getString(1).equals("true")) {
					return message.getSuccessInfo("允许超单生产");
				}else {
					//查询出上线没完成的投产总和
					ResultSet delsum = dbConn.query(String.format(
							"select COALESCE(SUM(qty),0) as ab from meq_line_commands where deleted = false and detail_id = %s and rec_id != %s", detailId, recId));
					// 查询出所有以上线的总数
					ResultSet onlineSum = dbConn.query(String.format(
							"select COALESCE(SUM(online_qty),0) as ab from meq_line_commands where deleted = true and detail_id = %s", detailId));
					// 查询出排产量
					ResultSet plansum = dbConn.query(String.format("select qty from meq_line_plan_details where detail_id = %s", detailId));
					if (onlineSum.next()) {}
					if (plansum.next()) {}
					if(delsum.next()) {}
					//int b = onlineSum.getInt(1); int c = plansum.getInt(1); int d = delsum.getInt(1);
					// 如果之前的投产总和加上本次修改的数大于排产量
					if (delsum.getInt(1) + num + onlineSum.getInt(1) > plansum.getInt(1)) {
						int res = plansum.getInt(1) - (delsum.getInt(1) + onlineSum.getInt(1));
						return message.getErrorInfo("不允许超单投产,本次最多可投产" + res + "个");
					}

				}
			}
		} catch (SQLException e) {
		}
		return message.getSuccessInfo("可以投产");
	}
	
	/**
	 * 投产表产线工位联动
	 */
	@RequestMapping(value = "lineStationJson", method = RequestMethod.GET)
	@ResponseBody
	public String getLineStationJson(@RequestParam String lineId) {
		StringBuilder sb = new StringBuilder();
		String sql = "select station_id,station_name from meq_line_stations where line_id = '" + lineId + "'";
		ResultSet rs = dbConn.query(sql);
		try {
			while (rs.next()) {
				sb.append("{\"id\":" + rs.getInt(1) + ",\"value\":\"" + rs.getString(2) + "\"},");
			}
		} catch (SQLException e) {}
		return "[" + Str.delComma(sb.toString()) + "]";
	}
	
	
	/**
	 * 修改排序
	 */
	@RequestMapping(value = "modifyLineSorting", method = RequestMethod.POST)
	@ResponseBody
	public String ModifySort(HttpServletRequest request, @RequestBody String jsonData) {
		JSONObject jsonObject = JSONObject.fromObject(jsonData);
		String userId = jsonObject.getString("userId");
		String menuId = jsonObject.getString("menuId");
		String funName = jsonObject.getString("funName");
		String tableName = jsonObject.getString("tableName");
		String rec_id = jsonObject.getString("id");
		JSONObject json = JSONObject.fromObject(jsonObject.getString("formData"));
		String line_id = json.getString("line_id");
		String prior = json.getString("prior");
		String resultInfo = null;
		try {
			ResultSet rsinfo = dbConn.query("select bxm_get_data_json('"+tableName+"','" + rec_id + "')");
			while (rsinfo.next()) {
				resultInfo = rsinfo.getString(1);
			}
			ResultSet rSet = dbConn.query(String.format(
					"SELECT EXISTS(select * from %s where audited and not deleted and line_id = %s and prior = %s and rec_id <> %s )",
					tableName, line_id, prior, rec_id));
			if (rSet.next()) {
				if (rSet.getBoolean(1)) {
					return message.getErrorInfo("修改失败,该产线已有相同排序");
				}
				String sqlUpdate = String.format("UPDATE %s SET prior= %s  WHERE rec_id = %s", tableName, prior,
						rec_id);
				boolean res = dbConn.queryUpdate(sqlUpdate);
				getLogs.getUserLog(request, menuId, funName, res, resultInfo, jsonObject.getString("formData"), userId);
				if (res) {
					return message.getSuccessInfo("修改成功");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("修改失败");
	}

}
