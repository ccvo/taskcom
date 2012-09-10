
import com.taskos.TaskOS;
import com.taskos.Constants;
import com.taskos.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Chuong Vo
 */
public final class GetRecommendedTasks extends HttpServlet implements Constants {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String serverId = request.getParameter("serverid");
        String userId = request.getParameter("userid");
        String realname = request.getParameter("realname");
        System.out.println("GetRecommendedTasks: userid=" + userId);
        if (userId != null && userId.length() > 0 && realname != null && realname.length() > 0) {
            if (!TaskOS.hasUser(userId)) {
                TaskOS.addUser(new User(userId, realname));
            }
        }
        out.println("<recommendations>");

        try {
            Connection con = null;
            Statement stmt = null;
            ResultSet rs = null;
            String sqlQuery = null;
            try {
                // connecting to database
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection(MYSQL_CONNECTION_STRING, MYSQL_CONNECTION_USERNAME, MYSQL_CONNECTION_PASSWORD);
                stmt = con.createStatement();
                if (serverId.equalsIgnoreCase("all")) {
                    sqlQuery = "SELECT * FROM " + MYSQL_TABLE_TASKS;
                } else {
                    sqlQuery = "SELECT * FROM " + MYSQL_TABLE_TASKS + " WHERE " + MYSQL_COLUMN_TABLE_TASK_SPACE_ID + "='" + serverId + "'";
                }
                rs = stmt.executeQuery(sqlQuery);
                while (rs.next()) {
                    out.println("  <recommendation>"
                            + "    <title>" + rs.getString(MYSQL_COLUMN_TABLE_TASK_TITLE) + "</title>"
                            + "    <description>" + rs.getString(MYSQL_COLUMN_TABLE_TASK_DESCRIPTION) + "</description>"
                            + "    <id>" + rs.getString(MYSQL_COLUMN_TABLE_TASK_ID) + "</id>"
                            + "  </recommendation>");
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
        out.println("</recommendations>");

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
