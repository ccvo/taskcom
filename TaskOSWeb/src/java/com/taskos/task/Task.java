package com.taskos.task;

import com.taskos.TaskOS;
import com.taskos.task.UiElement.Textview;
import com.taskos.task.ui.Mapview;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chuong Vo
 * This class represents corresponding XML task description
 */
public final class Task implements AbstractTask {

    private String xmlns;
    private String id;
    private boolean optional = false;
    private boolean shareable = false;
    private String title;
    private List<UiElement> ui = new ArrayList<UiElement>();
    private List<AbstractTask> subtasks = new ArrayList<AbstractTask>();
    private AbstractTask parent = null;
    private boolean isDone = false;

    public Task(String xmlns, String id, String title, List<UiElement> ui, List<AbstractTask> subtasks) {
        this.xmlns = xmlns;
        this.id = id;
        this.title = title;
        this.ui = ui;
        this.subtasks = subtasks;
    }

    @Override
    public void unDone() {
        this.setIsDone(false);
        if (subtasks != null && subtasks.size() > 0) {
            for (int i = 0; i < subtasks.size(); i++) {
                subtasks.get(i).unDone();
            }
        }
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    @Override
    public boolean isShareable() {
        return shareable;
    }

    public void setShareable(boolean shareable) {
        this.shareable = shareable;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public List<AbstractTask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<AbstractTask> subtasks) {
        this.subtasks = subtasks;
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

    public String getXmlns() {
        return xmlns;
    }

    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    @Override
    public AbstractTask getParent() {
        return parent;
    }

    @Override
    public void setParent(AbstractTask parentTask) {
        this.parent = parentTask;
    }

    @Override
    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }

    @Override
    public boolean getIsDone() {
        return isDone;
    }

    /**
     * execute the current sub task which can be the root task
     * @param out
     */
    @Override
    public void execute(PrintWriter out, String user_id) {
        out.println("<title>" + this.getTitle() + "</title>");
        if (this.ui != null) {
            List<UiElement> uitemp = (List<UiElement>) TaskOS.xstream.fromXML(TaskOS.xstream.toXML(this.ui));
            for (int i = 0; i < uitemp.size(); i++) {
                if (uitemp.get(i) instanceof Textview) {
                    Textview tv = (Textview) uitemp.get(i);
                    if (tv.getText().startsWith("$")) {
                        String name = tv.getText().substring(1);
                        Object value = TaskOS.getUser(user_id).variables.get(name);
                        tv.setText(value != null ? value.toString() : tv.getText());
                    }
                } else if (uitemp.get(i) instanceof Mapview) {
                    Mapview mv = (Mapview) uitemp.get(i);
                    if (mv.getAddress().startsWith("$")) {
                        String name = mv.getAddress().substring(1);
                        Object value = TaskOS.getUser(user_id).variables.get(name);
                        mv.setAddress(value != null ? value.toString() : mv.getAddress());
                    }
                } else if (uitemp.get(i) instanceof UiElement.Input) {
                    UiElement.Input input = (UiElement.Input) uitemp.get(i);
                    if(input.getType().equals("string")){
                        String name = input.getName();
                        Object value = TaskOS.getUser(user_id).variables.get(name);
                        if(value != null){
                            input.setValue(value.toString());
                        }
                    }
                }
            }
            out.println(TaskOS.xstream.toXML(uitemp));
        }
    }
}