package com.lc.bxm.meq.resources;
import java.io.BufferedOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import net.sf.json.JSONObject;
import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.FileUtil;
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.dbconnection.PostgreSQLConn;


/**
 * 检查模板
 * @author lh
 *
 */
@RestController
@RequestMapping("/check")
public class CheckResources {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;
	
	@SuppressWarnings("unused")
	private static String fiePath = null;
		
	/**
	 * 检测录入获取模板
	 * @param parm
	 * @param folder
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "getTemplate", method = RequestMethod.POST)
	@ResponseBody
	public void getTemplate(HttpServletRequest request,HttpServletResponse response, @RequestBody String fileCode){
		    try {
				FileUtil fileUtil = new FileUtil();
				String fileName = getfilePath(fileCode);
				 //String fileName = "template/"+fileCode+".xlsx";
				fileUtil.fileDownloadTemplate(response, fileName);
			} catch (Exception e) {
				e.printStackTrace();
			}

	}	
	
	/**
	 * lh 根据编号查找模板路径
	 * @param template
	 * @return
	 */
	public String getfilePath(String fileCode) {
		ResultSet rs = dbConn.query(String.format("select setting_value from sys_settings  where setting_code='%s'", fileCode));
		String templatePath = null;
		try {
			if (rs.next()) {
				templatePath = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return templatePath;
	}
	
	/**
	 * lh 检查数据保存;
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "addCheckData" , method = RequestMethod.POST )
	@ResponseBody
	public String addCheckData(MultipartHttpServletRequest request) {
		MultipartFile file = request.getFile("file");
		String  formData = request.getParameter("formData");
		String  table = request.getParameter("tableName");
		JSONObject jsonObject = JSONObject.fromObject(formData);
		if (table.equals("sys_cpk")) {
			UUID uuid=UUID.fromString(jsonObject.getString("create_name"));
			String sql = "INSERT INTO  sys_cpk(cpk_type, create_name, file_data, cpk_name)VALUES (?, ?, ?, ?)";
			PreparedStatement ps = null;
			Connection conn = null;
				try {
					conn = dbConn.getConn();
					ps = conn.prepareStatement(sql);
					InputStream is= file.getInputStream();
					int len = is.available();
					ps.setInt(1, jsonObject.getInt("cpk_type"));
					ps.setObject(2,uuid);
					ps.setBinaryStream(3, is, len);
					ps.setString(4,jsonObject.getString("cpk_name"));
				    ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				return message.getErrorInfo("新增失败");
			}finally {
				if (conn!=null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}else {
			UUID uuid=UUID.fromString(jsonObject.getString("creator"));
			String sql = "INSERT INTO meq_check_data_input (barcode, line_id, creator, file_data , check_code)VALUES (?, ?, ?, ?, ?)";
			PreparedStatement ps = null;
			Connection conn = null;
				try {
					conn = dbConn.getConn();
					ps = conn.prepareStatement(sql);
					InputStream is= file.getInputStream();
					int len = is.available();
					ps.setString(1, jsonObject.getString("barcode"));
					ps.setInt(2, jsonObject.getInt("line_id"));
					ps.setObject(3,uuid);
					ps.setBinaryStream(4, is, len);
					ps.setString(5, jsonObject.getString("check_code"));
				    ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				return message.getErrorInfo("新增失败");
			}finally {
				if (conn!=null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return message.getSuccessInfo("新增成功");
   }
	
	
	/**
	 * 根据check_id查询检查数据文件
	 * @param check_id
	 * @return
	 */
	@RequestMapping(value = "queyCheckData" , method = RequestMethod.POST)
	@ResponseBody
	public void queryData(HttpServletRequest request , HttpServletResponse response , @RequestBody String check_id) {
		ResultSet  rs =null;
		if (check_id.contains("cpk")) {
			String id = check_id.substring(4);
			  rs = dbConn.query(String.format("select file_data from sys_cpk  where id = %s", id));
		}else {
			  rs = dbConn.query(String.format("select file_data from meq_check_data_input  where check_id = %s", check_id));
		}
		try {
			if (rs.next()) {
				byte [] fileData = rs.getBytes(1);
				BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream()); 
				InputStream is = new ByteArrayInputStream(fileData);
				 byte[] buff = new byte[1024*10];
			        int len = 0;
			        while((len = is.read(buff))!=-1){
			            out.write(buff, 0, len);   
			        }
			        is.close();
			        out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * lh 检查数据录入编辑
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "editCheckData",method = RequestMethod.POST)
	@ResponseBody
	public String editCheckData(MultipartHttpServletRequest request) {
		MultipartFile file = request.getFile("file");
		String  formData = request.getParameter("formData");
		String  table = request.getParameter("tableName");
		JSONObject jsonObject = JSONObject.fromObject(formData);
       if (table.equals("sys_cpk")) {
   		  String sql = "UPDATE sys_cpk SET file_data=? ,cpk_name =? WHERE id =?";
   		PreparedStatement ps = null;
   		Connection conn = null;
   			try {
   				conn = dbConn.getConn();
   				ps = conn.prepareStatement(sql);
   				InputStream is= file.getInputStream();
   				int len = is.available();
   				ps.setBinaryStream(1, is, len);
   				ps.setString(2,jsonObject.getString("cpk_name"));
   				ps.setInt(3, jsonObject.getInt("id"));
   			    ps.executeUpdate();
   		} catch (Exception e) {
   			return message.getErrorInfo("修改失败");
   		}finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		}else {
			UUID uuid=UUID.fromString(jsonObject.getString("creator"));
			String sql = "UPDATE meq_check_data_input SET barcode=?, line_id=?, creator=?, file_data=? , check_code=? WHERE check_id =?";
			PreparedStatement ps = null;
			Connection conn = null;
				try {
					conn = dbConn.getConn();
					ps = conn.prepareStatement(sql);
					InputStream is= file.getInputStream();
					int len = is.available();
					ps.setString(1, jsonObject.getString("barcode"));
					ps.setInt(2, jsonObject.getInt("line_id"));
					ps.setObject(3,uuid);
					ps.setBinaryStream(4, is, len);
					ps.setString(5, jsonObject.getString("check_code"));
					ps.setInt(6, jsonObject.getInt("check_id"));
				    ps.executeUpdate();
			} catch (Exception e) {
				return message.getErrorInfo("修改失败");
			}finally {
				if (conn!=null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return message.getSuccessInfo("修改成功");
    }
	
	
	/**
	 * 文件上传
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "fileUpload",method = RequestMethod.POST)
	@ResponseBody
	public String fileUpload(MultipartHttpServletRequest request) {
		try {
			MultipartFile file = request.getFile("file");
			FileUtil fileUtil = new FileUtil();
			String fileName = request.getParameter("fileCode");
			if(fileUtil.saveFileTem(request, "template" , fileName , file)) {
				return message.getSuccessInfo("上传成功");
			}
		} catch (Exception e) {
           e.printStackTrace();
        }
		return message.getErrorInfo("上传失败");
	}
	
	
	/**
	 * 文件下载
	 * @param request
	 * @param response
	 * @param fileCode
	 * @return
	 */
	@RequestMapping(value = "fileDownload",method = RequestMethod.POST)
	@ResponseBody
	public String fileDownload(HttpServletRequest request,HttpServletResponse response ,@RequestBody String fileCode) {
		try {
			FileUtil fileUtil = new FileUtil();
			String fileName = getfilePath(fileCode);
			fileUtil.fileDownloadTemplate(response, fileName);
			return message.getSuccessInfo("下载成功");
		} catch (Exception e) {
           e.printStackTrace();
        }
		return message.getErrorInfo("下载失败");
	}
	
	
}
   
