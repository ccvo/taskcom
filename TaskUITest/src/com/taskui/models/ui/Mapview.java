package com.taskui.models.ui;

public final class Mapview implements UiElement {

	private String address;
	
	public Mapview(){
	}
	
	@Override
	public String getListener() {
		return null;
	}

	public String getAddress(){
		return this.address;
	}
}
