package com.lc.bxm.equipment.entity;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.Data;

/**
 * @serial 设备维修
 * @author liuhao
 *
 */
@Data
@SuppressWarnings("unused")
public class EquipmentRepair {
	
	private Long rId; 						//自增主键
	private String rOddnumber;				//维修单号
	private String rResult; 				//维修结果
	private String rRemarks; 				//维修说明
	private String rCauseanalysis;   		//原因分析
	private String rRecord; 				//维修记录
	private Timestamp rMaintenanceTime;		//维修时间
	private UUID rMaintenanceUserid; 		//维修人
//	private Timestamp rCreateTime; 			//创建时间
	private UUID rUserid;  					//创建人
	private Long fId;  						//故障id
//	private UUID iUserid;           		//操作人
//	private Integer iEquipmentid;   		//设备id
//	private Integer compId;   				//所属公司

}
