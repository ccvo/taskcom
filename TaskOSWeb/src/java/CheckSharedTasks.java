
import com.taskos.TaskOS;
import com.taskos.task.SharedTask;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Chuong Vo
 */
public final class CheckSharedTasks extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String userId = request.getParameter("userid");
        System.out.println("CheckSharedTasks: userid=" + userId);
        try {
            out.println("<sharedtasks>");
            if (TaskOS.getUser(userId) != null) {
                List<SharedTask> tasks = TaskOS.getUser(userId).getSharedTasks();
                for (int i = 0; i < tasks.size(); i++) {
                    SharedTask rs = tasks.get(i);
                    out.println("  <sharedtask>"
                            + "    <id>" + rs.getTaskid() + "</id>"
                            + "    <sharer>" + rs.getSharer() + "</sharer>"
                            + "  </sharedtask>");
                }
                tasks.clear();
            }
            out.println("</sharedtasks>");
        } finally {
            out.close();
        }
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
