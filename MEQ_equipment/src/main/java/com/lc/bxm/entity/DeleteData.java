package com.lc.bxm.entity;

/**
 * 删除
 * @author lh
 *
 */
public class DeleteData {
	
	private String funName;          //按钮名称
	private String menuId;           //菜单ID
	private String userId;           //用户ID
	private String tableName;        //表名
	private String idName;           //字段
	private Object idValue;          //字段值
	public String getFunName() {
		return funName;
	}
	public void setFunName(String funName) {
		this.funName = funName;
	}
	public String getMenuId() {
		return menuId;
	}
	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getIdName() {
		return idName;
	}
	public void setIdName(String idName) {
		this.idName = idName;
	}
	public Object getIdValue() {
		return idValue;
	}
	public void setIdValue(Object idValue) {
		this.idValue = idValue;
	}
	
	
}
