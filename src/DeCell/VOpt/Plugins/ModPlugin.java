package DeCell.VOpt.Plugins;

import DeCell.UI.DeCellUI;
import DeCell.VOpt.VOpt;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

import static DeCell.VOpt.DDSOverriding.HandleDDS;

public class ModPlugin extends BaseModPlugin {

    @Override
    public void onApplicationLoad() throws Exception {
        VOpt.Log("VOpt Loaded");
        VOpt.isDebug = Global.getSettings().getBoolean("VOpt_debug");
        VOpt.isVerbose = Global.getSettings().getBoolean("VOpt_verbose");

        DeCellUI.Init();

//        if (!VOpt.frEnabled)
        HandleDDS();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (true) return;
        Global.getSector().getListenerManager().addListener(new VRAMViewerSpawner(), true);
    }
}
