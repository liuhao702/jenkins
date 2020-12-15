package com.lc.bxm.system.resources;

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

@RestController
@RequestMapping("/datasource")
public class DatasourceResource {

	@Autowired
	PostgreSQLConn dbConn;
	
	/**
	 * 表字段联动
	 */
	@RequestMapping(value = "columnName", method = RequestMethod.GET)
	@ResponseBody
	public String productionLine(@RequestParam String tableName) {
		StringBuilder sb = new StringBuilder();
		JSONObject json = new JSONObject();
		String sql = String.format("SELECT column_name,column_name FROM v_table_name WHERE table_name = '%s'", tableName);
		ResultSet rs = dbConn.query(sql);
		try {
			while (rs.next()) {
				json.put("id", rs.getString(1));
				json.put("value", rs.getString(2));
				sb.append(json.toString()+",");
				//sb.append("{\"id\":\"" + rs.getString(1) + "\",\"value\":\"" + rs.getString(2) + "\"},");
			}
		} catch (SQLException e) {}
		return "[" + Str.delComma(sb.toString()) + "]";
	}
	
}
