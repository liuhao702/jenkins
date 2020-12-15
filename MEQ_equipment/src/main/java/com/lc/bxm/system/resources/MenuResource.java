package com.lc.bxm.system.resources;

import java.sql.ResultSet;


import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.helper.RedisUtil;
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;

@RestController
@RequestMapping({ "/menu" })
@SuppressWarnings("unused")
public class MenuResource {
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	RedisUtil redis;
	
	@RequestMapping(value = { "menuJson" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	@ResponseBody
	public String getMenuJsonByUser(@RequestParam String userCode, String is_admin,String user_id) {
		//连接redis缓存
		 //Jedis jedis = redis.init();
//		 String menuJson  =  getMenuJson("", userCode, is_admin);;
//		 return "[" + menuJson + "]";
		 
		 //连接redis缓存
		 Jedis jedis = redis.init();
		 String menuJson = null;
		if (jedis.get(user_id)==null) {
			menuJson =  getMenuJson("", userCode, is_admin);
	        	jedis.set(user_id, "["+menuJson+"]");
	        }else if(user_id!=null&&!user_id.equals("")) {
	   				return jedis.get(user_id);
			}else {
				 menuJson = getMenuJson("", userCode, is_admin);
	        	 jedis.set(user_id, "["+menuJson+"]");
			}
		 return "[" + menuJson + "]";
	}

	private String getMenuJson(String parentUid, String userCode, String is_admin) {
		StringBuilder sb = new StringBuilder();
		ResultSet rs = null;
		Boolean isAdmin = Boolean.valueOf(false);

		ResultSet rsIsAdmin = null;
		rsIsAdmin = this.dbConn.query(String.format("SELECT bxm_administrator_check('%s')", new Object[] { userCode }));
		try {
			while (rsIsAdmin.next()) {
				isAdmin = Boolean.valueOf(rsIsAdmin.getBoolean(1));
			}
			if (parentUid == "") {
				is_admin = (is_admin.equals("")) || (is_admin == null) ? "-1" : is_admin;
				if (is_admin.equals("bxmadmin")) {
					rs = this.dbConn.query(
							"SELECT uid,name,parent_uid,url,menu_image FROM v_active_menu_tree WHERE parent_uid IS null");
				} else if (isAdmin.booleanValue()) {
					rs = this.dbConn.query(
							"SELECT uid,name,parent_uid,url,menu_image FROM v_not_deactive_menu_tree WHERE parent_uid IS null");
				} else {
					rs = this.dbConn.query(String.format(
							"SELECT n.uid,n.name,n.parent_uid,n.url,n.menu_image FROM ( WITH RECURSIVE m AS (SELECT g.user_code,g.num,g.uid,g.name,g.idx,g.parent_uid,g.is_menu,g.url,g.para,g.menu_image FROM (select * from v_all_not_deactive_group_and_menu_tree where user_code = '%s' or not is_menu) g  WHERE g.is_menu UNION ALL SELECT a.user_code,a.num,a.uid,a.name,a.idx,a.parent_uid,a.is_menu,a.url,a.para,a.menu_image FROM (select * from v_all_not_deactive_group_and_menu_tree where user_code = '%s' or not is_menu) a  JOIN m m_1 ON a.uid = m_1.parent_uid) SELECT DISTINCT m.user_code,m.num,m.uid,m.name,m.idx,m.parent_uid,m.is_menu,m.url,m.para,m.menu_image FROM m) n where parent_uid is null and not is_menu order by idx",
							new Object[] { userCode, userCode }));
				}
			} else {
				is_admin = (is_admin.equals("")) || (is_admin == null) ? "-1" : is_admin;
				if (is_admin.equals("bxmadmin")) {
					rs = this.dbConn.query(String.format(
							"SELECT uid,name,parent_uid,url,menu_image FROM v_active_menu_tree WHERE parent_uid = '%s'",
							new Object[] { parentUid }));
				} else if (isAdmin.booleanValue()) {
					rs = this.dbConn.query(String.format(
							"SELECT uid,name,parent_uid,url,menu_image FROM v_not_deactive_menu_tree WHERE parent_uid = '%s'",
							new Object[] { parentUid }));
				} else {
					rs = this.dbConn.query(String.format(
							"SELECT  n.uid,n.name,n.parent_uid,n.url,n.menu_image FROM ( WITH RECURSIVE m AS (SELECT g.user_code,g.num,g.uid,g.name,g.idx,g.parent_uid,g.is_menu,g.url,g.para,g.menu_image FROM (select * from v_all_not_deactive_group_and_menu_tree where user_code = '%s' or not is_menu) g  WHERE g.is_menu UNION ALL SELECT a.user_code,a.num,a.uid,a.name,a.idx,a.parent_uid,a.is_menu,a.url,a.para,a.menu_image FROM (select * from v_all_not_deactive_group_and_menu_tree where user_code = '%s' or not is_menu) a  JOIN m m_1 ON a.uid = m_1.parent_uid) SELECT DISTINCT m.user_code,m.num,m.uid,m.name,m.idx,m.parent_uid,m.is_menu,m.url,m.para,m.menu_image FROM m) n where parent_uid = '%s' order by idx",
							new Object[] { userCode, userCode, parentUid }));
				}
			}
			while (rs.next()) {
				sb.append("{");
				sb.append(getRowJson(rs.getString(1), rs.getString(2), rs.getString(4), rs.getString(5)));
				String str = getMenuJson(rs.getString(1), userCode, is_admin);
				if (!str.equals("")) {
					sb.append(",\"children\":[" + str + "]");
				}
				sb.append("},");
			}
		} catch (SQLException localSQLException) {
		}
		return Str.delComma(sb.toString());
	}

	private String getRowJson(String pId, String pName, String pUrl, String pIcon) {
		JSONObject json = new JSONObject();
		json.put("id", pId);
		if ((pUrl == null) || (pUrl.isEmpty())) {
			json.put("name", "");
		} else {
			json.put("name", pUrl.substring(1, pUrl.indexOf("/", 1) > 0 ? pUrl.indexOf("/", 1) : pUrl.length()));
		}
		json.put("title", pName);
		json.put("url", "" + pUrl + "");
		json.put("icon", pIcon);

		return json.toString().replace("{", "").replace("}", "");
	}

	@RequestMapping(value = { "buttonJson" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	@ResponseBody
	public String getButtonJsonByMenu(@RequestParam String menuId, @RequestParam String userCode, @RequestParam String user_id) {
		String buttonJson = getButtonJson(menuId, userCode, user_id);
		return "[" + buttonJson + "]";
	}

	private String getButtonJsonBak(String menuId, String userCode,String user_id) {
		StringBuilder sb = new StringBuilder();
		JSONObject json = new JSONObject();
		Boolean isAdmin = Boolean.valueOf(false);
//		rsIsAdmin = this.dbConn.query(String.format("SELECT bxm_administrator_check('%s')", new Object[] { userCode }));
		ResultSet rsIsAdmin =dbConn.query(String.format("SELECT bxm_administrator_check_by_uid('%s')",  user_id ));
		try {
			while (rsIsAdmin.next()) {
				isAdmin = Boolean.valueOf(rsIsAdmin.getBoolean(1));
			}
			ResultSet rs = null;
			//is_admin = (is_admin.equals("")) || (is_admin == null) ? "-1" : is_admin;
			if (userCode.equals("bxmadmin")) {
				rs = this.dbConn.query(String.format(
						"SELECT fun_uid,fun_code,fun_name,menu_uid,is_deactive FROM v_all_menu_fun_code WHERE menu_uid = '%s' ORDER BY idx",
						new Object[] { menuId }));
			} else if (isAdmin.booleanValue()) {
				rs = this.dbConn.query(String.format(
						"SELECT fun_uid,fun_code,fun_name,menu_uid,is_deactive FROM v_all_not_deactive_menu_fun_code WHERE menu_uid = '%s' ORDER BY idx",
						new Object[] { menuId }));
			} else {
				rs = this.dbConn.query(String.format(
						"SELECT fun_uid,fun_code,fun_name,menu_uid,is_deactive FROM v_user_menu_fun_code WHERE user_code = '%s' AND menu_uid = '%s' ORDER BY idx",
						new Object[] { userCode, menuId }));
			}
			while (rs.next()) {
				json.put("buttonName", rs.getString(3));
				json.put("buttonAction", rs.getString(2));
				json.put("disabled", Boolean.valueOf(rs.getBoolean(5)));
				sb.append(json.toString() + ",");
			}
		} catch (SQLException localSQLException) {
		}
		return Str.delComma(sb.toString());
	}
	
	
	
	private String getButtonJson(String menuId, String userCode,String user_id) {
		StringBuilder sb = new StringBuilder();
		JSONObject json = new JSONObject();
		Boolean isAdmin = Boolean.valueOf(false);
		try {
			ResultSet rs = null;
			if (userCode.equals("bxmadmin")) {
				rs = this.dbConn.query(String.format(
						"SELECT fun_uid,fun_code,fun_name,menu_uid,is_deactive FROM v_all_menu_fun_code WHERE menu_uid = '%s' ORDER BY idx",
						new Object[] { menuId }));
			} else {
				ResultSet rsIsAdmin =dbConn.query(String.format("SELECT bxm_administrator_check_by_uid('%s')",  user_id ));
				while (rsIsAdmin.next()) {
					isAdmin = Boolean.valueOf(rsIsAdmin.getBoolean(1));
				}
				if (isAdmin) {
					rs = this.dbConn.query(String.format(
							"SELECT fun_uid,fun_code,fun_name,menu_uid,is_deactive FROM v_all_not_deactive_menu_fun_code WHERE menu_uid = '%s' ORDER BY idx",
							menuId));
				}else {
				rs = this.dbConn.query(String.format(
						"SELECT fun_uid,fun_code,fun_name,menu_uid,is_deactive FROM v_user_menu_fun_code WHERE user_code = '%s' AND menu_uid = '%s' ORDER BY idx",
						userCode, menuId ));
			  }
			}
			while (rs.next()) {
				json.put("buttonName", rs.getString(3));
				json.put("buttonAction", rs.getString(2));
				json.put("disabled", Boolean.valueOf(rs.getBoolean(5)));
				sb.append(json.toString() + ",");
			}
		} catch (SQLException localSQLException) {
		}
		return Str.delComma(sb.toString());
	}
	

	@RequestMapping(value = { "menuGroupJson" }, method = { org.springframework.web.bind.annotation.RequestMethod.GET })
	@ResponseBody
	public String getMenuGroupJson() {
		String menuGroupJson = getMenuGroupJson("");
		return "[" + menuGroupJson + "]";
	}

	private String getMenuGroupJson(String parentUid) {
		ResultSet rs = null;
		if (parentUid == "") {
			rs = this.dbConn.query("SELECT * FROM v_sys_menu_groups WHERE parent_group_uid IS NULL ORDER BY idx ASC");
		} else {
			rs = this.dbConn.query(
					String.format("SELECT * FROM v_sys_menu_groups WHERE parent_group_uid = '%s' ORDER BY idx ASC",
							new Object[] { parentUid }));
		}
		StringBuilder sb = new StringBuilder();
		try {
			while (rs.next()) {
				sb.append(rs.getString(4));
				String str = getMenuGroupJson(rs.getString(1));
				if (!str.equals("")) {
					sb.append(",\"children\":[" + str + "]");
				}
				sb.append("},");
			}
		} catch (SQLException localSQLException) {
		}
		return Str.delComma(sb.toString());
	}

	@RequestMapping(value = { "getFaMenu" }, method = {
			org.springframework.web.bind.annotation.RequestMethod.GET }, produces = { "text/html;charset=utf-8" })
	@ResponseBody
	public String getMunuById(@RequestParam String menuId) {
		String menuName = getFaMenu(menuId);
		return "[" + Str.delComma(menuName) + "]";
	}

	public String getFaMenu(String menuId) {
		StringBuilder sb = new StringBuilder();
		String sql = "select parent_uid,name from v_bread_crumb_group_and_menu_tree where uid = '" + menuId + "'";
		ResultSet rs = this.dbConn.query(sql);
		try {
			while (rs.next()) {
				sb.append("{\"name\":\"" + rs.getString(2) + "\"},");
				String id = rs.getString(1);
				if ((id != null) && (!id.equals(""))) {
					String str = getFaMenu(rs.getString(1));
					if (!str.equals("")) {
						sb.append(str);
					}
				}
			}
		} catch (SQLException localSQLException) {
		}
		return sb.toString();
	}
}
