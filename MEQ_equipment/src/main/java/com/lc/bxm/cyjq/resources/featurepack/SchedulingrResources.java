package com.lc.bxm.cyjq.resources.featurepack;

import java.sql.ResultSet;


import javax.servlet.http.HttpServletRequest;

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
 * MEQv1.0排产接口
 * 
 * @author liuhao
 *
 */
@Controller
@RequestMapping("/linePlanNew")
public class SchedulingrResources {

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
	 * 
	 * 排产审核接口
	 * 
	 */

	@RequestMapping(value = "auditLinePlans", method = RequestMethod.GET)

	@ResponseBody

	public String auditLinePlans(HttpServletRequest request, @RequestParam Integer detail_id,
			@RequestParam String userId) {

		try {
			ResultSet rsQty = dbConn
					.query(String.format("SELECT qty FROM meq_line_plan_details WHERE detail_id = %s", detail_id));
			if (rsQty.next()) {
				if (rsQty.getInt(1) <= 0) {
					return message.getErrorInfo("排产数量必须大于0");
				}
			}
//			String sqlInsert = String.format(
//					"UPDATE meq_line_plan_details_new SET is_audited= true, auditor = '%s',auditor_date = current_timestamp WHERE detail_id = '%s'",
//					userId, detail_id);
			String sqlInsert = String.format(
					"UPDATE meq_line_plan_details SET is_audited= true, auditor_user='%s',auditor_date = current_timestamp WHERE detail_id = '%s'",
					userId, detail_id);
			boolean res = dbConn.queryUpdate(sqlInsert);
//			String sqlInsertProduct = String.format("CALL select_new_products(%s)", linePlanId);
//			dbConn.queryUpdate(sqlInsertProduct);
			// 捕获用户操作日志
			getLogs.getUserLog(request, "375d7728-0f5f-4f25-b569-cd9073a0f0ff", "审核", res, "", "", userId);

			if (res) {
				return message.getSuccessInfo("审核成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("审核失败" + e.getMessage());
		}
		return message.getErrorInfo("审核失败");

	}

	/**
	 * 
	 * 排产撤审接口
	 * 
	 */

	@RequestMapping(value = "unauditLinePlans", method = RequestMethod.GET)

	@ResponseBody

	public String unauditLinePlans(HttpServletRequest request, @RequestParam String detail_id,
			@RequestParam String userId) {
		try {
			ResultSet rs = dbConn.query(String.format(
					"SELECT COALESCE(sum(online_qty),0) = 0 FROM meq_line_plan_details WHERE detail_id = '%s'",
					detail_id));

			if (rs.next()) {
				if (rs.getBoolean(1)) {
					ResultSet rsClose = dbConn.query(String.format(
							"SELECT EXISTS (SELECT * FROM meq_line_plan_details WHERE close_user != null and detail_id = %s)",
							detail_id));

					if (rsClose.next()) {
						if (rsClose.getBoolean(1)) {
							return message.getErrorInfo("该排产单已关闭,不能撤审");

						}

					}

					ResultSet rsCommand = dbConn
							.query(String.format("SELECT EXISTS (SELECT c.rec_id FROM meq_line_commands_new c \n" +

									"LEFT JOIN meq_line_plan_details d ON c.detail_id = d.detail_id\n" +

									"WHERE d.detail_id = %s)", detail_id));

					if (rsCommand.next()) {

						if (rsCommand.getBoolean(1)) {

							return message.getErrorInfo("该排产单已有投产数据,不能撤审");
						}
					}
					String sqlInsert = String.format(
							"UPDATE meq_line_plan_details SET auditor_user = null, is_audited =false ,auditor_date = null WHERE detail_id = '%s'",
							detail_id);

					boolean res = dbConn.queryUpdate(sqlInsert);
					// 捕获用户操作日志
					getLogs.getUserLog(request, "375d7728-0f5f-4f25-b569-cd9073a0f0ff", "撤审", res, "", "", userId);

					if (res) {
						return message.getSuccessInfo("撤审成功");
					} else {
						return message.getErrorInfo("撤审失败");
					}
				}
			}
			return message.getErrorInfo("此排产单已有产品上线,不能撤审");
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("撤审失败");

		}

	}

	/**
	 * 
	 * 排产关闭接口
	 * 
	 */

	@RequestMapping(value = "closeLinePlanDetails", method = RequestMethod.GET)

	@ResponseBody

	public String closeLinePlanDetails(HttpServletRequest request, @RequestParam String detail_id,
			@RequestParam String userId) {
		try {
			ResultSet closeValidResultSet = dbConn.query(String.format(
					"select detail_id,is_audited from meq_line_plan_details WHERE detail_id = '%s'", detail_id));
			if (closeValidResultSet.next()) {

				if (closeValidResultSet.getBoolean(2)) {

					ResultSet rs = dbConn.query(String.format(
							"SELECT EXISTS (SELECT 1 FROM meq_line_commands_new WHERE detail_id = %s)", detail_id));

					if (rs.next()) {

						if (!rs.getBoolean(1)) {

							String sqlUpdate = String.format(
									"UPDATE meq_line_plan_details SET  close_user = '%s',closed_date = CURRENT_TIMESTAMP WHERE detail_id = '%s'",
									userId, detail_id);

							boolean res = dbConn.queryUpdate(sqlUpdate);
							// 捕获用户操作日志
							getLogs.getUserLog(request, "375d7728-0f5f-4f25-b569-cd9073a0f0ff", "关闭", res, "", "", userId);

							if (res) {
								return message.getSuccessInfo("关闭成功");
							} else {
								return message.getErrorInfo("关闭失败");

							}
						}
					}
				}
				return message.getErrorInfo("该排产单正在投产,不能关闭");
			} else {
				return message.getErrorInfo("排产单未审核,不能关闭");

			}
		} catch (Exception e) {
			return message.getErrorInfo("关闭失败"+e.getMessage());

		}

	}

}
