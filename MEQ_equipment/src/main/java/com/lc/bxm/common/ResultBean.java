package com.lc.bxm.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 看板通用数据对象
 * @author ljz
 * @date 2017年10月19日 10:57
 */
public class ResultBean {

	/**
     * 数据集
     */
	private Map<String,Object> data;

    /**
     * 业务自定义状态码
     */
    private Integer code = 200;
    
    /**
     * 返回消息
     */
    private String msg;
    
    public ResultBean() {
    	data = new HashMap<String,Object>();
    }
    
	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public Integer getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setCode(Integer code) {
		this.code = code;
	}
    
}
