package com.lc.bxm.entity;

import java.io.Serializable;
import java.util.List;

public class Menu implements Serializable {
	
	private static final long serialVersionUID = -5990021029947688358L;
	    // 菜单id
	    private String id;
	    // 菜单名称
	    private String name;
	    // 菜单父级id
	    private String parentId;
	    // 菜单url
	    private String url;
	    // 菜单图标
	    private String icon;
	    // 子菜单
	    private List<Menu> children;
	    
	    
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getParentId() {
			return parentId;
		}
		public void setParentId(String parentId) {
			this.parentId = parentId;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getIcon() {
			return icon;
		}
		public void setIcon(String icon) {
			this.icon = icon;
		}
		public List<Menu> getChildren() {
			return children;
		}
		public void setChildren(List<Menu> children) {
			this.children = children;
		}
		public static long getSerialversionuid() {
			return serialVersionUID;
		}
}