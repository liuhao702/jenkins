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
import com.lc.bxm.entity.InsertData;
import net.sf.json.JSONObject;

/**
 * cyjq通用接口
 * @author LH
 * @date 2020年5月25日
 */
@RestController
@RequestMapping("/cyjqcommon")
public class CyjqCommonAddResouces {
	
	//private static Logger logger = Logger.getLogger(CommonResource.class);

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;
	
	
	/**
	 * LH 通用新增接口
	 */
	@RequestMapping(value = "addJson", method = RequestMethod.POST)
	@ResponseBody
	public String addJson(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		try {
			InsertData data = (InsertData) JSONObject.toBean(jsonSaveData, InsertData.class);
			//判断仪器设备种类编号是否存在
			if (data.getTableName().equals("cyj_instrument_equipment_species")) {
				 ResultSet res = dbConn.query(String.format("select exists (select 1 from  %s where  instrument_species_code ='%s')",
						 data.getTableName(),data.getFormData().get("instrument_species_code")));
				 if (res.next()) {
					if(res.getBoolean(1)) {
					   return message.getErrorInfo("新增失败，该仪器设备种类已存在");
				 }
			   } 
			 }
			
			//判断设备检验项目编号是否存在
			if (data.getTableName().equals("cyj_inspection_equipment_library")) {
				  ResultSet rs = dbConn.query(String.format("select exists (select 1 from  cyj_inspection_equipment_library where  test_item_no ='%s')",
						  data.getTableName(),data.getFormData().get("test_item_no")));
					 if (rs.next()) {
						 if(rs.getBoolean(1)) {
						     	return message.getErrorInfo("新增失败，该仪器设备检验项目已存在");
				 }
			   } 
			 }

			//判断产品层别编号是否存在
			if (data.getTableName().equals("cyj_product_inspection_level")) {
				ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where product_detection_no ='%s')" + 
						"", data.getTableName(), data.getFormData().get("product_detection_no")));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
					  return message.getErrorInfo("新增失败,产品检测层别编号已存在");
					}
				}
			 }
			
			//判断产品属性编号是否存在
			if (data.getTableName().equals("cyj_product_inspection_setting")) {
				ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where product_pro_no ='%s')" + 
						"", data.getTableName(), data.getFormData().get("product_pro_no")));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
					  return message.getErrorInfo("新增失败,产品检测属性编号已存在");
					}
				}
			 }
			//判断来料检验层别编号是否存在
			if (data.getTableName().equals("cyj_materials_inspection_stratification")) {
				ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where stratification_no ='%s')" + 
						"", data.getTableName(), data.getFormData().get("stratification_no")));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
					  return message.getErrorInfo("新增失败，来料检验层别编号已存在");
					}
				}
			 }
			
			//判断来料检验属性编号是否存在
			if (data.getTableName().equals("cyj_materials_inspection_attribute")) {
				ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where attribute_node ='%s')" + 
						"", data.getTableName(), data.getFormData().get("attribute_node")));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
					  return message.getErrorInfo("新增失败,来料检验属性编号已存在");
					}
				}
			 }
			//仪器设备参数
			if (data.getTableName().equals("cyj_instrument_parameter_setting")) {
				 ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where parameter_code ='%s')" +
		                    "", data.getTableName(), data.getFormData().get("parameter_code")));
		            if (rs.next()) {
		                if (rs.getBoolean(1)) {
		                    return message.getErrorInfo("新增失败,该仪器设备参数编号已存在");
		                }
		            }
			 }
			//仪器设备部件
			if (data.getTableName().equals("cyj_instrument_equipment_parts")) {
				 ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where core_components_no ='%s')" +
		                    "", data.getTableName(), data.getFormData().get("core_components_no")));
		            if (rs.next()) {
		                if (rs.getBoolean(1)) {
		                    return message.getErrorInfo("新增失败,该仪器设备部件编号已存在");
		                }
		            }
			 }
			//仪器设备
			if (data.getTableName().equals("cyj_instrument_equipment")) {
				 ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where instrument_equipment_no ='%s')" +
		                    "", data.getTableName(), data.getFormData().get("instrument_equipment_no")));
		            if (rs.next()) {
		                if (rs.getBoolean(1)) {
		                    return message.getErrorInfo("新增失败,该仪器设备编号已存在");
		                }
		         }
			 }
			//产品bom管理编号
			if (data.getTableName().equals("cyj_product_bom_management")) {
				 ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where bom_code ='%s')" +
		                    "", data.getTableName(), data.getFormData().get("bom_code")));
		            if (rs.next()) {
		                if (rs.getBoolean(1)) {
		                    return message.getErrorInfo("新增失败,该产品BOM项目编号已存在");
		                }
		         }
			 }
			//产品预设缺陷项目编号
			if (data.getTableName().equals("cyj_product_defects")) {
				 ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where defects_no ='%s')" +
		                    "", data.getTableName(), data.getFormData().get("defects_no")));
		            if (rs.next()) {
		                if (rs.getBoolean(1)) {
		                    return message.getErrorInfo("新增失败,该缺陷编号已存在");
		                }
		         }
			 }
			//产品编号
			if (data.getTableName().equals("meq_products")) {
				 ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where product_code ='%s')" +
		                    "", data.getTableName(), data.getFormData().get("product_code")));
		            if (rs.next()) {
		                if (rs.getBoolean(1)) {
		                    return message.getErrorInfo("新增失败,该产品编号已存在");
		                }
		         }
			 }
			
			//客户类别添加去重
			if (data.getTableName().equals("cyj_customer_category")) {
				 ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where customer_category_code ='%s')" +
		                    "", data.getTableName(), data.getFormData().get("customer_category_code")));
		            if (rs.next()) {
		                if (rs.getBoolean(1)) {
		                    return message.getErrorInfo("新增失败,该客户类别编号已存在");
		                }
		         }
			 }
			
			//客户层别添加去重
			if (data.getTableName().equals("cyj_customer_stratification")) {
				 ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where customer_stratification_code ='%s')" +
		                    "", data.getTableName(), data.getFormData().get("customer_stratification_code")));
		            if (rs.next()) {
		                if (rs.getBoolean(1)) {
		                    return message.getErrorInfo("新增失败,该客户层别编号已存在");
		                }
		         }
			 }
			Object str=helper.dataInsertCyjq(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData());
			if (str.equals(true)) {
				return message.getSuccessInfo("新增成功");
			}else if (str.toString().contains("唯一约束")) {
				return message.getErrorInfo("新增失败，该数据已存在");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("新增失败，系统内部出现问题"+e.getMessage());
		}
		return message.getErrorInfo("新增失败");
	}
	
}
