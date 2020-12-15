package com.lc.bxm.cyjq.resources.featurepack;

import java.sql.ResultSet;
import java.sql.SQLException;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.meq.resources.FileResources;

/**
 * MEQv1.0投产接口
 * 
 * @author liuhao
 *
 */
@Controller
@RequestMapping("/putIntoProduct")
public class PutIntoProductResources {

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;
	@Autowired
	FileResources fileResources;

	/**
	 * 审核
	 */
	@RequestMapping(value = "examineJson", method = RequestMethod.GET)
	@ResponseBody
	public String commandExamine(@RequestParam String recId) {
//		public String commandExamine(@RequestParam String recId,@RequestParam String lineId,@RequestParam String productCode) {
		//投产下面没工艺卡不能审核
		try {
				ResultSet barcodeHead = dbConn.query("SELECT setting_value = 'true' FROM sys_settings WHERE setting_code = 'BarcodeHeadLineCommand'");
				if(barcodeHead.next()) {
					if(barcodeHead.getBoolean(1)) {
						ResultSet haveBarcode = dbConn.query("SELECT EXISTS (SELECT 1 FROM meq_line_commands_new WHERE barcode_head is null and rec_id = '" + recId + "')");
						if(haveBarcode.next()) {
							if(haveBarcode.getBoolean(1)) {
								return message.getErrorInfo("审核失败，条码前缀不能为空");
							  }
						    }
					      }
				        }
						ResultSet haveBarcode = dbConn.query("SELECT EXISTS (SELECT 1 FROM meq_line_commands_new WHERE process_id is null and rec_id = '" + recId + "')");
						if(haveBarcode.next()) {
							if(haveBarcode.getBoolean(1)) {
								return message.getErrorInfo("该投产无工艺卡，不能审核");
							}
						}
					boolean res = dbConn.queryUpdate("update meq_line_commands_new set audited = true where rec_id = '"+ recId +"'");
					if(res) {
						return message.getSuccessInfo("审核成功");
			}
		} catch (SQLException e) {
			return message.getErrorInfo("审核失败"+e.getMessage());
		}
		return message.getErrorInfo("审核失败");
	}
	
	/**
	 * 撤审
	 */
	@RequestMapping(value = "withdrawalJson", method = RequestMethod.GET)
	@ResponseBody
	public String commandWithdrawal(@RequestParam String recId) {
		ResultSet result = dbConn.query("select online_qty from meq_line_commands_new where rec_id = " + recId);
		try {
			if (result.next()) {
				if (result.getInt(1) > 0) {
					return message.getErrorInfo("已有上线数量，不能撤审");
				}
			}
			boolean res = dbConn.queryUpdate("update meq_line_commands_new set audited = false where rec_id = " + recId);
			if (res) {
				return message.getSuccessInfo("撤审成功");
			}
		} catch (SQLException e) {
			return message.getErrorInfo("系统内部问题"+e.getMessage());
		}
		return message.getErrorInfo("撤审失败");
	}

	/**
	 * 强制完成数据接口
	 */
	@RequestMapping(value = "forceSucessJson", method = RequestMethod.GET)
	@ResponseBody
	public String forceSucess(@RequestParam String recId) {
		boolean res = dbConn.queryUpdate("update meq_line_commands_new set deleted = true where rec_id = '"+ recId +"'");
		if(res) {
			return message.getSuccessInfo("强制完成成功");
		}
		return message.getErrorInfo("强制完成失败");
	}
	/**
	 * 强制完成提示接口
	 */
	@RequestMapping(value = "forceSucessPrompt", method = RequestMethod.GET)
	@ResponseBody
	public String forceSucessPrompt(@RequestParam String recId) {
		ResultSet rs = dbConn.query("select meq_get_rec_id_status("+recId+")");
		try {
			if(rs.next()) {
				if (!rs.getBoolean(1)) {
					return message.getSystemPrompt("该投产正在生产中，确定强制完成吗");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return message.getSystemPrompt("确定强制完成吗");
		
	}
	
	

}
