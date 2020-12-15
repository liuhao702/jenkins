package com.lc.bxm.cyjq.resources.sheBeAppletsData;

import java.sql.ResultSet;


import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.Message;
import com.lc.bxm.common.util.TreeDateUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.system.resources.CommonResource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


@Api(value = " 小程序仪器设备检测数据接口", tags = { "小程序仪器设备检测数据接口" })
@RestController
@RequestMapping("/getApplets")
public class AppletsController {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	CommonResource comm;
	//获取下拉框数据方法
	@Autowired
	TreeDateUtil treeData;
	
	
	/**
	 * 总和检测数据
	 * @return
	 */
	@ApiOperation(value = "总检测数据", notes = "小程序头部综合数据")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "beginDate", value = "开始时间（1.当天就两个时间一样，2.昨天也是两个时间一样，一周就往前推七天），30天一样往前推", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "endDate", value = "结束时间", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "comp_id", value = "所属公司", paramType = "query", dataType = "Integer", required = true)
		
	})
	@RequestMapping(value = "getTotalCheckData", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String getTotalCheckData( String beginDate, String endDate, Integer comp_id) {
		ResultSet rs = null;
		JSONObject json = new JSONObject();
		List<Object> list = getEquipmentTypesTable(comp_id);
		try {
			if (list.size()==0) {
				return message.getErrorInfo("暂无数据");
			}
		DecimalFormat df = new DecimalFormat();
		System.err.println(String.format("select sum(check_total),sum(check_ok),sum(check_ng) from sb_totalcheckdata_one(%s,array%s,'%s','%s' )",
				comp_id,list,beginDate,endDate));
		rs = dbConn.query(String.format("select sum(check_total),sum(check_ok),sum(check_ng) from sb_totalcheckdata_one(%s,array%s,'%s','%s' )",
				comp_id, list, beginDate, endDate));
		if (rs.next()) {
			json.put("check_total",rs.getInt(1));
			json.put("check_ok", rs.getInt(2));
			json.put("check_ng", rs.getInt(3));
			if (rs.getInt(1)==0) {
				json.put("check_okrate","0%");
				json.put("check_ngrate","0%");
			}else {
				json.put("check_okrate",df.format(((double)rs.getInt(2)/(double)rs.getInt(1))*100)+"%");
				json.put("check_ngrate",df.format(((double)rs.getInt(3)/(double)rs.getInt(1))*100)+"%");
			}
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	return json.toString();
	}
	
	
	/**
	 * 产线检测类别统计
	 * @return
	 */
	@ApiOperation(value = "产线检测类别统计", notes = "小程序 产线检测类别统计")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "beginDate", value = "开始时间（1.当天就两个时间一样，2.昨天也是两个时间一样，一周就往前推七天），30天一样往前推", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "endDate", value = "结束时间", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "line_id", value = "产线", paramType = "query", dataType = "Integer", required = true),
		@ApiImplicitParam(name = "comp_id", value = "所属公司", paramType = "query", dataType = "Integer", required = true)
	})
	@RequestMapping(value = "getDetectionTatistics", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public List<Map<Object, Object>> getDetectionTatistics( String beginDate, String endDate,Integer line_id,Integer comp_id) {
		List<Object> listETypesTable = getEquipmentTypesTable(comp_id);
		List<Map<Object,Object>> list= new ArrayList<Map<Object,Object>>();
		if (listETypesTable.size()==0) {
			return list;
		}
	ResultSet rs = dbConn.query(String.format("select * from sb_totalcheckdata_two(%s,array%s,'%s','%s',%s) where species_name !='null' order by  total desc",
			comp_id, listETypesTable, beginDate, endDate, line_id));
	System.err.println(String.format("select * from sb_totalcheckdata_two(%s,array%s,'%s','%s',%s) where species_name !='null' order by  total desc",
			comp_id, listETypesTable, beginDate, endDate, line_id));
	try {
		while (rs.next()) {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("species_name",rs.getObject(1));
			map.put("total", rs.getObject(2));
			map.put("ok", rs.getObject(3));
			map.put("ng",rs.getObject(4));
			map.put("id",rs.getObject(5));
		    list.add(map);
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	return list;
	}
	
	
	/**
	 * 设备检测次数统计(根据设备类别获取设备检测数据)
	 * @return
	 */
	@ApiOperation(value = "设备检测次数统计", notes = "小程序 设备检测次数统计")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "beginDate", value = "开始时间（1.当天就两个时间一样，2.昨天也是两个时间一样，一周就往前推七天），30天一样往前推", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "endDate", value = "结束时间", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "species_id", value = "设备id", paramType = "query", dataType = "Integer", required = true),
		@ApiImplicitParam(name = "comp_id", value = "所属公司", paramType = "query", dataType = "Integer", required = true)
	})
	@RequestMapping(value = "getEquipmentCDTotal", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public List<Map<Object, Object>> getEquipmentCDTotal(String beginDate, String endDate,Integer species_id,Integer comp_id) {
	List<Map<Object,Object>> list= new ArrayList<Map<Object,Object>>();
	ResultSet rs=null;
	String tableName = null;
	try {
		rs = dbConn.query("select equipment_types_table from cyj_instrument_equipment_species where comp_id ="+comp_id+" and id = "+species_id+"");
		if (!rs.isBeforeFirst()) {
			return list;
		} 
		if (rs.next()) {
			tableName = rs.getString(1);
		}
	   rs = dbConn.query(String.format(" select * from sb_totalcheckdata_three(%s,'%s',%s,'%s','%s' )as ( total bigint,check_ngrate  text, line_id bigint) " + 
			"", comp_id, tableName, species_id, beginDate, endDate));
	   System.err.println(String.format(" select * from sb_totalcheckdata_three(%s,'%s',%s,'%s','%s' )as ( total bigint,check_ngrate  text, line_id bigint) " + 
			"", comp_id, tableName, species_id, beginDate, endDate));
		while (rs.next()) {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("total", rs.getObject(1));
			map.put("check_ngrate", rs.getObject(2));
			map.put("id",rs.getObject(3));
		    list.add(map);
		}
	} catch (Exception e) {
		e.printStackTrace();
		return list;
	}
	return list;
	}
	
	/**
	 * 不良项目排名
	 * @return
	 */
	@ApiOperation(value = "不良项目排名", notes = "小程序 不良项目排名")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "beginDate", value = "开始时间（1.当天就两个时间一样，2.昨天也是两个时间一样，一周就往前推七天），30天一样往前推", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "endDate", value = "结束时间", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "comp_id", value = "所属公司", paramType = "query", dataType = "Integer", required = true)
	})
	@RequestMapping(value = "getBaditmTanking", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public List<Map<Object, Object>> getBaditmTanking( String beginDate, String endDate, Integer comp_id) {
		List<Object> listETypesTable = getEquipmentTypesTable(comp_id);
		List<Map<Object,Object>> list= new ArrayList<Map<Object,Object>>();
		if (listETypesTable.size()==0) {
			return list;
		}
	ResultSet rs = dbConn.query(String.format("select * from sb_totalcheckdata_four(%s,array%s,'%s','%s')",comp_id, list, beginDate, endDate));
	System.err.println(String.format("select * from sb_totalcheckdata_four(%s,array%s,'%s','%s')",comp_id, list, beginDate, endDate));
	try {
		while (rs.next()) {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("reason_name", rs.getObject(1));
			map.put("check_ngrate", rs.getObject(2));
			map.put("count",rs.getObject(3));
		    list.add(map);
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	return list;
	}
	
	
	/**
	 * 设备选择下拉框数据
	 * @return
	 */
	@ApiOperation(value = "设备选择下拉框数据", notes = "小程序设备选择下拉框数据")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "species_id", value = "设备种类id", paramType = "query", dataType = "String", required = true)
	})
	@RequestMapping(value = "getDevice", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public List<Map<Object, Object>> getDevice(Integer species_id) {
	ResultSet rs = dbConn.query(String.format("select id, instrument_equipment_name from cyj_instrument_equipment where species_id =%s",species_id));
	List<Map<Object,Object>> list= new ArrayList<Map<Object,Object>>();
	try {
		while (rs.next()) {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("id", rs.getObject(1));
			map.put("value", rs.getObject(2));
		    list.add(map);
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	return list;
	}
	
	
	/**
	 * 根据设备id获取设备信息
	 * @return
	 */
	@ApiOperation(value = "获取设备信息", notes = "小程序根据设备id获取设备信息")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "species_id", value = "设备id", paramType = "query", dataType = "String", required = true)
	})
	@RequestMapping(value = "getDeviceInfo", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public Map<Object,Object>  getDeviceInfo(Integer species_id) {
	ResultSet rs = dbConn.query(String.format("select * from v_sb_equipmentinfo_one where id =%s",species_id));
//	StringBuffer str = new StringBuffer();
//	List<Map<Object,Object>> list= new ArrayList<Map<Object,Object>>();
	Map<Object, Object> map = new HashMap<Object, Object>();
	try {
		if (rs.next()) {
			map.put("equipment_no", rs.getObject(1));
			map.put("equipment_name", rs.getObject(2));
			map.put("manufacturer", rs.getObject(3));
			map.put("line_name", rs.getObject(4));
//			list.add(map);
//			str.append("{\"value\":\"");
//			str.append(rs.getString(1));
//			str.append("\"},{\"value\":\"");
//			str.append(rs.getString(2));
//			str.append("\"},{\"value\":\"");
//			str.append(rs.getString(3));
//			str.append("\"},{\"value\":\"");
//			str.append(rs.getString(4));
//			str.append("\"}");
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
//	return "["+str.toString()+"]";
	return map;
	}
	
	
	
	@ApiOperation(value = "获取设备信息", notes = "小程序根据设备id获取设备信息")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "species_id", value = "设备id", paramType = "query", dataType = "String", required = true)
	})
	@RequestMapping(value = "getDeviceInfo1", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String getDeviceInfo1(Integer species_id) {
	ResultSet rs = dbConn.query(String.format("select * from v_sb_equipmentinfo where id =%s",species_id));
	StringBuffer str = new StringBuffer();
	try {
		if (rs.next()) {
			str.append("{\"value\":\"");
			str.append(rs.getString(1));
			str.append("\"},{\"value\":\"");
			str.append(rs.getString(2));
			str.append("\"},{\"value\":\"");
			str.append(rs.getString(3));
			str.append("\"},{\"value\":\"");
			str.append(rs.getString(4));
			str.append("\"}");
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	return "["+str.toString()+"]";
	}
	
	
	/**
	 * 第二頁设备检测统计
	 * @return
	 */
	@ApiOperation(value = "设备检测统计第二页数据", notes = "小程序设备检测统计第二页数据")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "beginDate", value = "开始时间（1.当天就两个时间一样，2.昨天也是两个时间一样，一周就往前推七天），30天一样往前推", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "endDate", value = "结束时间", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "species_id", value = "结束时间", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "comp_id", value = "所属公司", paramType = "query", dataType = "Integer", required = true)
	})
	@RequestMapping(value = "getTotalCheckDataPageTwo", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public JSONObject getTotalCheckDataPageTwo( String beginDate, String endDate,Integer species_id,Integer comp_id) {
		 JSONObject json = new JSONObject();
		 String tableName = null;
	 try {
		ResultSet rs = dbConn.query(String.format("select id,equipment_types_table from cyj_instrument_equipment_species where id =(select species_id from cyj_instrument_equipment where id = %s) ", species_id ));
		if (!rs.isBeforeFirst()) {
			return json;
		} 
		if (rs.next()) {
			tableName = rs.getString(2);
		}
		System.err.println(String.format("select * from sb_totalcheckdata_twopage_two(%s,'%s',%s,'%s','%s') as ( total bigint,check_ok bigint,check_ng bigint, check_ngrate  text,check_item bigint, check_time text)"
				+ "",comp_id,tableName, species_id, beginDate, endDate ));
		rs = dbConn.query(String.format("select * from sb_totalcheckdata_twopage_two(%s,'%s',%s,'%s','%s') as ( total bigint,check_ok bigint,check_ng bigint, check_ngrate  text,check_item bigint, check_time text)",comp_id,tableName, species_id, beginDate, endDate ));
		if (rs.next()) {
			json.put("check_total",rs.getObject(1));
			json.put("check_ok",rs.getObject(2));
			json.put("check_ng",rs.getObject(3));
			json.put("check_ngrate",rs.getObject(4));
        	json.put("check_num",rs.getObject(5));
        	json.put("check_time",rs.getObject(6));
		}
	} catch (SQLException e) {
		e.printStackTrace();
	}
	return json;
	}
	
	
	/**
	 * 设备检测项目统计
	 * @return
	 */
	@ApiOperation(value = "设备检测项目统计", notes = "小程序设备检测项目统计")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "beginDate", value = "开始时间（1.当天就两个时间一样，2.昨天也是两个时间一样，一周就往前推七天），30天一样往前推", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "endDate", value = "结束时间", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "species_id", value = "设备编号", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "comp_id", value = "所属公司", paramType = "query", dataType = "Integer", required = true)
	})
	@RequestMapping(value = "getTotalCheckTtem", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public List<Map<Object, Object>> getTotalCheckTtem( String beginDate, String endDate,Integer species_id, Integer comp_id) {
		List<Map<Object,Object>> list= new ArrayList<Map<Object,Object>>();
		String tableName = null;
		try {
		ResultSet rs = dbConn.query(String.format("select id,equipment_types_table from cyj_instrument_equipment_species where id =(select species_id from cyj_instrument_equipment where id = %s) ", species_id ));
		if (!rs.isBeforeFirst()) {
			return list;
		} 
		if (rs.next()) {
			tableName = rs.getString(2);
		}
	   rs = dbConn.query(String.format("select * from sb_totalcheckdata_twopage_three (%s,'%s',%s,'%s','%s')as ( check_item character varying,check_time text, total bigint,check_ng bigint) order by total desc",
			comp_id, tableName, species_id,  beginDate, endDate));
		while (rs.next()) {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("reason_name", rs.getObject(1));
			map.put("check_time", rs.getObject(2));
			map.put("check_total", rs.getObject(3));
			map.put("check_ng", rs.getObject(4));
		    list.add(map);
		}
	} catch (SQLException e) {
		e.printStackTrace();
	}
	return list;
	}
	
	
	
	/**
	 * JF 获取下拉框通用接口
	 */
	@ApiOperation(value = "获取下拉框通用接口", notes = "小程序获取下拉框通用接口")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "line_list", value = "产线编号", paramType = "query", dataType = "String", required = true),
		@ApiImplicitParam(name = "species_list", value = "数据源编号", paramType = "query", dataType = "String", required = true)
	})
	@RequestMapping(value = "datasourceJson", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String filterJson(String line_list,String species_list) {
		JSONObject jsonObject = JSONObject.fromObject("{\"filterArray\": [\""+line_list+"\",\""+species_list+"\"]}");
		//获取JSON数组,然后根据它去组装2部分的数据
		JSONArray json = JSONArray.fromObject(jsonObject.getString("filterArray"));
		return "{" + comm.getFilterDataJson(json) + comm.getFilterFrameJson(json.toString()) + "}";
	}
  
	public List<Object> getEquipmentTypesTable(Integer comp_id) {
		ResultSet rs = null;
		List<Object> list = new ArrayList<Object>();
		try {
		rs = dbConn.query("select equipment_types_table from cyj_instrument_equipment_species where comp_id ="+comp_id+"");
		while (rs.next()) {
			list.add("'"+rs.getString(1)+"'");
		}
	  }catch (Exception e) {
	 }
		return list;
	}
	
	
	/**
	   *  通用的下拉框数据接口 （根据comp_id）
	 * @param request
	 * @param alueField, displayField,tableName, comp_id,condition 值字段，显示字段，表，所属ID,条件
	 * @return
	 */
	@RequestMapping(value = "getAppdropDownBox", method = RequestMethod.GET)
	@ResponseBody
	public List<Map<Object, Object>>  dropDownBox(@RequestParam String tableName,@RequestParam String valueField,
			@RequestParam String displayField, @RequestParam Integer comp_id ,@RequestParam String condition) {
		ResultSet rs = null;
		if (tableName.contains("v_sys_departments_tree")||tableName.contains("v_equipment_species_tree")) {
			if (condition.length()>0) {
				rs=dbConn.query(String.format("select * from %s where comp_id = %s and %s",tableName, comp_id,condition));
			}else {
				rs=dbConn.query(String.format("select * from %s where comp_id = %s",tableName, comp_id));
			}
			List<Map<Object, Object>> deptJson= treeData.getResultSet(rs);
			return deptJson;
		}else {
		if (condition.length()>0) {
			rs=dbConn.query(String.format("select %s,%s from %s where comp_id = %s and %s",valueField, displayField,tableName, comp_id,condition));
		}else {
			rs=dbConn.query(String.format("select %s,%s from %s where comp_id = %s",valueField, displayField,tableName, comp_id));
		}
		List<Map<Object, Object>> list = new ArrayList<Map<Object,Object>>();
		try {
			while (rs.next()) {
				Map<Object, Object> map = new HashMap<Object, Object>();
				map.put("id", rs.getObject(1));
				map.put("value", rs.getObject(2));
			    list.add(map);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	 }
		
	}
}
