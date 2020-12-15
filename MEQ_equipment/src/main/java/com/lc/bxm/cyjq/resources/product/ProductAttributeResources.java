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
 * MEQ产品检验属性
 * @author LH
 * @date 2020年5月21日
 */
@RestController
@RequestMapping("/productAttribute")
public class ProductAttributeResources {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	ReadExcel read;
	@Autowired
	Message message;
	@Autowired
	PostgresqlHelper helper;
	
	/**
	 * LH 获取产品检验属性树状图JSON
	 */
	@RequestMapping(value = "prodAttrMenuJson", method = RequestMethod.GET)
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
 	 * LH 拼接产品检验属性分组JSON
 	 */
 	private String getProdMenuGroupJson(String parentId) {
		ResultSet rs = null;
		if (parentId == "") {
			rs = dbConn.query("select id, product_testing_pro, up_product_pro_ids from cyj_product_inspection_setting where up_product_pro_ids is null");
		} else {
			rs = dbConn.query(String.format(
					"select id, product_testing_pro, up_product_pro_ids from cyj_product_inspection_setting where up_product_pro_ids = '%s'", parentId));
		}
 		StringBuilder sb = new StringBuilder();
 		try {
 			while (rs.next()) {
				sb.append("{\"id\":\"");
				sb.append(rs.getString(1));
				sb.append("\",\"name\":\"");
				sb.append(rs.getString(2));
				sb.append("\",\"up_product_pro_ids\":");
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
	 * LH 获取来料检验属性树状图JSON
	 */
	@RequestMapping(value = "materialsMenuJson", method = RequestMethod.GET)
 	@ResponseBody
 	public String materialsMenuJson() {
		String prodJson = getMaterialMenuGroupJson("");
		JSONObject json = new JSONObject();
		json.put("id", "0");
		json.put("name", "全部");
		json.put("children", "["+prodJson+"]");
		return "["+json+"]";
 	}
	
	/**
 	 * LH 拼接来料检验属性分组JSON
 	 */
 	private String getMaterialMenuGroupJson(String parentId) {
		ResultSet rs = null;
		if (parentId == "") {
			rs = dbConn.query("select id, inspection_attribute, attribute_id from cyj_materials_inspection_attribute where attribute_id is null");
		} else {
			rs = dbConn.query(String.format(
					"select id, inspection_attribute, attribute_id from cyj_materials_inspection_attribute where attribute_id = '%s'", parentId));
		}
 		StringBuilder sb = new StringBuilder();
 		try {
 			while (rs.next()) {
				sb.append("{\"id\":\"");
				sb.append(rs.getString(1));
				sb.append("\",\"name\":\"");
				sb.append(rs.getString(2));
				sb.append("\",\"attribute_id\":");
				sb.append(rs.getString(3));
				String str = getMaterialMenuGroupJson(rs.getString(1));
 				if (!str.equals("")) {
					sb.append(",\"children\":[" + str + "]");
 				}
 				sb.append("},");
 			}
		} catch (SQLException e) {}
 		return Str.delComma(sb.toString());
 	}
}