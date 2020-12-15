package com.lc.bxm.cyjq.resources.equipment;

import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.common.util.ReadExcel;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.entity.DeleteData;
import com.lc.bxm.entity.EditData;
import com.lc.bxm.entity.InsertData;
import net.sf.json.JSONObject;

/**
 * MEQ仪器设备参数设定
 *
 * @author HJQ
 * @date 2020年5月22日
 */
@RestController
@RequestMapping("/instrument")
public class ParameterSettingResources {

    @Autowired
    PostgreSQLConn dbConn;
    @Autowired
    ReadExcel read;
    @Autowired
    Message message;
    @Autowired
    PostgresqlHelper helper;

    /**
     * HJQ 仪器设备参数设定
     */
    @RequestMapping(value = "addParameterSetting", method = RequestMethod.POST)
    @ResponseBody
    public String addParameterSetting(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
        try {
            InsertData data = (InsertData) JSONObject.toBean(jsonSaveData, InsertData.class);
            ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where parameter_code ='%s')" +
                    "", data.getTableName(), data.getFormData().get("parameter_code")));
            if (rs.next()) {
                if (rs.getBoolean(1)) {
                    return message.getSuccessInfo("新增失败,该产品层别已存在");
                }
            }
            if (helper.dataInsert(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
                    data.getFormData())) {
                return message.getSuccessInfo("新增成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return message.getErrorInfo("新增失败，系统内部出现问题" + e.getMessage());
        }
        return message.getErrorInfo("新增失败");
    }

    /**
     * HJQ 仪器设备参数设定删除
     */
    @RequestMapping(value = "delParameterSetting", method = RequestMethod.POST)
    @ResponseBody
    public String delParameterSetting(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
        try {
            DeleteData data = (DeleteData) JSONObject.toBean(jsonSaveData, DeleteData.class);
            if (helper.dataDelete(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
                    data.getIdName(), jsonSaveData.getString("idValue"))) {
                return message.getSuccessInfo("删除成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return message.getErrorInfo("删除失败,系统内部问题" + e.getMessage());
        }
        return message.getErrorInfo("删除失败");
    }

    /**
     * HJQ 仪器设备参数设定编辑
     */
    @RequestMapping(value = "editParameterSetting", method = RequestMethod.POST)
    @ResponseBody
    public String editParameterSetting(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
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
}
