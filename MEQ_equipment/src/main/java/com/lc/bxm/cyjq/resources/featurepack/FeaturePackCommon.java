package com.lc.bxm.cyjq.resources.featurepack;

import java.io.IOException;


import java.io.InputStream;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.entity.DeleteData;
import com.lc.bxm.entity.EditData;
import com.lc.bxm.entity.InsertData;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * MEQV1.0通用CRUD接口
 * @author LH
 * @date 2020年5月25日
 */
@RestController
@RequestMapping("/packCommon")
public class FeaturePackCommon {
	

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;
	//文件流
    InputStream is =null;
	//文件大小
	int len =0;
	//文件名称
	String fileName;
	
	
	
	/**
	 * LH 通用新增接口
	 */
	@RequestMapping(value = "addJson", method = RequestMethod.POST)
	@ResponseBody
	public String addJson(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		try {
			InsertData data = (InsertData) JSONObject.toBean(jsonSaveData, InsertData.class);
			if (data.getTableName().equals("meq_inspect_process_details")) {
				ResultSet shutdown = dbConn.query(String.format(
						"SELECT exists(select * from  meq_inspect_process_details where station_num = %s and inspect_id = %s)",
						data.getFormData().getString("station_num"),data.getFormData().getString("inspect_id")));
				if (shutdown.next()) {
					if (shutdown.getBoolean(1)) {
						return message.getErrorInfo("新增失败,工位序号不能重复");
					}
				}
			}
			Object str=helper.dataInsertCyjq(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData());
			if (str.equals(true)) {
				return message.getSuccessInfo("新增成功");
			}else if (str.toString().contains("唯一约束")) {
				return message.getErrorInfo("新增失败该数据已存在，数据名称或编号重复");
			}
		} catch (Exception e) {
			return message.getErrorInfo("新增失败，系统内部出现问题"+e.getMessage());
		}
		return message.getErrorInfo("新增失败");
	}
	/**
	 * LH 通用删除接口
	 */
	@RequestMapping(value = "delJson", method = RequestMethod.POST)
	@ResponseBody
	public String delJson(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		try {
			DeleteData data = (DeleteData) JSONObject.toBean(jsonSaveData, DeleteData.class);
			JSONArray idValues =JSONArray.fromObject(data.getIdValue());
			String delString =idValues.toString().replace("[", "(").replace("]", ")");
			if (data.getTableName().equals("meq_line_plan_details")) {
				String  sqlStr = String.format("select exists(select detail_id from meq_line_plan_details where is_audited and detail_id in %s)",delString);
				ResultSet rs = dbConn.query(sqlStr);
				if (rs.next()) {
					if (rs.getBoolean(1)) {
					  return message.getErrorInfo("排产已审核，不能删除");
					}
				}
			}
			if (data.getTableName().equals("meq_line_commands_new")) {
				String  sqlStr = String.format("select exists(select rec_id from meq_line_commands_new where audited and rec_id in %s)",delString);
				ResultSet rs = dbConn.query(sqlStr);
				if (rs.next()) {
					if (rs.getBoolean(1)) {
					  return message.getErrorInfo("投产已审核，不能删除");
					}
				}
			}
		     Object str = helper.dataDeleteCyjq(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
						data.getIdName(), jsonSaveData.getString("idValue"));
			if (str.equals(true)) {
				return message.getSuccessInfo("删除成功");
			}else if (str.toString().contains("外键约束")) {
				return message.getErrorInfo("删除失败，该数据已被引用");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("删除失败,系统内部问题"+e.getMessage());
		}
		return message.getErrorInfo("删除失败");
	}
	
	/**
	 * LH 通用编辑接口
	 */
	@RequestMapping(value = "editJson", method = RequestMethod.POST)
	@ResponseBody
	public String editJson(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		try {
			EditData data = (EditData) JSONObject.toBean(jsonSaveData, EditData.class);
			if (data.getTableName().equals("meq_inspect_process_details")) {
				ResultSet shutdown = dbConn.query(String.format(
						"SELECT exists(select * from  meq_inspect_process_details where station_num = %s and inspect_id = %s and inspect_detail_id != %s )",
						data.getFormData().getString("station_num"),data.getFormData().getString("inspect_id"),data.getFormData().getString("inspect_detail_id")));
				if (shutdown.next()) {
					if (shutdown.getBoolean(1)) {
						return message.getErrorInfo("修改失败,工位序号不能重复");
					}
				}
			}
			if (data.getTableName().equals("meq_line_plan_details")) {
				ResultSet shutdown = dbConn.query(String.format(
						"SELECT (close_user IS NULL AND closed_date IS NULL) AS can_edit FROM meq_line_plan_details WHERE detail_id = '%s'",
		                 data.getFormData().getString("detail_id")));
				if (shutdown.next()) {
					if (!shutdown.getBoolean(1)) {
						return message.getErrorInfo("此行已关闭,不能修改");
					}
				}
				}
			if (data.getTableName().equals("meq_line_commands_new")) {
				ResultSet rs = dbConn.query(String.format("select exists(SELECT * from meq_line_commands_new "
						+ "where line_id= %s and barcode_head = '%s' and prior=%s and rec_id != %s and not deleted)",
						data.getFormData().getString("line_id"),data.getFormData().getString("barcode_head"),data.getFormData().getString("prior"),data.getFormData().getString("rec_id")));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("修改失败，同一产线条码前缀相同下优先排序不能重复");
					}
				}
				   rs = dbConn.query(String.format("select all_plan_qty(%s,%s,%s)",
						 data.getFormData().getString("rec_id"),data.getFormData().getString("detail_id"),data.getFormData().getInt("qty")));
				if (rs.next()) {
					if (!rs.getString(1).equals("true")) {
						return message.getErrorInfo(rs.getString(1));
					}
				  }
				}
			Object str=helper.dataUpdate(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData(), data.getId());
			if (str.equals(true)) {
				return message.getSuccessInfo("修改成功");
			}else if (str.toString().contains("唯一约束")) {
				return message.getErrorInfo("修改失败数据已存在，数据名称或编号重复");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("修改失败,系统内部问题"+e.getMessage());
		}
		return message.getErrorInfo("修改失败");
	}
	
	
	/**
	 * LH 获取检验流程树状图JSON
	 */
	@RequestMapping(value = "processMenu", method = RequestMethod.GET)
 	@ResponseBody
 	public String processMenu() {
		String prodJson = getProcessMenuGroupJson("");
		JSONObject json = new JSONObject();
		json.put("id", "0");
		json.put("name", "全部");
		json.put("children", "["+prodJson+"]");
		return "["+json+"]";
 	}
	
