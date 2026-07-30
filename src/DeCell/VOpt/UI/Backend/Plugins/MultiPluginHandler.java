package DeCell.VOpt.UI.Backend.Plugins;

import DeCell.VOpt.UI.Backend.UIContainer;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import java.util.ArrayList;
import java.util.List;

public class MultiPluginHandler extends PanelPlugin {
    private List<PanelPlugin> plugins = new ArrayList<>();

    public MultiPluginHandler() {
    }

    public MultiPluginHandler add(PanelPlugin _0) {
        plugins.add(_0);
        return this;
    }

    // --- CustomPanelAPI specific methods ---
    @Override
    public void init(UIContainer<?, CustomPanelAPI> parent) {
        for (PanelPlugin plugin : plugins) {
            plugin.init(parent);
        }
    }

    @Override
    public void update(UIContainer<?, CustomPanelAPI> parent) {
        for (PanelPlugin plugin : plugins) {
            plugin.update(parent);
        }

        this.needsUpdate = false;
    }

    // --- Overridden methods from CustomUIPanelPlugin ---

    @Override
    public void positionChanged(PositionAPI position) {
        for (PanelPlugin plugin : plugins) {
            plugin.positionChanged(position);
        }
    }

    @Override
    public void renderBelow(float alphaMult) {
        for (PanelPlugin plugin : plugins) {
            plugin.renderBelow(alphaMult);
        }
    }

    @Override
    public void render(float alphaMult) {
        for (PanelPlugin plugin : plugins) {
            plugin.render(alphaMult);
        }
    }

    @Override
    public void advance(float amount) {
        for (PanelPlugin plugin : plugins) {
            plugin.advance(amount);
        }

        if (plugins.stream().anyMatch(s -> s.needsUpdate))
            this.needsUpdate = true;
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        for (PanelPlugin plugin : plugins) {
            plugin.processInput(events);
        }
    }

    @Override
    public void buttonPressed(Object buttonId) {
        for (PanelPlugin plugin : plugins) {
            plugin.buttonPressed(buttonId);
        }
    }
}
