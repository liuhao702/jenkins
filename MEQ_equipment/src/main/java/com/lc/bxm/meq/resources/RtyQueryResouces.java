package com.lc.bxm.meq.resources;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/xccommon")
public class RtyQueryResouces {

	//private static Logger logger = Logger.getLogger(CommonResource.class);

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;
	
	/**
	 * CK 表主键名称必须id才能使用该接口树状图表格分页
	  *    查询全部filterString可以不传
	 */
	@RequestMapping(value = "tableJson", method = RequestMethod.GET)
	@ResponseBody
	public String getProdTableJson(@RequestParam String tableName, @RequestParam int pageSize,
			@RequestParam int currentPage, @RequestParam String inputSearch, @RequestParam String order,
			@RequestParam String prop, @RequestParam String filterString, @RequestParam String id) {
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
		// 根据传进来的树的ID查找出所有的下级ID
		 if(id != null && !id.equals("")) {
				String idArray = getId(id,tableName,filterString);
				if (idArray != null && !idArray.equals("")) {
					String res = "(" + idArray + ")";
					filterString = " id in" + res;
				} else {
					filterString = " id = null" ;
			}
		}
			
		String sql = "select bxm_get_grid_page_json('" + tableName + "','*','','" + filterString + "','"
				+ inputSearch + "','" + prop + " " + order + "'," + pageSize + "," + currentPage + ")";
		ResultSet rs = dbConn.query(sql);
		try {
			if (rs.next()) {
				userJson = rs.getString(1);
			}
		} catch (SQLException e) {
		}
		return userJson;
	}
	
	/**
	 * LH 根据父级ID递归查询所有的子ID
	 */
	private String getId(String id, String tableName, String filterString) {
		String sql = null;
		if (tableName.equals("v_cyj_bad_record_sheet")) {
			 sql = "select p.id from "+tableName+" p "
			 		+ "left join cyj_bad_record_sheet m on p.id = m.id  where  m.inspection_rty_id in ("+id+")";
		}else if(tableName.equals("v_cyj_defect_records")){
			sql = "select p.id from "+tableName+" p "
			 		+ "left join cyj_defect_records m on p.id = m.id  where  m.record_sheet_id in("+id+")";
		}else if (tableName.equals("v_qis_inspection_category")) {
				 sql = "select p.id from "+tableName+" p "
				 		+ "left join qis_inspection_category m on p.id = m.id  where  m.inspection_category_id in("+id+")";
		}else if(tableName.equals("v_qis_inspection_standard")){
			sql = "select p.id from "+tableName+" p "
			 		+"where  p.inspection_category_id in("+id+")";
		}else if (tableName.equals("v_qis_defect_why")) {
			 sql = "select p.id from "+tableName+" p where  p.category_id in("+id+")";
		} else /*
				 * if(tableName.equals("v_qis_defect_name_category")){ sql =
				 * "select p.id from "+tableName+" p where  p.defect_name_category_id ="+id+"";
				 * }else
				 */if(tableName.equals("v_qis_defect_name")){
			sql = "select p.id from "+tableName+" p where  p.defect_name_category_id in("+id+")";
		}else{
			sql = "select id from "+tableName+" where "+filterString+" in (" + id + ")";
		}
		StringBuilder sb = new StringBuilder();
		ResultSet rs = dbConn.query(sql);
		try {
			while(rs.next()) {
				sb.append(rs.getString(1)+",");
			//	sb.append(getId(rs.getString(1),tableName,filterString));
			}
		} catch (SQLException e) {}
		return Str.delComma(sb.toString());
	}
}
