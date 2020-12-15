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
 * 员工
 * @author LJZ
 * @date 2019年9月17日
 */
@RestController
@RequestMapping("/employee")
public class EmployeeResource {
	
	@Autowired
	PostgreSQLConn dbConn;
	
	@Autowired
	ReadExcel read;
	
	@Autowired
	Message message;
	
	/**
	 * 导入Excel数据
	 */
	@RequestMapping(value = "employeeUpload", method = RequestMethod.POST)
	@ResponseBody
	public String employeeUpload(MultipartHttpServletRequest request) throws IOException {
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
			List<List<Object>> list = read.readExcel(fileName,file.getInputStream(),true);
			
			// 关闭流
			input.close();
			// 执行数据插入的操作

			if(!addEmployee(list).equals("导入成功")) {
				return message.getErrorInfo("导入失败,文件内容不符合要求");
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
	public String addEmployee(List<List<Object>> list) {
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
						temp = lo.get(k) == null ? "" : lo.get(k).toString();
						if(columnName.equals("员工编码")) {
							if(temp.isEmpty()) {
								return "导入失败,员工编码不能为空";
							}else {
								String.format("SELECT EXISTS (SELECT employee_code FROM meq_employee WHERE employee_code = '%s')", lo.get(k));
								rs = dbConn.query(String.format("SELECT EXISTS (SELECT employee_code FROM meq_employee WHERE employee_code = '%s')", lo.get(k)));
								if(rs.next()) {
									if(rs.getBoolean(1)) {
										return "导入失败,员工编码不能重复";
									}else {
										temp = lo.get(k).toString();
									}
								}
							}
						}else if(columnName.equals("员工姓名")) {
							if(temp.isEmpty()) {
								return "导入失败,员工名称不能为空";
							}else {
								temp = lo.get(k).toString();
							}								
						}else if(columnName.equals("部门")) {
							rs = dbConn.query(String.format("SELECT dept_id FROM sys_departments WHERE dept_name = '%s'", lo.get(k)));
							if(rs.next()) {
								temp = rs.getString(1);
							}else {
								return "导入失败,部门不存在";
							}
						}
						else if(columnName.equals("在职")) {
							temp = lo.get(k).equals("是") ? "true" : "false";
						}else {
							temp = lo.get(k).toString();
						}
						
						if(!(temp == null) && !temp.isEmpty()) {
							valueString.append(String.format("'%s',", temp));
						}else {
							valueString.append(String.format("null,"));
						}
					}
					if(j > 0) {
						valueResult.append(String.format("(%s),",Str.delComma(valueString.toString())));
					}
				}
				sqlInsert = String.format("INSERT INTO meq_employee (employee_code,employee_name,dept_id,is_onduty) VALUES %s",
											Str.delComma(valueResult.toString()));
				if(dbConn.queryUpdate(sqlInsert)) {
					return "导入成功";
				}else {
					return "导入失败,sql语句不符合要求";
				}
			} else {
				return "导入失败,表格无数据";
			}
		} catch (Exception e) {
			return "导入失败," + e;
		}
	}	
}
