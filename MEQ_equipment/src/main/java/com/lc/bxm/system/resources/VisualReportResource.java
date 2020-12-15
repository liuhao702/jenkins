package com.lc.bxm.system.resources;

import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 图形报表接口
 * @author LJZ
 * @date 2019年8月20日
 */
@RestController
@RequestMapping("/visualReport")
public class VisualReportResource {

	@Autowired
	PostgreSQLConn dbConn;
	
	@Autowired
	Message message;
	
	@Autowired
	GetLogs getLogs;
	
	@Autowired
	PostgresqlHelper pgHelper;

	private String tableName = "sys_visual_reports";
	private String keyColumnName = "report_id";
	
	/**
	 * LJZ 菜单所有报表动态拼接
	 */
	@RequestMapping(value = "visaulJson", method = RequestMethod.GET)
	@ResponseBody
	public String getMenuVisualJson(@RequestParam String menuId) {
		StringBuilder jsonBuilder = new StringBuilder();
		try {
			ResultSet resultSet = dbConn.query(String.format("SELECT report_id,title,visual_type,width,height,x_distance,y_distance,sql,view_name,date_type,"
									+ "bxm_get_default_value(default_date) AS default_date,place_holder,is_station,default_station FROM sys_visual_reports  "
									+ "WHERE menu_uid = '%s' AND visual_type IS NOT NULL AND NOT is_deactive",menuId));
			while (resultSet.next()) {
				jsonBuilder.append(getVisualJson(resultSet,resultSet.getString("default_date"),resultSet.getString("default_station")));
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		return String.format("[%s]", Str.delComma(jsonBuilder.toString()));
	}
	
	/**
	 * LJZ 单个报表拼接
	 */
	@RequestMapping(value = "singleVisaulJson", method = RequestMethod.GET)
	@ResponseBody
	public String getSingleVisualJson(@RequestParam String reportId,@RequestParam String date,@RequestParam String stationId) {
		StringBuilder jsonBuilder = new StringBuilder();
		try {
			ResultSet resultSet = dbConn.query(String.format("SELECT report_id,title,visual_type,width,height,x_distance,y_distance,sql,view_name,date_type,"
														+ "bxm_get_default_value(default_date) AS default_date,place_holder,is_station,default_station  "
														+ "FROM sys_visual_reports  WHERE report_id = '%s' AND visual_type IS NOT NULL AND NOT is_deactive",reportId));
			while (resultSet.next()) {
				jsonBuilder.append(getVisualJson(resultSet,date,stationId));
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		return String.format("[%s]", Str.delComma(jsonBuilder.toString()));
	}
	
	//拼装图形json
	private String getVisualJson(ResultSet resultSet,String date,String stationId) {
		StringBuilder jsonBuilder = new StringBuilder();
		try {
			jsonBuilder.append(getVisualPropJson(resultSet.getInt("report_id"),resultSet.getString("title"),resultSet.getInt("width"),resultSet.getInt("height"),
								resultSet.getString("visual_type"),resultSet.getInt("x_distance"),resultSet.getInt("y_distance"),resultSet.getString("view_name"),
								resultSet.getString("date_type"),resultSet.getString("place_holder"),resultSet.getBoolean("is_station"),
								date,stationId));
			
			if(resultSet.getString("visual_type").equals("line") || resultSet.getString("visual_type").equals("histogram")) {
				jsonBuilder.append(getLineDataJson(resultSet.getString("view_name"),resultSet.getString("date_type"),date,stationId,resultSet.getBoolean("is_station")));
			}else if(resultSet.getString("visual_type").equals("pie")) {
				jsonBuilder.append(getPieDataJson(resultSet.getString("view_name"),resultSet.getString("date_type"),date,stationId,resultSet.getBoolean("is_station")));
			}else if(resultSet.getString("visual_type").equals("line_histogram")) {
				jsonBuilder.append(getPLineHistogramDataJson(resultSet.getString("view_name"),resultSet.getString("date_type"),date,stationId,resultSet.getBoolean("is_station")));
			}else if(resultSet.getString("visual_type").equals("table")) {
				jsonBuilder.append(getTableDataJson(resultSet.getString("view_name"),resultSet.getString("date_type"),date,stationId,resultSet.getBoolean("is_station")));
			}
			jsonBuilder.append("},");
		} catch (Exception e) {
			// TODO: handle exception
		}
		return jsonBuilder.toString();
	}
	
	//拼装图形属性json
	private String getVisualPropJson(Integer reportId,String title,Integer width,Integer height,String type,Integer x_distance,Integer y_distance,
											String viewName,String dateType,String placeHolder,boolean isStation,String date,String stationId) {
		StringBuilder propBuilder = new StringBuilder();
		String stationList = null;
		String yMarkName = null;
		Boolean visualCanDelete = false;
		
		propBuilder.append("{");
		propBuilder.append("\"report_id\":").append(reportId).append(",");
		propBuilder.append("\"width\":").append(width).append(",");
		propBuilder.append("\"height\":").append(height).append(",");
		propBuilder.append("\"type\":\"").append(type).append("\",");
		propBuilder.append("\"x_distance\":").append(x_distance).append(",");
		propBuilder.append("\"y_distance\":").append(y_distance).append(",");
		propBuilder.append("\"draggable\":").append("false").append(",");
		propBuilder.append("\"chart\":").append("null").append(",");
		propBuilder.append("\"title\":\"").append(title).append("\",");
		if(dateType == null) {
			propBuilder.append("\"date\":").append(dateType).append(",");
		}else {
			propBuilder.append("\"date\":\"").append(dateType).append("\",");
		}
		propBuilder.append("\"default_font_date\":\"").append(date).append("\",");
		propBuilder.append("\"place_holder\":\"").append(placeHolder).append("\",");
		propBuilder.append("\"showSations\":").append(isStation).append(",");
		propBuilder.append("\"sations\":").append(stationId).append(",");
		
		try {
			ResultSet visual = dbConn.query("SELECT setting_value FROM sys_settings WHERE setting_code = 'VisualCanDelete'");
			if(visual.next()) {
				visualCanDelete = visual.getString(1).equals("true");
			}
			
			ResultSet staResultSet = dbConn.query(String.format("SELECT array_to_json(array_agg(row_to_json(a.*))) FROM (" + 
					" SELECT station_id AS id,station_name AS value FROM meq_line_stations s" + 
					" JOIN (SELECT line_id FROM meq_line_stations WHERE station_id = %s) l ON s.line_id = l.line_id) a", stationId));
			if(staResultSet.next()) {
				stationList = staResultSet.getString(1);
			}
			propBuilder.append("\"s_options\":").append((stationList == null || stationList.isEmpty()) ? "[]" : stationList).append(",");
			
			ResultSet caption = dbConn.query(String.format("SELECT string_agg(caption,',') FROM v_sys_columns WHERE table_name = '%s'", viewName));
			if(caption.next()) {
				String[] split = caption.getString(1).split(",");
				yMarkName = split[1];
			}
			propBuilder.append("\"yMarkName\":\"").append(yMarkName).append("\",");		
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		propBuilder.append("\"shutdown\":").append(visualCanDelete).append(",");
		return propBuilder.toString();
	}
	
	//拼装柱状图,折线图数据json
	private String getLineDataJson(String viewName,String dateType,String date,String stationId,boolean isStation) {
		StringBuilder xData = new StringBuilder();
		StringBuilder yData = new StringBuilder();
		String stationString = String.format("%s", isStation ? String.format(" AND station_id = '%s'", stationId) : "");
		String whereString = String.format(" WHERE %s = '%s' %s", dateType,date,stationString);
		try {
			ResultSet resultSet = dbConn.query(String.format("SELECT * FROM %s %s", viewName,whereString));
			while (resultSet.next()) {
				xData.append("\"").append(resultSet.getString(1)).append("\",");
				yData.append(resultSet.getInt(2)).append(",");
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		return String.format("\"xData\":[%s],\"yData\":[%s]", Str.delComma(xData.toString()),Str.delComma(yData.toString()));
	}
	
	//拼装饼图数据json
	private String getPieDataJson(String viewName,String dateType,String date,String stationId,boolean isStation) {
		StringBuilder xData = new StringBuilder();
		StringBuilder yData = new StringBuilder();
		String stationString = String.format("%s", isStation ? String.format(" AND station_id = %s", stationId) : "");
		String whereString = String.format(" WHERE %s = '%s' %s", dateType,date,stationString);
		try {
			ResultSet resultSet = dbConn.query(String.format("SELECT * FROM %s %s", viewName,whereString));
			while (resultSet.next()) {
				xData.append("\"").append(resultSet.getString(1)).append("\",");
				yData.append("{\"value\":").append(resultSet.getInt(2)).append(",");
				yData.append("\"name\":\"").append(resultSet.getString(1)).append("\"},");
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		return String.format("\"xData\":[%s],\"yData\":[%s]", Str.delComma(xData.toString()),Str.delComma(yData.toString()));
	}
	
	//拼装柱状-折线图数据json
	private String getPLineHistogramDataJson(String viewName,String dateType,String date,String stationId,boolean isStation) {
		StringBuilder xData = new StringBuilder();
		StringBuilder yDataLeft = new StringBuilder();
		StringBuilder yDataRight = new StringBuilder();
		String leftString = "";
		String rightString = "";
		Integer max = 0;
		String stationString = String.format("%s", isStation ? String.format(" AND station_id = %s", stationId) : "");
		String whereString = String.format(" WHERE %s = '%s' %s", dateType,date,stationString);
		try {
			ResultSet caption = dbConn.query(String.format("SELECT string_agg(caption,',') FROM v_sys_columns WHERE table_name = '%s'", viewName));
			ResultSet resultSet = dbConn.query(String.format("SELECT * FROM %s %s", viewName,whereString));
			while (resultSet.next()) {
				xData.append("\"").append(resultSet.getString(1)).append("\",");
				yDataLeft.append(resultSet.getInt(2)).append(",");
				yDataRight.append(resultSet.getInt(3)).append(",");
				if(max < resultSet.getInt(2))max = resultSet.getInt(2);
				if(max < resultSet.getInt(3))max = resultSet.getInt(3);
			}
			if(caption.next()) {
				String[] split = caption.getString(1).split(",");//以逗号分割
				leftString = String.format("{\"name\":\"%s\",\"type\":\"bar\",\"barWidth\":12,\"data\":[%s]},", split[1],Str.delComma(yDataLeft.toString()));
				rightString = String.format("{\"name\":\"%s\",\"type\":\"line\",\"yAxisIndex\":1,\"data\":[%s]}", split[2],Str.delComma(yDataRight.toString()));
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		return String.format("\"xData\":[%s],\"yData\":[%s%s],\"Max\":%s", Str.delComma(xData.toString()),
												leftString.toString(),rightString.toString(),Math.ceil(max * 1.1));
	}
		
	//拼装表格图数据json
	private String getTableDataJson(String viewName,String dateType,String date,String stationId,boolean isStation) {
		StringBuilder xData = new StringBuilder();
		StringBuilder yData = new StringBuilder();
		String stationString = String.format("%s", isStation ? String.format(" AND station_id = %s", stationId) : "");
		String whereString = String.format(" WHERE %s = '%s' %s", dateType,date,stationString);
		String columnString = null;
		try {
			ResultSet columnSet = dbConn.query(String.format("SELECT string_agg(column_name,',') FROM v_sys_columns WHERE table_name = '%s' AND is_visible AND NOT is_deactive",viewName));
			if(columnSet.next()) {
				columnString = columnSet.getString(1);
			}
			
			ResultSet xDaSet = dbConn.query(String.format("SELECT caption FROM v_sys_columns WHERE table_name = '%s' AND is_visible AND NOT is_deactive", viewName));
			while (xDaSet.next()) {
				xData.append("\"").append(xDaSet.getString(1)).append("\",");			
			}

			ResultSet resultSet = dbConn.query(String.format("SELECT %s FROM %s %s", columnString,viewName,whereString));
			Integer count = resultSet.getMetaData().getColumnCount();
			while (resultSet.next()) {
				yData.append("[");
				for(int i = 1;i<=count;i++) {
					yData.append("\"").append(resultSet.getString(i)).append("\"");
					if(i != count) yData.append(",");
				}
				yData.append("],");
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
		return String.format("\"xData\":[%s],\"yData\":[%s]", Str.delComma(xData.toString()),Str.delComma(yData.toString()));
	}
		
	/**
	 * LJZ 图形报表编辑接口
	 */
	@RequestMapping(value = "updateVisual", method = RequestMethod.POST)
	@ResponseBody
	public String updateVisual(HttpServletRequest request, @RequestBody JSONObject jsonUpdateData) {
		try {
			JSONArray jsonArray = JSONArray.fromObject(jsonUpdateData.getString("formData"));
			boolean result = pgHelper.dataMultiUpdate(request, jsonUpdateData.getString("menuId"), jsonUpdateData.getString("funName"), 
													jsonUpdateData.getString("userId"), tableName, jsonArray);
			return result ? message.getSuccessInfo("保存成功") : message.getErrorInfo("保存失败");
		} catch (Exception e) {
			return message.getErrorInfo("保存失败");
		}
	}
	
	/**
	 * LJZ 图形报表编辑接口
	 */
	@RequestMapping(value = "deleteVisual", method = RequestMethod.POST)
	@ResponseBody
	public String deleteVisual(HttpServletRequest request, @RequestBody JSONObject jsonDeleteData) {
		try {
			boolean result = pgHelper.dataDelete(request, jsonDeleteData.getString("menuId"), "删除", 
											jsonDeleteData.getString("userId"), tableName, keyColumnName, jsonDeleteData.getString("idValue"));
			return result ? message.getSuccessInfo("删除成功") : message.getErrorInfo("删除失败");
		} catch (Exception e) {
			return message.getErrorInfo("删除失败");
		}
	}	
	
	/**
	 * LJZ 图形报表编辑接口
	 */
	@RequestMapping(value = "test", method = RequestMethod.POST)
	@ResponseBody
	public String test(HttpServletRequest request, @RequestBody JSONObject jsonDeleteData) {
		try {
			//InsertData insertData = (InsertData)JSONObject.fromObject();
			return "";
		} catch (Exception e) {
			return message.getErrorInfo("删除失败");
		}
	}	
}
