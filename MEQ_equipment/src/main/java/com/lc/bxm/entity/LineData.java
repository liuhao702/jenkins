package com.lc.bxm.entity;

public class LineData {
	
	//产线名称
	private String lineName;
	//产量
	private EnergyData product ;
	
	//耗电量
	private EnergyData power;
	
	//用气量
	private EnergyData gas ;
	
	//单件耗电量
	private EnergyData singlePower;
	
	//单件用气量
	private EnergyData singleGas;

	public String getLineName() {
		return lineName;
	}

	public void setLineName(String lineName) {
		this.lineName = lineName;
	}

	public EnergyData getProduct() {
		return product;
	}

	public void setProduct(EnergyData product) {
		this.product = product;
	}

	public EnergyData getPower() {
		return power;
	}

	public void setPower(EnergyData power) {
		this.power = power;
	}

	public EnergyData getGas() {
		return gas;
	}

	public void setGas(EnergyData gas) {
		this.gas = gas;
	}

	public EnergyData getSinglePower() {
		return singlePower;
	}

	public void setSinglePower(EnergyData singlePower) {
		this.singlePower = singlePower;
	}

	public EnergyData getSingleGas() {
		return singleGas;
	}

	public void setSingleGas(EnergyData singleGas) {
		this.singleGas = singleGas;
	}

	
//	//产量
//	private List<EnergyData> product = new ArrayList<EnergyData>();
//	
//	//耗电量
//	private List<EnergyData> power = new ArrayList<EnergyData>();
//	
//	//用气量
//	private List<EnergyData> gas = new ArrayList<EnergyData>();
//	
//	//单件耗电量
//	private List<EnergyData> singlePower = new ArrayList<EnergyData>();
//	
//	//单件用气量
//	private List<EnergyData> singleGas = new ArrayList<EnergyData>();
//
//	public String getLineName() {
//		return lineName;
//	}
//
//	public void setLineName(String lineName) {
//		this.lineName = lineName;
//	}
//
//	public List<EnergyData> getProduct() {
//		return product;
//	}
//
//	public void setProduct(List<EnergyData> product) {
//		this.product = product;
//	}
//
//	public List<EnergyData> getPower() {
//		return power;
//	}
//
//	public void setPower(List<EnergyData> power) {
//		this.power = power;
//	}
//
//	public List<EnergyData> getGas() {
//		return gas;
//	}
//
//	public void setGas(List<EnergyData> gas) {
//		this.gas = gas;
//	}
//
//	public List<EnergyData> getSinglePower() {
//		return singlePower;
//	}
//
//	public void setSinglePower(List<EnergyData> singlePower) {
//		this.singlePower = singlePower;
//	}
//
//	public List<EnergyData> getSingleGas() {
//		return singleGas;
//	}
//
//	public void setSingleGas(List<EnergyData> singleGas) {
//		this.singleGas = singleGas;
//	}
}
