package com.taskui.models;

import java.io.Serializable;

/**
 *
 * @author Chuong Vo
 */
public final class SharedTask implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -28121314594861094L;
	private String id;
    private String sharer;

    public SharedTask(){
    }

    public String getSharer() {
        return sharer;
    }

    public String getTaskid() {
        return id;
    }
}
