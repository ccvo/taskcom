package com.taskos.task;

import com.taskos.TaskOS;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Chuong Vo
 */
public final class If implements AbstractTask {

    private String id;
    private Then then;
    private Otherwise otherwise;
    private String condition;
    private AbstractTask parent = null;
    private boolean isDone = false;
    private String user_id;

    public void setUserId(String userId) {
        this.user_id = userId;
    }

    public If(Then then, Otherwise otherwise, String condition) {
        this.then = then;
        this.otherwise = otherwise;
        this.condition = condition;
    }

    public If(Then _then, Otherwise _else) {
        this.then = _then;
        this.otherwise = _else;
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Otherwise getOtherwise() {
        return otherwise;
    }

    public void setOtherwise(Otherwise _else) {
        this.otherwise = _else;
    }

    public Then getThen() {
        return then;
    }

    public void setThen(Then _then) {
        this.then = _then;
    }

    @Override
    public List<AbstractTask> getSubtasks() {
        String expression = replaceVariablesInString(this.condition, this.user_id);
        boolean isTrue = isTrue(TaskOS.evalCondition(expression, this.id + " condition"));
        System.out.println("expression: (" + expression + ")===" + isTrue);
        if (isTrue) {
            return then.getSubtasks();
        } else if (otherwise != null) {
            return otherwise.getSubtasks();
        } else {
            return null;
        }
    }

    @Override
    public void unDone() {
        this.setIsDone(false);
        List<AbstractTask> subtasks = this.getSubtasks();
        if (subtasks != null && subtasks.size() > 0) {
            for (int i = 0; i < subtasks.size(); i++) {
                subtasks.get(i).unDone();
            }
        }
    }

    @Override
    public boolean isShareable() {
        return false;
    }

    private static String replaceVariablesInString(String text, String userId) {
        int start, end;
        //System.out.println("replaceVariablesInString--text before:" + text);
        while (text.indexOf("$") >= 0) {
            start = text.indexOf("$");
            end = text.indexOf("$", start + 1);
            if (end > start) {
                String varName = text.substring(start + 1, end);
                //System.out.println("replaceVariablesInString--varName:" + varName);
                Object value = TaskOS.getUser(userId).variables.get(varName);
                //System.out.println("replaceVariablesInString--value:" + value);
                text = text.substring(0, start) + value + text.substring(end + 1);
                //System.out.println("replaceVariablesInString--text after:" + text);
            }
        }
        return text;
    }

    // three-valued logic (null represents unknown)
    private static boolean isTrue(Boolean b) {
        return b != null && b;
    }

    private static boolean isFalse(Boolean b) {
        return b != null && !b;
    }

    public class Otherwise {

        private List<AbstractTask> tasks = new ArrayList<AbstractTask>();

        public Otherwise(List<AbstractTask> tasks) {
            this.tasks = tasks;
        }

        public List<AbstractTask> getSubtasks() {
            return tasks;
        }

        public void setTasks(List<AbstractTask> tasks) {
            this.tasks = tasks;
        }
    }

    public final class Then extends Otherwise {

        public Then(List<AbstractTask> tasks) {
            super(tasks);
        }
    }
}
