package com.lc.bxm.cyjq.resources.equipment;

import java.sql.ResultSet;

import java.util.List;
import java.util.Map;

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
import com.lc.bxm.common.util.TreeDateUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.entity.DeleteData;
import com.lc.bxm.entity.EditData;
import com.lc.bxm.entity.InsertData;
import net.sf.json.JSONObject;

/**
 * MEQ仪器设备部件
 *
 * @author HJQ
 * @date 2020年5月22日
 */
@RestController
@RequestMapping("/instrument")
public class EquipmentPartsResources {

    @Autowired
    PostgreSQLConn dbConn;
    @Autowired
    ReadExcel read;
    @Autowired
    Message message;
    @Autowired
    PostgresqlHelper helper;
    @Autowired 
	TreeDateUtil tree;

    /**
     * HJQ 获取仪器设备部件树状图JSON
     */
    @RequestMapping(value = "equipmentPartsMenuJson", method = RequestMethod.GET)
    @ResponseBody
    public String equipmentPartsMenuJson() {
    	ResultSet rs  = dbConn.query("select id, instrument_equipment_name,species_id from cyj_instrument_equipment");
		List<Map<Object, Object>> equipmentPartsJson= tree.getResultSet(rs);
//        String equipmentPartsJson = getequipmentPartsMenuGroupJson("");
        JSONObject json = new JSONObject();
        json.put("id", "0");
        json.put("name", "全部");
        json.put("children", equipmentPartsJson );
        return "[" + json + "]";
    }

    /**
     * HJQ 拼接仪器设备部件分组JSON
     */
//    private String getequipmentPartsMenuGroupJson(String parentId) {
//        ResultSet rs = null;
//        if (parentId == "") {
//            rs = dbConn.query("select id, instrument_equipment_name,species_id from cyj_instrument_equipment where species_id is null");
//        } else {
//            rs = dbConn.query(String.format(
//                    "select id, instrument_equipment_name,species_id from cyj_instrument_equipment where species_id = '%s'", parentId));
//        }
//        StringBuilder sb = new StringBuilder();
//        try {
//            while (rs.next()) {
//                sb.append("{\"id\":\"");
//                sb.append(rs.getString(1));
//                sb.append("\",\"name\":\"");
//                sb.append(rs.getString(2));
//                String str = getequipmentPartsMenuGroupJson(rs.getString(1));
//                if (!str.equals("")) {
//                    sb.append(",\"children\":[" + str + "]");
//                }
//                sb.append("},");
//            }
//        } catch (SQLException e) {
//        }
//        return Str.delComma(sb.toString());
//    }

    /**
     * HJQ 仪器设备部件层别
     */
    @RequestMapping(value = "addEquipmentParts", method = RequestMethod.POST)
    @ResponseBody
    public String addEquipmentParts(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
        try {
            InsertData data = (InsertData) JSONObject.toBean(jsonSaveData, InsertData.class);
            ResultSet rs = dbConn.query(String.format("select exists (select * from  %s  where core_components_no ='%s')" +
                    "", data.getTableName(), data.getFormData().get("core_components_no")));
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
     * HJQ 仪器设备部件删除
     */
    @RequestMapping(value = "delEquipmentParts", method = RequestMethod.POST)
    @ResponseBody
    public String delEquipmentParts(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
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
     * HJQ 仪器设备部件通知
     */
    @RequestMapping(value = "editEquipmentParts", method = RequestMethod.POST)
    @ResponseBody
    public String editEquipmentParts(HttpServletRequest request, @RequestBody JSONObject jsonSaveData) {
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
