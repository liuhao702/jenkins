package com.lc.bxm.common.util;

import java.io.IOException;

import java.io.InputStream;
import java.io.PushbackInputStream;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * 读取Excel公共类
 * @author JF
 * @date 2019年6月11日
 */
@Component
@SuppressWarnings({"deprecation","resource","rawtypes","unused"})
public class ReadExcel {
	
	/**
	 * 对外提供读取excel 的方法
	 */
	public List<List<Object>> readExcel(String fileName,InputStream input,boolean ignoreNumber) throws IOException {
		String extension = fileName.lastIndexOf(".") == -1 ? "" : fileName
				.substring(fileName.lastIndexOf(".") + 1);
		if ("xls".equals(extension)) {
			return read2003Excel(input);
		} else if ("xlsx".equals(extension)) {
			if(!ignoreNumber){
				return read2007Excel(input);
			}else{
				return read2007ExcelIgnoreNumber(input);
			}
		} else {
			throw new IOException("不支持的文件类型");
		}
	}
	
	/**
	 * 对外提供读取excel 的方法 JESON
	 */
	public static Map<String,List<List<Object>>> readExcelBySheetName(String fileName,InputStream input) throws IOException {
		String extension = fileName.lastIndexOf(".") == -1 ? "" : fileName
				.substring(fileName.lastIndexOf(".") + 1);
		if ("xls".equals(extension)) {
			return read2003ExcelBySheetName(input);
		} else if ("xlsx".equals(extension)) 
		{
				return read2007ExcelBySheetName(input);
		} else {
			throw new IOException("不支持的文件类型");
		}
	}
	
	public List<List<Object>> create(InputStream in) throws IOException,InvalidFormatException {
        if (!in.markSupported()) {
            in = new PushbackInputStream(in, 8);
        }
        if (POIFSFileSystem.hasPOIFSHeader(in)) {
        	
//            return new HSSFWorkbook(in);
        	return read2007Excel(in);
        }
        if (POIXMLDocument.hasOOXMLHeader(in)) {
//            return new XSSFWorkbook(OPCPackage.open(in));
        	return  read2007ExcelIgnoreNumber(in);
        }
        throw new IllegalArgumentException("你的excel版本目前poi解析不了");
    }

	/**
	 * 读取 office 2003 excel
	 */
	private static List<List<Object>> read2003Excel(InputStream input) throws IOException {
		List<List<Object>> list = new LinkedList<List<Object>>();
		HSSFWorkbook hwb = new HSSFWorkbook(input);
		HSSFSheet sheet = hwb.getSheetAt(0);
		Object value = null;
		HSSFRow row = null;
		HSSFCell cell = null;
		int counter = 0;
		for (int i = sheet.getFirstRowNum(); counter < sheet
				.getPhysicalNumberOfRows(); i++) {
			row = sheet.getRow(i);
			if (row == null) {
				continue;
			} else {
				counter++;
			}
			List<Object> linked = new LinkedList<Object>();
			for (int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++) {
				cell = row.getCell(j);
				if (cell == null) {
					continue;
				}
				DecimalFormat df = new DecimalFormat("0");// 格式化 number String
															// 字符
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");// 格式化日期字符串
				DecimalFormat nf = new DecimalFormat("0.00");// 格式化数字
				switch (cell.getCellType()) {
				case XSSFCell.CELL_TYPE_STRING:
					value = null == cell.getStringCellValue()? null : cell.getStringCellValue().trim();
					break;
				case XSSFCell.CELL_TYPE_NUMERIC:
					if ("@".equals(cell.getCellStyle().getDataFormatString())) {
						value = df.format(cell.getNumericCellValue());
					} else if ("General".equals(cell.getCellStyle()
							.getDataFormatString())) {
						value = nf.format(cell.getNumericCellValue());
					} else {
						value = sdf.format(HSSFDateUtil.getJavaDate(cell
								.getNumericCellValue()));
					}
					break;
				case XSSFCell.CELL_TYPE_BOOLEAN:
					value = cell.getBooleanCellValue();
					break;
				case XSSFCell.CELL_TYPE_BLANK:
					value = "";
					break;
				default:
					value = cell.toString();
				}
				if (value == null || "".equals(value)) {
					continue;
				}
				linked.add(value);
			}
			list.add(linked);
		}
		return list;
	}
	
