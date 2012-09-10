
import com.taskos.Utils;
import com.taskos.Constants;

import com.taskos.TaskOS;
import com.taskos.task.AbstractTask;
import com.taskos.task.Task;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import net.balusc.http.multipart.MultipartMap;

/**
 *
 * @author Chuong Vo
 */
@MultipartConfig(location = "E:/upload", maxFileSize = 10485760L) // 10MB.
public final class EditTask extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        ServletOutputStream out = response.getOutputStream();

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Task Description Submission Result</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Result:</h2>");
        
        // Backup the streams
        PrintStream oStdOutBackup = System.out;
        PrintStream oStdErrBackup = System.err;
        String newline = System.getProperty("line.separator");

        // Redired STDOUT and STDERR to the ServletOuputStream
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(out));
        System.setProperty("line.separator", "<br>");

        try {
            MultipartMap map = new MultipartMap(request, this);
            String taskId = map.getParameter("taskid");
            String title = map.getParameter("title");
            String description = map.getParameter("description");
            String spaceId = map.getParameter("spaceid");
            File xmlfile = map.getFile("xmlfile");

            if (title != null && title.length() > 0 && description != null && description.length() > 0) {

                boolean checkOK = xmlfile == null || Utils.validate(xmlfile.getAbsolutePath(), Constants.TASK_DESCRIPTION_SCHEMA_PATH);

                if (checkOK) {
                    try {
                        Connection con = null;
                        Statement stmt = null;
                        ResultSet rs = null;
                        String sqlQuery = null;
                        try {
                            // connecting to database
                            Class.forName("com.mysql.jdbc.Driver");
                            con = DriverManager.getConnection(Constants.MYSQL_CONNECTION_STRING, Constants.MYSQL_CONNECTION_USERNAME, Constants.MYSQL_CONNECTION_PASSWORD);
                            stmt = con.createStatement();
                            sqlQuery = "SELECT * FROM " + Constants.MYSQL_TABLE_TASKS + " WHERE id='" + taskId + "'";
                            rs = stmt.executeQuery(sqlQuery);
                            if (!rs.next()) {
                                out.println("The task '" + taskId + "' does not exist in the database!");
                            } else {
                                String check = null;
                                if (xmlfile != null) {
                                    AbstractTask t = (AbstractTask) TaskOS.xstream.fromXML(xmlfile);
                                    if (t instanceof Task && !((Task) t).getId().equals(taskId)) { // this is an error
                                        xmlfile.delete();
                                        check = ((Task) t).getId();
                                    } else {// all other cases shoulbe be fine
                                        File f = new File(xmlfile.getParent() + "\\" + taskId + ".xml");
                                        if (f.exists()) {
                                            f.delete();
                                        }
                                        xmlfile.renameTo(f);
                                    }
                                }
                                if (check == null) {
                                    sqlQuery = "UPDATE " + Constants.MYSQL_TABLE_TASKS + " SET " + Constants.MYSQL_COLUMN_TABLE_TASK_TITLE + "='" + title.replace("'", "\\" + "\'").replace("\"", "\\" + "\"") + "', "
                                            + Constants.MYSQL_COLUMN_TABLE_TASK_DESCRIPTION + "='" + description.replace("'", "\\" + "\'").replace("\"", "\\" + "\"") + "', "
                                            + Constants.MYSQL_COLUMN_TABLE_TASK_SPACE_ID + "='" + spaceId + "' WHERE id='" + taskId + "'";

                                    if (stmt.executeUpdate(sqlQuery) > 0) {
                                        out.println("Successfully!<br>");
                                        out.println("<table border='1'>");
                                        out.println("<tr>");
                                        out.println("<td>Title:</td><td>" + title + "</td>");
                                        out.println("</tr>");
                                        out.println("<tr>");
                                        out.println("<td>Description:</td><td>" + description + "</td>");
                                        out.println("</tr>");
                                        out.println("<tr>");
                                        out.println("<td>Space ID:</td><td>" + spaceId + "</td>");
                                        out.println("</tr>");
                                        out.println("</table>");
                                        out.println("<a href=manageTasks>View all tasks</a><br>");
                                    } else {
                                        out.println("Could not add the task to the database!<br>");
                                    }
                                } else {
                                    out.println("ERROR: The root task id in the XML file is '" + check + "'. It is required to be the current task id '" + taskId + "'<br>");
                                }
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

                    } catch (Exception e) {
                        out.println(e.getMessage() + "<br>");
                    }
                } else {
                    out.println("<br>---Please check the validation errors above.");
                }
            } else {
                out.println("The fields should not empty!");
            }
            out.println("</body>");
            out.println("</html>");
        } finally {
            // Restore original STDOUT and STDERR
            System.setOut(oStdOutBackup);
            System.setErr(oStdErrBackup);
            System.setProperty("line.separator", newline);
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //request.getRequestDispatcher("addTask.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}