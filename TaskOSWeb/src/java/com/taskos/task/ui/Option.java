package com.taskos.task.ui;

/**
 *
 * @author Chuong Vo
 */
public final class Option {

    private String value;
    private String text;

    public Option(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