	/**
	 * 读取 office 2003 excel
	 */
	private static Map<String,List<List<Object>>> read2003ExcelBySheetName(InputStream input) throws IOException {
		Map<String,List<List<Object>>> result = null;
		HSSFWorkbook hwb = new HSSFWorkbook(input);
		int sheetNum = hwb.getNumberOfSheets();
		for(int k = 0; k < sheetNum; k++){
			List<List<Object>> list = new LinkedList<List<Object>>();
			//HSSFSheet sheet = null;
			Object value = null;
			HSSFRow row = null;
			HSSFCell cell = null;
			int counter = 0;
			HSSFSheet sheet = hwb.getSheetAt(k);
			String sheetName = hwb.getSheetName(k);
		for (int i = sheet.getFirstRowNum(); counter < sheet
				.getPhysicalNumberOfRows(); i++) {
			row = sheet.getRow(i);
			if (row == null) {
				continue;
			} else {
				counter++;
			}
			List<Object> linked = new LinkedList<Object>();
			for (int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++) {
				cell = row.getCell(j);
				if (cell == null) {
					continue;
				}
				DecimalFormat df = new DecimalFormat("0");// 格式化 number String
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");// 格式化日期字符串
				DecimalFormat nf = new DecimalFormat("0.00");// 格式化数字
				switch (cell.getCellType()) {
				case XSSFCell.CELL_TYPE_STRING:
					value = cell.getStringCellValue();
					break;
				case XSSFCell.CELL_TYPE_NUMERIC:
					if ("@".equals(cell.getCellStyle().getDataFormatString())) {
						value = df.format(cell.getNumericCellValue());
					} else if ("General".equals(cell.getCellStyle()
							.getDataFormatString())) {
						value = nf.format(cell.getNumericCellValue());
					} else {
						value = sdf.format(HSSFDateUtil.getJavaDate(cell
								.getNumericCellValue()));
					}
					break;
				case XSSFCell.CELL_TYPE_BOOLEAN:
					value = cell.getBooleanCellValue();
					break;
				case XSSFCell.CELL_TYPE_BLANK:
					value = "";
					break;
				default:
					value = cell.toString();
				}
				if (value == null || "".equals(value)) {
					continue;
				}
				linked.add(value);
			}
			list.add(linked);
			if(null == result)
			{
				result = new HashMap<String, List<List<Object>>>();
			}
			result.put(sheetName, list);
			}
		
		}
		return result;
	}

