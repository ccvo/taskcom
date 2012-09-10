package com.taskos;

/**
 *
 * @author Chuong Vo
 */
public interface Constants {
    public static final String MYSQL_CONNECTION_STRING = "jdbc:mysql://localhost:3306/task";
    public static final String MYSQL_CONNECTION_USERNAME = "root";
    public static final String MYSQL_CONNECTION_PASSWORD = "admin";
    
    public static final String MYSQL_TABLE_SPACES = "spaces";
    public static final String MYSQL_COLUMN_TABLE_SPACE_ID = "id";
    public static final String MYSQL_COLUMN_TABLE_SPACE_NAME = "name";
    
    public static final String MYSQL_TABLE_TASKS = "tasks";
    public static final String MYSQL_COLUMN_TABLE_TASK_ID = "id";
    public static final String MYSQL_COLUMN_TABLE_TASK_SPACE_ID = "spaceid";
    public static final String MYSQL_COLUMN_TABLE_TASK_TITLE = "title";
    public static final String MYSQL_COLUMN_TABLE_TASK_DESCRIPTION = "description";
    
    public static final String TASK_DESCRIPTION_FOLDER = "E:/upload/";//"E:/Dropbox/JavaProjects/TaskOSWeb/models/";
    public static final String TASK_DESCRIPTION_SCHEMA_PATH = "E:/Dropbox/JavaProjects/TaskOSWeb/models/task.rnc";
    
    public static final String NAME_VALUE_SEPERATOR = "::";
    public static final String NAME_VALUE_PAIR_SEPERATOR = "~";
}
