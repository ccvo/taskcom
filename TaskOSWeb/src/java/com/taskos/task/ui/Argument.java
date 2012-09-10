package com.taskos.task.ui;

/**
 *
 * @author Chuong Vo
 */
public final class Argument {

    private String name;
    private String value;

    public Argument(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
