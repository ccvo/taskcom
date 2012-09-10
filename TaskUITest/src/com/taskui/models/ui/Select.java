package com.taskui.models.ui;

import java.util.ArrayList;
import java.util.List;

public final class Select implements UiElement {

	private String name;
	private List<Option> options = new ArrayList<Option>();

	public Select() {
	}

	public Select(String name, List<Option> option) {
		this.name = name;
		this.options = option;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	@Override
	public String getListener() {
		return null;
	}
}