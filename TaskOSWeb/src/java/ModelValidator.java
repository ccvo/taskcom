
import com.taskos.TaskInstance;
import com.taskos.TaskOS;
import com.taskos.Utils;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import javax.servlet.ServletOutputStream;

/**
 *
 * @author Chuong Vo
 */
public final class ModelValidator extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        ServletOutputStream out = response.getOutputStream();

        // Backup the streams
        PrintStream oStdOutBackup = System.out;
        PrintStream oStdErrBackup = System.err;
        String newline = System.getProperty("line.separator");

        // Redired STDOUT and STDERR to the ServletOuputStream
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(out));
        System.setProperty("line.separator", "<br>");

        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Task Description Validation Result</title>");
            out.println("</head>");
            out.println("<body>");
            String filename;
            String taskid = request.getParameter("taskid");

            if (taskid == null || taskid.length() == 0) {
                filename = "E:/Dropbox/JavaProjects/TaskOSWeb/models/swicth_light_on.xml";
            } else {
                filename = "E:/Dropbox/JavaProjects/TaskOSWeb/models/" + taskid + ".xml";
            }
            if (Utils.validate(filename, "E:/Dropbox/JavaProjects/TaskOSWeb/models/task.rnc")) {
                out.println("Description is OK!");
                TaskInstance taskInstance = new TaskInstance(taskid, "ccvo");
                System.out.println(TaskOS.xstream.toXML(taskInstance.getRoot()));
            } else {
                out.println("ERROR!");
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
