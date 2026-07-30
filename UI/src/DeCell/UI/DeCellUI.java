package DeCell.UI;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Priority;

import java.util.regex.Pattern;

public class DeCellUI {

    public static boolean isDebugIUCharlie = false;
    public static boolean isDebugIU = false;

    public static void Init() {

        isDebugIU = Global.getSettings().getBoolean("DeCellUI_debug");
        isDebugIUCharlie = Global.getSettings().getBoolean("DeCellUI_debug_charlie");
    }


    public static void Log(String s) {
        Global.getLogger(DeCellUI.class).log(Priority.INFO, s);
    }

    public static void LogWarn(String s) {
        Global.getLogger(DeCellUI.class).log(Priority.WARN, s);
    }

    public static void LogErr(String s) {
        Global.getLogger(DeCellUI.class).log(Priority.ERROR, s);
    }


    public static class Patterns {
        public final static Pattern NUMBER_ONLY = Pattern.compile("-?[0-9]*");
        public final static Pattern DECIMAL_ONLY = Pattern.compile("-?[0-9]*\\.?[0-9]*");

        public static Pattern decimalWithMaxDecimalPlaces(int decimalPlaces) {
            if (decimalPlaces == 0)
                return NUMBER_ONLY;
            return Pattern.compile("-?[0-9]*\\.?[0-9]{0," + decimalPlaces + "}");
        }
    }
}
