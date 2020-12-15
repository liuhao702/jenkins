package com.lc.bxm.cyjq.resources.product;

import java.sql.ResultSet;


import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.common.util.ReadExcel;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import net.sf.json.JSONObject;

/**
 * MEQ产品检验层别
 * @author LH
 * @date 2020年5月21日
 */
@RestController
@RequestMapping("/productLayer")
public class ProductInspectionLayerResources {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	ReadExcel read;
	@Autowired
	Message message;
	@Autowired
	PostgresqlHelper helper;
	
	/**
	 * LH 获取产品检验层别树状图JSON
	 */
	@RequestMapping(value = "prodLayerMenuJson", method = RequestMethod.GET)
 	@ResponseBody
 	public String prodMenuJson() {
		String prodJson = getProdMenuGroupJson("");
		JSONObject json = new JSONObject();
		json.put("id", "0");
		json.put("name", "全部");
		json.put("children", "["+prodJson+"]");
		return "["+json+"]";
 	}
	
	/**
 	 * LH 拼接产品检验层别分组JSON
 	 */
 	private String getProdMenuGroupJson(String parentId) {
		ResultSet rs = null;
		if (parentId == "") {
			rs = dbConn.query("select id, product_testing_item, up_product_level_id from cyj_product_inspection_level where up_product_level_id is null");
		} else {
			rs = dbConn.query(String.format(
					"select id, product_testing_item, up_product_level_id from cyj_product_inspection_level where up_product_level_id = '%s'", parentId));
		}
 		StringBuilder sb = new StringBuilder();
 		try {
 			while (rs.next()) {
 					sb.append("{\"id\":\"");
 					sb.append(rs.getString(1));
 					sb.append("\",\"name\":\"");
 					sb.append(rs.getString(2));
 					sb.append("\",\"up_product_level_id\":");
 					sb.append(rs.getString(3));
 					String str = getProdMenuGroupJson(rs.getString(1));
 	 				if (!str.equals("")) {
 						sb.append(",\"children\":[" + str + "]");
 	 				}
 	 				sb.append("},");
 			}
		} catch (SQLException e) {}
 		return Str.delComma(sb.toString());
 	}
 	
 	
 	
	/**
	 * LH 获取来料检验层别树状图JSON
	 */
	@RequestMapping(value = "mateLayerMenuJson", method = RequestMethod.GET)
 	@ResponseBody
 	public String materialsMenuJson() {
		String prodJson = getMaterialsMenuGroupJson("");
		JSONObject json = new JSONObject();
		json.put("id", "0");
		json.put("name", "全部");
		json.put("children", "["+prodJson+"]");
		return "["+json+"]";
 	}
	
	/**
 	 * LH 拼接利来料检验层别分组JSON
 	 */
 	private String getMaterialsMenuGroupJson(String parentId) {
		ResultSet rs = null;
		if (parentId == "") {
			rs = dbConn.query("select id, stratification_project, stratification_id	 from cyj_materials_inspection_stratification where stratification_id is null");
		} else {
			rs = dbConn.query(String.format(
					"select id, stratification_project, stratification_id from cyj_materials_inspection_stratification where stratification_id = '%s'", parentId));
		}
 		StringBuilder sb = new StringBuilder();
 		try {
 			while (rs.next()) {
 					sb.append("{\"id\":\"");
 					sb.append(rs.getString(1));
 					sb.append("\",\"name\":\"");
 					sb.append(rs.getString(2));
 					sb.append("\",\"stratification_id\":");
 					sb.append(rs.getString(3));
 					String str = getMaterialsMenuGroupJson(rs.getString(1));
 	 				if (!str.equals("")) {
 						sb.append(",\"children\":[" + str + "]");
 	 				}
 	 				sb.append("},");
 			}
		} catch (SQLException e) {}
 		return Str.delComma(sb.toString());
 	}
}