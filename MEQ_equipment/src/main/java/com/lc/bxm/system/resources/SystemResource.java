package com.lc.bxm.system.resources;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import net.sf.json.JSONObject;

/**
 * 系统相关操作
 * @author JF
 * @date 2019年5月9日
 */
@RestController
@RequestMapping("/system")
public class SystemResource {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	
	/**
	 * JF 获取权限功能JSON前台调用
	 */
	@RequestMapping(value = "permissionJson", method = RequestMethod.GET)
	@ResponseBody
	public String getPermissionJson(@RequestParam String roleId,@RequestParam String userCode,@RequestParam Integer comp_id) {
		String menuJson = getPerJson("",roleId, userCode, comp_id);
		return "[" + menuJson + "]";
	}
	
	/**
	 * JF 拼接权限功能JSON
	 */
	private String getPerJson(String parentUid,String roleId, String userCode, Integer comp_id) {
		ResultSet rs = null;
		if (userCode.equals("bxmadmin")||comp_id==0) {
			if (parentUid == "") {
				rs = dbConn.query("SELECT uid,name,url from v_not_deactive_menu_tree where parent_uid is null");
			} else {
				rs = dbConn.query("SELECT uid,name,url from v_not_deactive_menu_tree where parent_uid = '"+ parentUid +"'");
			}
		}else {
			if (parentUid == "") {
				rs = dbConn.query("SELECT n.uid,n.name,n.url FROM ( WITH RECURSIVE m AS" + 
						" (SELECT g.user_code,g.num,g.uid,g.name,g.idx,g.parent_uid,g.is_menu,g.url,g.para,g.menu_image " + 
						" FROM (select * from v_all_not_deactive_group_and_menu_tree_comp where user_code = '"+userCode+"' and comp_id ="+comp_id+" or not is_menu) " + 
						" g WHERE g.is_menu UNION ALL SELECT a.user_code,a.num,a.uid,a.name,a.idx,a.parent_uid,a.is_menu," + 
						" a.url,a.para,a.menu_image FROM (select * from v_all_not_deactive_group_and_menu_tree_comp " + 
						" where user_code = '"+userCode+"' and comp_id ="+comp_id+" or not is_menu) a  JOIN m m_1 ON a.uid = m_1.parent_uid) SELECT DISTINCT " + 
						" m.user_code,m.num,m.uid,m.name,m.idx,m.parent_uid,m.is_menu,m.url,m.para,m.menu_image FROM m)" + 
						" n  where n.parent_uid is null order by idx");
				
			} else {
				rs = dbConn.query("SELECT n.uid,n.name,n.url FROM ( WITH RECURSIVE m AS" + 
						" (SELECT g.user_code,g.num,g.uid,g.name,g.idx,g.parent_uid,g.is_menu,g.url,g.para,g.menu_image " + 
						" FROM (select * from v_all_not_deactive_group_and_menu_tree_comp where user_code = '"+userCode+"' and comp_id ="+comp_id+" or not is_menu) " + 
						" g WHERE g.is_menu UNION ALL SELECT a.user_code,a.num,a.uid,a.name,a.idx,a.parent_uid,a.is_menu," + 
						" a.url,a.para,a.menu_image FROM (select * from v_all_not_deactive_group_and_menu_tree_comp where" + 
						" user_code = '"+userCode+"' and comp_id ="+comp_id+"  or not is_menu) a  JOIN m m_1 ON a.uid = m_1.parent_uid) SELECT DISTINCT " + 
						" m.user_code,m.num,m.uid,m.name,m.idx,m.parent_uid,m.is_menu,m.url,m.para,m.menu_image FROM m)" + 
						" n  where n.parent_uid ='"+parentUid+"'  order by idx");
			}
		}
	
		StringBuilder sb = new StringBuilder();
		try {
			while (rs.next()) {
				sb.append("{");
				sb.append("\"id\":\""+rs.getString(1)+"\",\"menu\":\""+rs.getString(2)+"\"");
				//根据URL为空判断是否为菜单,如果为菜单,拼接菜单功能按钮,如果为菜单分组,继续递归
				if(rs.getString(3) != null && !"".equals(rs.getString(3))) {
					sb.append(getFunJson(rs.getString(1)));
					sb.append(getCheckJson(roleId,rs.getString(1)));
				}else {
					String str = getPerJson(rs.getString(1),roleId,userCode, comp_id);
					if (!str.equals("")) {
						sb.append(",\"children\":[" + str+ "]");
					}
				}
				sb.append("},");
			}
		} catch (SQLException e) {}
		return Str.delComma(sb.toString());
	}
	
	/**
	 * JF 根据MENUID获取MENU下面的按钮
	 */
	private String getFunJson(String menuUid) {
		ResultSet rs = dbConn.query("select fun_uid,fun_name from sys_menu_functions where menu_uid = '"+menuUid+"' order by idx");
		StringBuilder sb = new StringBuilder();
		try {
			while (rs.next()) {
				sb.append("{\"id\":\"" + rs.getString(1) + "\",");
				sb.append("\"value\":\"" + rs.getString(2) + "\"},");
			}
		} catch (SQLException e) {}
		String res = Str.delComma(sb.toString());
		return ",\"fun\":["+res+"]";
	}
	
	/**
	 * JF 根据当前角色ROLEID和MENUID获取已选中的按钮
	 */
	private String getCheckJson(String roleId, String menuId) {
		ResultSet rs = dbConn.query("select fun.fun_uid from  sys_permissions per left join sys_menu_functions fun on(per.fun_uid = fun.fun_uid) where role_uid = '"+roleId+"'"
				+"and menu_uid ='"+menuId+"'");
		StringBuilder sb = new StringBuilder();
		try {
			while (rs.next()) {
				sb.append("\"" + rs.getString(1) + "\",");
			}
		} catch (SQLException e) {}
		String res = Str.delComma(sb.toString());
		return ",\"checkedfun\":["+res+"]";
	}
	
	/**
	 * JF 批量修改权限,先删除该角色下面所有权限,再批量添加
	 */
	@RequestMapping(value = "updatePermission", method = RequestMethod.POST)
	@ResponseBody
	public String updatePermission(@RequestBody String updateJson) {
		JSONObject jsonObject = JSONObject.fromObject(updateJson);
		String roleId = jsonObject.getString("roleId");
		String funId = jsonObject.getString("funId");
//		String comp_id = jsonObject.getString("comp_id");
		String fun[] = (funId.substring(1, funId.length()-1).replace("\"", "")).split(",");
		List<String> funList = Arrays.asList(fun);
		//先删除该角色下面所有权限dbConn
		dbConn.queryUpdate("delete from sys_permissions where role_uid = '"+roleId+"'");
		//新增该角色下面所选中的角色
		boolean res = dbConn.addPerList(funList,roleId);
		if(res) {
			return message.getSuccessInfo("权限保存成功");
		}else {
			return message.getErrorInfo("权限保存失败");
		}
	}
	
	/**
	 * JF 字段导入表格接口,分页
	 */
	@RequestMapping(value = "columnsImport", method = RequestMethod.GET)
	@ResponseBody
	public String importColumnsByTable(@RequestParam String tableName) {
		Boolean result = false;
		ResultSet rs = dbConn.query(
				String.format("select bxm_sys_columns_import_by_table_name('%s')",tableName));
		try {
			if (rs.next()) {
				result = rs.getBoolean(1);
			}
		} catch (SQLException e) {}
		if(result) {
			return message.getSuccessInfo("导入成功");
		}else {
			return message.getErrorInfo("导入失败");
		}
	}
	
}
