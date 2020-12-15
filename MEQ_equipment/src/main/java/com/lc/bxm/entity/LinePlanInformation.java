package com.lc.bxm.entity;

import java.util.List;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class LinePlanInformation {

	@JSONField(ordinal = 0)
	private String date;
	
	@JSONField(ordinal = 1)
	private String morderCode;
	
	@JSONField(ordinal = 2)
	private String productCode;
	
	@JSONField(ordinal = 3)
	private String qty;
	
	@JSONField(ordinal = 4)
	private String offlineQty;
	
	@JSONField(ordinal = 5)
	private String badCount;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getMorderCode() {
		return morderCode;
	}

	public void setMorderCode(String morderCode) {
		this.morderCode = morderCode;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getQty() {
		return qty;
	}

	public void setQty(String qty) {
		this.qty = qty;
	}

	public String getOfflineQty() {
		return offlineQty;
	}

	public void setOfflineQty(String offlineQty) {
		this.offlineQty = offlineQty;
	}

	public String getBadCount() {
		return badCount;
	}

	public void setBadCount(String badCount) {
		this.badCount = badCount;
	}
	
	public String getLinePlanInformations(List<LinePlanInformation> linePlanInformations) {
		String[] header = new String[]{"排产日期","排产订单","产品编码","排产数","完成数","不良数"};;
		JSONObject result = new JSONObject();
		result.put("header", header);
		result.put("data", linePlanInformations);
		JSONObject config = new JSONObject();
		config.put("config", JSONObject.toJSONString(result, SerializerFeature.WriteMapNullValue));
//		config.put("config", result.toString());
		config.put("waitTime", 5000);
		return JSON.toJSONString(config);
	}
}
