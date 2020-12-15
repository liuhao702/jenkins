package com.lc.bxm.meq.resources;


import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.GetLogs;

import com.lc.bxm.common.Message;

import com.lc.bxm.common.helper.PostgresqlHelper;

import com.lc.bxm.common.helper.Str;
import com.lc.bxm.common.util.TreeDateUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.system.resources.CommonResource;

import net.sf.json.JSONArray;

import net.sf.json.JSONNull;

import net.sf.json.JSONObject;

/**
 * 
 * 排产功能接口
 * 
 * @author LJZ
 * 
 * @date 2019年7月8日
 * 
 */

@RestController

@RequestMapping("/linePlan")

public class LinePlanResources {

	private static Logger logger = Logger.getLogger(CommonResource.class);

	@Autowired

	PostgreSQLConn dbConn;

	@Autowired

	Message message;

	@Autowired

	GetLogs getLogs;

	@Autowired

	PostgresqlHelper helper;
	
	//获取下拉框数据方法
	
	@Autowired
	TreeDateUtil treeData;


	/**
	 * 
	 * JF 获取表单信息通用接口
	 * 
	 */

	@RequestMapping(value = "formJson", method = RequestMethod.GET, produces = "text/html;charset=utf-8")
	@ResponseBody
	public String formJson(@RequestParam String id, @RequestParam String tableName) {
		logger.info("formJson start");
		String dateJson = getFormDataJson(tableName, id);
		String frameJson = getFormFrameJson(tableName, id);
		String planEndDate = "";
		String str =null;
		String str1 =null;
		JSONArray jsonArray =null;
		try {
		ResultSet rs = dbConn.query("select setting_value from sys_settings where setting_code = 'plan_date'");
		if (rs.next()) {
		if (!id.equals("")||id!="") {
				ResultSet rs1 = dbConn.query(
						String.format("SELECT plan_end_date FROM meq_line_plans where line_plan_id = '%s'", id));
				if (rs1.next()) {
					planEndDate = rs1.getString(1)==null?"":rs1.getString(1);
				}
			JSONObject jo = JSONObject.fromObject("{" + dateJson + "}").getJSONObject("formData");
			String plan_date = (String) jo.get("plan_date");
			JSONArray jsonArray1 = new JSONArray();
			if (!planEndDate.equals("")||planEndDate==null) {
				jsonArray1.add(plan_date);
				jsonArray1.add(planEndDate);
				jo.put("plan_date", jsonArray1);
			}
			String string = jo.toString();
			string.substring(1, string.length());
			string.substring(0, string.length() - 1);
			dateJson = "\"formData\":" + jo.toString();
			 str = frameJson.substring(1, frameJson.length());
			 str1 = "{" + str + "}";
			JSONObject jsonObject = JSONObject.fromObject(str1);
			 jsonArray = jsonObject.getJSONArray("formFrame");
			for (int i = 0; i < jsonArray.size(); i++) {
				if (jsonArray.getJSONObject(i).get("key").equals("plan_date")) {
					if (jo.get("plan_date").toString().contains(",")) {
						jsonArray.getJSONObject(i).put("type", "daterange");
					}else {
						jsonArray.getJSONObject(i).put("type", "datepicker");
					}
					
				}
			}
		    }else {
				JSONObject jo = JSONObject.fromObject("{" + dateJson + "}").getJSONObject("formData");
				@SuppressWarnings("unused")
				Object strJson =rs.getInt("setting_value") == 1?jo.put("plan_date","[]"):jo.put("plan_date","");
				dateJson = "\"formData\":" + jo.toString();
				 str = frameJson.substring(1, frameJson.length());
				 str1 = "{" + str + "}";
				JSONObject jsonObject = JSONObject.fromObject(str1);
				 jsonArray = jsonObject.getJSONArray("formFrame");
				for (int i = 0; i < jsonArray.size(); i++) {
					if (jsonArray.getJSONObject(i).get("key").equals("plan_date")) {
						if (jo.get("plan_date").toString().contains("[")) {
							jsonArray.getJSONObject(i).put("type","daterange" );
						}else {
							jsonArray.getJSONObject(i).put("type","datepicker" );
						}
						
						
					}
				}
			}
			str1 = jsonArray.toString();
			str1.substring(1, str1.length());
			str1.substring(0, str1.length() - 1);
			frameJson = ",\"formFrame\":" + str1;
		   }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "{" + dateJson + frameJson + "}";
	}

	/**
	 * 
	 * JF 拼接表单控件JSON FORMDATA部分
	 * 
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

								json.put(rs.getString(1), "[]");


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
		}

		return result;

	}

	/**
	 * 
	 * JF 拼接表单控件JSON FORMFRAME部分
	 * 
	 */

	private String getFormFrameJson(String tableName, String id) {

		ResultSet rs = dbConn.query(

				"select column_name,caption,control_type,place_holder,edit_mode,source_uid,is_visible,valid_reg,valid_prompt,required,source_uid,source_text,view_name,filter_string,value_member,display_member"

						+ " from v_sys_columns where is_visible = true and control_type is not null and table_name = '"
						+ tableName + "' order by column_idx");

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

							// str = "r_options";

						} else if (rs.getString(3).equals("checkbox")) {

							sb.append(",\"c_options\":");

						} else if (rs.getString(3).equals("select_one") || rs.getString(3).equals("select_many")
								|| rs.getString(3).equals("input_select_one")) {

							sb.append(",\"s_options\":");

						} else if (rs.getString(3).equals("select_tree") || rs.getString(3).equals("select_tree_end")
								|| rs.getString(3).equals("select_tree_end_many")) {

							sb.append(",\"tree_options\":");

						} else if (rs.getString(3).equals("tablebox_one")) {

							sb.append(",\"table_options\":");

						} else {

							sb.append(",\"r_options\":");

						}

						if (rs.getString(3).equals("select_tree") || rs.getString(3).equals("select_tree_end")
								|| rs.getString(3).equals("select_tree_end_many")) {

							sb.append("[" + getSelectTreeJson(rs.getString(13), rs.getString(15), rs.getString(16),

									rs.getString(14), "") + "]");

						} else {

							sb.append(rs.getString(12));

							if (rs.getString(3).equals("tablebox_one")) {

								ResultSet rsDatasource = dbConn.query(String.format(
										"SELECT source_text FROM v_sys_datasources WHERE is_value_member_visible = '是' and view_name = (SELECT d.view_name FROM v_sys_datasources d LEFT JOIN sys_columns c ON d.source_uid = c.source_uid WHERE c.table_name = '%s' AND c.control_type = 70)",
										tableName));

								if (rsDatasource.next()) {

									sb.append(String.format(",\"s_options\":%s", rsDatasource.getString(1)));

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
	 * 
	 * 行内编辑排产单批量新增编辑接口
	 * 
	 */

	@RequestMapping(value = "addOrUpdateJson", method = RequestMethod.POST)

	@ResponseBody

	public String getAddOrUpdateJson(HttpServletRequest request, @RequestBody String jsonSaveData) {

		JSONObject jsonObject = JSONObject.fromObject(jsonSaveData);

		StringBuilder allAddValue = new StringBuilder();// 所有新增value

		StringBuilder allUpdateValue = new StringBuilder();// 拼接所有sql语句

		StringBuilder deleteStr = new StringBuilder();// 需要删除的id集合

		StringBuilder columnName = new StringBuilder();// 列名
		
		String  columnNameStr = null;   //新增列名


		// String tableName = jsonObject.getString("tableName");

		JSONArray array = JSONArray.fromObject(jsonObject.getString("data"));// 数组对象

		boolean ok = false;

		// 第一层循环，将对象循环出来

		for (int i = 0; i < array.size(); i++) {

			StringBuilder addValue = new StringBuilder();// 用来拼接新增的对象

			StringBuilder updateStr = new StringBuilder();// 用来拼接编辑的对象

			Object object = null;

			String detailId = null;
			
			
			// 非空验证line_id,line_plan_id,product_code,product_name,cate_id,type_id,power

			if (array.getJSONObject(i).getString("line_id").equals("")
					|| array.getJSONObject(i).getString("line_id").equals("null")) {

				return message.getErrorInfo("产线不允许为空");
			}

			if (array.getJSONObject(i).getString("product_code").equals("")
					|| array.getJSONObject(i).getString("product_code").equals("null")) {

				return message.getErrorInfo("产品编码不允许为空");
			}

			if (array.getJSONObject(i).getString("product_name").equals("")
					|| array.getJSONObject(i).getString("product_name").equals("null")) {

				return message.getErrorInfo("产品名称不允许为空");
			}

			if (array.getJSONObject(i).getString("cate_id").equals("")
					|| array.getJSONObject(i).getString("cate_id").equals("null")) {

				return message.getErrorInfo("产品类别不允许为空");
			}
			if (array.getJSONObject(i).getString("power").equals("")
					|| array.getJSONObject(i).getString("power").equals("null")) {

				return message.getErrorInfo("功率不允许为空");

			} else if (!array.getJSONObject(i).getString("power").matches("^\\d{1,8}(\\.\\d{1,2})?$")) {

				return message.getErrorInfo("功率格式不符合.请输入整数位数为10,小数位数为2的数字");
			}

			if (array.getJSONObject(i).getString("plan_line_date").equals("")
					|| array.getJSONObject(i).getString("plan_line_date").equals("null")) {

				return message.getErrorInfo("排产单行日期不允许为空");

			} else {
             // 判断排产单行日期在排产的日期范围内
//				try {
//
//					ResultSet rs = dbConn.query(String.format(
//							"select '%s' between p.plan_date and coalesce(p.plan_end_date,p.plan_date) from meq_line_plans p"
//									+
//									" where p.line_plan_id = %s ",
//							array.getJSONObject(i).getString("plan_line_date"),
//							array.getJSONObject(i).getString("line_plan_id")));
//    
//					if (rs.next()) {
//						if (!rs.getBoolean(1)) {
//							return message.getErrorInfo("排产单行日期不符合");
//						}
//					}
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}

			}

			// 如果isDelete为true的话就是删除

			if (array.getJSONObject(i).getString("isDelete").equals("true")) {

				deleteStr.append(array.getJSONObject(i).getString("detail_id"));

				deleteStr.append(",");

			} else {

				// 第二层循环，循环出对象里面的每一个字段

				for (Iterator<?> iter = array.getJSONObject(i).keys(); iter.hasNext();) {

					// 获取当前对象的ID根据ID是否为空来控制当前对象的操作

					detailId = array.getJSONObject(i).getString("detail_id");

					// 如果ID不为空的话就是编辑

					if (!detailId.equals("null")) {

						String key = (String) iter.next();

						if (!key.equals("isEdit") && !key.equals("index") && !key.equals("isDelete")) {// 过滤掉isEdit字段

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
						if (!key.equals("isEdit") && !key.equals("detail_id") && !key.equals("index")
								&& !key.equals("isDelete")) {
							if (!ok) {
								columnName.append(key);
							}

							object = array.getJSONObject(i).get(key);

							if (array.getJSONObject(i).getString(key).isEmpty() || (object instanceof JSONNull)) {

								addValue.append("null");

							} else {

								addValue.append("'" + array.getJSONObject(i).getString(key) + "'");

							}

							if (iter.hasNext()) {
								if (!ok)
									columnName.append(",");
								addValue.append(",");
							}

						}

					}

				}
				ok = true;
				// 组装所有的value值
                  
				if (!detailId.equals("null")) {
					allUpdateValue.append(String.format("update meq_line_plan_details set %s where detail_id = %s;",
							Str.delComma(updateStr.toString()), detailId));
				} else {
					columnNameStr =Str.delComma(columnName.toString());
					allAddValue.append("(" + Str.delComma(addValue.toString())+ "),");

				}

			}

		}
		// 执行批量增删改的操作
		String sqlInsert = String.format("INSERT INTO meq_line_plan_details (%s) VALUES %s", columnNameStr,
				Str.delComma(allAddValue.toString()));
     System.err.println(sqlInsert);
		String sqlDelete = String.format("delete from meq_line_plan_details where detail_id in (%s)",
				Str.delComma(deleteStr.toString()));

		boolean addRes = dbConn.queryUpdate(sqlInsert);

		boolean deleteRes = dbConn.queryUpdate(sqlDelete);
		boolean updateRes = dbConn.queryUpdate(allUpdateValue.toString());
		if (addRes || updateRes || deleteRes) {
			return message.getSuccessInfo("保存成功");
		}
		return message.getErrorInfo("保存失败");

	}

	/**
	 * 
	 * 排产通用表单新增接口
	 * 
	 */

	@SuppressWarnings("unlikely-arg-type")
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

			if (tableName.equals("meq_line_plans")) {

				String deptId = jsonDataToAdd.getString("dept_id");

				String planType = jsonDataToAdd.getString("plan_type");

				String planDate = jsonDataToAdd.getString("plan_date");

				String endDate = null;

				ResultSet rs = dbConn.query("select setting_value from sys_settings where setting_code = 'plan_date'");

				try {

					if (rs.next()) {

						if (rs.getString(1).equals(1) || rs.getInt("setting_value") == 1) {

							String[] array = planDate.split(",");

							planDate = array[0];

							planDate = planDate.substring(1, planDate.length());

							endDate = array[1];

							endDate = endDate.substring(0, endDate.length() - 1);

							planDate = planDate.replace("\"", "");

							endDate = endDate.replace("\"", "");

							jsonDataToAdd.put("plan_date", planDate);

							jsonDataToAdd.put("plan_end_date", endDate);

						}

					}

				} catch (SQLException e) {
				}

				ResultSet rsPlanTypeValid = dbConn.query(String.format(
						"SELECT COUNT(*) >= 1 FROM meq_line_plans WHERE plan_type = 0 AND dept_id = %s AND plan_date = '%s'",
						deptId, planDate));

				if (rsPlanTypeValid.next()) {

					if (rsPlanTypeValid.getBoolean(1) && planType.equals("0")) {

						return message.getErrorInfo("新增失败,该车间已有一个正常的排产单");

					}

				}

				ResultSet rsCurrentDate = dbConn.query("SELECT current_date");

				if (rsCurrentDate.next()) {

					if (planDate.compareTo(rsCurrentDate.getString(1)) < 0) {

						return message.getErrorInfo("新增失败,排产日期不能小于当前日期");

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

					columnValue.append("'" + jsonDataToAdd.getString(key) + "'");

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
	 * 排产通用删除接口
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
			String sql = null;
			if (idValue.contains("[")) {
				JSONArray jsonArray = JSONArray.fromObject(idValue);
				for (int i = 0; i < jsonArray.size(); i++) {
					ResultSet rsinfo = dbConn
							.query("select bxm_get_data_row('" + tableName + "','" + jsonArray.get(i) + "')");
					while (rsinfo.next()) {
						resultInfo += rsinfo.getString(1);
					}
//					if (tableName.equals("meq_line_plan_details")) {// 如果删除的是排产主表记录,要判断排产子表是否有引用
//						ResultSet auditValid = dbConn.query(String.format(
//								"select (auditor is null and audited_date is null) as can_delete from meq_line_plans where line_plan_id = '%s'",
//								jsonArray.get(i)));
//					}
					if (tableName.equals("meq_line_plan_details")) {// 如果删除的是排产子表记录,要判断排产主表是否审核
						ResultSet rs = dbConn.query(String.format(
								"select is_audited from meq_line_plan_details where detail_id = '%s'",
								jsonArray.get(i)));
						if (rs.next()) {
							if (rs.getBoolean(1)) {
								return message.getErrorInfo("排产单已审核，不能删除");
							}
						}
					}
				}
				if (idValue != null && !idValue.equals("")) {
					idValue = idValue.replace("[", "").replace("]", "").replace("\"", "'");
				}
				sql = String.format("DELETE FROM %s WHERE %s", tableName,
						jsonObj.getString("idName") + " in" + "(" + idValue + ")");

			}
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
	 * 
	 * 排产通用编辑接口
	 * 
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

			if (tableName.equals("meq_line_plan_details")) {

				String detailId = jsonData.getString("detail_id");

				// 已关闭的不能修改

				ResultSet shutdown = dbConn.query(String.format(
						"SELECT (close_user IS NULL AND closed_date IS NULL) AS can_edit FROM meq_line_plan_details WHERE detail_id = '%s'",
						detailId));

				if (shutdown.next()) {

					if (!shutdown.getBoolean(1)) {

						return message.getErrorInfo("此行已关闭,不能修改");

					}

				}

				// 排产数量大于导入数量验证

				ResultSet exceedPlan = dbConn.query(
						String.format("SELECT setting_value FROM sys_settings WHERE setting_code = 'ExceedERP'"));

				ResultSet haveMorderCode = dbConn.query(String.format(
						"SELECT morder_line_no IS NOT NULL AS have_morder_code FROM meq_line_plan_details WHERE detail_id = '%s'",
						detailId));

				if (exceedPlan.next() && haveMorderCode.next()) {

					if (exceedPlan.getString(1).equals("false") && haveMorderCode.getBoolean(1)) {

						Integer qty = jsonData.getInt("qty");

						Integer originalQty = jsonData.getInt("original_qty");

						if (qty > originalQty) {

							return message.getErrorInfo("排产数量不能大于导入数量");

						}

					}

				}

			}

			if (tableName.equals("meq_line_plans")) {

				String deptId = jsonData.getString("dept_id");

				String planType = jsonData.getString("plan_type");

				String linePlanId = jsonData.getString("line_plan_id");

				String planDate = jsonData.getString("plan_date");
				ResultSet rsPlanTypeValid = null;
				if (planDate.contains("[")) {
					String[] plan_data = planDate.replace("[", "").replace("]", "").split(",");
					 rsPlanTypeValid = dbConn.query(String.format(
							"SELECT COUNT(*) >= 1 FROM meq_line_plans WHERE  plan_type = 0 AND plan_date = '%s' AND dept_id = %s AND line_plan_id <> %s and plan_end_date = '%s'",
							plan_data[0], deptId, linePlanId, plan_data[1]));
				}else {
					 rsPlanTypeValid = dbConn.query(String.format(
							"SELECT COUNT(*) >= 1 FROM meq_line_plans WHERE  plan_type = 0 AND plan_date = '%s' AND dept_id = %s AND line_plan_id <> %s",
							planDate, deptId, linePlanId));
				}

				if (rsPlanTypeValid.next()) {

					if (rsPlanTypeValid.getBoolean(1) && planType.equals("0")) {

						return message.getErrorInfo("修改失败,该车间已有一个正常的排产单");

					}

				}

				ResultSet rsCurrentDate = dbConn.query("SELECT current_date");

				if (rsCurrentDate.next()) {

					if (planDate.compareTo(rsCurrentDate.getString(1)) < 0) {

						return message.getErrorInfo("修改失败,排产日期不能小于当前日期");

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
						//判斷是否有这个plan_date key
						if (jsonData.containsKey("plan_date")) {
							//判断这个值是否是数组类型如果是拆分拼接到sql
							if (jsonData.getString("plan_date").contains("[")) {
	                        	 String[] plan_data = jsonData.getString("plan_date").replace("[", "").replace("]", "").split(",");
	                        	 updateStr.append(key + " = " + "'" + plan_data[0] + "'");
	                        	 updateStr.append(", plan_end_date = " + "'" + plan_data[1] + "'");
	                        	 //随后还要删除这个key不然会重复拼接
	                        	 jsonData.remove("plan_date");
	                        	 //重新给迭代赋值不然迭代是链表形式的我删除一个Key迭代会包异常。
	                        	 iter=jsonData.keys();
							}else {
								updateStr.append(key + " = " + "'" + jsonData.getString(key) + "'");
							}
						}else {
							updateStr.append(key + " = " + "'" + jsonData.getString(key) + "'");
						}
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
	 * 
	 * LJZ 表格接口,从表行内编辑
	 * 
	 */

	@RequestMapping(value = "tableJson", method = RequestMethod.GET)

	@ResponseBody

	public String getTableJson(@RequestParam String tableName, @RequestParam String inputSearch,
			@RequestParam String order,@RequestParam String prop, @RequestParam String filterString,
			@RequestParam String currentPage, @RequestParam String pageSize) {

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
		String sql = "select bxm_get_page_data_json('" + tableName + "','*','" + filterString + "','"

				+ inputSearch + "','" + prop + " " + order + "','"+pageSize+"','"+currentPage+"')";

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
	 * 
	 * JF 拼接表单控件JSON FORMDATA部分
	 * 
	 */
	private String getFormDataJson(String tableName) {

		String result = null;

		JSONObject json = new JSONObject();

		// 如果ID为空就是新增,不为空为编辑

		try {

			ResultSet rs = dbConn.query(

					"select column_name,control_type,bxm_get_default_value(default_value),data_type from v_sys_columns "

							+ "where not is_deactive and table_name = '" + tableName + "'");

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
			}
			result = "\"formData\":" + json.toString() + "";
		} catch (SQLException e) {
		}

		return result;

	}

	/**
	 * 
	 * JF 拼接表单控件JSON FORMFRAME部分
	 * 
	 */
	@RequestMapping(value = "tableFromData", method = RequestMethod.GET)
	@ResponseBody
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

				if (tableName.equals("meq_line_cards") && rs.getString(1).equals("part_count")) {

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

//							sb.append("[" + getTree(rs.getString(13), rs.getString(15), rs.getString(16),
//
//									rs.getString(14), "") + "]");
							sb.append(getTree(rs.getString(13), rs.getString(15), rs.getString(16),
									rs.getString(14), ""));

						} else {

							if (rs.getString(12).equals("{}")) {

								sb.append("[]");

							} else {

								sb.append(rs.getString(12));

							}

							if (rs.getString(3).equals("tablebox_one")) {

								try {

									ResultSet rsDatasource = dbConn.query(String.format(
											"SELECT source_text FROM v_sys_datasources WHERE is_value_member_visible = '是' "

													+ "and view_name = (SELECT d.view_name FROM v_sys_datasources d LEFT JOIN sys_columns c ON d.source_uid = c.source_uid "

													+ "WHERE c.table_name = '%s' AND c.control_type = 70)",
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
	 * 
	 * LJZ 下拉框层级
	 * 
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
	 * LH 下拉框层级new
	 */
	public Object getTree(String viewName, String valueMember, String displayMember, String filterString,String parentUid) {
		  String sql  = String.format("SELECT * FROM %s WHERE %s",viewName,filterString == null || filterString == "" ? "1=1" : filterString);
		  Object treeStr =treeData.getTree(sql);
       return treeStr;
    }
	
    
    
	/**
	 * 
	 * 排产主表审核接口
	 * 
	 */

	@RequestMapping(value = "auditLinePlans", method = RequestMethod.GET)

	@ResponseBody

	public String auditLinePlans(HttpServletRequest request, @RequestParam String linePlanId,
			@RequestParam String userId) {

		try {

			ResultSet rs = dbConn.query(
					String.format("SELECT COUNT(*) FROM meq_line_plan_details WHERE line_plan_id = '%s'", linePlanId));

			if (rs.next()) {

				if (rs.getInt(1) > 0) {

					ResultSet rsQty = dbConn.query(String.format(
							"SELECT EXISTS (SELECT 1 FROM meq_line_plan_details WHERE qty = 0 AND line_plan_id = '%s')",
							linePlanId));

					if (rsQty.next()) {

						if (rsQty.getBoolean(1)) {

							return message.getErrorInfo("排产数量必须大于0");

						}

					}

					String sqlInsert = String.format(
							"UPDATE meq_line_plans SET	auditor = '%s',audited_date = current_timestamp WHERE line_plan_id = '%s'",
							userId, linePlanId);

					boolean res = dbConn.queryUpdate(sqlInsert);

					String sqlInsertProduct = String.format("CALL select_new_products(%s)", linePlanId);

					dbConn.queryUpdate(sqlInsertProduct);

					// 捕获用户操作日志

					getLogs.getUserLog(request, "375d7728-0f5f-4f25-b569-cd9073a0f0ff", "审核", res, "", "", userId);

					if (res) {

						return message.getSuccessInfo("审核成功");

					} else {

						return message.getErrorInfo("审核失败");

					}

				}

			}

			return message.getErrorInfo("该排产单子表无数据,不能审核");

		} catch (Exception e) {

			return message.getErrorInfo("审核失败");

		}

	}

	/**
	 * 
	 * 排产主表撤审接口
	 * 
	 */

	@RequestMapping(value = "unauditLinePlans", method = RequestMethod.GET)

	@ResponseBody

	public String unauditLinePlans(HttpServletRequest request, @RequestParam String linePlanId,
			@RequestParam String userId) {

		try {

			ResultSet rs = dbConn.query(String.format(
					"SELECT COALESCE(sum(online_qty),0) = 0 FROM meq_line_plan_details WHERE line_plan_id = '%s'",
					linePlanId));

			if (rs.next()) {

				if (rs.getBoolean(1)) {

					ResultSet rsClose = dbConn.query(String.format(
							"SELECT EXISTS (SELECT * FROM meq_line_plan_details WHERE closed_date IS NOT null AND line_plan_id = %s)",
							linePlanId));

					if (rsClose.next()) {

						if (rsClose.getBoolean(1)) {

							return message.getErrorInfo("该排产单已有关闭单行,不能撤审");

						}

					}

					ResultSet rsCommand = dbConn
							.query(String.format("SELECT EXISTS (SELECT c.rec_id FROM meq_line_commands c \n" +

									"LEFT JOIN meq_line_plan_details d ON c.detail_id = d.detail_id\n" +

									"LEFT JOIN meq_line_plans p ON d.line_plan_id = p.line_plan_id\n" +

									"WHERE p.line_plan_id = %s)", linePlanId));

					if (rsCommand.next()) {

						if (rsCommand.getBoolean(1)) {

							return message.getErrorInfo("该排产单已有投产数据,不能撤审");

						}

					}

					String sqlInsert = String.format(
							"UPDATE meq_line_plans SET	auditor = null,audited_date = null WHERE line_plan_id = '%s'",
							linePlanId);

					boolean res = dbConn.queryUpdate(sqlInsert);

					// 捕获用户操作日志

					getLogs.getUserLog(request, "375d7728-0f5f-4f25-b569-cd9073a0f0ff", "撤审", res, "", "", userId);

					if (res) {

						return message.getSuccessInfo("撤审成功");

					} else {

						return message.getErrorInfo("撤审失败");

					}

				}

			}

			return message.getErrorInfo("此排产单已有产品上线,不能撤审");

		} catch (Exception e) {

			return message.getErrorInfo("撤审失败");

		}

	}

	/**
	 * 
	 * 排产子表选择产品带进参数接口
	 * 
	 */

	@RequestMapping(value = "getProductInfo", method = RequestMethod.GET)

	@ResponseBody

	public String getProductInfo(@RequestParam String productId) {

		try {

			ResultSet rs = dbConn.query(String.format(
					"SELECT bxm_get_from_json('meq_products','product_code,product_name,cate_id,type_id,gas_id,capacity,power,customer,customer_barcode','product_code = ''%s''','')",
					productId));

			if (rs.next()) {

				return rs.getString(1).toString();

			}

			return "{\"formData\":{}}";

		} catch (Exception e) {
			return "{\"formData\":{}}";

		}

	}

	/**
	 * 
	 * 排产子表关闭接口
	 * 
	 */

	@RequestMapping(value = "closeLinePlanDetails", method = RequestMethod.GET)

	@ResponseBody

	public String closeLinePlanDetails(HttpServletRequest request, @RequestParam String linePlanDetailId,
			@RequestParam String userId) {

		try {

//			ResultSet closeValidResultSet = dbConn.query(String.format(
//					"SELECT * FROM v_meq_line_plan_detail_close_valid WHERE detail_id = '%s'", linePlanDetailId));
			ResultSet closeValidResultSet = dbConn.query(String.format(
					"SELECT detail_id,is_audited FROM meq_line_plan_details WHERE detail_id = '%s'", linePlanDetailId));
			if (closeValidResultSet.next()) {
				if (closeValidResultSet.getBoolean(2)) {
					ResultSet rs = dbConn.query(String.format(
							"SELECT deleted FROM meq_line_commands_new WHERE detail_id = %s ", linePlanDetailId));
					if (rs.next()) {
						if (rs.getBoolean(1)) {
							String sqlUpdate = String.format(
									"UPDATE meq_line_plan_details SET close_user = '%s',closed_date = CURRENT_TIMESTAMP WHERE detail_id = '%s'",
									userId, linePlanDetailId);

							boolean res = dbConn.queryUpdate(sqlUpdate);

							// 捕获用户操作日志
							getLogs.getUserLog(request, "375d7728-0f5f-4f25-b569-cd9073a0f0ff", "关闭", res, "", "",
									userId);
							if (res) {
								return message.getSuccessInfo("关闭成功");
							} else {
								return message.getErrorInfo("关闭失败");
							}
						}else {
							return message.getErrorInfo("该排产单正在投产,不能关闭");
						}
					}else {
						return message.getSuccessInfo("关闭成功");
					}
				}
				return message.getErrorInfo("排产单未审核,不能关闭");
			}
		} catch (Exception e) {
			return message.getErrorInfo("关闭失败");
		}
		return message.getErrorInfo("关闭失败");
	}

	/**
	 * 
	 * 点击排产主表渲染排产子表按钮状态接口
	 * 
	 */

	@RequestMapping(value = "focusPlansValid", method = RequestMethod.GET)

	@ResponseBody

	public String focusPlansValid(@RequestParam String linePlanId) {

		JSONObject json = new JSONObject();

		JSONObject json2 = new JSONObject();

		JSONObject json3 = new JSONObject();
		
		String result = null;

		boolean exportERPPlanLines = true;

		boolean addPlanLines = true;
		
		try {

			ResultSet rsPlanAudit = dbConn.query(
					String.format("SELECT auditor IS NULL FROM meq_line_plans WHERE line_plan_id = %s", linePlanId));

			if (rsPlanAudit.next()) {

				if (rsPlanAudit.getBoolean(1)) {

					ResultSet rsERP = dbConn.query(
							"SELECT setting_value = 'true' AS is_erp FROM sys_settings WHERE setting_code = 'MustImportFromERP'");

					ResultSet rsImport = dbConn.query(
							"SELECT setting_value = 'true' AS is_import FROM sys_settings WHERE setting_code = 'EableImportIntoLinePlan'");

					if (rsERP.next()) {

						if (rsImport.next()) {

							if (rsERP.getBoolean(1) && rsImport.getBoolean(1)) {

								exportERPPlanLines = false;

								addPlanLines = true;

							} else if (!rsERP.getBoolean(1) && rsImport.getBoolean(1)) {

								exportERPPlanLines = false;

								addPlanLines = false;

							} else {

								addPlanLines = false;

							}
						}
					} else {

						addPlanLines = false;
					}
				}
			}
			json.put("buttonName", "导入工单");

			json.put("buttonAction", "exportERPPlanLines");

			json.put("disabled", exportERPPlanLines);

			json2.put("buttonName", "添加排产单行");

			json2.put("buttonAction", "addPlanLines");

			json2.put("disabled", addPlanLines);

			json3.put("buttonName", "关闭");

			json3.put("buttonAction", "closePlanLines");

			json3.put("disabled", true);
			
			result = "[" + json + "," + json2 + "," + json3 + "]";


			return result;

		} catch (Exception e) {

			json.put("buttonName", "导入工单");

			json.put("buttonAction", "exportERPPlanLines");

			json.put("disabled", exportERPPlanLines);

			json2.put("buttonName", "添加排产单行");

			json2.put("buttonAction", "addPlanLines");

			json2.put("disabled", addPlanLines);

			json3.put("buttonName", "关闭");

			json3.put("buttonAction", "closePlanLines");

			json3.put("disabled", true);
			
			result = "[" + json + "," + json2 + "," + json3 + "]";

			return result;

		}

	}

	/**
	 * 
	 * 点击排产子表渲染按钮状态接口
	 * 
	 */

	@RequestMapping(value = "focusDetailsValid", method = RequestMethod.GET)

	@ResponseBody

	public String focusDetailsValid(@RequestParam String detailId) {


		JSONObject json1 = new JSONObject();
		
		JSONObject json2 = new JSONObject();
		
		JSONObject json4 = new JSONObject();
		
		JSONObject json3 = new JSONObject();

		String result = null;

//		boolean addPlanLines = false;

		boolean closePlanLines = true;
		
		boolean auditScheduLines = false;
		
		boolean unauditScheduLines = false;
		
		boolean completePlanLines = false;

		try {

			ResultSet rsPlanAudit = dbConn.query(String.format(
					"SELECT is_audited FROM meq_line_plan_details WHERE detail_id =  %s",
					detailId));
			if (rsPlanAudit.next()) {
				if (rsPlanAudit.getBoolean(1)) {
					auditScheduLines = true;
//					ResultSet rsERP = dbConn.query(
//							"SELECT setting_value = 'true' AS is_erp FROM sys_settings WHERE setting_code = 'MustImportFromERP'");
//
//					ResultSet rsImport = dbConn.query(
//							"SELECT setting_value = 'true' AS is_import FROM sys_settings WHERE setting_code = 'EableImportIntoLinePlan'");
//
//					if (rsERP.next()) {
//
//						if (rsImport.next()) {
//
//							if (rsERP.getBoolean(1) && rsImport.getBoolean(1)) {
//
////								exportERPPlanLines = true;
//
//								addPlanLines = false;
//
//							} else if (rsImport.getBoolean(1)) {
////								exportERPPlanLines = false;
//
//							} else {
//								addPlanLines = false;
//
//							}
//						}
//
//					} else {
//						addPlanLines = false;
//					}
					ResultSet rsClose = dbConn.query(String.format(
							"SELECT close_user IS NULL FROM meq_line_plan_details WHERE detail_id = %s", detailId));
					if (rsClose.next()) {
						if (rsClose.getBoolean(1)) {
							closePlanLines = false;
						}
					}
				} else {
					unauditScheduLines = true;
				}
			}
			
			//是否有投产
			ResultSet rsIs_complete = dbConn.query(String.format(
					"select exists(select 1 from meq_line_commands_new where detail_id = %s and not deleted)",detailId));
			if (rsIs_complete.next()) {
				if (rsIs_complete.getBoolean(1)) {
					completePlanLines = true;
				}
			}
			//是否完结
			rsIs_complete = dbConn.query(String.format("SELECT is_complete  FROM meq_line_plan_details WHERE detail_id = %s", detailId));
			if (rsIs_complete.next()) {
				if (rsIs_complete.getBoolean(1)) {
					completePlanLines = true;
				}
				
			}
			json1.put("buttonName", "审核");

			json1.put("buttonAction", "auditScheduLines");

			json1.put("disabled", auditScheduLines);
			
			json2.put("buttonName", "撤审");

			json2.put("buttonAction", "unauditScheduLines");

			json2.put("disabled", unauditScheduLines);
			
			json4.put("buttonName", "关闭");

			json4.put("buttonAction", "closePlanLines");

			json4.put("disabled", closePlanLines);
			
			json3.put("buttonName", "完成结单");

			json3.put("buttonAction", "completePlanLines");

			json3.put("disabled", completePlanLines);

			result = "[" + json1 + "," + json2 + ","+ json3 + ","+ json4 + "]";

			return result;

		} catch (Exception e) {
              e.printStackTrace();
			json1.put("buttonName", "审核");

			json1.put("buttonAction", "auditScheduLines");

			json1.put("disabled", true);
			
			json2.put("buttonName", "撤审");

			json2.put("buttonAction", "unauditScheduLines");

			json2.put("disabled", true);
			
			json4.put("buttonName", "关闭");

			json4.put("buttonAction", "closePlanLines");

			json4.put("disabled", true);
			
			json3.put("buttonName", "完成结单");

			json3.put("buttonAction", "completePlanLines");

			json3.put("disabled", true);
			
			result = "[" + json1 + "," + json2 + ","+ json3 + ","+ json4 + "]";

			return result;

		}

	}

	/**
	 * 
	 * 排产表产能负荷分析接口
	 * 
	 */

	@RequestMapping(value = "planProductionLoad", method = RequestMethod.GET)

	@ResponseBody

	public String planProductionLoad(@RequestParam String linePlanId) {

		try {

			ResultSet rs = dbConn.query(String.format(
					"SELECT * FROM bxm_dept_daily_banlance(%s) AS (line_id varchar(20),zc_load_time numeric,cd_load_time numeric)",
					linePlanId));

			StringBuilder barData1 = new StringBuilder();

			StringBuilder barData2 = new StringBuilder();

			StringBuilder xAxisData = new StringBuilder();

			StringBuilder resuBuilder = new StringBuilder();

			while (rs.next()) {

				barData1.append(String.format("{\"value\":%s,\"name\":\"%s\"},", rs.getString(2), rs.getString(1)));

				barData2.append(String.format("{\"value\":%s,\"name\":\"%s\"},", rs.getString(3), rs.getString(1)));

				xAxisData.append(String.format("\"%s\",", rs.getString(1)));

			}

			resuBuilder.append(String.format("{\"barData1\":[%s],\"barData2\":[%s],\"xAxisData\":[%s]}",
					Str.delComma(barData1.toString()), Str.delComma(barData2.toString()),
					Str.delComma(xAxisData.toString())));

			return resuBuilder.toString();

		} catch (Exception e) {

			return "{\"barData\":[],\"xAxisData\":[]}";

		}

	}

	/**
	 * 
	 * 排产表产线联动
	 * 
	 */

	@RequestMapping(value = "productionLine", method = RequestMethod.GET)

	@ResponseBody

	public String productionLine(@RequestParam String deptId) {

		StringBuilder sb = new StringBuilder();

		JSONObject json = new JSONObject();

		String sql = "select line_id,line_name from meq_production_lines where dept_id = '" + deptId + "'";

		ResultSet rs = dbConn.query(sql);

		try {

			while (rs.next()) {

				json.put("id", rs.getInt(1));

				json.put("value", rs.getString(2));

				sb.append(json.toString() + ",");

			}

		} catch (SQLException e) {
		}

		return "[" + Str.delComma(sb.toString()) + "]";

	}
}
