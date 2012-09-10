package com.taskos.task.ui;

import com.taskos.task.UiElement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Chuong Vo
 */
public final class Listview implements UiElement{
    private String name;
    private int value;
    private String listener;
    private List<Option> options = new ArrayList<Option>();
}
