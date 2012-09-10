<%-- 
    Document   : addtask.jsp
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
        <h1>Add a new task description</h1>
        <form action="addTask" ENCTYPE="multipart/form-data" METHOD=POST>
            <table border="1" width="600">
                <tr>
                    <td>Task title (which will be displayed to users): </td>
                    <td><input type="text" name="title" style="width:300px"></td>
                </tr>
                <tr>
                    <td>Description (which allows users to recognize your task from the tasks which have similar titles):</td>
                    <td><textarea name="description" style="width:300px" rows="4"></textarea></td>
                </tr>
                <tr>
                    <td>Space:</td>
                    <td>
                        <select name="spaceid">
                            <%
                                Connection con = null;
                                Statement stmt = null;
                                ResultSet rs = null;
                                String sqlQuery = null;
                                try {
                                    // connecting to database
                                    Class.forName("com.mysql.jdbc.Driver");//A call to forName("X") causes the class named X to be initialized.
                                    con = DriverManager.getConnection(Constants.MYSQL_CONNECTION_STRING, Constants.MYSQL_CONNECTION_USERNAME, Constants.MYSQL_CONNECTION_PASSWORD);
                                    stmt = con.createStatement();

                                    sqlQuery = "SELECT * FROM " + Constants.MYSQL_TABLE_SPACES;
                                    rs = stmt.executeQuery(sqlQuery);
                                    while (rs.next()) {
                                        Space space = new Space(rs.getString(Constants.MYSQL_COLUMN_TABLE_SPACE_ID), rs.getString(Constants.MYSQL_COLUMN_TABLE_SPACE_NAME));
                                        out.println("<option value='" + space.getId() + "'>" + space.getName() + "</option>");
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
    </body>
</html>
