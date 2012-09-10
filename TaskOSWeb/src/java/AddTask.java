
import com.taskos.TaskOS;
import com.taskos.Utils;
import com.taskos.Constants;

import com.taskos.task.AbstractTask;
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
public final class AddTask extends HttpServlet {

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
            String title = map.getParameter("title");
            String description = map.getParameter("description");
            String spaceId = map.getParameter("spaceid");
            File xmlfile = map.getFile("xmlfile");

            if (title != null && title.length() > 0 && description != null && description.length() > 0 && xmlfile != null) {

                boolean checkOK = Utils.validate(xmlfile.getAbsolutePath(), Constants.TASK_DESCRIPTION_SCHEMA_PATH);

                if (checkOK) {
                    try {
                        AbstractTask t = (AbstractTask) TaskOS.xstream.fromXML(xmlfile);

                        Connection con = null;
                        Statement stmt = null;
                        ResultSet rs = null;
                        String sqlQuery = null;
                        try {
                            // connecting to database
                            Class.forName("com.mysql.jdbc.Driver");
                            con = DriverManager.getConnection(Constants.MYSQL_CONNECTION_STRING, Constants.MYSQL_CONNECTION_USERNAME, Constants.MYSQL_CONNECTION_PASSWORD);
                            stmt = con.createStatement();
                            sqlQuery = "SELECT * FROM " + Constants.MYSQL_TABLE_TASKS + " WHERE id='" + t.getId() + "'";
                            rs = stmt.executeQuery(sqlQuery);
                            File f = new File(xmlfile.getParent() + "\\" + t.getId() + ".xml");
                            if (rs.next() || f.exists()) {
                                out.println("The task '" + t.getId() + "' already exists in the database!");
                                xmlfile.delete();
                            } else {
                                if (f.exists()) {
                                    f.delete();
                                }
                                xmlfile.renameTo(f);

                                sqlQuery = "INSERT INTO " + Constants.MYSQL_TABLE_TASKS + " (" + Constants.MYSQL_COLUMN_TABLE_TASK_ID + ", " + Constants.MYSQL_COLUMN_TABLE_TASK_TITLE
                                        + ", " + Constants.MYSQL_COLUMN_TABLE_TASK_DESCRIPTION + ", " + Constants.MYSQL_COLUMN_TABLE_TASK_SPACE_ID + ")"
                                        + " VALUES('" + t.getId() + "', '" + title.replace("'", "\\" + "\'").replace("\"", "\\" + "\"")
                                        + "', '" + description.replace("'", "\\" + "\'").replace("\"", "\\" + "\"") + "', '" + spaceId + "')";

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