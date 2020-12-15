package com.lc.bxm.repotForm.resources;

import java.sql.ResultSet;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.FileUtil;
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.common.util.ReportGeneral;
import com.lc.bxm.dbconnection.PostgreSQLConn;


/**
 * 不良维修记录报表
 * @author lh
 */
@RestController
@RequestMapping("/reportForm")
public class BadMaintenanceRecord {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;

	private  ReportGeneral reportGeneral = new ReportGeneral();
	private static FileUtil fileUtil = new FileUtil();
	private static String filePath = null;
	
	
	/**
	 * 不良维修记录报表
	 * @param request
	 * @param response
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "getBadMaintenanceRecord", method = RequestMethod.POST)
	@ResponseBody
	public String commonReportForm(HttpServletRequest request, HttpServletResponse response, @RequestBody String date) {
		//System.out.println(date);
		filePath=dbConn.getPropertiesYun("fileRepot")+"/badMaintenanceRecord.xlsx";
		String currentDate = null; // 如果没有传时间就取当前日期
		String bigenDate = null; // 开始日期
		String endDate = null; // 结束日期
		if (date.equals("[]")) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
			currentDate = df.format(new Date());// new Date()为获取当前系统时间
			for (int i = 1; i <= 3; i++) {
				switch (i) {
				case 1:
					getData(currentDate, currentDate);
					break;
				case 2:
					//getData2(currentDate, currentDate);
					break;
				case 3:
				    getData3(currentDate);
					break;
				}
			}
		} else {
			String timeString = date.replace("[", "").replace("]", "");
			bigenDate = timeString.substring(0, timeString.indexOf(","));
			endDate = timeString.substring(timeString.indexOf(",") + 1, timeString.length());
			System.err.println(bigenDate + "---------" + endDate);
			for (int i = 1; i <= 3; i++) {
				switch (i) {
				case 1:
					getData(bigenDate, endDate);
					break;
				case 2:
				    //getData2(bigenDate,endDate);
					break;
				case 3:
					getData3(timeString);
					break;
				}
			}
		}
		return getfileShow(request, response, "badMaintenanceRecord.xlsx");
	}

	/**
	 * 获取工作区1数据
	 */
	public void getData(String bigenDate, String endDate) {
		ResultSet rs = null;
		int columns = 0;
		System.err.println(String.format("select * from meq_repairings_report('%s','%s')",
				bigenDate, endDate));
		rs = dbConn.query(String.format("select * from meq_repairings_report('%s','%s')",
				bigenDate, endDate));
		try {
			ResultSetMetaData rsmd = rs.getMetaData(); // 获取结果集的元数据
			columns = rsmd.getColumnCount();// 获取结果集的列数
		} catch (SQLException e) {
			e.printStackTrace();
		}
		reportGeneral.writeExcel(reportGeneral.getExcelDate(rs, columns), filePath, 2, columns);
	}

	
	/**
	 * 获取工作区2数据
	 */
	public void getData2(String bigenDate, String endDate) {
		ResultSet rs = null;
		int columns = 0;
		rs = dbConn.query(String.format(
				"select l_name , cate_hour from meq_loss_hour_report('%s','%s') "
						+ "AS (l_name character varying(60),cate_hour double precision,rate text)",
				bigenDate, endDate));
		try {
			ResultSetMetaData rsmd = rs.getMetaData(); // 获取结果集的元数据
			columns = rsmd.getColumnCount();// 获取结果集的列数
		} catch (SQLException e) {
			e.printStackTrace();
		}
		reportGeneral.writeExcel(reportGeneral.getExcelDate(rs, columns), filePath,3, columns);
	}

	
	/**
	 * 获取工作区3数据
	 */
	public void getData3(String date ) {
		String dateString = date.replace("\"", "").replace(",", "~");
		Map<String, Object> dataMap = null;
		@SuppressWarnings("rawtypes")
		List<Map> list = new ArrayList<Map>();
		dataMap = new HashMap<String, Object>();
		dataMap.put("column1", dateString);
		list.add(dataMap);
		reportGeneral.writeExcel(list, filePath, 1, 1);
	}
	
