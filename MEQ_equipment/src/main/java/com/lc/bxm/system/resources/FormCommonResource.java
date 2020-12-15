package com.lc.bxm.system.resources;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;

import com.lc.bxm.common.helper.Str;
import com.lc.bxm.dbconnection.PostgreSQLConn;

public class FormCommonResource {
	
	@Autowired
	PostgreSQLConn dbConn;
	
	/**
	 * LJZ 下拉框层级
	 */
	@SuppressWarnings("unused")
	private String getSelectTreeJson(String viewName, String valueMember, String displayMember, String filterString,
			String parentUid) {
		ResultSet rs = null;
		if (parentUid == "") {
			rs = dbConn.query(String.format("SELECT * FROM %s WHERE %s parent_uid IS NULL", viewName,
					filterString == null ? "" : filterString + " AND "));
		} else {
			rs = dbConn.query(String.format("SELECT * FROM %S WHERE %s parent_uid = '%s'", viewName,
					filterString == null ? "" : filterString + " AND ", parentUid));
		}
		StringBuilder sb = new StringBuilder();
		try {
			while (rs.next()) {
				sb.append("{");
				sb.append("\"id\":\"" + rs.getString(valueMember) + "\",");
				sb.append("\"label\":\"" + rs.getString(displayMember) + "\"");
				String str = getSelectTreeJson(viewName, valueMember, displayMember, filterString, rs.getString(valueMember));
				if (!str.equals("")) {
					sb.append(",\"children\":[" + str + "]");
				}
				sb.append("},");
			}
		} catch (SQLException e) {}
		return Str.delComma(sb.toString());
	}
}
