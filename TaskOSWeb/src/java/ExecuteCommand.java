
import com.taskos.TaskOS;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Chuong Vo
 */
public final class ExecuteCommand extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<response>");

        String command = request.getParameter("command");
        String args = request.getParameter("args");
        String taskId = request.getParameter("taskid");
        String userId = request.getParameter("userid");


        System.out.println("ExecuteCommand-userId: " + userId + " taskId: " + taskId + " args: " + args);

        //check if this task has been loaded (and is not finished yet) for this user?
        if (!TaskOS.isLoadedTaskInstace(taskId, userId)) {// this task has not been loaded yet
            TaskOS.executeTask(taskId, userId, out);
        } else {
            String UIToken = UUID.randomUUID().toString();
            TaskOS.getUser(userId).setUItoken(UIToken);
            out.println("<UItoken>" + UIToken + "</UItoken>");
            if (command.equalsIgnoreCase("next")) {
                TaskOS.executeNext(taskId, userId, args, out);
            } else if (command.equalsIgnoreCase("back")) {
                TaskOS.executeBack(taskId, userId, out);
            } else if (command.equalsIgnoreCase("skip")) {
                TaskOS.executeSkip(taskId, userId, out);
            }
        }
        out.println("</response>");
        out.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
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