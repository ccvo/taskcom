package com.taskos.task;

/**
 *
 * @author Chuong Vo
 */
public final class SharedTask {

    private String taskid;
    private String sharer;

    public SharedTask(String taskid, String sharer) {
        this.sharer = sharer;
        this.taskid = taskid;
    }

    public String getSharer() {
        return sharer;
    }

    public String getTaskid() {
        return taskid;
    }
}
