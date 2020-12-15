package com.lc.bxm.equipment.entity;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.Data;

/**
 * @serial 设备巡检
 * @author liuhao
 *
 */
@Data
@SuppressWarnings("unused")
public class EquipmentInspection {
	
	private Long iId; 						//自增主键
	private String iOddnumber;				//巡检单号
	private String iPosition; 				//巡检部位
	private String iThereareproblems; 		//存在问题
	private String iHandle;   			 	//解决处理
	private String iResult; 				//巡检结果
	private String iRemarks;				//巡检说明
	private Timestamp iLastinspectionTime; 	//本次巡检时间
	private Timestamp iNextinspectionTime;  //下次巡检时间
//	private Timestamp iCreateTime; 			//创建时间
	private UUID iUserid;           		//创建人
	private Integer iEquipmentid;   		//设备id
	private Integer compId;   				//所属公司

}
