package com.lc.bxm.common.util;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.springframework.stereotype.Component;

/**
 * base64加密解密
 * @author liuhao
 *
 */
@Component
public class Base64Util {
	
	//加密
    public String encode(String strData) {
    	String str = null;
    	try {
    		// BASE64加密
    		Encoder encoder = Base64.getEncoder();
    		byte[] data = encoder.encode(strData.getBytes());
    		 str = new String(data);
//    		System.out.println("BASE64加密：" +str);
    		// 或者采用以下方法，但是不赞成使用该方法，源码也做了@deprecation标记
//    		System.out.println("BASE64加密：" + encoder.encodeToString(strData.getBytes()));
	
	} catch (Exception e) {
//		System.out.println("BASE64加密异常");
		e.printStackTrace();
	}
    	return str;
    }
    
    //解密
    public String decode(String data) {
    	String str = null;
    	try {
    		// BASE64加密
    		Decoder decoder = Base64.getDecoder();
    		byte[] bytes = decoder.decode(data);
    		str = new String(bytes);
//		  System.out.println("BASE64解密：" + new String(bytes));
    		// 或者采用以下方法，但是不赞成使用该方法，源码也做了@deprecation标记
	} catch (Exception e) {
//		System.out.println("BASE64解密异常");
		e.printStackTrace();
	}
    	return str;
    }

}
