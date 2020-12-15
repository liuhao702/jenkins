package com.lc.bxm.entity;


import java.util.ArrayList;

import java.util.List;


public class EnergyData {

	// 名称
	private String energyName;
	
	//数值
	private List<Object> energyValue = new ArrayList<Object>();
	
	//平均值
//	private double energyAverage;

	public String getEnergyName() {
		return energyName;
	}

	public void setEnergyName(String energyName) {
		this.energyName = energyName;
	}

	public List<Object> getEnergyValue() {
		return energyValue;
	}

	public void setEnergyValue(List<Object> energyValue) {
		this.energyValue = energyValue;
	}

//	public double getEnergyAverage() {
//		return energyAverage;
//	}
//
//	public void setEnergyAverage(double energyAverage) {
//		this.energyAverage = energyAverage;
//	}
}
