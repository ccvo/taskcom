
import com.taskos.Constants;
import com.taskos.task.TaskDescription;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Chuong Vo
 */
public final class ManageTasks extends HttpServlet implements Constants {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String sqlQuery = null;
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Task Management</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("The schema for task descriptions: <a href=\"http://homepage.cs.latrobe.edu.au/ccvo/task/descriptions/task.rnc\">task.rnc</a><br>");
            out.println("Samples of task descriptions: <a href=\"http://homepage.cs.latrobe.edu.au/ccvo/task/descriptions/set_tv_channel.xml\">set_tv_channel.xml</a>, ");
            out.println("<a href=\"http://homepage.cs.latrobe.edu.au/ccvo/task/descriptions/set_tv_volume.xml\">set_tv_volume.xml</a>, ");
            out.println("<a href=\"http://homepage.cs.latrobe.edu.au/ccvo/task/descriptions/switch_light_off.xml\">switch_light_off.xml</a>, ");
            out.println("<a href=\"http://homepage.cs.latrobe.edu.au/ccvo/task/descriptions/switch_light_on.xml\">switch_light_on.xml</a><br>");
            out.println("<a href=\"http://homepage.cs.latrobe.edu.au/ccvo/task/descriptions/\">View all other XML descriptions</a><br>");
            out.println("The client app for Android (called TaskUI): <a href=\"http://homepage.cs.latrobe.edu.au/ccvo/taskui.apk\">taskui.apk</a><br>");
            // connecting to database
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(MYSQL_CONNECTION_STRING, MYSQL_CONNECTION_USERNAME, MYSQL_CONNECTION_PASSWORD);
            stmt = con.createStatement();
            sqlQuery = "SELECT * FROM " + MYSQL_TABLE_TASKS + " t, " + MYSQL_TABLE_SPACES + " s WHERE t." + MYSQL_COLUMN_TABLE_TASK_SPACE_ID + "="
                    + "s." + MYSQL_COLUMN_TABLE_SPACE_ID + " ORDER BY s." + MYSQL_COLUMN_TABLE_SPACE_ID;
            rs = stmt.executeQuery(sqlQuery);
            ArrayList<TaskDescription> tasks = new ArrayList<TaskDescription>();
            while (rs.next()) {
                TaskDescription task = new TaskDescription(rs.getString("t." + MYSQL_COLUMN_TABLE_TASK_ID),
                        rs.getString("t." + MYSQL_COLUMN_TABLE_TASK_SPACE_ID),
                        rs.getString("s." + MYSQL_COLUMN_TABLE_SPACE_NAME),
                        rs.getString("t." + MYSQL_COLUMN_TABLE_TASK_TITLE),
                        rs.getString("t." + MYSQL_COLUMN_TABLE_TASK_DESCRIPTION));
                tasks.add(task);
            }

            out.println("<h2>Number of tasks: " + tasks.size() + " - - - ");
            out.println("<a href=addTask.jsp>Add task</a></h2><br>");

            out.println("<table border=1>");
            out.println("<tr>");
            out.println("<td><b>ID</b></td><td><b>Title</b></td><td><b>Description</b></td><td><b>Space</b></td><td><b>Actions</b></td>");
            out.println("</tr>");

            for (int i = 0; i < tasks.size(); i++) {
                TaskDescription t = tasks.get(i);
                out.println("<tr>");
                out.println("<td>" + t.getId() + "</td><td>" + t.getTitle() + "</td><td>" + t.getDescription() + "</td><td>" + t.getSpace().getName() + "</td>");
                out.println("<td><a href=deleteTask?taskid=" + t.getId() + ">Delete</a> | <a href=editTask.jsp?taskid=" + t.getId() + ">Edit</a></td>");
                out.println("</tr>");
            }

            out.println("</table>");

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
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
