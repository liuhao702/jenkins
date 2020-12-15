package com.lc.bxm.entity;
import net.sf.json.JSONObject;

/**
 * 编辑
 * @author lh
 *
 */
public class EditData {
	private String funName;          //按钮名称
	private String menuId;           //菜单ID
	private String userId;           //用户ID
	private String tableName;        //表名
	private String keyColumnName;    //字段
	private JSONObject formData;     //表单数据
//	private String editedId;         //要编辑的
	private String id;         //要编辑的
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
	public String getKeyColumnName() {
		return keyColumnName;
	}
	public void setKeyColumnName(String keyColumnName) {
		this.keyColumnName = keyColumnName;
	}
	public JSONObject getFormData() {
		return formData;
	}
	public void setFormData(JSONObject formData) {
		this.formData = formData;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
//	public String getEditedId() {
//		return editedId;
//	}
//	public void setEditedId(String editedId) {
//		this.editedId = editedId;
//	}
   
}
