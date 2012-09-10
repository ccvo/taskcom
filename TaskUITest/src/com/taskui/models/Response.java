package com.taskui.models;

import java.util.ArrayList;
import java.util.List;

import com.taskui.models.ui.UiElement;


/** @author Chuong Vo
 *         This class represents an XML response from the task server */
public final class Response {

	private String title;
	private List<UiElement> ui = new ArrayList<UiElement>();
	private NavigationBar navigationbar;
	private String UItoken;

	public Response() {
	}

	public String getUItoken(){
		return this.UItoken;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<UiElement> getUi() {
		return ui;
	}

	public void setUi(List<UiElement> ui) {
		this.ui = ui;
	}

	public NavigationBar getNavBar() {
		return navigationbar;
	}

	public void setNavBar(NavigationBar navBar) {
		this.navigationbar = navBar;
	}
}