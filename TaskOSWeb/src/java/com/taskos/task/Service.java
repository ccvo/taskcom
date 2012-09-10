package com.taskos.task;

import com.taskos.TaskOS;
import com.taskos.task.ui.Argument;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Chuong Vo
 */
public final class Service implements AbstractTask {

    private String id;
    private String url;
    private String title;
    private List<UiElement> ui;
    private List<Argument> args;
    private AbstractTask parent = null;
    private boolean isDone;
    private List<AbstractTask> subtasks;

    public Service(String id, List<UiElement> ui, List<Argument> args) {
        this.id = id;
        this.ui = ui;
        this.args = args;
    }

    public List<Argument> getArgs() {
        return args;
    }

    public void setArgs(List<Argument> args) {
        this.args = args;
    }

    @Override
    public void unDone() {
        this.setIsDone(false);
        if(this.subtasks != null){
            this.subtasks.clear();
        }
    }

    @Override
    public boolean isOptional() {
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

    public List<UiElement> getUi() {
        return ui;
    }

    public void setUi(List<UiElement> ui) {
        this.ui = ui;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
        return this.isDone;
    }

    @Override
    public void execute(PrintWriter out, String user_id) {
        if (this.url != null) {
            String urlStr = this.url;
            if (this.args != null && this.args.size() > 0) {
                urlStr += "?a=.";
                for (int i = 0; i < this.args.size(); i++) {
                    if (this.args.get(i).getValue().startsWith("$")) {
                        String name = this.args.get(i).getValue().substring(1);
                        Object value = TaskOS.getUser(user_id).variables.get(name);
                        urlStr += "&" + this.args.get(i).getName() + "=" + value;
                    } else {
                        urlStr += "&" + this.args.get(i).getName() + "=" + this.args.get(i).getValue();
                    }
                }
            }
            System.out.println("Service execute(): " + urlStr);
            try {
                URL myURL = new URL(urlStr);
                URLConnection myURLConnection = myURL.openConnection();
                //myURLConnection.connect();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        myURLConnection.getInputStream()));
                String inputLine;
                String result = "";
                while ((inputLine = in.readLine()) != null) {
                    result += inputLine;
                }
                if (result != null && result.length() > 0) {
                    if ((result.equals("true") || result.equals("false")) && this.getId() != null && this.getId().length() > 0) {
                        TaskOS.getUser(user_id).variables.put(this.getId(), Boolean.valueOf(result));
                    } else if (result.trim().startsWith("<task") || result.trim().startsWith("<service") || result.trim().startsWith("<include") || result.trim().startsWith("<if")) {
                        this.subtasks = new ArrayList<AbstractTask>();
                        this.subtasks.add((AbstractTask) TaskOS.xstream.fromXML(result));
                    } else if(this.getId() != null && this.getId().length() > 0){
                        TaskOS.getUser(user_id).variables.put(this.getId(), result);
                    }
                }
            } catch (MalformedURLException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public List<AbstractTask> getSubtasks() {
        return this.subtasks;
    }
}
