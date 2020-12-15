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
import com.lc.bxm.entity.DeleteData;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * cyjq删除通用接口
 * @author LH
 * @date 2020年5月25日
 */
@RestController
@RequestMapping("/cyjqcommon")
public class CyjqCommonDelResouces {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;
	
	/**
	 * LH 通用删除接口
	 */
	@RequestMapping(value = "delJson", method = RequestMethod.POST)
	@ResponseBody
	public String delJson(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		try {
			DeleteData data = (DeleteData) JSONObject.toBean(jsonSaveData, DeleteData.class);
			JSONArray idValues =JSONArray.fromObject(data.getIdValue());
			String delString =idValues.toString().replace("[", "(").replace("]", ")");
			
			//产品检测属性删除验证
			if (data.getTableName().equals("cyj_product_inspection_setting")) {
				String  sqlStr = String.format("select exists (select id from cyj_product_inspection_setting where up_product_pro_ids in %s"+
						         "union select id from cyj_product_finished where inspection_setting_id in %s"+
						         "union select id from cyj_product_preset_detection where inspection_setting_id in %s)",delString,delString,delString);
				ResultSet rs = dbConn.query(sqlStr);
				if (rs.next()) {
					if (rs.getBoolean(1)) {
					  return message.getErrorInfo("删除失败，该产品检测属性下有数据");
					}
				}
			}
			
			//产品检测层别删除验证
			if (data.getTableName().equals("cyj_product_inspection_level")) {
				String  sqlStr = String.format("select exists (select id from cyj_product_inspection_level where up_product_level_id in %s"+
				         "union select id from cyj_product_finished where inspection_level_id in %s"+
				         "union select id from cyj_product_preset_detection where inspection_level_id in %s)",delString,delString,delString);
				ResultSet rs = dbConn.query(sqlStr);
				if (rs.next()) {
					if (rs.getBoolean(1)) {
					  return message.getErrorInfo("删除失败，该产品检测层别下有数据");
					}
				}
			}
			//产品预设检测删除验证
			if (data.getTableName().equals("cyj_product_preset_detection")) {
				ResultSet rs = dbConn.query(String.format("select exists (select * from cyj_product_defects where detection_id in %s)" + 
						"", delString));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
					  return message.getErrorInfo("删除失败，该产品预设检测项目下有数据");
					}
				}
			}
			//产品BOM清单
			if (data.getTableName().equals("cyj_product_bom_management")) {
				ResultSet rs = dbConn.query(String.format("select exists (select * from cyj_product_bom_listing where bom_maanagen_id in %s)" + 
						"", delString));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
					  return message.getErrorInfo("删除失败，该产品BOM下有产品清单");
					}
				}
			}
			//产品缺陷处理
			if (data.getTableName().equals("cyj_product_defects")) {
				ResultSet rs = dbConn.query(String.format("select exists (select * from cyj_product_defects_handling where defects_id in %s)" + 
						"", delString));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
					  return message.getErrorInfo("删除失败，该预设缺陷下有数据");
					}
				}
			}
			
			//产品删除
			if (data.getTableName().equals("meq_products")) {
				ResultSet rs = dbConn.query(String.format("select exists (select id from cyj_product_preset_detection where  product_code in %s UNION  " + 
						" select id from cyj_product_bom_management where product_code in %s)" + 
						"", delString.replace("\"", "'")));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
					  return message.getErrorInfo("删除失败，该产品下有数据");
					}
				}
			}
			
			//仪器设备种类删除
			if (data.getTableName().equals("cyj_instrument_equipment_species")) {
				JSONArray idValue=JSONArray.fromObject(data.getIdValue());
				String species_id = idValue.toString().replace("[", "(").replace("]", ")");
				String sql="select exists (select * from cyj_instrument_equipment_species " + 
						"	a inner JOIN cyj_instrument_equipment_species b on " + 
						"	a.id=b.species_id where b.species_id in"+species_id+")";
				ResultSet rs = dbConn.query(sql);
				
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						 return message.getErrorInfo("删除失败,该仪器设备种类下还有子层仪器设备种类");
					}
				}
				 sql="select exists (select species_id from cyj_instrument_equipment where species_id  in "+species_id+")";
				 rs = dbConn.query(sql);
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						 return message.getErrorInfo("该数据已被引用，不可删除");
					}
				}
			}
			//仪器设备台账删除
			if (data.getTableName().equals("cyj_instrument_equipment")) {
//				 ResultSet rs = dbConn.query(String.format("select exists (select * from %s e" +
//                         " join cyj_instrument_equipment_parts p on e.id=p.instrument_equipment_id" +
//                         " where e.%s in %s)" + "",
//                 data.getTableName(), data.getIdName(), delString));
//		         if (rs.next()) {
//		             if (rs.getBoolean(1)) {
//		                 return message.getErrorInfo("删除失败，该设备台账下有数据");
//		             }
//		         }
				 ResultSet rs = dbConn.query(String.format("select exists (select id from v_cyj_instrument_equipment_relation where id in %s)",delString));
		         if (rs.next()) {
		             if (rs.getBoolean(1)) {
		                 return message.getErrorInfo("该数据已被引用，不可删除");
		             }
		         }
			}
			
			//客户类别删除
			if (data.getTableName().equals("cyj_customer_category")) {
				 ResultSet rs = dbConn.query(String.format("select exists (select * from %s where customer_category_id in %s)",
                 data.getTableName(), delString));
		         if (rs.next()) {
		             if (rs.getBoolean(1)) {
		                 return message.getErrorInfo("该客户类别下有子集类别，不可删除");
		             }
		         }
				  rs = dbConn.query(String.format("select exists (select * from cyj_customer_data "
				 		+ "where customer_category_id in %s)", delString));
		         if (rs.next()) {
		             if (rs.getBoolean(1)) {
		                 return message.getErrorInfo("该数据已被引用，不可删除");
		             }
		         }
			}
			

			//客户层别删除
			if (data.getTableName().equals("cyj_customer_stratification")) {
				 ResultSet rs = dbConn.query(String.format("select exists (select * from cyj_customer_stratification_content where customer_stratification_id in %s)",
                  delString));
		         if (rs.next()) {
		             if (rs.getBoolean(1)) {
		                 return message.getErrorInfo("该数据已被引用，不可删除");
		             }
		         }
				  rs = dbConn.query(String.format("select exists (select * from cyj_customer_data "
				 		+ "where customer_stratification_id in %s)", delString));
		         if (rs.next()) {
		             if (rs.getBoolean(1)) {
		                 return message.getErrorInfo("该数据已被引用，不可删除");
		             }
		         }
			}
			
			//客户删除
			if (data.getTableName().equals("cyj_customer_data")) {
				 ResultSet rs = dbConn.query(String.format("select exists (select * from sys_users "
				 		+ "where comp_id in (select comp_id from %s where %s in %s))",data.getTableName(), data.getIdName(), delString));
		         if (rs.next()) {
		             if (rs.getBoolean(1)) {
		                 return message.getErrorInfo("删除失败，该客户使用中");
		             }
		         }
			}
		     Object str = helper.dataDeleteCyjq(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
						data.getIdName(), jsonSaveData.getString("idValue"));
			if (str.equals(true)) {
				return message.getSuccessInfo("删除成功");
			}else if (str.toString().contains("外键约束")) {
				return message.getErrorInfo("删除失败，该数据已被引用");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("删除失败,系统内部问题"+e.getMessage());
		}
		return message.getErrorInfo("删除失败");
	}
	
	


}
