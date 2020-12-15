package com.lc.bxm.cyjq.resources.rabbitMQ;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.stereotype.Component;

/**
 * 数据推送失败队列没有进入交换机WorkOrder.exchange
 * @author liuhao
 */
@Component
public class ReturnCall implements ReturnCallback{

	public Boolean returnBack=true;
	
	@Override
	public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
		System.err.println("入exchange失败");
		returnBack = false;
		String msgJson  = new String(message.getBody());
		System.out.println("Returned Message："+msgJson);

	}

}
