package com.taskui.models.ui;

public final class Input implements UiElement {

	private String type;
	private String name;
	private String value;

	public Input() {
	}

	public Input(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public String getValue(){
		return this.value;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getListener() {
		return null;
	}
}