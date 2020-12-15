package com.lc.bxm.cyjq.resources.featurepack;

import java.awt.image.BufferedImage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.meq.resources.FileResources;

import net.sf.json.JSONObject;

/**
 * MEQv1.0工艺模板接口
 * @author LH
 * @date 2020年5月25日
 */
@RestController
@RequestMapping("/packCommon")
public class ProcessTemplateResources {
	

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;
	@Autowired
	FileResources fileResources ;

	static FileUtil fileUitl = new FileUtil();
	
	/**
	 * LH 获取产品检验属性树状图JSON
	 */
	@RequestMapping(value = "templateMenu", method = RequestMethod.GET)
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
 	 * LH 拼接产品检验属性分组JSON
 	 */
 	private String getProcessMenuGroupJson(String parentId) {
		ResultSet rs = dbConn.query("select process_id, process_name from meq_process_template");
 		StringBuilder sb = new StringBuilder();
 		try {
 			while (rs.next()) {
				sb.append("{\"id\":\"");
				sb.append(rs.getString(1));
				sb.append("\",\"name\":\"");
				sb.append(rs.getString(2));
			    sb.append("\",\"children\":[]");
 				sb.append("},");
 			}
		} catch (SQLException e) {}
 		return Str.delComma(sb.toString());
 	}	
 	
 	/**
	 * 将PDF转换为多张图片
	 */
	@RequestMapping(value = "importPdf", method = RequestMethod.POST)
	@ResponseBody
	public String pdfpng(MultipartHttpServletRequest request) {
		String process_id = request.getParameter("process_id");
		MultipartFile multFile = request.getFile("file");
	
		File file = new File(dbConn.getPropertiesYun("CreateFilePath")+"/"+multFile.getOriginalFilename());
		// 格式化时间
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
		Connection conn = null;
		try {
			if(!file.exists()) {
				multFile.transferTo(file);
			}
			PDDocument doc = PDDocument.load(file);
			PDFRenderer renderer = new PDFRenderer(doc);
			int pageCount = doc.getNumberOfPages();
			if (pageCount > 100) {
				return message.getErrorInfo("导入文件页数过多,导入失败");
			}
			PreparedStatement ps = null;
			conn = dbConn.getConn();
			String sql = "insert into meq_process_template_details(process_id,file_name,file_path) values(?,?,?)";
			for (int i = 0; i < pageCount; i++) {
				// 获取当前时间并作为时间戳
				String timeStamp = simpleDateFormat.format(new Date());
				BufferedImage image = renderer.renderImageWithDPI(i, 144);
				ImageIO.write(image, "png", new File(dbConn.getPropertiesYun("CreateFilePath")+"/"+timeStamp + ".png"));
//				File filePDF= new File(dbConn.getPropertiesYun("CreateFilePath")+"/"+timeStamp + ".png");
				ps = conn.prepareStatement(sql);
				ps.setInt(1, Integer.parseInt(process_id));
				ps.setString(2, timeStamp + ".png");
				ps.setString(3, dbConn.getPropertiesYun("fileUrl")+timeStamp + ".png");// 查看文件路径
				ps.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("导入失败");
		}finally {
			if (conn!=null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return message.getSuccessInfo("导入成功");
	}
	
	
	/**
	 * lh 工艺模板导入图片
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value = "importImage", method = RequestMethod.POST)
	  @ResponseBody
	  public String importImage(MultipartHttpServletRequest request) {
		 String sql = "insert into meq_process_template_details(process_id,file_name,file_path) values(?,?,?)";
		    String process_id = request.getParameter("process_id");
		    PreparedStatement ps = null;
		    Connection conn = null;
		    String ImagePath = fileResources.fileImage(request,dbConn.getPropertiesYun("FilePath_DB"));
		    try {
		    	if(ImagePath.contains("上传失败")) {
					return message.getErrorInfo("导入失败");
				}
		        conn = dbConn.getConn();
		        ps = conn.prepareStatement(sql);
		        ps.setInt(1, Integer.parseInt(process_id));
		        ps.setString(2, ImagePath);
		        ps.setString(3, dbConn.getPropertiesYun("fileUrl")+ImagePath);
		        ps.executeUpdate();
		    } catch(SQLException e) {
		    	
		    }finally {
				if (conn!=null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		    return  message.getSuccessInfo("导入成功");
	 }

}
