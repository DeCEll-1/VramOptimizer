package DeCell.VOpt.Plugins;

import DeCell.UI.DeCellUI;
import DeCell.VOpt.VOpt;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.genir.renderer.Version;

import static DeCell.VOpt.DDSOverriding.HandleDDS;
import static DeCell.VOpt.VramCalculator.getTotalTextureVRAM;

public class ModPlugin extends BaseModPlugin {

    @Override
    public void onApplicationLoad() throws Exception {
        VOpt.Log("VOpt Loaded");
        VOpt.isDebug = Global.getSettings().getBoolean("VOpt_debug");
        VOpt.isVerbose = Global.getSettings().getBoolean("VOpt_verbose");

        DeCellUI.Init();
        if (VOpt.frEnabled) {
            String frVersion = "0.7.0";
            try {
                frVersion = Version.getVersion();
            } catch (Exception ignore) {}
            VOpt.Log("FR Version: " + frVersion);
            if (!isVersionStrictlyAbove(frVersion, 0, 8, 1)) {
                VOpt.Log("FR Version 0.8.1 or below, handing DDS files manually (be sure to update to the latest version!)");
                HandleDDS();
            } else {
                VOpt.Log("Suitable FR Version found, skipping DDS file handling");
                VOpt.Log("VRAM usage " + (getTotalTextureVRAM() / 1024 / 1024) + "MB");
            }
        } else {
            VOpt.Log("FR not enabled, handling DDS files manually");
            HandleDDS();
        }
    }

    public static boolean isVersionStrictlyAbove(String versionStr, int targetMajor, int targetMinor, int targetPatch) {
        String cleanVersion = versionStr.replaceAll("[a-zA-Z]", "");

        String[] parts = cleanVersion.split("\\.");
        int major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
        int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        if (major != targetMajor) {
            return major > targetMajor;
        }
        if (minor != targetMinor) {
            return minor > targetMinor;
        }
        return patch > targetPatch;
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (true) return;
        Global.getSector().getListenerManager().addListener(new VRAMViewerSpawner(), true);
    }
}
