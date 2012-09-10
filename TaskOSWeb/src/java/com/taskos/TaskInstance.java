package com.taskos;

import com.taskos.task.AbstractTask;
import com.taskos.task.If;
import com.taskos.task.IncludedTask;
import com.taskos.task.Service;
import com.taskos.task.Task;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author Chuong Vo
 */
public final class TaskInstance implements Constants {

    private AbstractTask currentTask;
    /**
     * taskStack is used to store the already executed [visible] subtasks of a task instance
     */
    private Stack<AbstractTask> taskStack = new Stack<AbstractTask>();
    private AbstractTask root;
    private String task_id;
    private String user_id;

    /**
     * @param taskId is a string number which is generated and stored in the database, not a string like in the XML file
     */
    public TaskInstance(String taskId, String _user_id) {
        this.task_id = taskId;
        this.user_id = _user_id;
        this.root = load(Constants.TASK_DESCRIPTION_FOLDER + taskId + ".xml");
        this.currentTask = this.root;
    }

    /**
     * Load, parse and return root description object from specified XML document.
     *
     * @param xmlFilePath string identifying the file containing XML document
     */
    private AbstractTask load(String xmlFilePath) {
        return (AbstractTask) TaskOS.xstream.fromXML(new File(xmlFilePath));
    }

    public String getTaskId() {
        return task_id;
    }

    /**
     * execute the current task which can also be the root task or a sub task of the root task
     * @param out
     */
    public void execute(PrintWriter out) {
        if (this.currentTask != null) {
            /**
             * Task is the only type of abstract tasks which is user interactive,
             * so it only makes sense for the user to go for 'Next' or 'Back' with Tasks, but not for Service...
             */
            if (this.currentTask instanceof Task) {
                if (!this.currentTask.getIsDone()) {
                    // push this task into the stack for navigation Next and Back
                    if (!this.taskStack.contains(this.currentTask)) {
                        this.taskStack.push(this.currentTask);
                    }
                    this.currentTask.execute(out, this.user_id);
                    out.println("<navigationbar>"
                            + "  <next>" + this.canGoNext() + "</next>" //canGoNext
                            + "  <skip>" + this.canSkip() + "</skip>"
                            + "  <share>" + this.canShare() + "</share>" //canShare
                            + "  <back>" + this.canGoBack() + "</back>"
                            + "</navigationbar>");
                } else {
                    //check if there are undone subtasks of the current task, do it
                    // otherwise jum to its parent and execute the parent
                    List<AbstractTask> subtasks = this.currentTask.getSubtasks();
                    if (subtasks != null && subtasks.size() > 0) {
                        for (int i = 0; i < subtasks.size(); i++) {
                            if (!subtasks.get(i).getIsDone()) {
                                subtasks.get(i).setParent(this.currentTask);
                                this.currentTask = subtasks.get(i);
                                execute(out);
                                //return here to send the out to the user
                                return;
                            }
                        }
                    }
                    //otherwire, this current task and its subtasks are done, back to its parent
                    this.currentTask.setIsDone(true);
                    this.currentTask = this.currentTask.getParent();
                    execute(out);
                }
            } else if (this.currentTask instanceof Service) {
                if (!this.currentTask.getIsDone()) {
                    this.currentTask.execute(out, this.user_id);
                    this.currentTask.setIsDone(true);
                    List<AbstractTask> subtasks = this.currentTask.getSubtasks();
                    if (subtasks != null && subtasks.size() > 0) {
                        for (int i = 0; i < subtasks.size(); i++) {
                            if (!subtasks.get(i).getIsDone()) {
                                subtasks.get(i).setParent(this.currentTask.getParent());
                                this.currentTask = subtasks.get(i);
                                execute(out);
                                //return here to send the out to the user
                                return;
                            }
                        }
                    }
                    //otherwire, this current task and its subtasks are done, back to its parent
                    this.currentTask.setIsDone(true);
                    this.currentTask = this.currentTask.getParent();
                    execute(out);
                }
            } else if (this.currentTask instanceof If) {
                //check if there are undone subtasks of the current task, do it
                // otherwise jum to its parent and execute the parent
                ((If) this.currentTask).setUserId(this.user_id);
                List<AbstractTask> subtasks = this.currentTask.getSubtasks();
                if (subtasks != null && subtasks.size() > 0) {
                    for (int i = 0; i < subtasks.size(); i++) {
                        if (!subtasks.get(i).getIsDone()) {
                            subtasks.get(i).setParent(this.currentTask.getParent());
                            this.currentTask = subtasks.get(i);
                            execute(out);
                            //return here to send the out to the user
                            return;
                        }
                    }
                }
                //otherwire, this current task and its subtasks are done, back to its parent
                this.currentTask.setIsDone(true);
                this.currentTask = this.currentTask.getParent();
                execute(out);
            } else if (this.currentTask instanceof IncludedTask) {
                //check if there are undone subtasks of the current task, do it
                // otherwise jum to its parent and execute the parent
                List<AbstractTask> subtasks = this.currentTask.getSubtasks();
                if (subtasks != null && subtasks.size() > 0) {
                    for (int i = 0; i < subtasks.size(); i++) {
                        if (!subtasks.get(i).getIsDone()) {
                            subtasks.get(i).setParent(this.currentTask.getParent());
                            this.currentTask = subtasks.get(i);
                            execute(out);
                            //return here to send the out to the user
                            return;
                        }
                    }
                }
                //otherwire, this current task and its subtasks are done, back to its parent
                this.currentTask.setIsDone(true);
                this.currentTask = this.currentTask.getParent();
                execute(out);
            }
        } else if (this.root != null) {//this.currentTask == null)
            //we have done the task, let's remove it from the alive task list for this user
            TaskOS.removeTask(this.task_id, this.user_id);
        } else {
            TaskOS.removeTask(this.task_id, this.user_id);
            out.println("<ui><textview text=\"This '" + this.task_id + "' task does not have its task description!\"/></ui>");
        }
    }

