package com.lc.bxm.common.util;

import java.io.File;



import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lc.bxm.common.helper.FileUtil;

/**
 * 操作报表的通用方法
 * @author lh
 */
public class ReportGeneral {
	
	static FileUtil fileUtil = new FileUtil();
	private static final String EXCEL_XLS = "xls";
	private static final String EXCEL_XLSX = "xlsx";
	
	
	/**
	 * 从数据库获取excel的数据
	 * @param rs
	 * @param list
	 * @param dataMap
	 */
	@SuppressWarnings("rawtypes")
	public List<Map> getExcelDate(ResultSet rs , int columns) {
		Map<String, Object> dataMap = null;
		List<Map> list = new ArrayList<Map>();
		list.clear();
		 try {
			 while (rs.next()) {
				 dataMap = new HashMap<String, Object>();
				 for (int j = 1; j <= columns; j++) {
						dataMap.put("column" +j , rs.getObject(j));
				 }
				list.add(dataMap);
			}
		} catch (Exception e) { e.printStackTrace();}
		return list;
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
			//Workbook workBook = WorkbookFactory.create(new File(finalXlsxPath));
			 workBook = getWorkbok(finalXlsxFile);
			// sheet 对应一个工作页
			Sheet sheet = workBook.getSheetAt(num);
			 //删除原有数据，除了属性列
			int rowNumber = sheet.getLastRowNum(); // 第一行从0开始算
			//System.out.println("原始数据总行数，除属性列：" + rowNumber);
			for (int i = 0; i < rowNumber; i++) {
				Row row = sheet.getRow(i + 1);
				sheet.removeRow(row);
			}
			// 创建文件输出流，输出电子表格：这个必须有，否则你在sheet上做的任何操作都不会有效
			out = new FileOutputStream(finalXlsxPath);
			workBook.write(out);
			/**
			 * 往Excel中写新数据
			 */
			for (int j = 0; j < dataList.size(); j++) {
				// 创建一行：从第二行开始，跳过属性列
				Row row = sheet.createRow( j+1);
				// 得到要插入的每一条记录
				Map dataMap = dataList.get(j);
				// for (int k = 1; k < rowCount; k++) {
				// 在一行内循环
				Cell cell = null;
				for (int i = 1; i <= column; i++) {
					cell = row.createCell(i - 1);
					if (dataMap.get("column"+i) instanceof Double) {
						cell.setCellValue((double) dataMap.get("column" + i));
					}else if (dataMap.get("column"+i) instanceof Long) {
						cell.setCellValue((long) dataMap.get("column" + i));
					}else if (dataMap.get("column"+i) instanceof Integer) {
						cell.setCellValue((Integer) dataMap.get("column" + i));
					}else if (dataMap.get("column"+i) instanceof Timestamp) {
						cell.setCellValue((Timestamp) dataMap.get("column" + i));	
					}else if (dataMap.get("column"+i) instanceof BigDecimal) {
						BigDecimal a = (BigDecimal) dataMap.get("column"+i);
						cell.setCellValue(a.doubleValue());
					}else if (dataMap.get("column"+i) == null) {
						cell.setCellValue("");
					}else{
						cell.setCellValue(dataMap.get("column" + i).toString());
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
					workBook.close();
					out.flush();
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//System.out.println("数据导出成功");
	}

	/**
	 * 判断Excel的版本,获取Workbook
	 * 
	 * @param in
	 * @param filename
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("resource")
	public static Workbook getWorkbok(File file) throws Exception {
		Workbook wb = null;
		FileInputStream in = new FileInputStream(file);
		if (file.getName().endsWith(EXCEL_XLS)) { // Excel&nbsp;2003
			wb = new HSSFWorkbook(in);
		} else if (file.getName().endsWith(EXCEL_XLSX)) { // Excel 2007/2010
			wb = new XSSFWorkbook(in);
		}else {
			throw new Exception("文件不是Excel文件");
		}
		return wb;
	}
	
	 public String isWeekend(String bDate) {
		    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	        Date bdate = null;
			try {
				bdate = df.parse(bDate);
				Calendar cal = Calendar.getInstance();
		        cal.setTime(bdate);
		        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
		            return "OK";
		        } else{
		            return "NO";
		        }
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return "NO";
	  }
	
}
