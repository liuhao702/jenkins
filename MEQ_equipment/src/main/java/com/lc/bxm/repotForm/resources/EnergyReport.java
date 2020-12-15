package com.lc.bxm.repotForm.resources;
import java.sql.ResultSet;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.lc.bxm.common.GetLogs;
import com.lc.bxm.common.Message;
import com.lc.bxm.common.helper.PostgresqlHelper;
import com.lc.bxm.common.helper.Str;
import com.lc.bxm.dbconnection.PostgreSQLConn;
import com.lc.bxm.entity.DepartmentData;
import com.lc.bxm.entity.EnergyData;
import com.lc.bxm.entity.LineData;

/**
 * 能源数据报表
 */
@RestController
@RequestMapping("/reportForm")
public class EnergyReport {
    
	@Autowired 
	Str str;
	@Autowired
	PostgreSQLConn dbConn;
	@Autowired
	Message message;
	@Autowired
	GetLogs getLogs;
	@Autowired
	PostgresqlHelper helper;

	@RequestMapping(value = "getEnergyReport",method = RequestMethod.POST )
	@ResponseBody
	public String TestDept(HttpServletRequest request, HttpServletResponse response, @RequestBody String date){
		date.trim();
		if (date == null || date.equals("") || date.equals("123")) {
			 Date sDate = new Date();// 获取当前日期
			 SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM");
			 date = sdf.format(sDate);
			
		}
		ResultSet rsDept = dbConn.query("select dept_name from v_dept_lines group by dept_name");
		DepartmentData data = new DepartmentData();
		LineData lineData = null;
		EnergyData energyData = null;
		List<Object> dataValue = null;
		try {
			while(rsDept.next()) {
				data.setDeptName(rsDept.getString(1));
				ResultSet rsLine = dbConn.query(String.format("select line_name from v_dept_lines where dept_name = '%s' group by line_name", rsDept.getString(1)));
				while (rsLine.next()) {
					ResultSet rsEnergy = dbConn.query(String.format("select * from meq_energy_report('%s') where line_name = '%s'", date, rsLine.getString(1)));
					lineData = new LineData();
					lineData.setLineName(rsLine.getString(1));
					while (rsEnergy.next()) {
						dataValue = new ArrayList<Object>();
						energyData = new EnergyData();
						energyData.setEnergyName(rsEnergy.getString("type"));
						ResultSetMetaData rsmd = rsEnergy.getMetaData(); // 获取结果集的元数据
						int columns = rsmd.getColumnCount();// 获取结果集的列数
						for (int i = 4; i <= columns; i++) {
							if (rsEnergy.getString(i) == null) {
								dataValue.add(null);
							}else {
								dataValue.add(rsEnergy.getBigDecimal(i));
							}
							
							
						}
						energyData.setEnergyValue(dataValue);
						switch (rsEnergy.getString("type")) {
						case "产量":
							//lineData.getProduct().add(energyData);
							lineData.setProduct(energyData);
							break;
							
						case "耗电量":
							//lineData.getPower().add(energyData);
							lineData.setPower(energyData);
							break;	
							
						case "用气量":
							//lineData.getGas().add(energyData);
							lineData.setGas(energyData);
							break;	
							
						case "单件耗电量":
							//lineData.getSinglePower().add(energyData);
							lineData.setSinglePower(energyData);
							break;		
							
						case "单件用气量":
							//lineData.getSingleGas().add(energyData);
							lineData.setSingleGas(energyData);
							break;	

						default:
							break;
						}
					}
					data.getLine().add(lineData);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "["+data.getDeptJson(data).toString()+"]";
	}

}







