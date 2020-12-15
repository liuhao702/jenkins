package com.lc.bxm.entity;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

public class DepartmentData {
	//车间名称
	private String deptName;
	
	//车间下的产线
	private List<LineData> line = new ArrayList<LineData>();

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public List<LineData> getLine() {
		return line;
	}

	public void setLine(List<LineData> line) {
		this.line = line;
	}
	
	public JSONObject getDeptJson(DepartmentData data) {
		return JSONObject.fromObject(data);
	}
}
