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
 * 设备类别接口
 * @author JF
 * @date 2019年6月19日
 */
@RestController
@RequestMapping("/cate")
public class CateResources {

	@Autowired
	PostgreSQLConn dbConn;
	
	/**
	 * JF 类别树状图表格分页
	 */
	@RequestMapping(value = "cateTableJson", method = RequestMethod.GET)
	@ResponseBody
	public String getTreeTableJson( @RequestParam int pageSize,@RequestParam int currentPage, @RequestParam String inputSearch, 
			@RequestParam String order, @RequestParam String prop, @RequestParam String filterString,@RequestParam String id,@RequestParam String level) {
		if (filterString != "") {
			filterString = filterString.replace("'", "''");
		}
		String userJson = null;
		if (order.equals("descending")) {
			order = "desc";
		}
		if (order.equals("ascending")) {
			order = "asc";
		}
		//根据传进来的树的表名和ID查找出所有的ID
		if(!id.equals("") && id != null) {
			//如果是一级类别要找出相应的二级类别ID集合
			if(level.equals("1")) {
				//如果一级类别下面没有二级类别,就查空
				if(!getId(id).equals("")) {
					filterString = filterString +" and cate_id in (" + getId(id) + ")";
				}else {
					filterString = filterString +" and cate_id = null";
				}
			}else {//如果是二级类别就直接查自身
				filterString = filterString +" and cate_id = " + id;
			}
		}
		String sql = "select bxm_get_grid_page_json('v_meq_resources','*','','" + filterString + "','"
				+ inputSearch + "','" + prop + " " + order + "'," + pageSize + "," + currentPage + ")";
		ResultSet rs = dbConn.query(sql);
		try {
			if (rs.next()) {
				userJson = rs.getString(1);
			}
		} catch (SQLException e) {}
		return userJson;
	}
	
	/**
	 * JF 根据传进来的ID判断是否为一级类别，如果是一级类别就找所属的二级类别
	 */
	private String getId(String id) {
		StringBuilder sb = new StringBuilder();
		ResultSet rs = dbConn.query("select cate_id from v_meq_resource_cates_tree where top_cate_id = '" + id + "'");
		try {
			while (rs.next()) {
				sb.append(rs.getString(1) + ",");
			}
		} catch (SQLException e) {}
		return Str.delComma(sb.toString());
	}

	/**
	 * JF 类别树状图
	 */
	@RequestMapping(value = "cateJson", method = RequestMethod.GET)
	@ResponseBody
	private String getCateJson() {
		ResultSet rs = dbConn.query("select top_cate_id,top_cate_name from meq_resource_top_cates order by idx");
		StringBuilder sb = new StringBuilder();
		JSONObject json = new JSONObject();
		JSONObject jsons = new JSONObject();
		try {
			while (rs.next()) {
				json.put("id", rs.getString(1));
				json.put("name", rs.getString(2));
				json.put("level", "1");
//				sb.append("{\"id\":\"");
//				sb.append(rs.getString(1));
//				sb.append("\",\"name\":\"");
//				sb.append(rs.getString(2));
//				sb.append("\",\"level\":\"1\"");
				String str = cate(rs.getString(1));
				json.put("children", str);
				sb.append(json.toString()+",");
				//sb.append(str);
				//sb.append("},");
				jsons.put("id", "0");
				jsons.put("name", "全部");
				jsons.put("children", "["+Str.delComma(sb.toString())+"]");
			}
		} catch (SQLException e) {}
		//return "[{\"id\":\"0\",\"name\":\"全部\",\"children\":[" + Str.delComma(sb.toString()) + "]}]";
		return "["+jsons.toString()+"]";
	}
	
	/**
	 * JF 根据一级类别ID获取二级类别
	 */
	private String cate(String topCateId) {
		ResultSet rs = dbConn.query("select cate_id,cate_name from meq_resource_cates where top_cate_id = '" + topCateId + "'");
		StringBuilder sb = new StringBuilder();
		JSONObject json = new JSONObject();
 		try {
			while (rs.next()) {
				json.put("id", rs.getString(1));
				json.put("name", rs.getString(2));
				sb.append(json.toString()+",");
//				sb.append("{\"id\":\"");
//				sb.append(rs.getString(1));
//				sb.append("\",\"name\":\"");
//				sb.append(rs.getString(2));
//				sb.append("\"},");
			}
		} catch (SQLException e) {}
		return "[" + Str.delComma(sb.toString()) + "]";
	}

}
