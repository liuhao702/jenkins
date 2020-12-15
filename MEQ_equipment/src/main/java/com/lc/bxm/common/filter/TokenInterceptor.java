package com.lc.bxm.common.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.lc.bxm.common.helper.RedisUtil;
import com.lc.bxm.common.util.TokenUtil;
import com.lc.bxm.entity.TestConnection;

/**
 * 自定义token拦截器
 * @author LH
 * @date 2019年6月18日
 */
public class TokenInterceptor  implements HandlerInterceptor {
	
	@Autowired
	TestConnection testConn;
	
	@Autowired
	RedisUtil redis;
	

    //第一个函数preHandle是预处理函数,比如我们用于拦截登录时,它是第一个工作的
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	String headerToken=request.getHeader("token");
        //获取我们请求头中的token验证字符
        String tokenStr=request.getParameter("token");
        //刷新token 
		String  newToken = null;
		//返回响应
		String str= null;
		
        //getParameter的变量是放在我们请求附带的对象中的字符串,例如post方法中附带的account变量等
        //检测当前页面,我们设置当页面不是登录页面时对其进行拦截//具体方法就是检测URL中有没有login字符串
        // autoCompleteWorkOrder,getDataBaseCard C++端调用不需要进行拦截appletsLogin
        if(!request.getRequestURI().contains("login")&&!request.getRequestURI().contains("changePassword") && !request.getRequestURI().contains("getFile")
        		&&!request.getRequestURI().contains("getDataBaseCard")&&!request.getRequestURI().contains("swagger")&&!request.getRequestURI().contains("configuration")
        		&&!request.getRequestURI().contains("v2")&&!request.getRequestURI().contains("autoCompleteWorkOrder")&&!request.getRequestURI().contains("getApplets")){
        	//String tokenName=null;
            if(headerToken==null && tokenStr==null){
            	//验证不通过的话返回一个状态给前端，前端根据这个状态跳转到登录页面
				  str="{\"status\":10,\"tokenMsg\":\"缺少token，无法验证\"}";
	              dealErrorReturn(request,response,str);
                  return false;
                //当返回值是false的时候,表示拦截器不会进行处理了,我们调用response来进行响应。
            }
            //验证token是否正确
            String result = TokenUtil.verifyToken(headerToken);
        	if (result.equals("4005")) {
        		 //无效
			  	str="{\"status\":10,\"tokenMsg\":\"token令牌无效。。。。\"}";
                dealErrorReturn(request,response,str);
                return false;
			}
            testConn.setToken(headerToken);
            if(tokenStr!=null){
            	headerToken=tokenStr;
            	//进行token同步,后面我们会对token做验证与更新
            }
            try {
            	//有效令牌，需要重新刷新token，再将token传回客户端，客户端会拿着新的token进行访问
            	//刷新token 
                  newToken = TokenUtil.refreshToken(headerToken);
            	//对token进行更新与验证
            }catch(Exception e) {
            	//当token验证出现异常返回到登录页面
            	str="{\"status\":10,\"tokenMsg\":\"token出现异常。。。。\"}";
                dealErrorReturn(request,response,str);
            	return false;
            }
        }
        response.setHeader("Access-Control-Expose-Headers","Token");
        response.setHeader("Token",newToken);
        return true;
       // 当返回true表示第一个阶段结束,随后会执行postHandle和afterCompletion
    }

    //当请求到达Controller但是未渲染View时进行处理
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    //相当于最后的回调函数
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
    

    // 检测到没有token，直接返回不验证
    public void dealErrorReturn(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,Object obj){
        String json = (String)obj;
        PrintWriter writer = null;
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("text/html; charset=utf-8");
        try {
            writer = httpServletResponse.getWriter();
            writer.print(json);

        } catch (IOException ex) {
        } finally {
            if (writer != null)
                writer.close();
        }
    }

}
