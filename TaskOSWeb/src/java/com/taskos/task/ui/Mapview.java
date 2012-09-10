package com.taskos.task.ui;

import com.taskos.task.UiElement;

/**
 * @author Chuong Vo
 */
public final class Mapview implements UiElement {

    private String address;

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String _address) {
        this.address = _address;
    }
}
