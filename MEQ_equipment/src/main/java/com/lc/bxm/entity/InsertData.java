package com.lc.bxm.entity;

import net.sf.json.JSONObject;
/**
 * 添加
 * @author lh
 *
 */
public class InsertData {
	private String funName;
	private String menuId;
	private String userId;
	private String tableName;
	private JSONObject formData;
	
	//按钮名称
	public String getFunName(){
		return funName;
	}
	
	public void setFunName(String funName) {
		this.funName = funName;
	}
	
	//菜单ID
	public String getMenuId(){
		return menuId;
	}
	
	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}
	
	//用户ID
	public String getUserId(){
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	//表名
	public String getTableName(){
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	//表单数据
	public JSONObject getFormData(){
		return formData;
	}
	
	public void setFormData(JSONObject formData) {
		this.formData = formData;
	}
}
