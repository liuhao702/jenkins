package com.lc.bxm.equipment.controller;




import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lc.bxm.equipment.dao.EquipmentStateMapper;
import com.lc.bxm.equipment.entity.EquipmentState;
import com.lc.bxm.equipment.service.EquipmentStateService;

@RestController
@RequestMapping("/state")
public class EquipmentStateController {
	
	@Autowired
	EquipmentStateMapper stateMapper;
	
    @GetMapping(value = "statedataadd")
    @ResponseBody
    public String add() {
    	//UUID.fromString("f8e0638d-492e-4cf4-8b26-52fb539a74c1")
//    	String sUuid = UUID.fromString("f8e0638d-492e-4cf4-8b26-52fb539a74c1");
    	 UUID tUuid = UUID.randomUUID();
    	EquipmentState equipmentState = new EquipmentState("a", "b", "aaaa",UUID.fromString("f8e0638d-492e-4cf4-8b26-52fb539a74c1"), 1, 1);
    	int num = stateMapper.insert(equipmentState);
    	if (num>0) {
			return "成功";
		}
	 return "失败";
    }

}
