package com.taskos.task;

/**
 *
 * @author Chuong Vo
 */
public final class TaskDescription {

    private String id;
    private Space space;
    private String title;
    private String description;

    public TaskDescription(String id, String space_id, String space_name, String title, String description) {
        this.id = id;
        this.space = new Space(space_id, space_name);
        this.title = title;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
