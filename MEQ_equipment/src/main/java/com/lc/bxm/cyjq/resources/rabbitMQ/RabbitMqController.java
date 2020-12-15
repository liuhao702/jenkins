//package com.lc.bxm.cyjq.resources.rabbitMQ;
//
//import java.sql.ResultSet;
//
//
//import java.sql.SQLException;
//import javax.servlet.http.HttpServletRequest;
//import org.apache.log4j.Logger;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//import com.lc.bxm.common.GetLogs;
//import com.lc.bxm.common.Message;
//import com.lc.bxm.dbconnection.PostgreSQLConn;
//
//@RestController
//@RequestMapping("/rabbitMqData")
//public class RabbitMqController  {
//
//	private static Logger logger = Logger.getLogger(RabbitMqController.class);
//
//	@Autowired
//	PostgreSQLConn dbConn;
//	@Autowired
//	Message message;
//	@Autowired
//	GetLogs getLogs;
//	@Autowired
//	private RabbitTemplate rabbitTemplate ;
//	
////	@Autowired
////	private CallBackMQ callBackMQ;
//	
//	@Autowired
//	private ReturnCall returnCall;
//	
//	/**
//	 * 完成结单接口 手动推送
//	 * @param request
//	 * @param detail_id
//	 * @return
//	 */
//	@RequestMapping(value = "completeWorkOrder", method = RequestMethod.GET)
//	@ResponseBody
//	public String completeWorkOrder(HttpServletRequest request, @RequestParam Integer detail_id, boolean  is_finished_product ) {
//		logger.info("開始结单");
//		ResultSet rs = null;
//		try {
//			rs = dbConn.query(String.format("select is_complete from meq_line_plan_details where detail_id = %s", detail_id));
//			if (rs.next()) {
//				if (rs.getBoolean(1)) {
//					return message.getErrorInfo("结单失败，该工单已完结");
//				} else {
//					rs = dbConn.query(String.format(
//							"select exists(select 1 from meq_line_commands_new where detail_id = %s and not deleted)",detail_id));
//					if (rs.next()) {
//						if (!rs.getBoolean(1)) {
//							 Object stateStr = sendMassage(detail_id,is_finished_product);
//							if (stateStr.equals(true)) {
//							  dbConn.queryUpdate(String.format("update meq_line_plan_details set is_complete = true where detail_id =%s",detail_id));
//							  return message.getSuccessInfo("结单成功，数据推送成功");
//							} else if (stateStr.equals(false)) {
//								return message.getErrorInfo("结单失败，数据推送失败");
//							}else if (stateStr.equals("null")) {
//								return message.getErrorInfo("结单失败，没有数据可推送");
//							}
//						}
//						return message.getErrorInfo("结单失败，该工单正在生产");
//					}
//				}
//			}
//		  } catch (SQLException e) {
//			e.printStackTrace();
//			return message.getErrorInfo("结单失败，系统异常" + e.getMessage());
//		}
//	  return message.getErrorInfo("结单失败");
//	}
//
//	
//	/**
//	 * 往二级节点队列推送结单数据
//	 * @param detail_id
//	 * @return
//	 */
//	public Object sendMassage(Integer detail_id, boolean state) {
//		ResultSet rs = null;
//		try {
//		if (state) {
//			 rs = dbConn.query(String.format("select * from meq_product_info(%s) as (product_json varchar,quality_data varchar,part_data varchar,picture_data varchar)", detail_id));
//			 if (rs.next()) {
//					logger.info("发送消息为:成品");
//					if (rs.getObject(1).equals("")||rs.getObject(2).equals("")||rs.getObject(3).equals("")||rs.getObject(4).equals("")) {
//						return "null";
//					}
//					//成品
//					rabbitTemplate.convertAndSend("RabbitMQ_FinishedProduct", rs.getObject(1));
//					//检测数据
//					rabbitTemplate.convertAndSend("RabbitMQ_TestData",rs.getObject(2));
//					//配件
//					rabbitTemplate.convertAndSend("RabbitMQ_ProductParts", rs.getObject(3));
//					//产品图片
//					rabbitTemplate.convertAndSend("RabbitMQ_PictureInfo", rs.getObject(4));
//		       }
//		 }else {
//			 rs = dbConn.query(String.format("select * from meq_parts_info(%s)", detail_id));
//			 if (rs.next()) {
//					logger.info("发送消息为:配件");
//					if (rs.getObject(1).equals("")) {
//						return "null";
//					}
//					//配件
//					rabbitTemplate.convertAndSend("RabbitMQ_PartsInfo", rs.getObject(1));
//		       }
//		     }
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//		return returnCall.returnBack;
//	}
//	
//	/**
//	  * 完成结单接口 自动推送（c++调用）
//	 * @param request
//	 * @param detail_id
//	 * @return
//	 */
//	@RequestMapping(value = "autoCompleteWorkOrder", method = RequestMethod.GET)
//	@ResponseBody
//	public String autoCompleteWorkOrder(String barcode,boolean state ) {
//		   logger.info("開始结单");
//				Object stateStr = sendMassageBarcode(barcode,state);
//						if (stateStr.equals(true)) {
//						  return message.getSuccessInfo("结单成功，数据推送成功");
//						} else if (stateStr.equals(false)) {
//							return message.getErrorInfo("结单失败，数据推送失败");
//						}else if (stateStr.equals("null")) {
//							return message.getErrorInfo("结单失败，没有数据可推送");
//						}
//	          return message.getErrorInfo("结单失败");
//	    }
//	
//	
//	/**
//	   * 根据条码获取数据往二级节点队列推送结单数据
//	 * @param detail_id
//	 * @return
//	 */
//	public Object sendMassageBarcode(String barcode, boolean state) {
//		ResultSet rs = null;
//		try {
//		if (state) {
//			 rs = dbConn.query(String.format("select * from meq_product_info_by_barcode('%s') as (product_json varchar,quality_data varchar,part_data varchar,picture_data varchar)", barcode));
//			 if (rs.next()) {
//					logger.info("发送消息为:成品");
//					if (rs.getObject(1).equals("")||rs.getObject(2).equals("")||rs.getObject(3).equals("")||rs.getObject(4).equals("")) {
//						return "null";
//					}
//					//成品
//					rabbitTemplate.convertAndSend("RabbitMQ_FinishedProduct", rs.getObject(1));
//					//检测数据
//					rabbitTemplate.convertAndSend("RabbitMQ_TestData",rs.getObject(2));
//					//配件
//					rabbitTemplate.convertAndSend("RabbitMQ_ProductParts", rs.getObject(3));
//					//产品图片
//					rabbitTemplate.convertAndSend("RabbitMQ_PictureInfo", rs.getObject(4));
//		       }
//		 }else {
//			 rs = dbConn.query(String.format("select meq_parts_info_by_barcode('%s')", barcode));
//			 if (rs.next()) {
//					logger.info("发送消息为:配件");
//					if (rs.getObject(1).equals("")) {
//						return "null";
//					}
//					//配件
//					rabbitTemplate.convertAndSend("RabbitMQ_PartsInfo", rs.getObject(1));
//		       }
//		     }
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return returnCall.returnBack;
//	}
//
//}
