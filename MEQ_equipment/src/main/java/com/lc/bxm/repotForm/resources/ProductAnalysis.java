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
 * 生产分析报表
 * @author lh
 *
 */
@RestController
@RequestMapping("/reportForm")
public class ProductAnalysis {
	
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
	private static String filePath = null;
	

	@RequestMapping(value = "getProductReportForm", method = RequestMethod.POST)
	@ResponseBody
	public String commonReportForm(HttpServletRequest request, HttpServletResponse response, @RequestBody String date) {
		filePath=dbConn.getPropertiesYun("fileRepot")+"/productAnalysis.xlsx";
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
		return getfileShow(request, response, "productAnalysis.xlsx");
	}

	/**
	 * 获取工作区1数据
	 */
	public void getData(String bigenDate, String endDate) {
		ResultSet rs = null;
		int columns = 0;
		//rs = dbConn.query(String.format("select line_name, person_liable, plan_qty, offline_qty,once_rate from meq_product_report('%s','%s') AS"
		//		+ " (line_name character varying(20),person_liable text,plan_qty bigint,offline_qty bigint,rate numeric,once_rate numeric)",
		//		bigenDate, endDate));
		rs = dbConn.query(String.format("select * from meq_product_report('%s','%s')",
				bigenDate, endDate));
		/*
		 * System.err.println(String.
		 * format("select * from meq_product_report('%s','%s')", bigenDate, endDate));
		 */
		System.err.println(String.format("select * from meq_product_report('%s','%s')",
				bigenDate, endDate));
		try {
			ResultSetMetaData rsmd = rs.getMetaData(); // 获取结果集的元数据
			columns = rsmd.getColumnCount();// 获取结果集的列数
		} catch (SQLException e) {
			e.printStackTrace();
		}
		reportGeneral.writeExcel(reportGeneral.getExcelDate(rs, columns), filePath, 1, columns);
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
		reportGeneral.writeExcel(list, filePath, 2, 1);
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
