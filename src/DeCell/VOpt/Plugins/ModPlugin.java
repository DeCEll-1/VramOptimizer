package DeCell.VOpt.Plugins;

import DeCell.UI.DeCellUI;
import DeCell.VOpt.FileMetadata;
import DeCell.VOpt.Reflections;
import DeCell.VOpt.VOpt;
import com.fs.graphics.Sprite;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static DeCell.VOpt.DDSOverriding.HandleDDS;

public class ModPlugin extends BaseModPlugin {

    @Override
    public void onApplicationLoad() throws Exception {
        VOpt.isDebug = Global.getSettings().getBoolean("VOpt_debug");
        VOpt.isVerbose = Global.getSettings().getBoolean("VOpt_verbose");

        DeCellUI.Init();

        HandleDDS();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (true)
            return;
        Global.getSector().getListenerManager().addListener(new VRAMViewerSpawner(), true);
    }
}