	/**
	 * 读取Office 2007 excel
	 */
	private static List<List<Object>> read2007Excel(InputStream input) throws IOException {
		List<List<Object>> list = new LinkedList<List<Object>>();
		// 构造 XSSFWorkbook 对象，strPath 传入文件路径
		XSSFWorkbook xwb = new XSSFWorkbook(input);
		// 读取第一章表格内容
		XSSFSheet sheet = null;
		Object value = null;
		XSSFRow row = null;
		XSSFCell cell = null;
		int counter = 0;
		int startRow;
		for(int k = 0; k < 3; k++){
			sheet = xwb.getSheetAt(k);
			startRow = sheet.getFirstRowNum();
			for (int i =startRow; counter < sheet
				.getPhysicalNumberOfRows(); i++) {
			row = sheet.getRow(i);
			if (row == null) {
				continue;
			} else {
				counter++;
			}
			List<Object> linked = new LinkedList<Object>();
			for (int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++) {
				cell = row.getCell(j);
				if (cell == null) {
					linked.add(null);
					continue;
				}
				DecimalFormat df = new DecimalFormat("0");// 格式化 number String
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");// 格式化日期字符串
				DecimalFormat nf = new DecimalFormat("0.00");// 格式化数字
				switch (cell.getCellType()) {
				case XSSFCell.CELL_TYPE_STRING:
					value = null == cell.getStringCellValue()? null : cell.getStringCellValue().trim();
					break;
				case XSSFCell.CELL_TYPE_NUMERIC:
					if ("@".equals(cell.getCellStyle().getDataFormatString())) {
						value = df.format(cell.getNumericCellValue());
					} else if ("General".equals(cell.getCellStyle()
							.getDataFormatString())) {
						value = nf.format(cell.getNumericCellValue());
					} else {
						value = sdf.format(HSSFDateUtil.getJavaDate(cell
								.getNumericCellValue()));
					}
					break;
				case XSSFCell.CELL_TYPE_BOOLEAN:
					value = cell.getBooleanCellValue();
					break;
				case XSSFCell.CELL_TYPE_BLANK:
					value = "";
					break;
				case XSSFCell.CELL_TYPE_FORMULA:
					value = cell.getCTCell().getV();
					break;
				default:
					value = cell.toString();
				}
				if (value == null || "".equals(value)) {
					linked.add(null);
					continue;
				}
				linked.add(value);
			}
			list.add(linked);
		   }
		}
		return list;
	}
	
