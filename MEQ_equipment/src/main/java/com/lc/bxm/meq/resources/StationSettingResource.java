package com.lc.bxm.meq.resources;

import java.io.IOException;

import java.io.InputStream;
import java.sql.ResultSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.common.util.ReadExcel;
import com.lc.bxm.dbconnection.PostgreSQLConn;

/**
 * 工位属性设置
 * @author LJZ
 * @date 2019年9月11日
 */
@RestController
@RequestMapping("/stationSetting")
public class StationSettingResource {
	
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	ReadExcel read;
	@Autowired
	Message message;
	
	/**
	 * 导入Excel数据
	 */
	@RequestMapping(value = "settingUpload", method = RequestMethod.POST)
	@ResponseBody
	public String settingUpload(MultipartHttpServletRequest request) throws IOException {
		try {
			
			MultipartFile file = request.getFile("file");
			if (file == null) {
				return message.getErrorInfo("导入文件为空");
			}
			// 获取文件名称
			String fileName = file.getOriginalFilename();
			// 判断上传的文件是否为excel
			String fileType = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
			if (!"xlsx".equalsIgnoreCase(fileType)) {
				return message.getErrorInfo("导入失败,文件格式不符合要求");
			}
			InputStream input = file.getInputStream();
			List<List<Object>> list = read.readExcel(fileName, input, true);
			// 关闭流
			input.close();
			// 执行数据插入的操作
			if (request.getParameter("station").equals("stationPropUpload")) {
				if(!addSettingProp(list)) {
					return message.getErrorInfo("导入失败,文件内容不符合要求");
				}
			}else {
			if(!addStationSetting(list)) {
				return message.getErrorInfo("导入失败,文件内容不符合要求");
			}
		  }
		} catch (Exception e) {
			e.printStackTrace();
			return message.getErrorInfo("导入失败,文件内容不符合要求");
		}
		
		return message.getSuccessInfo("导入成功");
	}
		
	
	/**
	 * 导入ERP数据
	 */
	public boolean addStationSetting(List<List<Object>> list) {
		try {
			if (null != list && list.size() > 1) {
				String sqlInsert = null;
				StringBuilder valueString = new StringBuilder();
				StringBuilder valueResult = new StringBuilder();
				String temp = null;
				String columnName = null;
				String lineId = "";
				String stationId = "";
				ResultSet rs = null;
				// 验证数据
				//List<Object> lo2 = list.get(1);
				for (int j = 1; j < list.size(); j++) {
					valueString.setLength(0);
					List<Object> lo = list.get(j);
					// 下面是判断1,2,3列不允许为空
					for(int k = 0;k<lo.size();k++) {
						columnName = list.get(0).get(k).toString();
						if(columnName.equals("产线")) {
							rs = dbConn.query(String.format("SELECT line_id FROM meq_production_lines WHERE line_name = '%s'", lo.get(k)));
							if (rs.next()) {
								lineId = temp = rs.getString(1);
							}else {
								temp = "";
							}
						}else if(columnName.equals("工位")) {
							rs = dbConn.query(String.format("SELECT s.station_id FROM meq_line_stations s LEFT JOIN meq_production_lines l ON s.line_id = l.line_id" + 
																" WHERE s.station_name = '%s' AND l.line_name = '%s'", lo.get(k),lo.get(k-1)));
							if(rs.next()) {
								stationId = temp = rs.getString(1);
							}else {
								temp = "";
							}
						}else if(columnName.equals("属性")) {											
							rs = dbConn.query(String.format("SELECT prop_code FROM meq_station_properties WHERE prop_name = '%s'", lo.get(k)));
							if(rs.next()) {
								temp = rs.getString(1);
								ResultSet rsValid = dbConn.query(String.format("SELECT EXISTS (SELECT 1 FROM meq_station_settings WHERE line_id = %s AND "
										+ "station_id = %s AND prop_code = '%s') ", lineId,stationId,temp));
								if(rsValid.next()) {
									if(rsValid.getBoolean(1)) {
									return false;
									}
								}
							}else {
								temp = "";
							}
						}
						else if(columnName.equals("加密")) {
							temp = lo.get(k).equals("是") ? "true" : "false";
						}else {
							temp = lo.get(k) == null ? "" : lo.get(k).toString();
						}
						if(!temp.isEmpty()) {
							valueString.append(String.format("'%s',", temp));
						}else {
							valueString.append(String.format("null,"));
						}
					}
					if(j > 0) {
						valueResult.append(String.format("(%s),",Str.delComma(valueString.toString())));
					}
				}
				sqlInsert = String.format("INSERT INTO meq_station_settings(line_id,station_id,prop_code,prop_value,is_password) VALUES%s",
											Str.delComma(valueResult.toString()));
				return dbConn.queryUpdate(sqlInsert);	
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * 导入ERP数据
	 */
	public boolean addSettingProp(List<List<Object>> list) {
		try {
			if (null != list && list.size() > 1) {
				String sqlInsert = null;
				StringBuilder valueString = new StringBuilder();
				StringBuilder valueResult = new StringBuilder();
				String temp = null;
				String columnName = null;
				ResultSet rs = null;
				// 验证数据
				//List<Object> lo2 = list.get(1);
				for (int j = 1; j < list.size(); j++) {
					valueString.setLength(0);
					List<Object> lo = list.get(j);
					// 下面是判断1,2,3列不允许为空
					for(int k = 0;k<lo.size();k++) {
						columnName = list.get(0).get(k).toString();
						if(columnName.equals("工位属性编码")) {
							rs = dbConn.query(String.format("SELECT EXISTS(SELECT 1 FROM meq_station_properties WHERE prop_code = '%s')", lo.get(k)));
							if(rs.next()) {
								if (rs.getBoolean(1)) {
									return false;
								}
							}
						}else if(columnName.equals("工位属性名称")) {
								rs = dbConn.query(String.format("SELECT EXISTS(SELECT 1 FROM meq_station_properties WHERE  prop_name ='%s')", lo.get(k)));
								if(rs.next()) {
									if (rs.getBoolean(1)) {
										return false;
									}
								}
						}else {
							temp = lo.get(k) == null ? "" : lo.get(k).toString();
						}
						   temp = lo.get(k) == null ? "" : lo.get(k).toString();
						if(!temp.isEmpty()) {
							valueString.append(String.format("'%s',", temp));
						}else {
							valueString.append(String.format("null,"));
						}
					}
					if(j > 0) {
						valueResult.append(String.format("(%s),",Str.delComma(valueString.toString())));
					}
				}
				sqlInsert = String.format("INSERT INTO meq_station_properties(prop_code,prop_name) VALUES%s",
											Str.delComma(valueResult.toString()));
				return dbConn.queryUpdate(sqlInsert);	
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
