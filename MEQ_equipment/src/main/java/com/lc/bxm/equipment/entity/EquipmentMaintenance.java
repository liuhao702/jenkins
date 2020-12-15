package com.lc.bxm.equipment.entity;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.Data;

/**
 * @serial 设备养护
 * @author liuhao
 *
 */
@Data
@SuppressWarnings("unused")
public class EquipmentMaintenance {
	
	private Long mId; 				//自增主键
	private String mCode;			//保养编号
	private String mName; 			//保养名称
	private String mPeriod; 		//保养周期
	private Timestamp mListTime;    //最近保养时间
	private Timestamp mNextTime; 	//下次保养时间
	private String mResult;			//保养结果
	private String mRemarks; 		//保养说明
//	private Timestamp mCreateTime;  //创建时间
	private UUID mUserid;           //创建人
	private Integer mEquipmentid;   //设备id
	private Integer compId;   		//所属公司

}