	/**
	 * 获取报表文件给前端显示
	 * @param request
	 * @param response
	 * @param fileCode
	 * @return
	 */
	public String getfileShow(HttpServletRequest request, HttpServletResponse response, String filePath) {
		try {
			fileUtil.fileDownload(response, filePath);
			return message.getSuccessInfo("下载成功");
		} catch (Exception e) {
		}
		return message.getErrorInfo("下载失败");
	}
}
	/*public String commonReportForm(HttpServletRequest request, HttpServletResponse response, @RequestBody String jsonData) {
		//System.err.println(jsonData);
		JSONObject jsonObject = JSONObject.fromObject(jsonData);
		Object date = jsonObject.get("date");
		Object line_id = jsonObject.get("line_id");
		      // 如果没有传时间和产线就取当前日期和最小的那条线
		
		if ( date == null || date.equals("") || date.equals("null")) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
			date = df.format(new Date());
		}
		if ( line_id == null || line_id.equals("")|| line_id.equals("null")) {
			 line_id = getMinLine();//获取最小产线
		}
			for (int i = 1; i <= 5; i++) {
				switch (i) {
				case 1:
				   getData(date,line_id);
					break;
				case 2:
				   //getData2(date);
					break;
				case 3:
				   getData3(date,line_id);
				    break;								    
				}
			}
		return getfileShow(request, response, "reportForm/badMaintenanceRecord.xlsx");
	}
    
	*//**
	 * 获取最小的那条产线
	 * @return
	 *//*
	public String getMinLine() {
		ResultSet rs = dbConn.query("select min(line_id) from v_meq_production_lines");
		String line = null;
		try {
			if (rs.next()) {
				line = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return line;
	}
	
	*//**
	 * 获取1工作区数据
	 *//*
	public void getData(Object date ,Object line_id) {
		ResultSet rs = null;
		int columns = 0;
		rs = dbConn.query(String.format("select * from meq_repairings_report('%s',%s) as (line_plan_code character varying(20), product_code character varying(30),barcode character varying(60), " + 
				"date_time timestamp without time zone, reason_code character varying(10), cate_name character varying(30)," + 
				"reason_name character varying(30),handling_name character varying(30))",
				date,line_id));
		try {
			ResultSetMetaData rsmd = rs.getMetaData(); // 获取结果集的元数据
			columns = rsmd.getColumnCount();// 获取结果集的列数
		} catch (SQLException e) {
			e.printStackTrace();
		}
		eportGeneral.writeExcel(eportGeneral.getExcelDate(rs, columns), filePath, 1, columns);
	}

	
	*//**
	 * 获取2工作区数据
	 *//*
	public void getData2( Object date ) {
		ResultSet rs = null;
		int columns = 0;
		rs = dbConn.query(String.format("select * from meq_bad_reason_count_report('%s') "
				+ "as (reason_name character varying(30),bad_count bigint)" ,date));
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			columns = rsmd.getColumnCount();// 获取结果集的列数line_id
		} catch (SQLException e) {
			e.printStackTrace();
		} // 获取结果集的元数据
	eportGeneral.writeExcel(eportGeneral.getExcelDate(rs, columns), filePath, 2, columns);
	}
	
	*//**
	 * 获取工作区3数据
	 *//*
	public void getData3(Object date, Object line_id) {
		Map<String, Object> dataMap = null;
		@SuppressWarnings("rawtypes")
		List<Map> list = new ArrayList<Map>();
		dataMap = new HashMap<String, Object>();
		String line_name = null;
		ResultSet rSet = dbConn.query(String.format("select line_name from v_meq_production_lines where line_id ='%s'",line_id));
		try {
			if (rSet.next()) {
			 line_name = rSet.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		dataMap.put("column1", date);
		dataMap.put("column2", line_name);
		list.add(dataMap);
		eportGeneral.writeExcel(list, filePath, 2, dataMap.size());
	}
	
	
	*//**
	 * 获取报表文件给前端显示
	 * @param request
	 * @param response
	 * @param fileCode
	 * @return
	 *//*
	public String getfileShow(HttpServletRequest request, HttpServletResponse response, String filePath) {
		try {
			fileUtil.fileDownload(response, filePath);
			return message.getSuccessInfo("下载成功");
		} catch (Exception e) {
		}
		return message.getErrorInfo("下载失败");
	}
}
*/