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
import com.lc.bxm.common.helper.DataList;
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.entity.DeleteData;
import com.lc.bxm.entity.EditData;
import com.lc.bxm.entity.InsertData;

import net.sf.json.JSONObject;

/**
 * 不良原因类别接口
 * 
 * @author ljz
 *
 */
@RestController
@RequestMapping("/badReasonCate")
public class BadReasonCateResource {

	@Autowired
	PostgreSQLConn dbConn;

	@Autowired
	Message message;

	@Autowired
	GetLogs getLogs;

	@Autowired
	PostgresqlHelper helper;
	
	@Autowired
	DataList dataList;

	/**
	 * 新增单行不良原因类别
	 * 
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "addBadReasonCates", method = RequestMethod.POST)
	@ResponseBody
	public String addBadReasonCates(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			InsertData data = (InsertData) JSONObject.toBean(jsonObject, InsertData.class);
			if (data.getTableName().equals("meq_bad_reason_cates")) {
				ResultSet rs = dbConn.query(
						String.format("SELECT EXISTS (SELECT 1 FROM meq_bad_reason_cates WHERE cate_name = '%s')",
								data.getFormData().getString("cate_name")));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("新增失败,该类别名称已存在");
					}
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
	 * 修改单行不良原因类别
	 * 
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "editBadReasonCates", method = RequestMethod.POST)
	@ResponseBody
	public String editBadReasonCates(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			EditData data = (EditData) JSONObject.toBean(jsonObject, EditData.class);
			if (data.getTableName().equals("meq_bad_reason_cates")) {
				ResultSet rs = dbConn.query(
						String.format("SELECT EXISTS (SELECT 1 FROM meq_bad_reason_cates WHERE cate_name = '%s' AND cate_id <> %s)",
								data.getFormData().getString("cate_name"),data.getFormData().getString("cate_id")));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("修改失败,该类别名称已存在");
					}
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
	 * 删除单行不良原因类别
	 * 
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@RequestMapping(value = "deleteBadReasonCates", method = RequestMethod.POST)
	@ResponseBody
	public String deleteBadReasonCates(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
		try {
			DeleteData data = (DeleteData) JSONObject.toBean(jsonObject, DeleteData.class);
			if (data.getTableName().equals("meq_bad_reason_cates")) {
				ResultSet rs = dbConn.query(
						String.format("SELECT EXISTS (SELECT 1 FROM meq_bad_reasons WHERE cate_id = %s)",data.getIdValue()));
				if (rs.next()) {
					if (rs.getBoolean(1)) {
						return message.getErrorInfo("删除失败,该类别下有不良原因数据");
					}
				}
			}
			if (helper.dataDelete(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(), data.getIdName(), data.getIdValue().toString())){
				return message.getSuccessInfo("删除成功");
			} else {
				return message.getErrorInfo("删除失败");
			}
		} catch (Exception e) {
			return message.getErrorInfo("删除失败");
		}
	}
	
	@RequestMapping(value = "badReasonCatesList", method = RequestMethod.GET)
	@ResponseBody
	public String badReasonCatesList() {
		try {
			String string = dataList.getDataSingleListJson("meq_bad_reason_cates", "cate_id", "cate_name");
			return string;
		} catch (Exception e) {
			return message.getErrorInfo("[]");
		}
	}
}
