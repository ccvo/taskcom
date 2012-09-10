
import com.taskos.TaskInstance;
import com.taskos.TaskOS;
import com.taskos.task.Service;
import com.taskos.task.ui.Argument;

/**
 *
 * @author Chuong Vo
 */
public final class TestXML2Object {

    public static void main(String[] args) {
        String taskid;
        if (args != null && args.length > 0) {
            taskid = args[0];
        } else {
            taskid = "book_taxi";
        }
        TaskInstance taskInstance = new TaskInstance(taskid, "ccvo");
        System.out.println(TaskOS.xstream.toXML(taskInstance.getRoot()));
        /*
        Service s = (Service) taskInstance.getRoot();
        System.out.println(s.getArgs().size());
        for(int i = 0; i< s.getArgs().size(); i++){
        Argument a = s.getArgs().get(i);
        System.out.println(a.getName() + "=" + a.getValue());
        }
         */
    }
}
