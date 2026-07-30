package DeCell.UI.Backend.Renderable;

import DeCell.UI.Backend.Rect;
import DeCell.UI.Backend.UIContainer;
import DeCell.UI.Backend.UIElement;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;

public abstract class PluginRenderable {
    protected Rect zone;
    public boolean render = true;

    private void updateZone(PositionAPI p) {
        zone = new Rect(p.getX(), p.getY(), p.getWidth(), p.getHeight());
    }

    public void init(UIContainer<? extends UIElement<?, CustomPanelAPI>, CustomPanelAPI> parent) {
        PositionAPI p = parent.getPosition();
        zone = new Rect(-1f, -1f, p.getWidth(), p.getHeight());
    }

    public boolean needsUpdate() {
        if (zone == null)
            return true;
        return zone.x == -1f && zone.y == -1f;
    }

    public void update(UIContainer<? extends UIElement<?, CustomPanelAPI>, CustomPanelAPI> parent) {
        updateZone(parent.getPosition());
    }

    public void render(float alpha) {
    }

    public void renderBelow(float alpha) {
    }
}
