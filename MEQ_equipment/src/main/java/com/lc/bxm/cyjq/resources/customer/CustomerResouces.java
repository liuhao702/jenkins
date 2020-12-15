package com.lc.bxm.cyjq.resources.customer;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.common.util.TreeDateUtil;
import com.lc.bxm.dbconnection.PostgreSQLConn;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;

@Api(value="客户接口类",tags= {"客户接口类"})
@RestController
@RequestMapping("/customer")
public class CustomerResouces {
	
	@Autowired
    PostgreSQLConn dbConn;
    @Autowired
    TreeDateUtil tree;
    
    @ApiOperation(value="客户类别",notes="客户类别")
    @GetMapping("customerMenuJson")
    public String equipmentMenuJson() {
    	ResultSet rs = dbConn.query("select id,customer_category_name,customer_category_id from cyj_customer_category");
    	List<Map<Object,Object>> deptJson = tree.getResultSet(rs);
		JSONObject json = new JSONObject();
		json.put("id", "0");
		json.put("name", "全部");
		json.put("children", deptJson);
		return "["+json+"]";
    }
    
}