	/**
 	 * LH 拼接检验流程JSON
 	 */
 	private String getProcessMenuGroupJson(String parentId) {
		ResultSet rs = dbConn.query("select inspect_id, inspect_code, inspect_name from meq_inspect_process");
 		StringBuilder sb = new StringBuilder();
 		try {
 			while (rs.next()) {
				sb.append("{\"id\":\"");
				sb.append(rs.getString(1));
				sb.append("\",\"code\":\"");
				sb.append(rs.getString(2));
				sb.append("\",\"name\":\"");
				sb.append(rs.getString(3));
			    sb.append("\",\"children\":[]");
 				sb.append("},");
 			}
		} catch (SQLException e) {}
 		return Str.delComma(sb.toString());
 	}	
 	
 	/**
 	 * LH 功能包上传文件
 	 */
 	@RequestMapping(value = "fileUpload", method = RequestMethod.POST)
	@ResponseBody 
	public String fileUpload(MultipartHttpServletRequest request) throws IOException {
	    MultipartFile file = request.getFile("file");
        fileName = file.getOriginalFilename();
	      is =file.getInputStream();
			 len = is.available();
	        if (is!=null) {
	        	return message.getSuccessInfo("上传成功");
			}
	     return message.getErrorInfo("上传失败,请重新上传");
	}
 	
 	
	/**
	 * 功能包添加
	 */
	@RequestMapping(value = "packfilesAdd", method = RequestMethod.POST)
	@ResponseBody
	public String packfilesadd(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		String jsonObject = jsonSaveData.getString("formData");
		JSONObject json = JSONObject.fromObject(jsonObject);
		String sql = "INSERT INTO meq_package_manager(" + 
				" package_name, version_no, package_file, file_size, remark)" + 
				"VALUES (?, ?, ?, ?, ?);";
		PreparedStatement ps = null;
		Connection conn = null;
		try {
			conn = dbConn.getConn();
			ps = conn.prepareStatement(sql);
			ps.setString(1, fileName);
			ps.setString(2, json.getString("version_no"));
			ps.setBinaryStream(3, is, len);
			ps.setLong(4, len);
			ps.setString(5, (json.getString("remark").equals("null"))?"":json.getString("remark"));
			ps.executeUpdate();
		} catch (Exception e) {
			return message.getErrorInfo("添加失败系统内部问题"+e.getMessage());
		}finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return message.getSuccessInfo("添加成功");
	}
	
	/**
	 * 功能包编辑
	 */
	@RequestMapping(value = "packfilesedit", method = RequestMethod.POST)
	@ResponseBody
	public String packfilesedit(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		String jsonObject = jsonSaveData.getString("formData");
		JSONObject json = JSONObject.fromObject(jsonObject);
		String sql=null;
		PreparedStatement ps = null;
		Connection conn = null;
		try {
			conn = dbConn.getConn();
			if (json.getBoolean("file")) {
				 sql = "UPDATE public.meq_package_manager SET package_name=?, version_no=?, package_file=?, file_size=?, remark=?, upload_time= CURRENT_TIMESTAMP WHERE package_id=?";
				    ps = conn.prepareStatement(sql);
				    ps.setString(1, fileName);
					ps.setString(2, json.getString("version_no"));
					ps.setBinaryStream(3, is, len);
					ps.setLong(4, len);
					ps.setString(5, json.getString("remark"));
					ps.setInt(6, json.getInt("package_id"));
			}else {
				 sql = "UPDATE public.meq_package_manager SET version_no=?, remark=?, upload_time= CURRENT_TIMESTAMP  WHERE package_id=?";
				    ps = conn.prepareStatement(sql);
					ps.setString(1, json.getString("version_no"));
					ps.setString(2, json.getString("remark"));
					ps.setInt(3, json.getInt("package_id"));
			}
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
				return message.getErrorInfo("修改失败系统内部问题"+e.getMessage());
		}finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return message.getSuccessInfo("修改成功");
	}
	
	/**
	 * 检验流程根据功能包获取功能包版本号
	 * @param request
	 * @param package_id
	 * @return
	 */
	@RequestMapping(value = "packRemark", method = RequestMethod.GET)
	@ResponseBody
	public String queryPackVersion(HttpServletRequest request, @RequestParam String package_id) {
				ResultSet rs = dbConn.query("select remark from meq_package_manager WHERE package_id="+package_id);
				String remark = null;
			try {
				if (rs.next()) {
					remark = rs.getString(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return remark;
	} 
	
}
