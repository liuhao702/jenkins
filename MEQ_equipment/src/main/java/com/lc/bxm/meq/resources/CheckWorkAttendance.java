package com.lc.bxm.meq.resources;

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
import com.lc.bxm.entity.EditData;
import com.lc.bxm.entity.InsertData;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@RequestMapping("/workHours")
public class CheckWorkAttendance {
	
	//CheckWorkAttendance
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;
	
	
	/**
	 * 新增工时损失类别
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "addWorkHoursType", method = RequestMethod.POST)
	@ResponseBody
	public String addWorkHoursType(HttpServletRequest request ,@RequestBody JSONObject jsonObject) {
		try {
			String jsonString = jsonObject.getString("formData");
		    JSONObject json = JSONObject.fromObject(jsonString);
		    String l_code = json.getString("l_code");
		    ResultSet rs = dbConn.query(String.format("select exists (select 1 from  meq_loss_cate where  l_code ='%s')", l_code));
			 if (rs.next()) {
				 if(rs.getBoolean(1)) {
				     	return message.getErrorInfo("新增失败该工时类别已存在");
				}else {
					InsertData   data = (InsertData) JSONObject.toBean(jsonObject,InsertData.class);
					if (helper.dataInsert(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(), 
							data.getFormData())) {
						return message.getSuccessInfo("新增成功");
					}else {
						return message.getErrorInfo("新增失败");
					}
				}
			}
		} catch (Exception e) {
				e.printStackTrace();
		}
		return message.getSuccessInfo("新增成功");
	}
	
	
	/**
	 * 修改工时损失类别
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "editWorkHoursType" , method = RequestMethod.POST)
	@ResponseBody
	public String  editWorkHoursType(HttpServletRequest request , @RequestBody JSONObject jsonObject) {
		try {
		EditData  data = (EditData) JSONObject.toBean(jsonObject,EditData.class);
		if (helper.dataUpdate(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
				data.getFormData(), data.getId())) {
			return message.getSuccessInfo("修改成功");
		}else {
			return message.getErrorInfo("修改失败");
		}
		} catch (Exception e) {}
		return message.getSuccessInfo("修改成功");
	}
	
	/**
	 * 删除工时损失类别
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "deleteWorkHoursType", method = RequestMethod.POST)
	@ResponseBody
	public String getDeleteMessageJson(HttpServletRequest request, @RequestBody JSONObject jsonDeleteData) {
		try {
			 ResultSet rs = null;
			DeleteData data = (DeleteData) JSONObject.toBean(jsonDeleteData, DeleteData.class);
			JSONArray json = JSONArray.fromObject(jsonDeleteData.get("idValue"));
			for (int i = 0; i < json.size(); i++) {
				rs = dbConn.query(String.format("select exists (select * from meq_loss_cate mc,meq_loss_details md where mc.l_id = md.cate_id and mc.l_id = '%s')",json.get(i)));
		      if(rs.next()) {
		    	if (rs.getBoolean(1)) {
			    	return message.getErrorInfo("该工时类别与工时损失模块表有关联数据，不能删除");
			    }
		      }
			}
			  if (helper.dataDelete(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
						data.getIdName(), jsonDeleteData.get("idValue").toString())) {
					return message.getSuccessInfo("删除成功");
				} else {
					return message.getErrorInfo("删除失败");
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("删除失败");
	}
	
}
