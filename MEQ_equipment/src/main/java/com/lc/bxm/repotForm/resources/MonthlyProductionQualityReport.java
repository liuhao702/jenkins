package com.lc.bxm.repotForm.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.FileUtil;
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.common.util.ReportGeneral;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONObject;

/**
 * 月生产品质分析报表
 * @author liuhao
 */
@RestController
@RequestMapping("/Report")
public class MonthlyProductionQualityReport {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;

	private static final String EXCEL_XLS = "xls";
	private static final String EXCEL_XLSX = "xlsx";
	
	private ReportGeneral eportGeneral = new ReportGeneral();
	
	private static FileUtil fileUtil = new FileUtil();
	private String filePath =null;

	/**
	 * 气密检测分析报表
	 * @param request
	 * @param response
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "getMonthProdQuaReport", method = RequestMethod.POST)
	@ResponseBody
	public String commonReportForm(HttpServletRequest request, HttpServletResponse response,@RequestBody String jsonData) {
		filePath=dbConn.getPropertiesYun("fileRepot")+"/monthlyProductionQualityReport.xlsx";
		JSONObject jsonObject = JSONObject.fromObject(jsonData);
		String date = jsonObject.get("date").toString();
		Object line_id = jsonObject.get("line_id");
		if ( line_id == null || line_id.equals("")|| line_id.equals("null")) {
			 line_id = 0;//获取最小产线
		}
		for (int i = 1; i <5; i++) {
			switch (i) {
			case 1:
				 getData(date,line_id);
				break;
			case 2:
				//getData2(date,line_id);
				break;
			}
		}
		return getfileShow(request, response, "monthlyProductionQualityReport.xlsx");
	}

	/**
	 * 获取1工作区数据
	 */
	public void getData(String date, Object line_id) {
		
		ResultSet rs =dbConn.query(String.format("select * from meq_monthly_production_quality(%s,'%s') "
				+ "as (dt text,count bigint,curveq_count bigint,qm_count bigint,xx_count bigint)",line_id,date));
		int columns = 0;
		//判断是否是周末
		try {
			ResultSetMetaData rsmd = rs.getMetaData(); // 获取结果集的元数据
			columns = rsmd.getColumnCount();// 获取结果集的列数
		} catch (SQLException e) {
			e.printStackTrace();
		}
		eportGeneral.writeExcel(eportGeneral.getExcelDate(rs, columns), filePath, 1, columns);
		
	}

	/**
	 * 获取2工作区数据
	 */
	public void getData2(String date, Object line_id) {
		ResultSet rs= dbConn.query(String.format("select * from meq_monthly_production_quality_record(%s,'%s')",line_id,date));
		int columns = 0;
		try {
			
			ResultSetMetaData rsmd = rs.getMetaData(); //获取总数据
			columns = rsmd.getColumnCount();
		} catch (SQLException e) {
			e.printStackTrace();
		} // 获取结果集的元数据
		eportGeneral.writeExcel(eportGeneral.getExcelDate(rs, columns), filePath, 1, columns);
	}

