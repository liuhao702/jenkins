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
import com.lc.bxm.common.util.TreeDateUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.entity.DeleteData;
import com.lc.bxm.entity.InsertData;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Api(value = "仪器设备种类接口类", tags = { "仪器设备种类接口类" })
@RestController
@RequestMapping("/apparatus")
public class ApparatusResources {

	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	PostgresqlHelper helper;
	@Autowired
	TreeDateUtil tree;

	/**
	 * CK新增仪器设备种类
	 * 
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@ApiOperation(value = "新增仪器设备种类接口", notes = "新增仪器设备种类接口")
	@RequestMapping(value = "addSpecies", method = RequestMethod.POST)
	@ResponseBody
	public String addInstrumentSpecies(HttpServletRequest request,
			@RequestBody @ApiParam(name = "jsonObject", value = "传入json格式", required = true) JSONObject jsonObject) {
		try {
			InsertData data = (InsertData) JSONObject.toBean(jsonObject,InsertData.class);

			String species_code = data.getFormData().getString("instrument_species_code");
			ResultSet rs = dbConn.query(String.format(
					"select exists (select 1 from  cyj_instrument_equipment_species where  instrument_species_code ='%s')",
					species_code));
			if (rs.next()) {
				if (rs.getBoolean(1)) {
					return message.getErrorInfo("新增失败该仪器设备种类已存在");
				} else {

					if (helper.dataInsert(request, data.getMenuId(), data.getFunName(), data.getUserId(),
							data.getTableName(), data.getFormData())) {
						return message.getSuccessInfo("新增成功");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("新增失败");
	}

	/**
	 * CK 获取仪器设备种类树状图JSON
	 */
	@ApiOperation(value = "仪器设备种类树状图接口", notes = "仪器设备种类树状图接口")
	@RequestMapping(value = "speciesMenuJson", method = RequestMethod.GET)
	@ResponseBody
	public String speciesMenuJson(Integer comp_id) {
		ResultSet rs = null;
		if (comp_id!=null) {
			rs = dbConn.query("select id, instrument_species_name, species_id from cyj_instrument_equipment_species where comp_id = "+comp_id+"");
		}else {
			rs = dbConn.query("select id, instrument_species_name, species_id from cyj_instrument_equipment_species");
		}
		List<Map<Object, Object>> prodJson = tree.getResultSet(rs);
		JSONObject json = new JSONObject();
		json.put("id", "0");
		json.put("name", "全部");
		json.put("children", prodJson);
		return "[" + json + "]";

	}

	
	/**
	 * CK 仪器设备种类删除
	 */
	@ApiOperation(value = "仪器设备种类删除接口", notes = "仪器设备种类删除接口")
	@RequestMapping(value = "deleteSpecies", method = RequestMethod.POST)
	@ResponseBody
	public String getDeleteSpeciesJson(HttpServletRequest request, @RequestBody@ApiParam(name="删除数据对象",value="传入json格式",required=true) JSONObject jsonSaveData) {
		DeleteData data = (DeleteData) JSONObject.toBean(jsonSaveData, DeleteData.class);
		try {
			JSONArray idValues = JSONArray.fromObject(data.getIdValue());
			String species_id = idValues.toString().replace("[", "(").replace("]", ")");
			String sql = "select exists (select * from cyj_instrument_equipment_species "
					+ "	a inner JOIN cyj_instrument_equipment_species b on "
					+ "	a.id=b.species_id where b.species_id in" + species_id + ")";
			ResultSet rs = dbConn.query(sql);

			if (rs.next()) {
				if (rs.getBoolean(1)) {
					return message.getSuccessInfo("删除失败,该仪器设备种类下还有子层仪器设备种类");
				}
			}
			if (helper.dataDelete(request, data.getMenuId(), data.getFunName(), data.getUserId(), data.getTableName(),
					data.getIdName(), idValues.toString())) {
				return message.getSuccessInfo("删除成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getErrorInfo("删除失败");
	}

	/**
	 * CK新增仪器设备检验项目
	 * 
	 * @param request
	 * @param jsonObject
	 * @return
	 */
	@ApiOperation(value = "新增仪器设备检验项目接口", notes = "新增仪器设备检验项目接口")
	@RequestMapping(value = "addLibrary", method = RequestMethod.POST)
	@ResponseBody
	public String addInstrumentLibrary(HttpServletRequest request, @RequestBody@ApiParam(name = "新增数据对象", value = "传入json格式", required = true) JSONObject jsonObject) {
		try {
			InsertData data = (InsertData) JSONObject.toBean(jsonObject, InsertData.class);
			String jsonString = data.getFormData().toString();
			JSONObject json = JSONObject.fromObject(jsonString);
			String test_item_no = json.getString("test_item_no");
			ResultSet rs = dbConn.query(String.format(
					"select exists (select 1 from  cyj_inspection_equipment_library where  test_item_no ='%s')",
					test_item_no));
			if (rs.next()) {
				if (rs.getBoolean(1)) {
					return message.getErrorInfo("新增失败该仪器设备检验项目已存在");
				} else {
					//InsertData data = (InsertData) JSONObject.toBean(jsonObject, InsertData.class);
					if (helper.dataInsert(request, data.getMenuId(), data.getFunName(), data.getUserId(),
							data.getTableName(), data.getFormData())) {
						return message.getSuccessInfo("新增成功");
					} else {
						return message.getErrorInfo("新增失败");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return message.getSuccessInfo("新增成功");
	}
}
