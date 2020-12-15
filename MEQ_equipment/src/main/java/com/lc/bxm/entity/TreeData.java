package com.lc.bxm.entity;

import java.util.List;

/**
 * 通用的拼接下拉框数据
 * @author liuhao
 *
 */
public class TreeData {
	
	private Object id;
	private Object label; 
	private Object parentUid;
	private  List<TreeData>  children;
	
	public Object getId() {
		return id;
	}
	public void setId(Object id) {
		this.id = id;
	}
	public Object getLabel() {
		return label;
	}
	public void setLabel(Object label) {
		this.label = label;
	}
	public Object getParentUid() {
		return parentUid;
	}
	public void setParentUid(Object parentUid) {
		this.parentUid = parentUid;
	}
	public List<TreeData> getChildren() {
		return children;
	}
	public void setChildren(List<TreeData> children) {
		this.children = children;
	}
	

}
