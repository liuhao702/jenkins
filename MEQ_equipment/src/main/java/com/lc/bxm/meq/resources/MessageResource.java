package com.lc.bxm.meq.resources;
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
import com.lc.bxm.entity.QueryData;
import net.sf.json.JSONObject;

/**
 * 消息通知类
 * 
 * @author LJZ
 * @date 2019年9月20日
 */
@RestController
@RequestMapping("/message")
public class MessageResource {

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;

	/**
	 * LJZ 消息通知新增
	 */
	@RequestMapping(value = "addMessage", method = RequestMethod.POST)
	@ResponseBody
	public String getAddMessageJson(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		try {
			InsertData data = (InsertData) JSONObject.toBean(jsonSaveData, InsertData.class);
			if (helper.dataInsert(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData())) {
				return message.getSuccessInfo("新增成功");
			} else {
				return message.getErrorInfo("新增失败");
			}
		} catch (Exception e) {
		}
		return message.getErrorInfo("新增失败");
	}

	/**
	 * LJZ 消息通知删除
	 */
	@RequestMapping(value = "deleteMessage", method = RequestMethod.POST)
	@ResponseBody
	public String getDeleteMessageJson(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		try {
			DeleteData data = (DeleteData) JSONObject.toBean(jsonSaveData, DeleteData.class);
			if (helper.dataDelete(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getIdName(), jsonSaveData.getString("idValue"))) {
				return message.getSuccessInfo("删除成功");
			} else {
				return message.getErrorInfo("删除失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("删除失败");
	}

	/**
	 * LJZ 消息编辑通知
	 */
	@RequestMapping(value = "editMessage", method = RequestMethod.POST)
	@ResponseBody
	public String getEditMessageJson(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		try {
			EditData data = (EditData) JSONObject.toBean(jsonSaveData, EditData.class);
			if (helper.dataUpdate(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getFormData(), data.getId())) {
				return message.getSuccessInfo("修改成功");
			} else {
				return message.getErrorInfo("修改失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("修改失败");
	}

	/**
	 * LJZ 消息通知查询
	 */
	@RequestMapping(value = "queryMessage", method = RequestMethod.POST)
	@ResponseBody
	public String getQueryMessages(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
		QueryData data = (QueryData) JSONObject.toBean(jsonSaveData, QueryData.class);
	    return helper.dataQuery(request, data.getTableName(), data.getPageSize(), data.getCurrentPage(),
			data.getOrder(), data.getProp(), data.getFilterString(), data.getInputSearch());
	}
}
