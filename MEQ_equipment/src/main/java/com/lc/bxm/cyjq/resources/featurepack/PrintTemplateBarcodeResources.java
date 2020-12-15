package com.lc.bxm.cyjq.resources.featurepack;

import java.io.IOException;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONObject;

/**
 * 打印模板管理
 * @author liuhao
 * @date 2020年8月6日
 */
@RestController
@RequestMapping("/printBarTemp")
public class PrintTemplateBarcodeResources {
	
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
	String fileName =null;
	
	
	
	/**
 	 * LH 打印条码模板文件上传
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
	 * 打印条码模块新增
	 * @param request
	 * @param jsonSaveData
	 * @return
	 */
	@RequestMapping(value = "printBarTempAdd", method = RequestMethod.POST)
	@ResponseBody
	public String packfilesadd(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		String jsonObject = jsonSaveData.getString("formData");
		JSONObject json = JSONObject.fromObject(jsonObject);
        if (getSerialNumExists(0,json.getString("serial_num"),0)) {
        	return message.getErrorInfo("添加失败该流水号已存在");
		}
         if (getLineExists(0,json.getInt("workshop"),json.getInt("line_id"),0)) {
			return message.getErrorInfo("添加失败同一车间下产线不能相同");
		 }
		String sql = "INSERT INTO meq_print_template_barcode(" + 
				"serial_num, workshop, line_id, template_name, template_file, create_user, remark,file_size)" + 
				"VALUES ( ?, ?, ?, ?, ?, ?, ?,?);";
		PreparedStatement ps = null;
		Connection conn = null;
		  UUID uuid=UUID.fromString(json.getString("create_user"));
		try {
			conn = dbConn.getConn();
			ps = conn.prepareStatement(sql);
			ps.setString(1, json.getString("serial_num"));
			ps.setLong(2, json.getInt("workshop"));
			ps.setLong(3, json.getInt("line_id"));
			ps.setString(4, fileName);
			ps.setBinaryStream(5, is, len);
			ps.setObject(6, uuid);
			ps.setString(7, (json.getString("remark").equals("null"))?"":json.getString("remark"));
		    ps.setLong(8,len);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
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
		fileName = null;
		is=null;
		len=0;
		return message.getSuccessInfo("添加成功");
	}
	
	/**
	 * 打印条码模块编辑
	 * @param request
	 * @param jsonSaveData
	 * @return
	 */
	@RequestMapping(value = "printBarTempEdit", method = RequestMethod.POST)
	@ResponseBody
	public String packfilesedit(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		String jsonObject = jsonSaveData.getString("formData");
		JSONObject json = JSONObject.fromObject(jsonObject);
		if (getSerialNumExists(1,json.getString("serial_num"),json.getInt("template_id"))) {
        	return message.getErrorInfo("修改失败该流水号已存在");
		}
		if (getLineExists(1,json.getInt("workshop"),json.getInt("line_id"),json.getInt("template_id"))) {
			return message.getErrorInfo("修改失败同一车间下产线不能相同");
		 }
		String sql=null;
		PreparedStatement ps = null;
		Connection conn = null;
		try {
			conn = dbConn.getConn();
			if (json.getBoolean("file")) {
				 sql = "UPDATE public.meq_print_template_barcode SET  serial_num=?, workshop=?, line_id=?, "
				 		+ "template_name=?, template_file=?, remark=?, file_size=? WHERE template_id=? ";
				    ps = conn.prepareStatement(sql);
					ps.setString(1, json.getString("serial_num"));
					ps.setLong(2, json.getInt("workshop"));
					ps.setLong(3, json.getInt("line_id"));
					ps.setString(4, fileName);
					ps.setBinaryStream(5, is, len);
					ps.setString(6, (json.getString("remark").equals("null"))?"":json.getString("remark"));
					ps.setLong(7,len);
					ps.setLong(8, json.getInt("template_id"));
			}else {
				 sql = "UPDATE public.meq_print_template_barcode SET serial_num=?, workshop=?, line_id=?,remark=? WHERE template_id=?";
				    ps = conn.prepareStatement(sql);
					ps.setString(1, json.getString("serial_num"));
					ps.setLong(2, json.getInt("workshop"));
					ps.setLong(3, json.getInt("line_id"));
					ps.setString(4, (json.getString("remark").equals("null"))?"":json.getString("remark"));
					ps.setLong(5, json.getInt("template_id"));
			}
			ps.executeUpdate();
		} catch (Exception e) {
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
		fileName = null;
		is=null;
		len=0;
		return message.getSuccessInfo("修改成功");
	}
	
	/**
	 * 根据车间获取该车间下的产线
	 * @param workshop
	 * @return
	 */
 	@RequestMapping(value = "getLine", method = RequestMethod.GET)
	@ResponseBody 
	public List<Map<Object, Object>> getLine(Integer workshop) {
 		ResultSet rs = dbConn.query(String.format("select line_id , line_name from  meq_production_lines where  dept_id=%s",workshop));
 		List<Map<Object, Object>> list = new ArrayList<Map<Object,Object>>();
 		Map<Object, Object> map = null;
 		try {
			while (rs.next()) {
				map = new HashMap<Object, Object>();
				map.put("id", rs.getInt(1));
				map.put("value", rs.getString(2));
				list.add(map);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	   return list;
	}
 	
 	/**
 	 * 新增和编辑是验证流水号是否存在
 	 * @param state
 	 * @param serial_num
 	 * @param template_id
 	 * @return
 	 */
 	public boolean getSerialNumExists(Integer state, String serial_num, Integer template_id ) {
 		ResultSet rs = null;
 		if (state==0) {
 			rs=dbConn.query(String.format("select exists (select serial_num from  meq_print_template_barcode where serial_num ='%s')"
 					,serial_num));
 		}else {
 	 		rs=dbConn.query(String.format("select exists (select serial_num from  meq_print_template_barcode "
 	 				+ "where serial_num ='%s' and template_id!=%s)",serial_num, template_id));
		}
 		try {
			if(rs.next()) {
			 if(rs.getBoolean(1)) {
				return rs.getBoolean(1);
			 }
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	 return false;
 	}
 	
 	/**
 	 * 新增和编辑时验证车间产线是否存在
 	 * @param state
 	 * @param workshop
 	 * @param line_id
 	 * @param template_id
 	 * @return
 	 */
 	public boolean getLineExists(Integer state, Integer workshop,Integer line_id , Integer template_id ) {
 		ResultSet rs = null;
 		if (state==0) {
 			rs=dbConn.query(String.format("select exists (select line_id from  meq_print_template_barcode where workshop =%s and line_id =%s)"
 					,workshop,line_id));
 		}else {
 	 		rs=dbConn.query(String.format("select exists (select line_id from  meq_print_template_barcode "
 	 				+ "where workshop =%s and line_id =%s and template_id!=%s)",workshop, line_id, template_id));
		}
 		try {
			if(rs.next()) {
			 if(rs.getBoolean(1)) {
				return rs.getBoolean(1);
			 }
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	 return false;
 	}
 	
}
