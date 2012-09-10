package com.taskui.models.ui;

public final class Img implements UiElement {

	private String url;

	public Img() {
	}

	public Img(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String getListener() {
		return null;
	}
}