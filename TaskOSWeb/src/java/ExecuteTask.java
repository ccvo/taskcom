
import com.taskos.TaskOS;
import com.taskos.User;
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
public final class ExecuteTask extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<response>");

        String taskId = request.getParameter("taskid");
        String userId = request.getParameter("userid");
        System.out.println("ExecuteTask: userid=" + userId);
        String UIToken = TaskOS.getUser(userId).getUItoken();
        if (null == TaskOS.getUser(userId).getUItoken()) {
            UIToken = UUID.randomUUID().toString();
            TaskOS.getUser(userId).setUItoken(UIToken);
        }

        out.println("<UItoken>" + UIToken + "</UItoken>");

        if (!TaskOS.hasUser(userId)) {
            //this is only used during development of the system. Real system will not go to this.
            TaskOS.addUser(new User(userId, "any name"));
        }

        //check if this task has been loaded (and is not finished yet) for this user?
        if (!TaskOS.isLoadedTaskInstace(taskId, userId)) {// this task has not been loaded yet
            TaskOS.executeTask(taskId, userId, out);
        } else {
            TaskOS.continueTask(taskId, userId, out);
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
        return "This is to execute a task";
    }
}