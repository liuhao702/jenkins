package com.lc.bxm.system.resources;

import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.DataList;
import com.lc.bxm.common.helper.RedisUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;

@RestController
@RequestMapping("/homePage")
public class HomePageResource {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	RedisUtil redis;
	@Autowired
	DataList  datalist;
	
	/**
	 * 主页头部数据
	 * @return
	 */
	@RequestMapping(value = "homePageTopData", method = RequestMethod.GET)
	@ResponseBody
	public String getHomePageTopData() {
	 String  fileName = "homePage/homePageData.json";
	 JSONObject json = null;
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
			json = JSONObject.parseObject(IOUtils.toString(is, "utf-8"));
		} catch (Exception e) {
			System.out.println(fileName + "文件读取异常" + e);
			return message.getErrorInfo(fileName+"文件读取异常"+e.getMessage());
		}
		return json.toJSONString();
	}
	
	/**
	 * 过程直通率
	 * @param line_id
	 * @return
	 */
	@RequestMapping(value = "homePageRtyData", method = RequestMethod.GET)
	@ResponseBody
	public List<Object> getHomePageRtyData(Integer line_id) {
	ResultSet rs = dbConn.query("select rty, substring(create_time::text from 0 for 11 ) from v_cyj_process_inspection_rty  where line_id ="+line_id);
	List<Object> list = new ArrayList<Object>();
	try {
		while (rs.next()) {
			JSONObject json = new JSONObject();
			json.put("rty", rs.getObject(1));
			json.put("create_time",rs.getObject(2) );
			list.add(json);
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	return list;
	}
	
	/**
	 * 过程缺陷数量
	 * @return
	 */
	@RequestMapping(value = "homePageDefectsData", method = RequestMethod.GET)
	@ResponseBody
	public List<Object> getHomePageDefectsData() {
	ResultSet rs = dbConn.query("select * from v_process_defects");
	List<Object> list = new ArrayList<Object>();
	try {
		while (rs.next()) {
			JSONObject json = new JSONObject();
			json.put("reason_name", rs.getObject(1));
			json.put("d_rate",rs.getObject(2));
			json.put("count",rs.getObject(3));
			list.add(json);
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	return list;
	}
	
}
