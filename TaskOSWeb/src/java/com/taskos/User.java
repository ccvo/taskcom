package com.taskos;

import com.taskos.task.SharedTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chuong VO
 */
public final class User {

    private String id; // userid or username
    private String realname;
    /**
     * current (last) ui elements sent to the user
     */
    private String lastUI;
    /**
     * to check if the user has the latest ui
     */
    private String UItoken;
    /**
     * the tasks in progress
     */
    private Map<String, TaskInstance> taskInstances = new HashMap<String, TaskInstance>();
    public Map<String, Object> variables = new HashMap<String, Object>();
    private List<SharedTask> sharedTasks = new ArrayList<SharedTask>();

    public User(String id, String _realname) {
        this.id = id;
        this.realname = _realname;
    }

    public TaskInstance getTaskInstance(String taskId) {
        return this.taskInstances.get(taskId);
    }

    public Map<String, TaskInstance> getLiveTasks() {
        return taskInstances;
    }

    public void setCurrentUI(String ui) {
        this.lastUI = ui;
    }

    public String getCurrentUI() {
        return this.lastUI;
    }

    public String getUItoken() {
        return this.UItoken;
    }

    public void setUItoken(String token) {
        this.UItoken = token;
    }

    public String getId() {
        return this.id;
    }

    void removeTaskInstance(String taskId) {
        this.taskInstances.remove(taskId);
    }

    void addTaskInstance(TaskInstance taskInstance) {
        this.taskInstances.put(taskInstance.getTaskId(), taskInstance);
    }

    public void addSharedTask(String taskId, String sharer) {
        this.sharedTasks.add(new SharedTask(taskId, sharer));
    }

    public  List<SharedTask> getSharedTasks() {
        return this.sharedTasks;
    }
}
