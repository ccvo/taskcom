package com.taskos.task;

import java.io.PrintWriter;
import java.util.List;

/**
 *
 * @author Chuong VO
 * 
 * This abstract task is used for Task, Service, and If element, see Task Schema
 */
public interface AbstractTask {

    public void setIsDone(boolean isDone);

    public boolean getIsDone();

    public void setParent(AbstractTask t);

    public AbstractTask getParent();

    public void execute(PrintWriter out, String user_id);

    public List<AbstractTask> getSubtasks();

    public String getId();

    public boolean isShareable();

    public boolean isOptional();

    public void unDone();
}