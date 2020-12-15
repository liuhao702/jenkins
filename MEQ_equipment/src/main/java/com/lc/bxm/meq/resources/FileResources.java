package com.lc.bxm.meq.resources;

import java.awt.image.BufferedImage;




import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.FileUtil;
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.common.util.ReadExcel;
import com.lc.bxm.common.util.UploadUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;


/**
 * 文件上传下载
 * 
 * @author JF
 * @date 2019年7月9日
 */
@RestController
@RequestMapping("/file")
public class FileResources {

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	ReadExcel read;
	@Autowired
	Message message;
	
	
	static FileUtil fileUitl = new FileUtil();

	/**
	 * 多文件上传接口
	 */
	@RequestMapping(value = "filesUploadOld", method = RequestMethod.POST)
	@ResponseBody
	public String filesUploadOld(MultipartHttpServletRequest request) {
		String savePath = "http://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath();
		String sql = "insert into meq_process_cards(sop_id,idx,file_name,file_data,file_path,file_size) values(?,?,?,?,?,?)";
		MultipartFile file = request.getFile("file");
		String fileName = file.getOriginalFilename();
		// long fileZise = file.getSize();
		String uuid = UUID.randomUUID().toString();
		String[] str1 = fileName.split("\\.");
		fileName = str1[0] + uuid + "." + str1[1];
		String sopId = request.getParameter("sop_id");
		PreparedStatement ps = null;
		Connection conn = null;
		try {
			conn = dbConn.getConn();
			ps = conn.prepareStatement(sql);
			InputStream is = file.getInputStream();
			int len = is.available();
			ps.setInt(1, Integer.parseInt(sopId));
			ps.setInt(2, 0);
			ps.setString(3, fileName);
			ps.setBinaryStream(4, is, len);
			ps.setString(5, savePath + "/file/getFile?fileName=" + fileName);// 查看文件路径
			ps.setLong(6, len);
			ps.executeUpdate();
		} catch (Exception e) {
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
		fileUpload1(request, fileName);
		return message.getSuccessInfo("导入成功");
	}

	/**
	 * JF 文件上传sys_files
	 */
	public String fileUpload1(MultipartHttpServletRequest request, String fileName) {
		String sql = "insert into sys_files(file_name,file_title,file_content) values(?,?,?)";
		MultipartFile file = request.getFile("file");
		PreparedStatement ps = null;
		Connection conn = null;
		try {
			conn = dbConn.getConn();
			ps = conn.prepareStatement(sql);
			InputStream is = file.getInputStream();
			int len = is.available();
			ps.setString(1, fileName);
			ps.setString(2, fileName);
			ps.setBinaryStream(3, is, len);
			ps.executeUpdate();
		} catch (Exception e) {
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
		return "file/getFile?fileName=" + fileName;
	}

	/**
	 * 将PDF转换为多张图片
	 */
	@RequestMapping(value = "importPdfOld", method = RequestMethod.POST)
	@ResponseBody
	public String pdf2pngOld(MultipartHttpServletRequest request) {
		String sopId = request.getParameter("sop_id");
		MultipartFile multFile = request.getFile("file");
		File file = new File("test.pdf");
		// 格式化时间
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmssSSS");
		String savePath = "http://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath();
		Connection conn = null;
		try {
			multFile.transferTo(file);
			PDDocument doc = PDDocument.load(file);
			PDFRenderer renderer = new PDFRenderer(doc);
			int pageCount = doc.getNumberOfPages();
			if (pageCount > 100) {
				return message.getErrorInfo("导入文件页数过多,导入失败");
			}
			PreparedStatement ps = null;
			conn = dbConn.getConn();
			String sql = "insert into meq_process_cards(sop_id,idx,file_name,file_data,file_path,file_size) values(?,?,?,?,?,?)";
			for (int i = 0; i < pageCount; i++) {
				// 获取当前时间并作为时间戳
				String timeStamp = simpleDateFormat.format(new Date());
				BufferedImage image = renderer.renderImageWithDPI(i, 144);
				ImageIO.write(image, "png", new File(timeStamp + ".png"));
				File fileImg = new File(timeStamp + ".png");
				ps = conn.prepareStatement(sql);
				InputStream is = new FileInputStream(fileImg);
				int len = is.available();
				ps.setInt(1, Integer.parseInt(sopId));
				ps.setInt(2, i + 1);
				ps.setString(3, timeStamp + ".png");
				ps.setBinaryStream(4, is, len);
				ps.setString(5, savePath + "/file/getFile?fileName=" + timeStamp + ".png");// 查看文件路径
				ps.setInt(6, len);
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
	 * JF 文件上传
	*/
	@RequestMapping(value = "fileUpload", method = RequestMethod.POST)
	@ResponseBody 
	public String fileUpload(MultipartHttpServletRequest request) throws IOException {
	    String sql = "insert into sys_files(file_name,file_title,file_path) values(?,?,?)";
//	    MultipartFile file = request.getFile("file");
	    PreparedStatement ps = null;
	    Connection conn = null;
	    String ImagePath = fileImage(request,dbConn.getPropertiesYun("FilePath_DB"));
	    try {
	    	if(ImagePath.contains("上传失败")) {
				return message.getErrorInfo("导入失败");
			}
	        conn = dbConn.getConn();
	        ps = conn.prepareStatement(sql);
	        ps.setString(1, ImagePath);
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
	    return dbConn.getPropertiesYun("fileUrl")+ImagePath;
	}
				 

	/**
	 * JF 预览文件,将文件丢到浏览器上面预览
	 */
	@RequestMapping(value = "getFileOld", method = RequestMethod.GET, produces = "multipart/form-data")
	@ResponseBody
	private void getFileOld(HttpServletRequest request, HttpServletResponse response, @RequestParam String fileName) {
		ResultSet rs = dbConn
				.query("select file_name,file_data from meq_process_cards where file_name = '" + fileName + "'");
		try {
			if (rs.next()) {
				byte[] fileData = rs.getBytes(2);
				BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
				InputStream is = new ByteArrayInputStream(fileData);
				byte[] buff = new byte[1024];
				int len = 0;
				while ((len = is.read(buff)) != -1) {
					out.write(buff, 0, len);
				}
				is.close();
				out.close();
			} else {
				ResultSet cc = dbConn
						.query("select file_name,file_content from sys_files where file_name = '" + fileName + "'");
				if (cc.next()) {
					byte[] fileData = cc.getBytes(2);
					BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
					InputStream is = new ByteArrayInputStream(fileData);
					byte[] buff = new byte[1024];
					int len = 0;
					while ((len = is.read(buff)) != -1) {
						out.write(buff, 0, len);
					}
					is.close();
					out.close();
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 导入Excel数据 , 导入工单
	 */
	@RequestMapping(value = "erpUpload", method = RequestMethod.POST)
	@ResponseBody
	public String erpUpload(MultipartHttpServletRequest request) throws IOException {
		try {
			String linePlanId = request.getParameter("linePlanId");
			MultipartFile file = request.getFile("file");
			if (file == null) {
				return message.getErrorInfo("导入文件为空");
			}
			// 获取文件名称
			String fileName = file.getOriginalFilename();
			// 判断上传的文件是否为excel
			String fileType = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
			if (!"xlsx".equalsIgnoreCase(fileType)) {
				return message.getErrorInfo("导入失败,文件格式不符合要求");
			}
			InputStream input = file.getInputStream();
			List<List<Object>> list = read.readExcel(fileName, input, true);
			// 关闭流
			input.close();
			// 执行数据插入的操作
			String importMessage = addErp(list, linePlanId);
			if (importMessage.equals("导入成功"))
				return message.getSuccessInfo(importMessage);
			else
				return message.getErrorInfo(importMessage);
		} catch (Exception e) {
			return message.getErrorInfo("导入失败,文件内容不符合要求");
		}
	}

	/**
	 * 导入车间日排产数据
	 */
	public String addErp(List<List<Object>> list, String linePlanId) {
		try {
			if (null != list && list.size() > 0) {
				String sqlInsert = null;
				StringBuilder columnString = new StringBuilder();
				StringBuilder valueString = new StringBuilder();
				StringBuilder valueResult = new StringBuilder();
				String temp = null;
				String columnName = null;
				@SuppressWarnings("unused")
				String originalQty = null;
				String productCode = "";
				boolean haveProduct = false;
				ResultSet rs = null;
				ResultSet rsNone = null;
				if (list.size() < 2)
					return "导入失败,文档没有数据";
				for (int j = 0; j < list.size(); j++) {
					haveProduct = false;
					valueString.setLength(0);
					List<Object> lo = list.get(j);
					for (int k = 0; k < lo.size(); k++) {
						if (j > 0) {
							if (list.get(0).get(k).toString().equals("产品编码")) {
								if (lo.get(k) == null || lo.get(k).toString().trim().isEmpty())
									return "导入失败,产品编码不能为空";
								productCode = lo.get(k).toString();
								rs = dbConn.query(String.format(
										"SELECT product_name,cate_id,power,type_id,gas_id,capacity,customer_barcode,customer FROM meq_products "
												+ "WHERE product_code = '%s'",
										lo.get(k)));
								if (rs.next()) {
									haveProduct = true;
									for (int l = 0; l < lo.size(); l++) {
										switch (list.get(0).get(l).toString()) {
										case "产品名称":
											lo.set(l, rs.getString("product_name"));
											break;

										case "产品类别":
											lo.set(l, rs.getString("cate_id"));
											break;

										case "功率(KW)":
											lo.set(l, rs.getString("power"));
											break;

										case "产品类型":
											lo.set(l, rs.getString("type_id"));
											break;

										case "气种":
											lo.set(l, rs.getString("gas_id"));
											break;

										case "容量":
											lo.set(l, rs.getString("capacity"));
											break;

//    									case "customer_barcode":
//    										lo.set(l, rs.getString("customer_barcode"));
//    										break;
//    										
//    									case "customer":
//    										lo.set(l, rs.getString("customer"));
//    										break;		

										default:
											break;
										}
									}
								}
							}
						}
					}
					for (int k = 0; k < lo.size(); k++) {
						temp = lo.get(k) == null ? "" : lo.get(k).toString();
						columnName = list.get(0).get(k).toString();
						if (j == 0) {
							columnString.append(String.format("%s,", temp));
						} else if (j > 0) {
							columnName = list.get(0).get(k).toString();
							// 必填字段验证
							if (columnName.equals("产线")) {
								if (temp.isEmpty())
									return "导入失败,产线不能为空";
								rs = dbConn.query(String.format(
										"SELECT line_id FROM meq_production_lines "
												+ "WHERE line_name = '%s'",
										temp));
								if (rs.next()) {
									temp = rs.getString(1);
								} else {
									return "导入失败,产线不存在";
								}
							} else if (columnName.equals("排产数量")) {
								if (temp.isEmpty())
									return "导入失败,排产数量不能为空";
								else
									originalQty = temp;
							} else if (columnName.equals("销售订单号")) {
								if (temp.isEmpty())
									return "导入失败,销售订单号不能为空";
							} else if (columnName.equals("气种")) {
								rsNone = dbConn.query(String.format("SELECT gas_id FROM meq_product_gases WHERE gas_name = '%s'", temp));
								if (rsNone.next()) {
									temp = rsNone.getString(1);
								} else {
									temp = "";
								}
							} else if (columnName.equals("有无客户条码")) {
								if (!temp.trim().isEmpty())
									temp = lo.get(k).equals("有") ? "true" : "false";
							}

							if (!haveProduct) {
								if (columnName.equals("产品名称")) {
									if (temp.trim().isEmpty())
										return String.format("导入失败,产品编码为%s的产品名称不能为空", productCode);
								} else if (columnName.equals("产品类别")) {
									if (temp.trim().isEmpty())
										return String.format("导入失败,产品编码为%s的产品类别不能为空", productCode);
									rsNone = dbConn.query(String.format(
											"SELECT cate_id FROM meq_product_cates WHERE cate_name = '%s'", temp));
									if (rsNone.next()) {
										temp = rsNone.getString(1);
									} else {
										return String.format("导入失败,产品编码为%s的产品类别不存在", productCode);
									}
								} else if (columnName.equals("功率(KW)")) {
									if (temp.trim().isEmpty())
										return String.format("导入失败,产品编码为%s的功率不能为空", productCode);
								} 
							}

							if (temp.trim().isEmpty()) {
								valueString.append("null,");
							} else {
								valueString.append(String.format("'%s',", temp));
							}
						}
					}
					if (j > 0) {
//						valueResult.append(String.format("(%s%s,%s),", valueString.toString(), linePlanId, originalQty));
						valueResult.append(String.format("(%s),", Str.delComma(valueString.toString())));
					}
				}
				String column = "line_id,product_code,product_name,qty,cate_id,power,sale_order_code,morder_code,morder_line_no,gas_id,capacity,customer_barcode,customer,original_qty,\r\n" + 
						" online_qty,repairing_qty,offline_qty,plan_line_date,auditor_date,auditor_user,close_user,closed_date";
				
//				sqlInsert = String.format("INSERT INTO meq_line_plan_details(%s) VALUES %s",
//						columnString.toString(), Str.delComma(valueResult.toString()));
				sqlInsert = String.format("INSERT INTO meq_line_plan_details(%s) VALUES %s",
						column, Str.delComma(valueResult.toString()));
				rsNone.close();
				if (dbConn.queryUpdate(sqlInsert)) {
					return "导入成功";
				} else {
					return "导入失败,模板不符合要求";
				}
			} else {
				return "导入失败,文件没有数据";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "导入失败," + e;
		}
	}

	/**
	 * 多文件上传接口 上传工艺卡的
	 */
	@RequestMapping(value = "filesUpload", method = RequestMethod.POST)
	@ResponseBody
	public String filesUpload(MultipartHttpServletRequest request) {
		String sql = "insert into meq_process_cards(sop_id,idx,file_name,file_path,file_size) values(?,?,?,?,?)";
		MultipartFile file = request.getFile("file");
		String sopId = request.getParameter("sop_id");
		PreparedStatement ps = null;
		Connection conn = null;
		try {
			String ImagePath = fileImage(request,dbConn.getPropertiesYun("FilePath_DB"));
			if(ImagePath.contains("上传失败")) {
				return message.getErrorInfo("导入失败");
			}
			conn = dbConn.getConn();
			ps = conn.prepareStatement(sql);
			InputStream is = file.getInputStream();
			int len = is.available();
			ps.setInt(1, Integer.parseInt(sopId));
			ps.setInt(2, 0);
			ps.setString(3, ImagePath);
			ps.setString(4,dbConn.getPropertiesYun("fileUrl")+ImagePath);
			ps.setLong(5, len);
			ps.executeUpdate();
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
	 * 上传图片
	 */
	/*
	 * @SuppressWarnings("static-access")
	 * @RequestMapping(value = "fileImage", method = RequestMethod.POST)
	 * @ResponseBody
	 */
	@SuppressWarnings("static-access")
	public String fileImage(MultipartHttpServletRequest request,String FilePaht) {
		MultipartFile file = request.getFile("file");
		String fileName = null;
		// 文件不大于1M
		if (fileUitl.checkFileSize(file.getSize(), 1, "M")) {
			return message.getErrorInfo("上传失败上传文件不能超过1M");
		} else {
			try {
				fileName= fileNameTime(file.getOriginalFilename());
				if (!fileUitl.saveFiles(request, fileName, file)) {
					return message.getErrorInfo("上传失败1");
				}
			} catch (Exception e) {
				e.printStackTrace();
				return message.getErrorInfo("上传失败2");
			}
		}
		return fileName;
	}

	/**
	 * JF 预览文件,将文件丢到浏览器上面预览
	 */
	@RequestMapping(value = "getFile", method = RequestMethod.GET)
	@ResponseBody
	private String getFile(HttpServletRequest request, HttpServletResponse response, @RequestParam String fileName) {
		ResultSet rs = dbConn
				.query("select file_name,file_path from meq_process_cards where file_name = '" + fileName + "'");
		byte[] buff = null;
		
		try {
			if (rs.next()) {
//				String urls=dbConn.getPropertiesYun("fileUrl") + rs.getString("file_name");
				return rs.getString("file_path");
			} else {
				ResultSet cc = dbConn
						.query("select file_name,file_content from sys_files where file_name = '" + fileName + "'");
				if (cc.next()) {
					byte[] fileData = cc.getBytes(2);
					BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
					InputStream is = new ByteArrayInputStream(fileData);
					buff = new byte[1024];
					int len = 0;
					while ((len = is.read(buff)) != -1) {
						out.write(buff, 0, len);
					}
					is.close();
					out.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 从输入流中获取数据
	 * 
	 * @param inStream 输入流
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private static byte[] readInputStream(InputStream inStream) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[10240];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		return outStream.toByteArray();

	}
	
	/**
	 * 将PDF转换为多张图片
	 */
	@RequestMapping(value = "importPdf", method = RequestMethod.POST)
	@ResponseBody
	public String pdf2png(MultipartHttpServletRequest request) {
		String sopId = request.getParameter("sop_id");
		MultipartFile multFile = request.getFile("file");
	
		File file = new File(dbConn.getPropertiesYun("CreateFilePath")+"/"+multFile.getOriginalFilename());
		// 格式化时间
		Connection conn = null;
		try {
			if(!file.exists()) {
				multFile.transferTo(file);
			}
			PDDocument doc = PDDocument.load(file);
			PDFRenderer renderer = new PDFRenderer(doc);
			int pageCount = doc.getNumberOfPages();
			System.err.println(pageCount);
			if (pageCount > 100) {
				return message.getErrorInfo("导入文件页数过多,导入失败");
			}
			PreparedStatement ps = null;
			conn = dbConn.getConn();
			String sql = "insert into meq_process_cards(sop_id,idx,file_name,file_path,file_size) values(?,?,?,?,?)";
			for (int i = 0; i <pageCount; i++) {
				// 获取当前时间并作为时间戳
				int index = multFile.getOriginalFilename().indexOf(".");
				String name = multFile.getOriginalFilename().substring(0,index)+i;
				BufferedImage image = renderer.renderImageWithDPI(i, 144);
				ImageIO.write(image, "png", new File(dbConn.getPropertiesYun("CreateFilePath")+"/"+name + ".png"));
				File filePDF= new File(dbConn.getPropertiesYun("CreateFilePath")+"/"+name + ".png");
				//File fileImg = new File(timeStamp + ".png");
				ps = conn.prepareStatement(sql);
				@SuppressWarnings("resource")
				InputStream is = new FileInputStream(filePDF);
				int len = is.available();
				ps.setInt(1, Integer.parseInt(sopId));
				ps.setInt(2, i + 1);
				ps.setString(3, name + ".png");
				ps.setString(4, dbConn.getPropertiesYun("fileUrl")+name + ".png");// 查看文件路径
				ps.setInt(5, len);
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
	 * lh 通用的上传图片接口
	 * @param request
	 * @return
	 */
	
	 @SuppressWarnings("static-access")
	 @RequestMapping(value = "fileImage", method = RequestMethod.POST)
	  @ResponseBody
	  public String fileImage(MultipartHttpServletRequest request) {
	    MultipartFile file = request.getFile("file");
	    String name = null;
	    // 文件不大于10M
	    if (fileUitl.checkFileSize(file.getSize(), 10, "M")) {
	      return message.getErrorInfo("上传失败上传文件不能超过10M");
	    } else {
	      try {
	    	   name = fileNameTime(file.getOriginalFilename());
	        if (!fileUitl.saveFiles(request, name, file)) {
	          return message.getErrorInfo("上传失败");
	        }
	      } catch (Exception e) {
	        return message.getErrorInfo("上传失败"+e.getMessage());
	      }
	    }
	    return dbConn.getPropertiesYun("fileUrl")+name;
	  }
	 
	     /**
		 * lh 上传图片接口到阿里云需要的数据
		 * @param request
		 * @return
		 */
	 @SuppressWarnings("static-access")
	 @RequestMapping(value = "fileImageAly", method = RequestMethod.POST)
	  @ResponseBody
	  public String fileImageAly(MultipartHttpServletRequest request) {
	    MultipartFile file = request.getFile("file");
	    String name = null;
	    // 文件不大于10M
	    if (fileUitl.checkFileSize(file.getSize(), 50, "M")) {
	      return message.getErrorInfo("上传失败上传文件不能超过50M");
	    } else {
	      try {
	    	   name = fileNameTime(file.getOriginalFilename());
	        if (!fileUitl.saveFilesAly(request, name, file)) {
	          return message.getErrorInfo("上传失败");
	        }
	      } catch (Exception e) {
	        return message.getErrorInfo("上传失败"+e.getMessage());
	      }
	    }
	    return dbConn.getPropertiesYun("fileUrlAly")+name;
	  }
      
	 //文件名称进行拼接时间戳
	 public String  fileNameTime(String fileName) {
			int index = fileName.indexOf(".");
			String  fileNameTimeStamp =fileName.substring(0,index)+System.currentTimeMillis() + fileName.substring(index);
		 return fileNameTimeStamp;
	 }
	 
	    /**
	     * ftp文件上传
	     * @param request
	     * @param response
	     * @return
	     */
	    @RequestMapping(value ="FtpUpload",method =RequestMethod.POST)
	    @ResponseBody
	    public String handleFileUpload(HttpServletRequest request){
	        //创建文件对象并获取请求中的文件对象
	        MultipartFile file = null;
	        String fileName = null;
	        try{
	            MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
	            file = mRequest.getFile("file");
	            //判断上传非空
	            if(null == file) {
	                return message.getErrorInfo("上传文件失败");
	            }
	            //上传需要导入数据的文件
	            fileName = file.getOriginalFilename();
	            InputStream inputStream = file.getInputStream();
	            String suffix = fileName.substring(fileName.indexOf("."),fileName.length());
	            fileName = UploadUtil.upload(dbConn.getPropertiesYun("upload.hostname"),dbConn.getPropertiesYun("upload.username"),
	            		dbConn.getPropertiesYun("upload.password"),dbConn.getPropertiesYun("upload.targetPath"),dbConn.getPropertiesYun("upload.port"),suffix,inputStream);
	        } catch (Exception e) {
	        	e.printStackTrace();
	        	return message.getErrorInfo("上传失败，系统异常"+e.getMessage());
	        }
	        return fileName;
	    }
}
