package DeCell.VOpt.Plugins;

import DeCell.UI.Backend.Components.MyPanel;
import DeCell.UI.Backend.Components.MyTooltip;
import DeCell.UI.Backend.Renderable.MonoColorRenderable;
import DeCell.UI.Backend.Renderable.RenderableHandlerPlugin;
import DeCell.UI.Backend.UIElement;
import DeCell.UI.WrapperPanelPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.CampaignInputListener;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.ScrollPanelAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.awt.*;
import java.util.List;

import static DeCell.UI.Misc.getCurrentTab;
import static DeCell.UI.Misc.getScreenPanel;

public class VRAMViewerSpawner implements CampaignInputListener {
    private UIPanelAPI container;
    private MyPanel customPanel;
    private MyPanel menuContainer;

    boolean isMenuOpen = false;

    float sw = Global.getSettings().getScreenWidthPixels();
    float sh = Global.getSettings().getScreenHeightPixels();

    @Override
    public int getListenerInputPriority() {
        return 0;
    }

    @Override
    public void processCampaignInputPreCore(List<InputEventAPI> events) {
        for (InputEventAPI event : events) {
            if (event.isAltDown() && event.getEventChar() == 'v') {
                isMenuOpen = !isMenuOpen;
                if (isMenuOpen) {
                    if (container == null)
                        container = (UIPanelAPI) getScreenPanel();
                    container.getPosition().inTL(0, 0);
                    customPanel = new MyPanel
                            .Builder(sw, sh)
                            .setPlugin(new WrapperPanelPlugin().setInit(this::init))
                            .build(container);
                } else
                    menuContainer.markForDeletion();
            }
        }
    }

    private void init(MyPanel parent) {

        menuContainer = new MyPanel.Builder(sw, sh).setPlugin(new RenderableHandlerPlugin()).build(parent).initPlugin();


    }

    @Override
    public void processCampaignInputPreFleetControl(List<InputEventAPI> events) {

    }

    @Override
    public void processCampaignInputPostCore(List<InputEventAPI> events) {

    }
}
