package com.lc.bxm.equipment.entity;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.amqp.core.AnonymousQueue.UUIDNamingStrategy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @serial 设备状态
 * @author liuhao
 *
 */
@Data
//@TableName(value = "cyj_instrument_equipment_state")
@NoArgsConstructor
@SuppressWarnings("unused")
public class EquipmentState {
	
	
//	@TableId(value = "s_id", type = IdType.INPUT)
	private Long sID; 				//自增主键
	private String sCode;			//状态编号
	private String sName; 			//状态名称
	private String sRemarks; 		//状态说明
	private Timestamp sCreateTime;  //创建时间
	private Object  sUserid;           //创建人
	private Integer sEquipmentid;   //设备id
	private Integer compId;   		//所属公司
	
	public EquipmentState( String sCode, String sName, String sRemarks, Object sUserid,
			Integer sEquipmentid, Integer compId) {
		super();
		this.sCode = sCode;
		this.sName = sName;
		this.sRemarks = sRemarks;
//		this.sCreateTime = sCreateTime;
		this.sUserid = sUserid;
		this.sEquipmentid = sEquipmentid;
		this.compId = compId;
	}
	
}
