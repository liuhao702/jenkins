package com.lc.bxm.common;

import org.springframework.stereotype.Component;

import net.sf.json.JSONObject;

@Component
public class Message {
	
    private int status = 0;
    
    private String message = "";
    
    private String messageData = "";
    
	public int getStatus() {
    	return status;
    }
    
    private void setStatus(int status) {
    	this.status = status;
    }
    
    public String getMessage() {
    	return message;
    }
    
    public String getMessageData() {
    	return messageData;
    }
    
    public void setMessage(String message) {
		this.message = message;
	}

	private  void setMessageData(String messageData) {
    	this.messageData = messageData;
    }
    
	public String getSuccessInfo(String messageData) {
		Message jsonMessage = new Message();
		jsonMessage.setStatus(1);
		jsonMessage.setMessageData(messageData);
		JSONObject jsonResult = JSONObject.fromObject(jsonMessage);
		return jsonResult.toString();
	}
	
	public String getErrorInfo(String message) {
		Message jsonMessage = new Message();
		jsonMessage.setStatus(0);
		jsonMessage.setMessageData(message);
		JSONObject jsonResult = JSONObject.fromObject(jsonMessage);
		return jsonResult.toString();
	}
	public String getSystemPrompt(String message) {
		Message jsonMessage = new Message();
		jsonMessage.setStatus(2);
		jsonMessage.setMessageData(message);
		JSONObject jsonResult = JSONObject.fromObject(jsonMessage);
		return jsonResult.toString();
	}
}