	/**
	 * 读取Office 2007 excel
	 */
	private static List<List<Object>> read2007ExcelIgnoreNumber(InputStream input) throws IOException {
		List<List<Object>> list = new LinkedList<List<Object>>();
		// 构造 XSSFWorkbook 对象，strPath 传入文件路径
		XSSFWorkbook workbook = new XSSFWorkbook(input);
		// 读取第一章表格内容
		int sheetNum = workbook.getNumberOfSheets();
		//XSSFSheet sheet = null;
		for(int k = 0; k < sheetNum; k++){
			Object value = null;
			Row row = null;
			Cell cell = null;
			Sheet sheet = workbook.getSheetAt(k);
			//String sheetName = xwb.getSheetName(k);
			for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
			row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			List<Object> linked = new LinkedList<Object>();
			
			for (int j = 0; j < sheet.getRow(0).getLastCellNum(); j++) {
				cell = row.getCell(j);
				if (cell == null) {
					linked.add(null);
					continue;
				}
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_STRING:
					value = null == cell.getStringCellValue()? null : cell.getStringCellValue().trim();
					break;
				case Cell.CELL_TYPE_NUMERIC:
					cell.setCellType(Cell.CELL_TYPE_STRING);
					value=cell.getStringCellValue();
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					value = cell.getBooleanCellValue();
					break;
				case Cell.CELL_TYPE_BLANK:
					value = "";
					break;
				case Cell.CELL_TYPE_FORMULA:
					value = cell.getCellFormula();
					break;
				default:
					value = cell.toString();
				}
				if (value == null || "".equals(value)) {
					linked.add(null);
					continue;
				}
				linked.add(value);
			}
			list.add(linked);
			
			}
		}
		return list;
	}
	
	/**
	 * 读取Office 2007 excel
	 */
	private static Map<String,List<List<Object>>> read2007ExcelBySheetName(InputStream input) throws IOException {
		Map<String,List<List<Object>>> result = null;
		// 构造 XSSFWorkbook 对象，strPath 传入文件路径
		XSSFWorkbook xwb = new XSSFWorkbook(input);
		// 读取第一章表格内容
		int sheetNum = xwb.getNumberOfSheets();
		if(sheetNum > 0)
		{
			result = new TreeMap<String, List<List<Object>>>();
		}
		//XSSFSheet sheet = null;
		for(int k = 0; k < sheetNum; k++){
			List<List<Object>> list = new LinkedList<List<Object>>();
			Object value = null;
			XSSFRow row = null;
			XSSFCell cell = null;
			XSSFSheet sheet = xwb.getSheetAt(k);
			String sheetName = xwb.getSheetName(k);
			
			for (int i = 1; i < sheet
				.getPhysicalNumberOfRows(); i++) {
			row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			List<Object> linked = new LinkedList<Object>();
			
			for (int j = 0; j <= row.getLastCellNum(); j++) {
				cell = row.getCell(j);
				if (cell == null) {
					linked.add(null);
					continue;
				}
				switch (cell.getCellType()) {
				case XSSFCell.CELL_TYPE_STRING:
					value = cell.getStringCellValue();
					break;
				case XSSFCell.CELL_TYPE_NUMERIC:
					cell.setCellType(Cell.CELL_TYPE_STRING);
					value=cell.getStringCellValue();
					break;
				case XSSFCell.CELL_TYPE_BOOLEAN:
					value = cell.getBooleanCellValue();
					break;
				case XSSFCell.CELL_TYPE_BLANK:
					value = "";
					break;
				case XSSFCell.CELL_TYPE_FORMULA:
					value = cell.getCTCell().getV();
					break;
				default:
					value = cell.toString();
				}
				if (value == null || "".equals(value)) {
					linked.add(null);
					continue;
				}
				linked.add(value);
			}
			list.add(linked);
			result.put(sheetName, list);
			}
		}
		return result;
	}
	
	
	/**
	 * 从数据库获取excel的数据
	 * @param rs
	 * @param list
	 * @param dataMap
	 */
	public List<Map> getExcelDate(ResultSet rs , int columns) {
		Map<String, Object> dataMap = null;
		List<Map> list = new ArrayList<Map>();
		list.clear();
		 try {
			 while (rs.next()) {
				 dataMap = new HashMap<String, Object>();
				 for (int j = 1; j <= columns; j++) {
//					if (rs.getObject(j) instanceof String) {
//						dataMap.put("line_name" +j , rs.getString(j));
//					}else if (rs.getObject(j) instanceof Double) {
//						dataMap.put("line_name" +j , rs.getDouble(j));
//					}else if (rs.getObject(j) instanceof Long) {
//						dataMap.put("line_name" +j , rs.getLong(j));
//					}else {
						System.out.println(rs.getObject(j) instanceof Date);
						dataMap.put("column" +j , rs.getObject(j));
				 }
				list.add(dataMap);
			}
		} catch (Exception e) { e.printStackTrace();}
		return list;
	}
	
	
	
	public static boolean convertCellValueToString(Cell cell) {
		  boolean falg = false;
	        if(cell==null){
	            return false;
	        }
	        String returnValue = null;
	        switch (cell.getCellType()) {
	            case Cell.CELL_TYPE_NUMERIC:   //数字
	                Double doubleValue = cell.getNumericCellValue();

	                // 格式化科学计数法，取一位整数
	                DecimalFormat df = new DecimalFormat("0");
	                returnValue = df.format(doubleValue);
	                falg = true;
	                break;
	            case Cell.CELL_TYPE_STRING:    //字符串
	                returnValue = cell.getStringCellValue();
	                break;
	            case Cell.CELL_TYPE_BOOLEAN:   //布尔
	                Boolean booleanValue = cell.getBooleanCellValue();
	                returnValue = booleanValue.toString();
	                break;
	            case Cell.CELL_TYPE_BLANK:     // 空值
	                break;
	            case Cell.CELL_TYPE_FORMULA:   // 公式
	                returnValue = cell.getCellFormula();
	                break;
	            case Cell.CELL_TYPE_ERROR:     // 故障
	                break;
	            default:
	                break;
	        }
	        System.err.println(cell.getCellType()+","+Cell.CELL_TYPE_BLANK);
	        return falg;
    }
}
