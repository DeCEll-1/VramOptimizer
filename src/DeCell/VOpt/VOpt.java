package DeCell.VOpt;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Priority;

public class VOpt {
    public static void Log(String s) {
        Global.getLogger(VOpt.class).log(Priority.INFO, s);
    }

    public static void LogWarn(String s) {
        Global.getLogger(VOpt.class).log(Priority.WARN, s);
    }

    public static void LogErr(String s) {
        Global.getLogger(VOpt.class).log(Priority.ERROR, s);
    }
}
