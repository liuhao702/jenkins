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
 * 生产综合信息报表
 * @author lh
 *
 */
@RestController
@RequestMapping("/reportForm")
public class ProductionSynthesis {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;

	private static ReportGeneral reportGeneral = new ReportGeneral();
	private static FileUtil fileUtil = new FileUtil();
	private String filePath =null;
	

	@RequestMapping(value = "getProductionSynthesis", method = RequestMethod.POST)
	@ResponseBody
	public String commonReportForm(HttpServletRequest request, HttpServletResponse response, @RequestBody String jsonData) {
		filePath=dbConn.getPropertiesYun("fileRepot")+"/productionSynthesis.xlsx";
		JSONObject jsonObject = JSONObject.fromObject(jsonData);
		Object date = jsonObject.get("date");
		Object line_id = jsonObject.get("line_id");
		      // 如果没有传时间和产线就取当前日期和最小的那条线
		
		if ( date == null || date.equals("") || date.equals("null")) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
			date = df.format(new Date());
		}
		/*
		 * if ( line_id == null || line_id.equals("")|| line_id.equals("null")) {
		 * line_id = getMinLine();//获取最小产线 }
		 */
			for (int i = 1; i <= 5; i++) {
				switch (i) {
				case 1:
				   getData(date,line_id);
					break;
				case 2:
				   //getData2(date);
					break;
				case 3:
				   //getData3(date,line_id);
				    break;								    
				}
			}
		return getfileShow(request, response, "productionSynthesis.xlsx");
	}
    
	
	
	
	/**
	 * 获取1工作区数据
	 */
	public void getData(Object date ,Object line_id) {
		ResultSet rs = null;
		int columns = 0;
		if (line_id == null || line_id.equals("")|| line_id.equals("null")) {
			System.err.println(String.format("SELECT  line_name , morder_code , qty , product_code ," + 
					"  product_name , online_qty , offline_qty ,day_qty , day_offline_qty ,complete_rate , well_rate ," + 
					"  product_status  from meq_product_general_information('%s')",date));
		rs = dbConn.query(String.format("SELECT  line_name , morder_code , qty , product_code ," + 
				"  product_name , online_qty , offline_qty ,day_qty , day_offline_qty ,complete_rate , well_rate ," + 
				"  product_status  from meq_product_general_information('%s')",
			date));		
		}else {
			
		rs = dbConn.query(String.format("SELECT  line_name , morder_code , qty , product_code ," + 
				"  product_name , online_qty , offline_qty ,day_qty , day_offline_qty ,complete_rate , well_rate ," + 
				"  product_status  from meq_product_general_information('%s') where line_id = %s",
			date,line_id));		
		}
		try {
			ResultSetMetaData rsmd = rs.getMetaData(); // 获取结果集的元数据
			columns = rsmd.getColumnCount();// 获取结果集的列数
		} catch (SQLException e) {
			e.printStackTrace();
		}
		reportGeneral.writeExcel(reportGeneral.getExcelDate(rs, columns), filePath, 1, columns);
	}

	
	/**
	 * 获取2工作区数据
	 */
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
	reportGeneral.writeExcel(reportGeneral.getExcelDate(rs, columns), filePath, 2, columns);
	}
	
	/**
	 * 获取工作区3数据
	 */
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
		reportGeneral.writeExcel(list, filePath, 2, dataMap.size());
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
