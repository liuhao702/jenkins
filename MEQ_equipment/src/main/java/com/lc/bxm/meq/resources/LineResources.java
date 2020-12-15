package com.lc.bxm.meq.resources;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.helper.Str;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONObject;

/**
 * MEQ产线工位接口
 * @author JF
 * @date 2019年7月2日
 */
@RestController
@RequestMapping("/line")
public class LineResources {
	
	@Autowired
	PostgreSQLConn dbConn;
	
	/**
	 * JF 根据产线ID获取下面的工位
	 */
	@RequestMapping(value = "stationJson", method = RequestMethod.GET)
	@ResponseBody
	public String getStationJson(@RequestParam String lineId) {
		StringBuilder sb = new StringBuilder();
		JSONObject json = new JSONObject();
 		if(lineId.equals("")) {
			return "[]";
		}
		String sql = "select station_id,station_name from meq_line_stations where line_id = " + lineId;
		ResultSet rs = dbConn.query(sql);
		try {
			while(rs.next()) {
				json.put("id", rs.getString(1));
				json.put("value", rs.getString(2));
				sb.append(json.toString()+",");
//				sb.append("{\"id\":" + rs.getString(1));
//				sb.append(",\"value\":\"" + rs.getString(2) + "\"},");
			}
		} catch (SQLException e) {}
		return "[" + Str.delComma(sb.toString()) + "]";
	}
	
}
