<%-- 
    Document   : validateModel
    Created on : Apr 5, 2011, 3:11:10 PM
    Author     : Chuong Vo
--%>

<%@page import="java.sql.SQLException"%>
<%@page import="com.taskos.task.Space"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.taskos.Constants"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="java.sql.Connection"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Add a new task description</title>
    </head>
    <body>
        <%
            String taskId = request.getParameter("taskid");
            Connection con = null;
            Statement stmt = null;
            ResultSet rs = null;
            String sqlQuery = null;
            try {
                // connecting to database
                Class.forName("com.mysql.jdbc.Driver");//A call to forName("X") causes the class named X to be initialized.
                con = DriverManager.getConnection(Constants.MYSQL_CONNECTION_STRING, Constants.MYSQL_CONNECTION_USERNAME, Constants.MYSQL_CONNECTION_PASSWORD);
                stmt = con.createStatement();
                sqlQuery = "SELECT * FROM " + Constants.MYSQL_TABLE_TASKS + " WHERE " + Constants.MYSQL_COLUMN_TABLE_TASK_ID + "='" + taskId + "'";
                rs = stmt.executeQuery(sqlQuery);
                if (rs.next()) {%>
        <h1>Edit a task description</h1>
        <form action="editTask" ENCTYPE="multipart/form-data" METHOD=POST>
            <input type="hidden" name="taskid" value="<%= taskId%>">
            <table border="1" width="600">
                <tr>
                    <td>Task title (which will be displayed to users): </td>
                    <td><input type="text" name="title" style="width:300px" value="<%= rs.getString(Constants.MYSQL_COLUMN_TABLE_TASK_TITLE)%>"></td>
                </tr>
                <tr>
                    <td>Description (which allows users to recognize your task from the tasks which have similar titles):</td>
                    <td><textarea name="description" style="width:300px" rows="4"><%= rs.getString(Constants.MYSQL_COLUMN_TABLE_TASK_DESCRIPTION)%></textarea></td>
                </tr>
                <tr>
                    <td>Space:</td>
                    <td>
                        <select name="spaceid">
                            <%
                                ResultSet rs2 = null;
                                Statement stmt2 = null;
                                try {
                                    stmt2 = con.createStatement();
                                    sqlQuery = "SELECT * FROM " + Constants.MYSQL_TABLE_SPACES;
                                    rs2 = stmt2.executeQuery(sqlQuery);
                                    while (rs2.next()) {
                                        Space space = new Space(rs2.getString(Constants.MYSQL_COLUMN_TABLE_SPACE_ID), rs2.getString(Constants.MYSQL_COLUMN_TABLE_SPACE_NAME));
                                        out.println("<option " + (space.getId().equals(rs.getString(Constants.MYSQL_COLUMN_TABLE_TASK_SPACE_ID)) ? "selected" : "") + " value='" + space.getId() + "'>" + space.getName() + "</option>");
                                    }
                                } catch (SQLException e2) {
                                    out.println("sqlQuery: " + sqlQuery + ". " + e2.getMessage());
                                } finally {
                                    if (rs2 != null) {
                                        rs2.close();
                                        rs2 = null;
                                    }
                                    if (stmt2 != null) {
                                        stmt2.close();
                                        stmt2 = null;
                                    }
                                }
                            %>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td>XML description file:</td>
                    <td><INPUT NAME="xmlfile" TYPE="file"></td>
                </tr>
                <tr>
                    <td></td>
                    <td><INPUT TYPE="submit" VALUE="Submit"></td>
                </tr>
            </table>
        </form>
        <%} else {
                    out.println("Not found the task '" + taskId + "'");
                }
            } catch (ClassNotFoundException e1) {
                out.println("My SQL driver is not found. " + e1.getMessage());
            } catch (SQLException e2) {
                out.println("sqlQuery: " + sqlQuery + ". " + e2.getMessage());
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                        rs = null;
                    }
                    if (stmt != null) {
                        stmt.close();
                        stmt = null;
                    }
                    if (con != null) {
                        con.close();
                        con = null;
                    }
                } catch (SQLException e) {
                    out.println(e.getMessage());
                }
            }
        %>

    </body>
</html>
