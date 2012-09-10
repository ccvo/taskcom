package com.taskos;

import com.taskos.task.AbstractTask;
import com.taskos.task.UiElement;
import com.taskos.task.If;
import com.taskos.task.If.Otherwise;
import com.taskos.task.If.Then;
import com.taskos.task.IncludedTask;
import com.taskos.task.Service;
import com.taskos.task.Task;
import com.taskos.task.ui.Argument;
import com.taskos.task.ui.Listener;
import com.taskos.task.ui.Listview;
import com.taskos.task.ui.Mapview;
import com.taskos.task.ui.Option;
import com.taskos.task.ui.Slider;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * This is the task engine that loads XML file 
 * @author Chuong VO
 */
public final class TaskOS {

    private static Map<String, User> users = new HashMap<String, User>();
    public static final XStream xstream;
    public static final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("ECMAScript");
    public static final boolean COMPILABLE = scriptEngine instanceof Compilable;

    static {
        xstream = new XStream(new DomDriver());
        xstream.alias("task", Task.class);
        xstream.alias("service", Service.class);
        xstream.alias("if", If.class);
        xstream.alias("then", Then.class);
        xstream.alias("otherwise", Otherwise.class);
        xstream.alias("include", IncludedTask.class);
        xstream.alias("arg", Argument.class);

        xstream.alias("textview", UiElement.Textview.class);
        xstream.alias("img", UiElement.Img.class);
        xstream.alias("select", UiElement.Select.class);
        xstream.alias("option", Option.class);
        xstream.alias("input", UiElement.Input.class);
        xstream.alias("slider", Slider.class);
        xstream.alias("listview", Listview.class);
        xstream.alias("listener", Listener.class);
        xstream.alias("ui", List.class);
        xstream.alias("mapview", Mapview.class);

        /*
         * xstream.addImplicitCollection(Task.class, "subtasks", AbstractTask.class);
         * this tells that the member 'subtasks' in the class 'Task' is a collection of objects of the 'AbstractTask'
         */
        xstream.addImplicitCollection(Task.class, "subtasks", AbstractTask.class);
        xstream.addImplicitCollection(UiElement.Select.class, "options", Option.class);
        xstream.addImplicitCollection(Then.class, "tasks", AbstractTask.class);
        xstream.addImplicitCollection(Otherwise.class, "tasks", AbstractTask.class);
        xstream.addImplicitCollection(Listview.class, "options", Option.class);

        xstream.useAttributeFor(Task.class, "xmlns");
        xstream.useAttributeFor(Task.class, "id");
        xstream.useAttributeFor(Task.class, "optional");
        xstream.useAttributeFor(Task.class, "shareable");

        xstream.useAttributeFor(Service.class, "id");
        xstream.useAttributeFor(Service.class, "url");

        xstream.useAttributeFor(Argument.class, "name");
        xstream.useAttributeFor(Argument.class, "value");

        xstream.useAttributeFor(IncludedTask.class, "taskid");
        xstream.useAttributeFor(If.class, "condition");

        xstream.useAttributeFor(UiElement.Textview.class, "text");
        xstream.useAttributeFor(UiElement.Img.class, "url");
        xstream.useAttributeFor(UiElement.Select.class, "name");
        xstream.useAttributeFor(Option.class, "value");
        xstream.useAttributeFor(Option.class, "text");
        xstream.useAttributeFor(UiElement.Input.class, "type");
        xstream.useAttributeFor(UiElement.Input.class, "name");
        xstream.useAttributeFor(UiElement.Input.class, "value");

        xstream.useAttributeFor(Slider.class, "name");
        xstream.useAttributeFor(Slider.class, "min");
        xstream.useAttributeFor(Slider.class, "max");
        xstream.useAttributeFor(Slider.class, "value");
        xstream.useAttributeFor(Slider.class, "step");
        xstream.useAttributeFor(Slider.class, "listener");

        xstream.useAttributeFor(Mapview.class, "address");
        
        xstream.useAttributeFor(Listview.class, "name");
        xstream.useAttributeFor(Listview.class, "value");
        xstream.useAttributeFor(Listview.class, "listener");

        xstream.useAttributeFor(Listener.class, "id");
        xstream.useAttributeFor(Listener.class, "url");

    }

    public static User getUser(String userId) {
        return TaskOS.users.get(userId);
    }

    public static void addUser(User user) {
        TaskOS.users.put(user.getId(), user);
    }

    private static TaskInstance getTaskInstance(String taskId, String userId) {
        return TaskOS.getUser(userId).getTaskInstance(taskId);
    }

    public static void removeTask(String taskId, String userId) {
        TaskOS.getUser(userId).removeTaskInstance(taskId);
    }

    public static void executeTask(String taskId, String userId, PrintWriter out) {
        TaskInstance taskInstance = new TaskInstance(taskId, userId);
        TaskOS.getUser(userId).addTaskInstance(taskInstance);
        taskInstance.execute(out);
    }

    public static void continueTask(String taskId, String userId, PrintWriter out) {
        TaskInstance taskInstance = getTaskInstance(taskId, userId);
        taskInstance.execute(out);
    }

    public static void executeNext(String taskId, String userId, String args, PrintWriter out) {
        TaskInstance taskInstance = getTaskInstance(taskId, userId);
        if (taskInstance.done(args, out)) {
            taskInstance.executeNext(out);
        }
    }

    public static void executeSkip(String taskId, String userId, PrintWriter out) {
        TaskInstance taskInstance = getTaskInstance(taskId, userId);
        taskInstance.executeSkip(out);
    }

    public static void executeBack(String taskId, String userId, PrintWriter out) {
        TaskInstance taskInstance = getTaskInstance(taskId, userId);
        taskInstance.executeBack(out);
    }

    public static boolean isLoadedTaskInstace(String taskId, String userId) {
        return TaskOS.getUser(userId).getLiveTasks().containsKey(taskId);
    }

    public static boolean hasUser(String userId) {
        return TaskOS.users.containsKey(userId);
    }

    public static Boolean evalCondition(String expression, String where) {
        Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put(ScriptEngine.FILENAME, where);
        try {
            if (TaskOS.COMPILABLE) {
                CompiledScript compiledScript = TaskOS.compileScript(expression, where);
                if (compiledScript == null) {
                    return null;
                }
                return (Boolean) compiledScript.eval(bindings);
            } else {
                return (Boolean) scriptEngine.eval(expression, bindings);
            }
        } catch (ScriptException e) {
            throw new RuntimeException("Evaluating " + expression + "\n:" + e.getMessage());
        } finally {
            bindings.remove(ScriptEngine.FILENAME);
        }
    }

    private static CompiledScript compileScript(String script, String where) {
        if (!COMPILABLE || script.length() == 0) {
            return null;
        }
        Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
        try {
            bindings.put(ScriptEngine.FILENAME, where);
            return ((Compilable) scriptEngine).compile(script);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        } finally {
            bindings.remove(ScriptEngine.FILENAME);
        }
    }
}
