package com.lc.bxm.common.log4j;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Priority;

/**
 * 自定义文件输出
 * @author JF
 * @date 2019年6月1日
 */
public class BackUpAppender extends DailyRollingFileAppender {
	
	@Override  
	public boolean isAsSevereAsThreshold(Priority priority) {  
		//只判断是否相等，而不判断优先级   
		return this.getThreshold().equals(priority);
	}
	
}
