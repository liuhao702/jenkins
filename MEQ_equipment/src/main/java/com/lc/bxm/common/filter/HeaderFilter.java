package com.lc.bxm.common.filter;

import java.io.IOException;  
import javax.servlet.Filter;  
import javax.servlet.FilterChain;  
import javax.servlet.FilterConfig;  
import javax.servlet.ServletException;  
import javax.servlet.ServletRequest;  
import javax.servlet.ServletResponse;  
import javax.servlet.http.HttpServletResponse;

/**
 * 解决跨域问题
 * @author JF
 * @date 2019年4月16日
 */
public class HeaderFilter implements Filter {
	
	public void doFilter(ServletRequest request, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) resp;
		response.setHeader("Access-Control-Allow-Origin", "*"); // 解决跨域访问报错
		response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600"); // 设置过期时间
		//response.setHeader("Access-Control-Allow-Headers", "*");
		response.setHeader("Access-Control-Allow-Headers","Origin, X-Requested-With, Cache-Control,Content-Type, Accept, Expires,Pragma,Content-Language,Last-Modified,Token");
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // 支持HTTP 1.1.
		response.setHeader("Pragma", "no-cache"); // 支持HTTP 1.0. response.setHeader("Expires", "0");
		response.setHeader("Content-type", "text/html;charset=UTF-8");//让浏览器用utf8来解析返回的数据 
		chain.doFilter(request, resp);
	}

	public void init(FilterConfig filterConfig) {
	}

	public void destroy() {
	}
	
}