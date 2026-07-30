package DeCell.VOpt.UI.Backend.Components.Charlie;

import DeCell.VOpt.UI.Backend.Components.MyPanel;
import DeCell.VOpt.UI.Backend.Plugins.MultiPluginHandler;
import DeCell.VOpt.UI.Backend.Renderable.MonoColorRenderable;
import DeCell.VOpt.UI.Backend.Renderable.RenderableHandlerPlugin;
import DeCell.VOpt.UI.Backend.UIContainer;
import DeCell.VOpt.VOpt;
import com.fs.starfarer.api.ui.CustomPanelAPI;

import java.awt.*;

// heres charlie, his job is to make the behind of the panels darker
public class CharlieElement extends UIContainer<CharlieElement, CustomPanelAPI> {

    private final MonoColorRenderable background = new MonoColorRenderable(new Color(0x9A000000, true));
    private MultiPluginHandler plugin;

    public CharlieElement(MyPanel _parent) {
        super(_parent.u.createCustomPanel(_parent.w(), _parent.h(), new MultiPluginHandler()));
        this.setConsumeEvents(false);
        this.name = "Charlie_" + this.name;
        _parent.addComponent(this.u).inBL(0, 0);
        this.parent = _parent;
        this.parent.name = "Charlie_parent_" + this.parent.name;
        parent.addElement(this);
        parent.setConsumeEvents(false);

        this.plugin = ((MultiPluginHandler) this.u.getPlugin());
        RenderableHandlerPlugin renderHandler = new RenderableHandlerPlugin().addBelow(background);
        this.plugin.add(renderHandler);
        if (!VOpt.isDebugIUCharlie)
            renderHandler.cleanDebugRenderables();
        this.plugin.update(this);

        this.update();

        disable();
    }

    public CharlieElement(CustomPanelAPI customPanelAPI) {
        super(customPanelAPI);
    }

    public <T extends UIContainer<?, ?> & Openable> CharlieElement addOpenable(T o) {
        this.u.addComponent(o.u);
        o.setOnOpenClose(this::onOpenStateChanged);
        return this;
    }

    private void onOpenStateChanged(boolean isNowOpen) {
        if (isNowOpen) {
            enable();
        } else {
            disable();
        }
    }

    public CharlieElement enable() {
        this.background.render = true;
        return this;
    }

    public CharlieElement disable() {
        this.background.render = false;
        return this;
    }
}