    public boolean done(String args, PrintWriter out) {
        if (this.currentTask == null) {
            out.println("<ui><textview text=\"I don't know what you are doing!!\"/></ui>");
            return false;
        } else {
            if (args != null && args.length() >= 3) {
                String[] nameValuePairs = args.split(NAME_VALUE_PAIR_SEPERATOR);
                for (int i = 0; i < nameValuePairs.length; i++) {
                    String[] nameValuePair = nameValuePairs[i].split(NAME_VALUE_SEPERATOR);
                    if (nameValuePair.length == 2) {
                        TaskOS.getUser(this.user_id).variables.put(nameValuePair[0], nameValuePair[1]);
                    }
                }
            }
            this.currentTask.setIsDone(true);
            return true;
        }
    }

    public void executeNext(PrintWriter out) {
        execute(out);
    }

    public void executeSkip(PrintWriter out) {
        // set the current task and its subtasks are done
        this.currentTask.setIsDone(true);

        List<AbstractTask> subtasks = this.currentTask.getSubtasks();
        if (subtasks != null && subtasks.size() > 0) {
            for (int i = 0; i < subtasks.size(); i++) {
                subtasks.get(i).setIsDone(true);
            }
        }
        // now jump to the parent task
        this.currentTask = this.currentTask.getParent();
        execute(out);
    }

    public void executeBack(PrintWriter out) {
        if (this.taskStack.empty() || this.taskStack.size() == 1) { // size == 1 means that there is only the current subtask in the stack
            this.taskStack.clear();
            //you've asked to go back from the root task, let's remove it from the alive task list for this user
            TaskOS.removeTask(this.task_id, this.user_id);
        } else {
            // remove the current subtask from the stack
            this.taskStack.pop();
            // get out the previous subtask
            this.currentTask = this.taskStack.pop();
            //unDone(this.currentTask);
            this.currentTask.unDone();
            execute(out);
        }
    }
    /*
    private void unDone(AbstractTask task) {
    task.setIsDone(false);
    
    List<AbstractTask> subtasks = task.getSubtasks();
    if (subtasks != null && subtasks.size() > 0) {
    for (int i = 0; i < subtasks.size(); i++) {
    unDone(subtasks.get(i));
    }
    }
    }
     */

    public AbstractTask getRoot() {
        return this.root;
    }

    private boolean canGoBack() {
        //return this.taskStack.size() > 1;
        return true;
    }

    private boolean canSkip() {
        return this.currentTask.isOptional();
    }

    private boolean canShare() {
        return this.currentTask.isShareable();
    }

    private boolean canGoNext() {
        return (this.getNextTask(this.root) != null);
    }

    private AbstractTask getNextTask(AbstractTask rootTask) {
        if (!rootTask.getIsDone() && rootTask != this.currentTask) {
            return rootTask;
        } else {
            List<AbstractTask> subtasks = rootTask.getSubtasks();
            if (subtasks != null && subtasks.size() > 0) {
                for (int i = 0; i < subtasks.size(); i++) {
                    AbstractTask subtask = getNextTask(subtasks.get(i));
                    if (subtask != null) {
                        return subtask;
                    }
                }
            }
        }
        return null;
    }
}
