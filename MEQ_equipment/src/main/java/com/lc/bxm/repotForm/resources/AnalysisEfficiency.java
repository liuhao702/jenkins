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

import net.sf.json.JSONObject;



/**
 * 分时效率分析报表
 * @author lh
 */
@RestController
@RequestMapping("/reportForm")
public class AnalysisEfficiency {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;
    
	
	private  ReportGeneral eportGeneral = new ReportGeneral();
	private static FileUtil fileUtil = new FileUtil();
	private static String filePath = null;
	
	/**
	 * 分时效率分析报表
	 * @param request
	 * @param response
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "getEfficiencyAnalysisReportForm", method = RequestMethod.POST)
	@ResponseBody
	public String commonReportForm(HttpServletRequest request, HttpServletResponse response, @RequestBody String jsonData) {
		//System.err.println(jsonData);
		filePath=dbConn.getPropertiesYun("fileRepot")+"/analysisEfficiency.xlsx";
		JSONObject jsonObject = JSONObject.fromObject(jsonData);
		String date = jsonObject.get("date").toString();
		Object line_id = jsonObject.get("line_id");
		      // 如果没有传时间和产线就取当前日期和最小的那条线
		
		if ( date == null || date.equals("") || date.equals("null")) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
			date = df.format(new Date());
		}
		if ( line_id == null || line_id.equals("")|| line_id.equals("null")) {
			 line_id = getMinLine();//获取最小产线
		}
			for (int i = 1; i <= 3; i++) {
				switch (i) {
				case 1:
					getData(date,line_id);
					break;
				case 2:
				    getData2(line_id);
					break;
				case 3:
				    getData3(date);
					break;
				}
			}
		return getfileShow(request, response, "analysisEfficiency.xlsx");
	}
    
	/**
	 * 获取最小的那条产线
	 * @return
	 */
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
	
	/**
	 * 获取1工作区数据
	 */
	public void getData(String date, Object line_id) {
		ResultSet rs = null;
		int columns = 0;
		//判断是否是周末
		if (eportGeneral.isWeekend(date).equals("KO")) {
			rs = dbConn.query(String.format("select * from meq_weekend_hour_output_report('%s',%s) AS"
					+ " (plan_qty bigint, eight_qty bigint, nine_qty bigint, ten_qty bigint, eleven_qty bigint, fourteen_qty bigint, "
					+ "fifteen_qty bigint, sixteen_qty bigint, seven_teen_qty bigint, eighteen_qty bigint)", 
					date, line_id));
		} else {
			rs = dbConn.query(String.format("select * from meq_week_hour_output_report('%s',%s) AS" + 
					"(plan_qty bigint, eight_qty bigint, nine_qty bigint, ten_qty bigint," + 
					" eleven_qty bigint, fourteen_qty bigint, fifteen_qty bigint, sixteen_qty bigint, seven_teen_qty bigint, eighteen_qty bigint, nineteen_qty bigint, twenty_qty bigint)",
					date, line_id));
		}
		try {
			ResultSetMetaData rsmd = rs.getMetaData(); // 获取结果集的元数据
			columns = rsmd.getColumnCount();// 获取结果集的列数
		} catch (SQLException e) {
			e.printStackTrace();
		}
		eportGeneral.writeExcel(eportGeneral.getExcelDate(rs, columns), filePath, 1, columns);
	}

	
	/**
	 * 获取3工作区数据
	 */
	public void getData2( Object line_id) {
		ResultSet rs = null;
		int columns = 0;
		rs = dbConn.query(String.format("select line_name,person_liable from v_meq_production_lines "
				+ "where line_id=(select min(%s) from v_meq_production_lines)",
				 line_id));
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			columns = rsmd.getColumnCount();// 获取结果集的列数line_id
		} catch (SQLException e) {
			e.printStackTrace();
		} // 获取结果集的元数据
		eportGeneral.writeExcel(eportGeneral.getExcelDate(rs, columns), filePath, 2, columns);
	}
	
	/**
	 * 获取工作区3数据
	 */
	public void getData3(Object date) {
		Map<String, Object> dataMap = null;
		@SuppressWarnings("rawtypes")
		List<Map> list = new ArrayList<Map>();
		dataMap = new HashMap<String, Object>();
		dataMap.put("column1", date);
		list.add(dataMap);
		eportGeneral.writeExcel(list, filePath, 3, 1);
	}
	
	
	/**
	 * 获取报表文件给前端显示
	 * @param request
	 * @param response
	 * @param fileCode
	 * @return
	 */
	public String getfileShow(HttpServletRequest request, HttpServletResponse response,  String fileName) {
		try {
			fileUtil.fileDownload(response, fileName);
			return message.getSuccessInfo("下载成功");
		} catch (Exception e) {
		}
		return message.getErrorInfo("下载失败");
	}
}
	
