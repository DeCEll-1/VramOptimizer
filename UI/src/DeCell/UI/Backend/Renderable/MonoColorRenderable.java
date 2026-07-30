package DeCell.UI.Backend.Renderable;

import org.lwjgl.opengl.GL11;

import java.awt.*;

public class MonoColorRenderable extends PluginRenderable {
    public Color c;
    private boolean needsUpdate = false;

    public MonoColorRenderable(Color _c) {
        this.c = _c;
    }

    @Override
    public void renderBelow(float alpha) {
        if (zone == null)
            return;
        if (!zone.hasColor() || needsUpdate) {
            zone.monoColor(c);
            this.needsUpdate = false;
        }
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        zone.render();

        GL11.glDisable(GL11.GL_BLEND);
    }

    public MonoColorRenderable setColor(Color _c) {
        this.c = _c;
        return this;
    }

    public MonoColorRenderable updateColors() {
        needsUpdate = true;
        return this;
    }
}
