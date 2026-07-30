package DeCell.VOpt.UI.Backend.Renderable;

import org.lwjgl.opengl.GL11;

import java.awt.*;

public class QuadColorRenderable extends PluginRenderable {

    private Color colorBL;
    private Color colorBR;
    private Color colorTL;
    private Color colorTR;
    private boolean needsUpdate = false;

    public QuadColorRenderable(Color bl, Color br, Color tl, Color tr) {
        this.colorBL = bl;
        this.colorBR = br;
        this.colorTL = tl;
        this.colorTR = tr;
    }

    public QuadColorRenderable(Color _c) {
        this.colorBL = this.colorBR = this.colorTL = this.colorTR = _c;
    }

    @Override
    public void renderBelow(float alpha) {
        if (zone == null)
            return;
        if (!zone.hasColor() || needsUpdate) {
            zone.CBL(colorBL);
            zone.CBR(colorBR);
            zone.CTL(colorTL);
            zone.CTR(colorTR);
            this.needsUpdate = false;
        }
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        zone.render();

        GL11.glDisable(GL11.GL_BLEND);
    }

    public QuadColorRenderable monoColor(Color col) {
        this.colorTL = this.colorTR = this.colorBL = this.colorBR = col;
        this.needsUpdate = false;
        return this;
    }

    public QuadColorRenderable CTL(Color col) {
        this.colorTL = col;
        this.needsUpdate = false;
        return this;
    }

    public QuadColorRenderable CTR(Color col) {
        this.colorTR = col;
        this.needsUpdate = false;
        return this;
    }

    public QuadColorRenderable CBL(Color col) {
        this.colorBL = col;
        this.needsUpdate = false;
        return this;
    }

    public QuadColorRenderable CBR(Color col) {
        this.colorBR = col;
        this.needsUpdate = false;
        return this;
    }

    public QuadColorRenderable updateColors() {
        needsUpdate = true;
        return this;
    }
}
