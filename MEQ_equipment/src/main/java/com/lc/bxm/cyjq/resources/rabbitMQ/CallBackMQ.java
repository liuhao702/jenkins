package com.lc.bxm.cyjq.resources.rabbitMQ;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.support.CorrelationData;

public class CallBackMQ implements ConfirmCallback {
	
	
	public Boolean returnAck =false;

	@Override
	public void confirm(CorrelationData correlationData, boolean ack, String cause) {
		 if (ack) {
	            //设置消息投递成功
			    returnAck = ack;
	            System.out.println("消息投递成功");
	        } else {
	            //消息投递失败
	            System.out.println("消息投递失败");
	        	returnAck = ack;
	    }
	}
}