	/**
	 * 获取报表文件给前端显示
	 * 
	 * @param request
	 * @param response
	 * @param fileCode
	 * @return
	 */
	public String getfileShow(HttpServletRequest request, HttpServletResponse response, String fileName) {
		try {
			fileUtil.fileDownload(response, fileName);
			return message.getSuccessInfo("下载成功");
		} catch (Exception e) {
		}
		return message.getErrorInfo("下载失败");
	}
	
	
	/**
	 * 将获取的数据写进文件
	 * @param dataList      存放的内容
	 * @param finalXlsxPath 写入的文件
	 * @param num           那个工作区
	 * @param column        多少个字段
	 */
	@SuppressWarnings("rawtypes")
	public void writeExcel(List<Map> dataList, String finalXlsxPath, int num, int column) {
		OutputStream out = null;
		Workbook workBook =null;
		try {
			// 读取Excel文档
			File finalXlsxFile = new File(finalXlsxPath);
			//InputStream tempFileStream = new FileInputStream(finalXlsxFile);
			 workBook = getWorkbok(finalXlsxFile);
			// sheet 对应一个工作页
			if (num!=2) { //判断是哪个工作区
				Sheet sheet = workBook.getSheetAt(num);
				int rowNumber = sheet.getLastRowNum();
				workBook.removeSheetAt(num);
				 //删除原有数据，除了属性列
				// 创建文件输出流，输出电子表格：这个必须有，否则你在sheet上做的任何操作都不会有效，由于表格长度从0开始
				for (int i = 1; i <= rowNumber; i++) {
					Row row = sheet.getRow(i);
					sheet.removeRow(row);
				}
//				out = new FileOutputStream(finalXlsxPath);
//				workBook.write(out);
				/**
				 * 往Excel中写新数据
				 */
				for (int j = 0; j < dataList.size(); j++) {
					// 创建一行：从第二行开始，跳过属性列
					Row row = sheet.createRow(j+1);
					// 得到要插入的每一条记录
					Map dataMap = dataList.get(j);
					// for (int k = 1; k < rowCount; k++) {
					// 在一行内循环
					Cell cell = null;
					for (int i = 1; i <=column; i++) {
						cell = row.createCell(i-1); 
//						if (dataMap.get("column"+i) instanceof Double) {
//							cell.setCellValue((double) dataMap.get("column" + i));
//						}else if (dataMap.get("column"+i) instanceof Long) {
//							cell.setCellValue((long) dataMap.get("column" + i));
//						}else if (dataMap.get("column"+i) instanceof Integer) {
//							cell.setCellValue((Integer) dataMap.get("column" + i));
//						}else if (dataMap.get("column"+i) instanceof Timestamp) {
//							cell.setCellValue((Timestamp) dataMap.get("column" + i));	
//						}else if (dataMap.get("column"+i) instanceof BigDecimal) {
//							BigDecimal a = (BigDecimal) dataMap.get("column"+i);
//							cell.setCellValue(a.doubleValue());
//						}else if (dataMap.get("column"+i) == null) {
//							cell.setCellValue("");
//						}else{
							cell.setCellValue(dataMap.get("column" + i).toString());
//						}
					}
				}
				sheet.setForceFormulaRecalculation(true);
			}else {
			Sheet sheet = workBook.getSheetAt(num);
			 //删除原有数据，除了属性列
			// 创建文件输出流，输出电子表格：这个必须有，否则你在sheet上做的任何操作都不会有效
			int rowNumber = sheet.getLastRowNum();
			for (int i = 1; i < rowNumber; i++) {
				Row row = sheet.getRow(i + 1);
				sheet.removeRow(row);
			}
//			out = new FileOutputStream(finalXlsxPath);
//			workBook.write(out);
			/**
			 * 往Excel中写新数据
			 */
			for (int j = 0; j <dataList.size(); j++) {
				// 创建一行：从第二行开始，跳过属性列
				Row row = sheet.createRow(j+1);
				// 得到要插入的每一条记录
				Map dataMap = dataList.get(j);
				// for (int k = 1; k < rowCount; k++) {
				// 在一行内循环
				Cell cell = null;
				for (int i = 1; i <= column; i++) {
					cell = row.createCell(i-1); 
//					if (dataMap.get("column"+i) instanceof Double) {
//						cell.setCellValue((double) dataMap.get("column" + i));
//					}else if (dataMap.get("column"+i) instanceof Long) {
//						cell.setCellValue((long) dataMap.get("column" + i));
//					}else if (dataMap.get("column"+i) instanceof Integer) {
//						cell.setCellValue((Integer) dataMap.get("column" + i));
//					}else if (dataMap.get("column"+i) instanceof Timestamp) {
//						cell.setCellValue((Timestamp) dataMap.get("column" + i));	
//					}else if (dataMap.get("column"+i) instanceof BigDecimal) {
//						BigDecimal a = (BigDecimal) dataMap.get("column"+i);
//						cell.setCellValue(a.doubleValue());
//					}else if (dataMap.get("column"+i) == null) {
//						cell.setCellValue("");
//					}else{
						cell.setCellValue(dataMap.get("column" + i).toString());
//					}
				}
			   }
			sheet.setForceFormulaRecalculation(true);
			}
			// 创建文件输出流，准备输出电子表格：这个必须有，否则你在sheet上做的任何操作都不会有效
			out = new FileOutputStream(finalXlsxPath);
			workBook.write(out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 判断Excel的版本,获取Workbook
	 * 
	 * @param in
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static Workbook getWorkbok(File file) throws IOException {
		Workbook wb = null;
		FileInputStream in = new FileInputStream(file);
		if (file.getName().endsWith(EXCEL_XLS)) { // Excel&nbsp;2003
			wb = new HSSFWorkbook(in);
		} else if (file.getName().endsWith(EXCEL_XLSX)) { // Excel 2007/2010
			wb = new XSSFWorkbook(in);
		}
		return wb;
	}
	
	@RequestMapping(value = "getDownloadData",method = RequestMethod.GET)
 	@ResponseBody
 	public String  getDownloadData(HttpServletResponse response,@RequestParam String date) {
	 filePath=dbConn.getPropertiesYun("filedDetailRepotDownload")+date+".xlsx";
	 String  fileExists =dbConn.getPropertiesYun("filedDetailRepot")+date+".xlsx";
      // String  mkdirs = dbConn.getPropertiesYun("filedDetailRepot");
         File file = new File(fileExists); //用来判断文件是否存在
         if(!file.exists()) {
        	 return message.getErrorInfo("该月份的无详细数据");
        }
 			return filePath;
 	}
 }
