package com.taskui.models.ui;

import java.util.ArrayList;
import java.util.List;

/** @author Chuong Vo */
public final class Listview implements UiElement {
	private String name;
	private int value;
	private String listener;
	private ArrayList<Option> options = new ArrayList<Option>();

	public Listview() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getListener() {
		return listener;
	}

	public void setListener(String listener) {
		this.listener = listener;
	}

	public List<Option> getOptions() {
		return options;
	}
}
