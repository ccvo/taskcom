package com.taskui.models.ui;

import java.util.ArrayList;
import java.util.List;


/** @author Chuong Vo */
public final class Listener implements UiElement {

	private String id;
	private String url;
	private List<Argument> args = new ArrayList<Argument>();

	public Listener() {
	}

	public String getId() {
		return this.id;
	}

	public String getUrl() {
		return this.url;
	}

	public List<Argument> getArgs() {
		return this.args;
	}

	@Override
	public String getListener() {
		return null;
	}
}
