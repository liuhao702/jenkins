package com.lc.bxm.equipment.entity;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.Data;

/**
 * @serial 设备故障警报
 * @author liuhao
 *
 */
@Data
@SuppressWarnings("unused")
public class EquipmentFaultalarm {
	
	private Long fId; 						//自增主键
	private Timestamp fAccidentTime;		//故障发生日期
	private String fRemarks; 				//故障描述
	private UUID fFindUserid; 				//故障发现人
	private Boolean fIsitimproved;   		//是否改善
	private UUID fChargeUserid; 			//故障负责人
	private Timestamp fCode;				//故障代码
//	private Timestamp fCreateTime; 			//创建时间
	private UUID fUserid; 					//创建人
	private Integer fEquipmentid;  			//设备id
	private Integer compId;  				//所属公司

}
