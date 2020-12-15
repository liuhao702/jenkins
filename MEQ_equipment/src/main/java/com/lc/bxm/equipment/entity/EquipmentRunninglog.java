package com.lc.bxm.equipment.entity;

import lombok.Data;

/**
 * @serial 设备运行日志
 * @author liuhao
 *
 */
@Data
@SuppressWarnings("unused")
public class EquipmentRunninglog {
	
	private Long lId; 					//自增主键
	private Integer lEquipmentid;		//设备id
	private Integer compId; 				//所属公司
	
}
