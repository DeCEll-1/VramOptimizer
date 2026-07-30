package DeCell.VFXCommons;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Priority;

public class VFXCommons {
    public static void Log(String s) {
        Global.getLogger(VFXCommons.class).log(Priority.INFO, s);
    }

    public static void LogWarn(String s) {
        Global.getLogger(VFXCommons.class).log(Priority.WARN, s);
    }

    public static void LogErr(String s) {
        Global.getLogger(VFXCommons.class).log(Priority.ERROR, s);
    }
}
