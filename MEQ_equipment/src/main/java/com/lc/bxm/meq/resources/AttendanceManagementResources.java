package com.lc.bxm.meq.resources;

import java.sql.CallableStatement;
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
import com.lc.bxm.entity.InsertData;

import net.sf.json.JSONObject;

/**
 * 工时表接口
 */
@RestController
@RequestMapping("/attendance")
public class AttendanceManagementResources {

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;

	/**
	 * 编辑考勤管理主表
	 * 
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "editWorkHours", method = RequestMethod.POST)
	@ResponseBody
	public String editWorkHours(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			EditData data = (EditData) JSONObject.toBean(jsonObject, EditData.class);
			if (helper.dataUpdate(request, data.getMenuId(), data.getFunName(), data.getUserId(),
					data.getTableName(), data.getFormData(), data.getId())) {
				return message.getSuccessInfo("修改成功");
			} else {
				return message.getErrorInfo("修改失败");
			}
		} catch (Exception e) {
		}
		return message.getSuccessInfo("修改成功");
	}

	/**
	 * 新增工时损失主表数据
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "getDatas", method = RequestMethod.POST)
	@ResponseBody
	public String addWorkHours(HttpServletRequest request) {
		try {
			ResultSet rs = dbConn.query("select exists (select 1 from meq_attendance_record where date::date = current_date - 1)");
			if(rs.next()) {
				if(!rs.getBoolean(1)) {
					String sql = "call insert_meq_attendance_record_test()";
					CallableStatement proc = dbConn.getConn().prepareCall(sql);
					proc.execute();
				}else {
					return message.getErrorInfo("已获取过昨天数据");
				}
				String sql="insert into meq_attendance_record(dept_id,line_id,user_id,standard_hours,date) (select dept_id,line_id,\n" + 
						"user_id,standard_hours,date from meq_attendance_record_test  where date::date = current_date-1 );";
				boolean res = dbConn.queryUpdate(sql);
				if(res) {
					return message.getSuccessInfo("获取成功"); 
				}
			}
		} catch (Exception e) {
				e.printStackTrace();
		}
		return message.getErrorInfo("获取失败");
	}



	/**
	 * 新增损失工时
	 */
	@RequestMapping(value = "addLossDetails", method = RequestMethod.POST)
	@ResponseBody
	public String addWorkHoursType(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			InsertData data = (InsertData) JSONObject.toBean(jsonObject, InsertData.class);
			if(data.getTableName().equals("meq_loss_details")) {
				ResultSet rs = dbConn.query(String.format("SELECT meq_loss_hours_insert_valid(%s,%s)", data.getFormData().getString("loss_hour"),
															data.getFormData().getString("ar_id")));
				if(rs.next()) {
					Integer insertStatus = rs.getInt(1);
					if(insertStatus.equals(0)) {
						return message.getErrorInfo("新增失败,考勤主表损失工时不能为空");
					}else if(insertStatus.equals(1)) {
						return message.getErrorInfo("新增失败,考勤主表损失工时不能为0");
					}else if(insertStatus.equals(2)) {
						return message.getErrorInfo("新增失败,损失工时原因总损失工时不能大于该考勤总表记录的总损失工时");
					}else {}
				}
			}
			if (helper.dataInsert(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData())) {
				return message.getSuccessInfo("新增成功");
			} else {
				return message.getErrorInfo("新增失败");
			}
		} catch (Exception e) {
			return message.getErrorInfo("新增失败");
		}
	}

	/**
	 * 编辑损失工时
	 */
	@RequestMapping(value = "editLossDetails", method = RequestMethod.POST)
	@ResponseBody
	public String etidWorkHoursType(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			EditData data = (EditData) JSONObject.toBean(jsonObject, EditData.class);
			if(data.getTableName().equals("meq_loss_details")) {
				ResultSet rs = dbConn.query(String.format("SELECT meq_loss_hours_update_valid(%s,%s)", data.getFormData().getString("loss_hour"),
															data.getFormData().getString("l_id")));
				if(rs.next()) {
					Integer insertStatus = rs.getInt(1);
					if(insertStatus.equals(0)) {
						return message.getErrorInfo("修改失败,考勤主表损失工时不能为空");
					}else if(insertStatus.equals(1)) {
						return message.getErrorInfo("修改失败,考勤主表损失工时不能为0");
					}else if(insertStatus.equals(2)) {
						return message.getErrorInfo("修改失败,损失工时原因总损失工时不能大于该考勤总表记录的总损失工时");
					}else {}
				}
			}
			if (helper.dataUpdate(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData(), data.getId())) {
				return message.getSuccessInfo("修改成功");
			} else {
				return message.getErrorInfo("修改失败");
			}
		} catch (Exception e) {
			return message.getErrorInfo("修改失败");
		}
	}
	
	/**
	 * 新增记录
	 */
	@RequestMapping(value = "addRecord", method = RequestMethod.POST)
	@ResponseBody
	public String addRecord(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			if(jsonObject.get("date") == null && jsonObject.getString("date").isEmpty()) {
				return message.getErrorInfo("新增失败,日期不能为空");
			}
			ResultSet rs = dbConn.query(String.format("SELECT EXISTS (SELECT 1 FROM meq_attendance_record WHERE date = '%s') ", jsonObject.getString("date")));
			if(rs.next()) {
				return message.getErrorInfo("新增失败,该日期下已有数据");
			}else {
				if(dbConn.queryUpdate(String.format("CALL insert_meq_attendance_record('%s')", jsonObject.get("date")))) {
					return message.getSuccessInfo("新增成功");
				}else {
					return message.getSuccessInfo("新增失败");
				}
			}
		} catch (Exception e) {
			return message.getErrorInfo("新增失败" + e);
		}
	}
}
