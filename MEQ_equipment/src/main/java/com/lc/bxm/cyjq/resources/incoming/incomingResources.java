package com.lc.bxm.cyjq.resources.incoming;

import java.sql.ResultSet;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.util.TreeDateUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

/**
 * MEQ产品检验属性
 * @author LH
 * @date 2020年5月21日
 */
@Api(value = "产品检验属性接口类", tags = { "产品检验属性接口类" })
@RestController
@RequestMapping("/incoming")
public class incomingResources {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	TreeDateUtil tree;
	
	
	/**
	 * LH 获取产品检验属性树状图JSON
	 */
	@ApiOperation(value = "产品检验属性树状图JSON接口", notes = "产品检验属性树状图JSON接口")
	@RequestMapping(value = "incomingTypeMenuJson", method = RequestMethod.GET)
 	@ResponseBody
 	public String prodMenuJson() {
		ResultSet rs = dbConn.query("select id, type_name, parent_type_id from cyj_materials_type");
		List<Map<Object, Object>> prodJson = tree.getResultSet(rs);
		JSONObject json = new JSONObject();
		json.put("id", "0");
		json.put("name", "全部");
		json.put("children", prodJson);
		return "["+json+"]";
 	}
	
 	/**
	 * LH 获取来料类别树状图JSON
	 */
 	@ApiOperation(value = "来料类别树状图JSON接口", notes = "来料类别树状图JSON接口")
	@RequestMapping(value = "incomingCateMenuJson", method = RequestMethod.GET)
 	@ResponseBody
 	public String prodCateMenuJson() {
 		ResultSet rs = dbConn.query("select id, cate_name, parent_cate_id from cyj_materials_cate");
		List<Map<Object, Object>> prodJson = tree.getResultSet(rs);
		JSONObject json = new JSONObject();
		json.put("id", "0");
		json.put("name", "全部");
		json.put("children", prodJson);
		return "["+json+"]";
 	}
 	
	/**
	 * LH 获取产品检验属性树状图JSON
	 */
 	@ApiOperation(value = "产品检验属性树状图JSON接口", notes = "产品检验属性树状图JSON接口")
	@RequestMapping(value = "incomingMenuJson", method = RequestMethod.GET)
 	@ResponseBody
 	public String incomingMenuJson() {
		ResultSet rs = dbConn.query("select id, type_name, parent_type_id from cyj_materials_type");
		List<Map<Object, Object>> prodJson = tree.getResultSet(rs);
		ResultSet rs1 = dbConn.query("select id, cate_name, parent_cate_id from cyj_materials_cate");
		List<Map<Object, Object>> prodJson1 = tree.getResultSet(rs1);
		JSONObject jsonCate = new JSONObject();
		JSONObject jsonType = new JSONObject();
		jsonType.put("id", "0");
		jsonType.put("name", "来料类型");
		jsonType.put("children", prodJson);
		jsonCate.put("id", "1");
		jsonCate.put("name", "来料类别");
		jsonCate.put("children", prodJson1);
		JSONObject json = new JSONObject();
		json.put("id", "0");
		json.put("name", "全部");
		json.put("children", "["+jsonType+","+jsonCate+"]");
		return "["+json+"]";
 	}

}