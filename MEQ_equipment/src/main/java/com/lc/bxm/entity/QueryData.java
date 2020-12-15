package com.lc.bxm.entity;

/**
 * 查询
 * @author lh
 *
 */
public class QueryData {
	
	 private String tableName;       //表
     private String pageSize ;       //页大小
	 private String currentPage ;    //当前页
	 private String order ;          //排序方式
	 private String prop ;           //下拉框条件
     private String filterString ;   //过滤字符串
	 private String inputSearch ;    //输入条件
	
	 
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getPageSize() {
		return pageSize;
	}
	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}
	public String getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(String currentPage) {
		this.currentPage = currentPage;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getProp() {
		return prop;
	}
	public void setProp(String prop) {
		this.prop = prop;
	}
	public String getFilterString() {
		return filterString;
	}
	public void setFilterString(String filterString) {
		this.filterString = filterString;
	}
	public String getInputSearch() {
		return inputSearch;
	}
	public void setInputSearch(String inputSearch) {
		this.inputSearch = inputSearch;
	}
}
