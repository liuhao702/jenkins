package com.lc.bxm.common.helper;

import org.springframework.stereotype.Component;

/**
 * 系统常量类
 * @author LC
 * @date 2019年8月16日
 */
@Component
public class Str {

	/**
	 * 去掉每层的最后一个逗号
	 */
	public static String delComma(String res) {
		if (!res.equals("")) {
			res = res.substring(0, res.length() - 1);
		}
		System.err.println(res);
		return res;
	}
	
	/**
	 * 去掉每层的最后一个"and"
	 */
	public static String delStringAnd(String res) {
		if (!res.equals("")) {
			res = res.substring(0, res.length() - 3);
		}
		return res;
	}
}