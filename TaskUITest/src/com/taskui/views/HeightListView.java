package com.taskui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class HeightListView extends ListView {

	/** This height come with normal layout, don't know where take this constant */
	private static final int WIDE_HEIGHT = -2147483218;

	public HeightListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public HeightListView(Context context) {
		super(context);
	}

	public HeightListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, WIDE_HEIGHT);
	}
}
