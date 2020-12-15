package com.lc.bxm.cyjq.resources.common;

import java.sql.ResultSet;


import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.entity.EditData;

import net.sf.json.JSONObject;

/**
 * cyjq编辑通用接口
 * @author LH
 * @date 2020年5月25日
 */
@RestController
@RequestMapping("/cyjqcommon")
public class CyjqCommonEditResouces {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;
	
	/**
	 * LH 通用编辑接口
	 */
	@RequestMapping(value = "editJson", method = RequestMethod.POST)
	@ResponseBody
	public String editJson(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		try {
			EditData data = (EditData) JSONObject.toBean(jsonSaveData, EditData.class);
			ResultSet rs = null;
			//产品检测层别设定
			if (data.getTableName().equals("cyj_product_inspection_level")) {
				if (data.getFormData().get("id")==data.getFormData().get("up_product_level_id")) {
					return message.getErrorInfo("修改失败，不能选择自身为上级层");
				}
				rs=dbConn.query("select id from cyj_product_inspection_level where up_product_level_id = "+data.getFormData().get("id"));
				while (rs.next()) {
					if (data.getFormData().get("up_product_level_id").toString().equals((rs.getString(1)))) {
						return message.getErrorInfo("修改失败，不能选择自己的下级作为上级层别");
					}			
				}
			}
			//产品检测属性设定
            if (data.getTableName().equals("cyj_product_inspection_setting")) {
            	if (data.getFormData().get("id")==(data.getFormData().get("up_product_pro_ids"))) {
					return message.getErrorInfo("修改失败，不能选择自身为上级层");
				}
            	rs=dbConn.query("select id from cyj_product_inspection_setting where up_product_pro_ids = "+data.getFormData().get("id"));
            	while (rs.next()) {
					if (data.getFormData().get("up_product_pro_ids").toString().equals((rs.getString(1)))) {
						return message.getErrorInfo("修改失败，不能选择自己的下级作为上级层别");
					}
				}
			}
           //来料检验层别
            if (data.getTableName().equals("cyj_materials_inspection_stratification")) {
            	if (data.getFormData().get("id")==(data.getFormData().get("stratification_id"))) {
					return message.getErrorInfo("修改失败，不能选择自身为上级层");
				}
            	rs=dbConn.query("select id from cyj_materials_inspection_stratification where stratification_id = "+data.getFormData().get("id"));
            	while (rs.next()) {
					if (data.getFormData().get("stratification_id").toString().equals((rs.getString(1)))) {
						return message.getErrorInfo("修改失败，不能选择自己的下级作为上级层别");
					}
				}
			}
            //来料检验属性
            if (data.getTableName().equals("cyj_materials_inspection_attribute")) {
            	if (data.getFormData().get("id")==(data.getFormData().get("attribute_id"))) {
					return message.getErrorInfo("修改失败，不能选择自身为上级层");
				}
            	rs=dbConn.query("select id from cyj_materials_inspection_attribute where attribute_id = "+data.getFormData().get("id"));
            	while (rs.next()) {
					if (data.getFormData().get("attribute_id").toString().equals((rs.getString(1)))) {
						return message.getErrorInfo("修改失败，不能选择自己的下级作为上级层别");
					}
				}
			}
           //设备种类设定
            if (data.getTableName().equals("cyj_instrument_equipment_species")) {
            	if (data.getFormData().get("id")==(data.getFormData().get("species_id"))) {
					return message.getErrorInfo("修改失败，不能选择自身为上级层");
				}
            	rs=dbConn.query("select id from cyj_instrument_equipment_species where species_id = "+data.getFormData().get("id"));
				while (rs.next()) {
					if (data.getFormData().get("species_id").toString().equals((rs.getString(1)))) {
						return message.getErrorInfo("修改失败，不能选择自己的下级作为上级层别");
					}
				}
			}
			if (helper.dataUpdate(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData(), data.getId())) {
				return message.getSuccessInfo("修改成功");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("修改失败,系统内部问题"+e.getMessage());
		}
		return message.getErrorInfo("修改失败");
	}
}
