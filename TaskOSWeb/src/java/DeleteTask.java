
import com.taskos.Constants;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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
public final class DeleteTask extends HttpServlet implements Constants {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        Connection con = null;
        Statement stmt = null;
        String sqlQuery = null;
        String taskId = request.getParameter("taskid");
        try {
            // connecting to database
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(MYSQL_CONNECTION_STRING, MYSQL_CONNECTION_USERNAME, MYSQL_CONNECTION_PASSWORD);
            stmt = con.createStatement();
            sqlQuery = "DELETE FROM " + MYSQL_TABLE_TASKS + " WHERE " + MYSQL_COLUMN_TABLE_TASK_ID + "='" + taskId + "'";
            stmt.executeUpdate(sqlQuery);
            File f = new File("E:\\upload\\" + taskId + ".xml");
            if(f.exists()){
                f.delete();
            }
        } catch (ClassNotFoundException e1) {
            System.out.println("My SQL driver is not found. " + e1.getMessage());
        } catch (SQLException e2) {
            System.out.println("sqlQuery: " + sqlQuery + ". " + e2.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (con != null) {
                    con.close();
                    con = null;
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        response.sendRedirect("manageTasks");
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
