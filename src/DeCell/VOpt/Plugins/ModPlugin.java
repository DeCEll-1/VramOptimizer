package DeCell.VOpt.Plugins;

import DeCell.VOpt.VOpt;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.lwjgl.opengl.*;

import static DeCell.VOpt.DDSOverriding.HandleDDS;

public class ModPlugin extends BaseModPlugin {

    @Override
    public void onApplicationLoad() throws Exception {
        VOpt.Log("VOpt Loaded");
        VOpt.isDebug = Global.getSettings().getBoolean("VOpt_debug");
        VOpt.isVerbose = Global.getSettings().getBoolean("VOpt_verbose");

        if (VOpt.frEnabled) {
            String frVersion = "0.7.0";
            try {
                frVersion = com.genir.renderer.Version.getVersion();
            } catch (Throwable ignore) {}
            VOpt.Log("FR Version: " + frVersion);
            if (!isVersionStrictlyAbove(frVersion, 0, 8, 1)) {
                VOpt.Log("FR Version 0.8.1 or below, handing DDS files manually (be sure to update to the latest version!)");
                HandleDDS();
            } else {
                VOpt.Log("Suitable FR Version found, skipping DDS file handling");
            }
        } else {
            VOpt.Log("FR not enabled, handling DDS files manually");
            HandleDDS();
        }
    }

    public static boolean isVersionStrictlyAbove(String versionStr, int targetMajor, int targetMinor, int targetPatch) {
        if (versionStr == null || versionStr.isEmpty()) {
            return false;
        }

        String cleanVersion = versionStr.trim();
        if (cleanVersion.startsWith("v") || cleanVersion.startsWith("V")) {
            cleanVersion = cleanVersion.substring(1);
        }

        int dashIndex = cleanVersion.indexOf('-');
        if (dashIndex != -1) {
            cleanVersion = cleanVersion.substring(0, dashIndex);
        }
        int plusIndex = cleanVersion.indexOf('+');
        if (plusIndex != -1) {
            cleanVersion = cleanVersion.substring(0, plusIndex);
        }

        String[] parts = cleanVersion.split("\\.");
        int major = parts.length > 0 && !parts[0].isEmpty() ? Integer.parseInt(parts[0].replaceAll("\\D+", "")) : 0;
        int minor = parts.length > 1 && !parts[1].isEmpty() ? Integer.parseInt(parts[1].replaceAll("\\D+", "")) : 0;
        int patch = parts.length > 2 && !parts[2].isEmpty() ? Integer.parseInt(parts[2].replaceAll("\\D+", "")) : 0;

        if (major != targetMajor) {
            return major > targetMajor;
        }
        if (minor != targetMinor) {
            return minor > targetMinor;
        }
        return patch > targetPatch;
    }
}
