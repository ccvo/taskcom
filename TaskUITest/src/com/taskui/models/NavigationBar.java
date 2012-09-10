package com.taskui.models;

public final class NavigationBar {
	private boolean back;
	private boolean share;
	private boolean next;
	private boolean skip;
	
	public NavigationBar(){
	}

	public boolean isBack() {
		return back;
	}

	public void setBack(boolean back) {
		this.back = back;
	}

	public boolean isShare() {
		return share;
	}

	public void setShare(boolean share) {
		this.share = share;
	}

	public boolean isNext() {
		return next;
	}

	public void setNext(boolean next) {
		this.next = next;
	}

	public boolean isSkip() {
		return skip;
	}
}
