package com.taskui.models.ui;

public final class Textview implements UiElement {

	private String text;

	public Textview() {
	}

	public Textview(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getListener() {
		return null;
	}
}