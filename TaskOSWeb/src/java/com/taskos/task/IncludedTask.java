package com.taskos.task;

import com.taskos.TaskOS;
import com.taskos.Constants;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Chuong Vo
 */
public final class IncludedTask implements AbstractTask {
    private String id;
    private String taskid;
    private AbstractTask parent = null;
    private boolean isDone = false;
    private Task task;

    public IncludedTask(String _taskid) {
        this.taskid = _taskid;
        task = (Task) TaskOS.xstream.fromXML(new File(Constants.TASK_DESCRIPTION_FOLDER + _taskid + ".xml"));
    }

    @Override
    public boolean isOptional(){
        return false;
    }
    
    @Override
    public boolean isShareable() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskid;
    }

    public void setTaskId(String _taskid) {
        this.taskid = _taskid;
        task = (Task) TaskOS.xstream.fromXML(new File(Constants.TASK_DESCRIPTION_FOLDER + taskid + ".xml"));
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

    @Override
    public void execute(PrintWriter out, String user_id) {
        //do nothing, no one will call this method
    }

    @Override
    public void unDone(){
        this.setIsDone(false);
        this.task = null; // set to be unloaded
    }
    
    @Override
    public List<AbstractTask> getSubtasks() {
        if (this.task == null) {
            task = (Task) TaskOS.xstream.fromXML(new File(Constants.TASK_DESCRIPTION_FOLDER + taskid + ".xml"));
        }
        List<AbstractTask> subtasks = new ArrayList<AbstractTask>();
        subtasks.add(task);
        return subtasks;
    }
}
