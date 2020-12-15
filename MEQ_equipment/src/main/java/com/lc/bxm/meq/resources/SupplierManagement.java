package com.lc.bxm.meq.resources;

import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.common.util.TreeDateUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.entity.DeleteData;
import com.lc.bxm.entity.EditData;
import com.lc.bxm.entity.InsertData;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 供应商管理
 * @author liuhao
 *
 */

@RestController
@RequestMapping("/supplier")
public class SupplierManagement {
	
	@Autowired
	PostgreSQLConn dbConn;

	@Autowired
	Message message;

	@Autowired
	GetLogs getLogs;

	@Autowired
	PostgresqlHelper helper;
	
	@Autowired
	DataList dataList;
	
	@Autowired 
	TreeDateUtil tree;
	
	

	/**
	 * 新增分类
	 * 
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "addSupplierCategory", method = RequestMethod.POST)
	@ResponseBody
	public String addSupplierCategory(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			InsertData data = (InsertData) JSONObject.toBean(jsonObject, InsertData.class);
				ResultSet rs = dbConn.query(
						String.format("SELECT EXISTS (SELECT 1 FROM "+data.getTableName()+" WHERE category_name = '%s')",
								data.getFormData().getString("category_name")));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,该类别名称已存在");
				}
			}
			if (helper.dataInsert(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData())) {
				return message.getSuccessInfo("新增成功");
			} else {
				return message.getErrorInfo("新增失败");
			}
		} catch (Exception e) {
			return message.getErrorInfo("新增失败");
		}
	}

	/**
	 * 修改分类
	 * 
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "editSupplierCategory", method = RequestMethod.POST)
	@ResponseBody
	public String editSupplierCategory(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			EditData data = (EditData) JSONObject.toBean(jsonObject, EditData.class);
				ResultSet rs = dbConn.query(
						String.format("SELECT EXISTS (SELECT 1 FROM "+data.getTableName()+" WHERE category_name = '%s' AND id <> %s)",
								data.getFormData().getString("category_name"),data.getId()));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("修改失败,该类别名称已存在");
					}
				}
			if (helper.dataUpdate(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData(), data.getFormData().getString("id"))) {
				return message.getSuccessInfo("修改成功");
			} else {
				return message.getErrorInfo("修改失败");
			}
		} catch (Exception e) {
			return message.getErrorInfo("修改失败");
		}
	}
	
	/**
	 * 删除分类
	 * 
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "deleteSupplierCategory", method = RequestMethod.POST)
	@ResponseBody
	public String deleteSupplierCategory(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			DeleteData data = (DeleteData) JSONObject.toBean(jsonObject, DeleteData.class);
				ResultSet rs = dbConn.query(
						String.format("SELECT EXISTS (SELECT 1 FROM meq_supplier supp" + 
								" join meq_supplier_category cg on cg.id = supp.kind_id  WHERE cg.id = %s)",data.getIdValue()));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("删除失败,该分类下有供应商");
					}
			}
			if (helper.dataDelete(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(), data.getIdName(), data.getIdValue().toString())){
				return message.getSuccessInfo("删除成功");
			} else {
				return message.getErrorInfo("删除失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("删除失败");
		}
	}
	
	
	/**
	 * 新增供应商
	 * 
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "addSupplier", method = RequestMethod.POST)
	@ResponseBody
	public String addSupplier(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			InsertData data = (InsertData) JSONObject.toBean(jsonObject, InsertData.class);
				ResultSet rs = dbConn.query(
						String.format("SELECT EXISTS (SELECT 1 FROM "+data.getTableName()+" WHERE supplier_code = '%s')",
								data.getFormData().getString("supplier_code")));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,该供应商已存在");
				}
			}
			if (helper.dataInsert(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData())) {
				return message.getSuccessInfo("新增成功");
			} else {
				return message.getErrorInfo("新增失败");
			}
		} catch (Exception e) {
			return message.getErrorInfo("新增失败");
		}
	}

	/**
	 * 修改供应商
	 * 
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "editSupplier", method = RequestMethod.POST)
	@ResponseBody
	public String edidSupplier(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			EditData data = (EditData) JSONObject.toBean(jsonObject, EditData.class);
				ResultSet rs = dbConn.query(
						String.format("SELECT EXISTS (SELECT 1 FROM "+data.getTableName()+" WHERE supplier_code = '%s' AND id <> %s)",
								data.getFormData().getString("supplier_code"),data.getFormData().getString("id")));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("修改失败,该供应商已存在");
					}
				}
			if (helper.dataUpdate(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData(), data.getFormData().getString("id"))) {
				return message.getSuccessInfo("修改成功");
			} else {
				return message.getErrorInfo("修改失败");
			}
		} catch (Exception e) {
			return message.getErrorInfo("修改失败");
		}
	}
	
	/**
	 * 删除供应商
	 * 
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "deleteSupplier", method = RequestMethod.POST)
	@ResponseBody
	public String deleteSupplier(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			ResultSet rs =null;
			DeleteData data = (DeleteData) JSONObject.toBean(jsonObject, DeleteData.class);
			JSONArray json = JSONArray.fromObject(jsonObject.get("idValue"));
			for (int i = 0; i < json.size(); i++) {
			rs = dbConn.query(
						String.format("select exists (select 1 from meq_supplier supp join meq_supplier_assess ass" + 
								" on supp.id = ass.supplier_id where supp.id = "+json.get(i)+")"));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("删除失败,该供应商已被评审");
					}
			      }
				rs = dbConn.query(
						String.format("select exists (	select 1 from meq_supplier supp	join meq_supplier_grade gra" + 
								" on supp.id = gra.supplier_id where supp.id = "+json.get(i)+")"));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("删除失败,该供应商已被评分");
					}
				}
			}
			if (helper.dataDelete(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(), data.getIdName(),json.toString() )){
				return message.getSuccessInfo("删除成功");
			} else {
				return message.getErrorInfo("删除失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("删除失败");
		}
	}
	
	/**
	 * JF 获取产品类别树状图JSON
	 */
	@RequestMapping(value = "supplierCategoryList", method = RequestMethod.GET)
 	@ResponseBody
 	public String prodMenuJson() {
		ResultSet rs = dbConn.query("select id,category_name, category_parent_id from  meq_supplier_category ");
		List<Map<Object, Object>> prodJson= tree.getResultSet(rs);
//		String deptJson = getDeptMenuGroupJson("");
		JSONObject json = new JSONObject();
		json.put("id", "0");
		json.put("name", "全部");
		json.put("children", prodJson);
		return "["+json+"]";
 	}
    

	/**
	 * JF 部门树状图表格分页
	 */
	@RequestMapping(value = "supplierList", method = RequestMethod.GET)
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
		filterString = filterString +" and kind_id in"+sb.toString();
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
	
 	private String getId(String id) {
		StringBuilder sb = new StringBuilder();
		String sql = "select id from meq_supplier_category where category_parent_id = '" + id + "'";
		ResultSet rs = dbConn.query(sql);
		try {
			while(rs.next()) {
				sb.append(","+rs.getString(1));
				sb.append(getId(rs.getString(1)));
			}
		} catch (SQLException e) {}
		return sb.toString();
	}
}
