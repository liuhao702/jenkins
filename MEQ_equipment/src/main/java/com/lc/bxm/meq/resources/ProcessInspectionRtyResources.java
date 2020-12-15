package com.lc.bxm.meq.resources;


import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.FileUtil;
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.entity.DeleteData;
import com.lc.bxm.entity.EditData;
import com.lc.bxm.entity.InsertData;
import com.lc.bxm.entity.QueryData;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @author CK 过程检验直通率（人工录入）
 *
 */
@RestController
@RequestMapping("/rty")
public class ProcessInspectionRtyResources {
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;
	
	static FileUtil fileUitl = new FileUtil();

	/**
	 * CK 过程检验直通率新增
	 */
	@RequestMapping(value = "addInspectionRty", method = RequestMethod.POST)
	@ResponseBody
	public String addProcessInspectionRty(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		try {
			InsertData data = (InsertData) JSONObject.toBean(jsonSaveData, InsertData.class);
			JSONObject json = data.getFormData();
			String repair_order_no = json.getString("repair_order_no");
			ResultSet rs = dbConn.query(String.format(
					"select exists (select 1 from  cyj_process_inspection_rty where  repair_order_no ='%s')",
					repair_order_no));
			if (rs.next()) {
				if (rs.getBoolean(1)) {
					return message.getErrorInfo("新增失败!该工单编号已存在");
				} else {
					String sqql="select product_name,cate_id,specifications from meq_products_new where product_code ='"+json.getString("product_no")+"'";
					System.err.println(sqql);
					ResultSet rss = dbConn.query(sqql);
					
					while (rss.next()) {
						json.put("product_name", rss.getString(1));
						json.put("specifications", rss.getString(3));
						String sql1="select cate_name from meq_product_cates where cate_id ="+rss.getString(2);
						
						ResultSet rsS = dbConn.query(sql1);
						while(rsS.next()) {
								json.put("product_category", rsS.getString(1));
							}
					}
					if (helper.dataInsert(request, data.getMenuId(), data.getFunName(), data.getUserId(),
							data.getTableName(), json)) {
						return message.getSuccessInfo("新增成功");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("新增失败");

	}

	/**
	 * CK 查询产品数据
	 */
	@RequestMapping(value = "selectProductCode", method = RequestMethod.GET)
	@ResponseBody
	public String getSelectProducts(String code) {
		//String sqql = "select A.product_code,A.product_name,A.customer,B.cate_name AS cate_id from  meq_products A LEFT JOIN meq_product_cates B ON A.cate_id=B.cate_id where  product_code = '"
		//		+ code + "'";
		String sqql="select * from v_meq_line_plan_details where morder_code ="+code;
		ResultSet rss = dbConn.query(sqql);
		JSONObject json = new JSONObject();
		try {
			while (rss.next()) {
				json.put("repair_order_no", rss.getString("morder_code"));
				json.put("product_no", rss.getString("product_code")+"("+rss.getString("product_name")+")");
				
			//	json.put("product_name", rss.getString("product_name")); //!= null ? rss.getString("customer") : "");
			//	json.put("specifications", null);// != null ? rss.getString("cate_id") : "");
			//	String sql="select cate_name from meq_product_cates where cate_id ="+rss.getString("cate_id");
			//	ResultSet rs = dbConn.query(sql);
			//	while(rs.next()) {
			//		json.put("product_category", rs.getString(1));
			//	}
				
				json.put("customer", rss.getString("customer"));
				json.put("order_no", rss.getString("sale_order_code"));
				json.put("line", rss.getString("line_id"));
				json.put("production_scheduling_number", rss.getString("qty"));
				json.put("quantity_production_number", rss.getString("offline_qty"));
				json.put("schedule_time", rss.getString("plan_line_date"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return json.toString();

	}

	/**
	 * CK 过程检验直通率删除
	 */
	@RequestMapping(value = "deleteInspectionRty", method = RequestMethod.POST)
	@ResponseBody
	public String getDeleteInspectionRtyJson(HttpServletRequest request, @RequestBody JSONObject jsonData) {
		try {
			DeleteData data = (DeleteData) JSONObject.toBean(jsonData, DeleteData.class);
			JSONArray idValues = JSONArray.fromObject(data.getIdValue());
			String delString = idValues.toString().replace("[", "(").replace("]", ")");
			ResultSet rs = dbConn.query(String.format("select exists (select * from cyj_bad_record_sheet where inspection_rty_id in %s)" + 
					"",delString));
			if (rs.next()) {
				if (rs.getBoolean(1)) {
				  return message.getErrorInfo("删除失败，该过程检验直通率下有数据");
				}
			}
			//if (!getId(delString)) {
			//	return message.getErrorInfo("删除失败,它的不良记录跟缺陷记录无法删除");
		//	}
			if (helper.dataDelete(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getIdName(), jsonData.getString("idValue"))) {

				return message.getSuccessInfo("删除成功");
			} else {
				return message.getErrorInfo("删除失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("删除失败");
	}

	public boolean getId(String id) {

		String sqql = "select 1 as id from  cyj_bad_record_sheet where  inspection_rty_id in " + id;
		ResultSet rss = dbConn.query(sqql);
		boolean isdel = false;
		try {
			int stid = 0;
			while (rss.next()) {
				stid = Integer.valueOf(rss.getString("id"));
			}
			if (stid == 1) {
				String sql = "delete from cyj_bad_record_sheet where inspection_rty_id in" + id;
				isdel = dbConn.queryUpdateUserLogin(sql);
				String str = "delete from cyj_defect_records where id in"
						+ "(select a.id from cyj_defect_records a left join cyj_bad_record_sheet b on a.record_sheet_id="
						+ "b.id where b.id is null)";
				dbConn.queryUpdateUserLogin(str);
			} else {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isdel;
	}

	/**
	 * CK 过程检验直通率UPDATE
	 */
	@RequestMapping(value = "editInspectionRty", method = RequestMethod.POST)
	@ResponseBody
	public String getEditInspectionRtyJson(HttpServletRequest request, @RequestBody JSONObject jsonData) {
		try {
			EditData data = (EditData) JSONObject.toBean(jsonData, EditData.class);
			if (helper.dataUpdate(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData(), data.getId())) {
				return message.getSuccessInfo("修改成功");
			} else {
				return message.getErrorInfo("修改失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("修改失败");
	}

	/**
	 * CK 过程检验直通率查询
	 */
	@RequestMapping(value = "queryInspectionRty", method = RequestMethod.POST)
	@ResponseBody
	public String getQueryInspectionRty(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		QueryData data = (QueryData) JSONObject.toBean(jsonSaveData, QueryData.class);
		String order=data.getOrder();
		if (order.equals("descending")) {
			order = "desc";
		}
		if (order.equals("ascending")) {
			order = "asc";
		}
		String sql = "select bxm_get_grid_page_json('" + data.getTableName() + "','*','','" + data.getFilterString() + "','"
				+ data.getInputSearch() + "','" + data.getProp() + " " + order + "'," + data.getPageSize() + "," + data.getCurrentPage() + ")";
		ResultSet rs = dbConn.query(sql);
		String messageJson = null;
		try {
			if (rs.next()) {
				messageJson = rs.getString(1);
		  }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return messageJson;
		
	}

	/**
	 * 
	 * @return CK不良记录新增
	 */
	@RequestMapping(value = "addBadRecordSheet", method = RequestMethod.POST)
	@ResponseBody
	public String addBadRecordSheet(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		try {
			InsertData data = (InsertData) JSONObject.toBean(jsonSaveData, InsertData.class);
			JSONObject json = data.getFormData();
			if(!updateInspectionRtyAll(json)) {
				return message.getErrorInfo("新增失败,不良数不能大于实收数量!");
			}
			if (helper.dataInsert(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData())) {
				//updateInspectionRtyAll(json.getString("inspection_rty_id"));
				return message.getSuccessInfo("新增成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("新增失败");

	}

	/**
	 * 
	 * @param rtyId 新增不良记录时，修改过程检验直通率
	 * @return
	 */
	public boolean updateInspectionRtyAll(JSONObject json) {
		String rtyId=json.getString("inspection_rty_id");
		ResultSet rs3 =null;
		
		
		String str = "select sum(production_quantity),sum(bad_number) from cyj_bad_record_sheet where inspection_rty_id=" + rtyId+" GROUP BY  inspection_rty_id="
				+ rtyId;
		String shishou="select quantity_production_number,bad_number from cyj_process_inspection_rty where id="+rtyId;
		
		ResultSet rs2 = dbConn.query(String.format("select exists (select * from cyj_bad_record_sheet where inspection_rty_id = %s)" + 
				"", rtyId));
		ResultSet rS= dbConn.query(shishou);
		ResultSet rs = dbConn.query(str);
		double rty =0;
		double bad_number=0;
		try {
			while(rS.next()) {
				if(Double.parseDouble(json.getString("bad_number"))>Double.parseDouble(rS.getString(1))) {
					return false;
				}
				
				if (rs2.next()) {
					if (!rs2.getBoolean(1)) {
						bad_number=Double.parseDouble(json.getString("bad_number"));
						rty = (Double.parseDouble(rS.getString(1)) - bad_number)
								/ Double.parseDouble(rS.getString(1));
						String sqlInsert = "update cyj_process_inspection_rty set bad_number=" + bad_number + ", " + " rty="
								+ rty + " WHERE id=" + rtyId;
						dbConn.queryUpdateUserLogin(sqlInsert);
						return true;
					}
				}
				if(json.containsKey("id")) {
					 rs3 = dbConn.query(String.format("select bad_number from cyj_bad_record_sheet where id = %s" + 
							"", json.getString("id")));
					 while(rs3.next()) {
							bad_number=Double.parseDouble(rS.getString(2))-Double.parseDouble(rs3.getString(1))+Double.parseDouble(json.getString("bad_number"));
					}
					 if(bad_number<0||bad_number>Double.parseDouble(rS.getString(1))) {
							return false;
						}
					 
					 rty = (Double.parseDouble(rS.getString(1)) - bad_number)
								/ Double.parseDouble(rS.getString(1));
						rty = (double) Math.round(rty * 100) / 100;
						String sqlInsert = "update cyj_process_inspection_rty set bad_number=" + bad_number + ", " + " rty="
									+ rty + " WHERE id=" + rtyId;
						dbConn.queryUpdateUserLogin(sqlInsert);
						return true;
				}
				
				while (rs.next()) {
					bad_number=Double.parseDouble(rs.getString(2))+Double.parseDouble(json.getString("bad_number"));
					if(bad_number>Double.parseDouble(rS.getString(1))) {
						return false;
					}
					rty = (Double.parseDouble(rS.getString(1)) - bad_number)
							/ Double.parseDouble(rS.getString(1));
					rty = (double) Math.round(rty * 100) / 100;
					String sqlInsert = "update cyj_process_inspection_rty set bad_number=" + bad_number + ", " + " rty="
								+ rty + " WHERE id=" + rtyId;
					dbConn.queryUpdateUserLogin(sqlInsert);
				}
				
			}
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 
	 * @param id
	 * @return 删除不良记录时，更新直通率的数据
	 */
	public boolean updateInspectionRtyDel(String str) {
		JSONArray idValues = JSONArray.fromObject(str);
		ResultSet rs = null;
		int quantity_production_number = 0;
		int bad_number = 0;
		String rtyID = "";

		try {
			String delString = idValues.toString().replace("[", "(").replace("]", ")");
			rs = dbConn.query(
					"SELECT production_quantity,bad_number,inspection_rty_id from cyj_bad_record_sheet where id in"
							+ delString);
			while (rs.next()) {
				//quantity_production_number = quantity_production_number + Integer.valueOf(rs.getString(1));
				bad_number = bad_number + Integer.valueOf(rs.getString(2));
				rtyID = rs.getString(3);
			}
			String sqlInsert = null;
			String sqql = "select quantity_production_number,bad_number from  cyj_process_inspection_rty where  id ="
					+ rtyID;
			ResultSet rss = dbConn.query(sqql);
			while (rss.next()) {
				quantity_production_number = Integer.valueOf(rss.getString(1));
				bad_number = Integer.valueOf(rss.getString(2)) - bad_number;
				double rtys = (Double.valueOf(quantity_production_number) - Double.valueOf(bad_number))
						/ Double.valueOf(quantity_production_number);
				rtys = (double) Math.round(rtys * 100) / 100;
				sqlInsert = "update cyj_process_inspection_rty set bad_number=" + bad_number + ", rty=" + rtys
						+ " WHERE id=" + rtyID;
			}
			dbConn.queryUpdateUserLogin(sqlInsert);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 
	 * @param request
	 * @param jsonData
	 * @return 删除不良记录
	 */
	@RequestMapping(value = "deleteDeleteBadRecordSheet", method = RequestMethod.POST)
	@ResponseBody
	public String getDeleteBadRecordSheetJson(HttpServletRequest request, @RequestBody JSONObject jsonData) {
		try {
			DeleteData data = (DeleteData) JSONObject.toBean(jsonData, DeleteData.class);
			JSONArray idValues = JSONArray.fromObject(data.getIdValue());
			String delString = idValues.toString().replace("[", "(").replace("]", ")");
			ResultSet rs = dbConn.query(String.format("select exists (select * from cyj_defect_records where record_sheet_id in %s)" + 
					"",delString));
			if (rs.next()) {
				if (rs.getBoolean(1)) {
				  return message.getErrorInfo("删除失败，该不良记录下有数据");
				}
			}
			//if (!getDelDefectRecords(delString)) {
			//	return message.getErrorInfo("删除失败,它的缺陷记录无法删除");
			//}
			String str = jsonData.getString("idValue").toString();
			updateInspectionRtyDel(str);

			if (helper.dataDelete(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getIdName(), jsonData.getString("idValue"))) {
				return message.getSuccessInfo("删除成功");
			} else {
				return message.getErrorInfo("删除失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("删除失败");

	}

	/**
	 * 
	 * @return 删除跟不良记录没有关联的缺陷记录
	 */
	public boolean getDelDefectRecords(String id) {

		String sqql = "select 1 as id from  cyj_defect_records where  record_sheet_id in " + id;
		ResultSet rss = dbConn.query(sqql);
		boolean isdel = false;
		try {
			int stid = 0;
			while (rss.next()) {
				stid = Integer.valueOf(rss.getString("id"));
			}
			if (stid == 1) {
				String sql = "delete from cyj_defect_records where record_sheet_id in" + id;
				isdel = dbConn.queryUpdateUserLogin(sql);
			} else {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isdel;
	}

	/**
	 * 
	 * @param request
	 * @param jsonData
	 * @return 修改不良记录
	 */
	@RequestMapping(value = "editBadRecordSheet", method = RequestMethod.POST)
	@ResponseBody
	public String getEditBadRecordSheet(HttpServletRequest request, @RequestBody JSONObject jsonData) {
		try {
			EditData data = (EditData) JSONObject.toBean(jsonData, EditData.class);
			JSONObject json = data.getFormData();
			if(!updateInspectionRtyAll(json)) {
				return message.getErrorInfo("修改失败,不良数不能大于实收数量");
			}
			if (helper.dataUpdate(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData(), data.getId())) {
				//updateInspectionRtyAll(data.getFormData().getString("inspection_rty_id"));
				return message.getSuccessInfo("修改成功");
			} else {
				return message.getErrorInfo("修改失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("修改失败");
	}

	/**
	 * 
	 * @param request      CK 新增缺陷记录
	 * @param jsonSaveData
	 * @return
	 */
	@RequestMapping(value = "addDefectRecords", method = RequestMethod.POST)
	@ResponseBody
	public String addDefectRecords(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		try {
			InsertData data = (InsertData) JSONObject.toBean(jsonSaveData, InsertData.class);
			JSONObject json = data.getFormData();
			if (helper.dataInsert(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData())) {
				updateInspectionRtyAndBad(json.getString("record_sheet_id"));
				return message.getSuccessInfo("新增成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("新增失败");

	}
	
	@SuppressWarnings("static-access")
	@RequestMapping(value = "fileImage", method = RequestMethod.POST)
	@ResponseBody
	public String fileImage(MultipartHttpServletRequest request) {
		MultipartFile file = request.getFile("file");
		String fileName = null;
		// 文件不大于1M
		if (fileUitl.checkFileSize(file.getSize(), 1, "M")) {
			return message.getErrorInfo("上传失败上传文件不能超过1M");
		} else {
			String uuid = UUID.randomUUID().toString().trim();
			try {
				fileName = file.getOriginalFilename();// 原始
				int index = fileName.indexOf(".");
				fileName = uuid + fileName.substring(index);
				if (!fileUitl.saveFiles(request,fileName, file)) {
					return message.getErrorInfo("上传失败1");
				}

			} catch (Exception e) {
				e.printStackTrace();
				return message.getErrorInfo("上传失败2");
			}
		}
		return dbConn.getPropertiesYun("fileUrl")+fileName;
	}

	/**
	 * 
	 * @param stid 新增缺陷记录时，修改不良记录的缺陷数与直通率的缺陷数
	 * @return
	 */
	public boolean updateInspectionRtyAndBad(String stid) {
		String str = "select sum(number_of_defects) from cyj_defect_records where record_sheet_id=" + stid+" GROUP BY  record_sheet_id=" + stid;
		ResultSet rs = dbConn.query(str);
		boolean isUpdateRecord = false;
		try {
			while (rs.next()) {
				String sqlInsert = "update cyj_bad_record_sheet set number_of_defects=" + rs.getString(1) + " where id="
						+ stid;
				isUpdateRecord = dbConn.queryUpdateUserLogin(sqlInsert);
				String str1 = "select inspection_rty_id from cyj_bad_record_sheet where id=" + stid + " limit 1";

				ResultSet rs1 = dbConn.query(str1);
				while (rs1.next()) {
					String strs = "select sum(number_of_defects) from cyj_bad_record_sheet where inspection_rty_id="+ rs1.getString(1)+" GROUP BY  inspection_rty_id="
							+ rs1.getString(1);
					ResultSet rss = dbConn.query(strs);
					while (rss.next()) {
						String sqlInsert1 = "update cyj_process_inspection_rty set number_of_defects="
								+ rss.getString(1) + " where id=" + rs1.getString(1);
						isUpdateRecord = dbConn.queryUpdateUserLogin(sqlInsert1);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isUpdateRecord;
	}

	/**
	 * 
	 * @param request
	 * @param jsonData
	 * @return 删除缺陷记录
	 */
	@RequestMapping(value = "deleteDefectRecords", method = RequestMethod.POST)
	@ResponseBody
	public String getDeleteDefectRecordsJson(HttpServletRequest request, @RequestBody JSONObject jsonData) {
		try {
			DeleteData data = (DeleteData) JSONObject.toBean(jsonData, DeleteData.class);
			updateInspectionRtyAndBads(jsonData.getString("idValue"));
			if (helper.dataDelete(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getIdName(), jsonData.getString("idValue"))) {
				return message.getSuccessInfo("删除成功");
			} else {
				return message.getErrorInfo("删除失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("删除失败");
	}

	/**
	 * 
	 * @param tid
	 * @return 删除缺陷记录时修改直通率和不良记录的数据
	 */
	public boolean updateInspectionRtyAndBads(String tid) {

		JSONArray idValues = JSONArray.fromObject(tid);
		ResultSet rs = null;
		int number_of_defects = 0;
		String rtyID = "";
		boolean isUpdateRecord = false;
		try {
			String delString = idValues.toString().replace("[", "(").replace("]", ")");
			rs = dbConn
					.query("SELECT number_of_defects,record_sheet_id from cyj_defect_records where id in" + delString);
			while (rs.next()) {
				number_of_defects = number_of_defects + Integer.valueOf(rs.getString(1));
				rtyID = rs.getString(2);
			}
			String sqlInsert = null;
			String sqql = "select number_of_defects,inspection_rty_id from  cyj_bad_record_sheet where  id =" + rtyID;
			ResultSet rss = dbConn.query(sqql);
			while (rss.next()) {
				number_of_defects = Integer.valueOf(rss.getString(1)) - number_of_defects;
				sqlInsert = "update cyj_bad_record_sheet set number_of_defects=" + number_of_defects + " where id="
						+ rtyID;
				dbConn.queryUpdateUserLogin(sqlInsert);
				String strs = "select sum(number_of_defects) from cyj_bad_record_sheet where inspection_rty_id=" + rss.getString(2)+"GROUP BY  inspection_rty_id="
						+ rss.getString(2);
				ResultSet rss1 = dbConn.query(strs);
				while (rss1.next()) {
					String sqlInsert1 = "update cyj_process_inspection_rty set number_of_defects=" + rss1.getString(1)
							+ " where id=" + rss.getString(2);
					isUpdateRecord = dbConn.queryUpdateUserLogin(sqlInsert1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isUpdateRecord;
	}

	/**
	 * 
	 * @param request
	 * @param jsonData
	 * @return 修改缺陷记录
	 */
	@RequestMapping(value = "editDefectRecords", method = RequestMethod.POST)
	@ResponseBody
	public String getEditDefectRecordsJson(HttpServletRequest request, @RequestBody JSONObject jsonData) {
		try {
			EditData data = (EditData) JSONObject.toBean(jsonData, EditData.class);
			if (helper.dataUpdate(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData(), data.getId())) {
				updateInspectionRtyAndBad(data.getFormData().getString("record_sheet_id"));
				return message.getSuccessInfo("修改成功");
			} else {
				return message.getErrorInfo("修改失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("修改失败");
	}
	
	
}
