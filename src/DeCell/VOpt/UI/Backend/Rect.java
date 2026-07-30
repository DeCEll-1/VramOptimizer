package DeCell.VOpt.UI.Backend;

import DeCell.VOpt.Misc;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Rect {
    public float x, y, w, h;
    private Color colorTL, colorTR, colorBL, colorBR;
    private boolean monocolor = false;
    private boolean hasColor = false;

    public Rect(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public Rect monoColor(Color col) {
        this.colorTL = this.colorTR = this.colorBL = this.colorBR = col;
        this.monocolor = true;
        this.hasColor = true;
        return this;
    }

    public Rect CTL(Color col) {
        this.colorTL = col;
        this.monocolor = false;
        this.hasColor = true;
        return this;
    }

    public Rect CTR(Color col) {
        this.colorTR = col;
        this.monocolor = false;
        this.hasColor = true;
        return this;
    }

    public Rect CBL(Color col) {
        this.colorBL = col;
        this.monocolor = false;
        this.hasColor = true;
        return this;
    }

    public Rect CBR(Color col) {
        this.colorBR = col;
        this.monocolor = false;
        this.hasColor = true;
        return this;
    }

    public boolean hasColor() {
        return hasColor;
    }

    public boolean containsMouse() {
        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();

        return mouseX >= this.x &&
                mouseX <= (this.x + this.w) &&
                mouseY >= this.y &&
                mouseY <= (this.y + this.h);
    }

    // from top left
    public Point getRelativeMousePosition() {
        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        return new Point((int) (mouseX - x), (int) (mouseY - y));
    }

    public PointF getRelativeMousePositionNormalised() {
        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        return new PointF((mouseX - x) / w, (mouseY - y) / h);
    }

    // texCoordinates
    public void render(Rect tc) {
        if (monocolor && hasColor)
            Misc.setColor(colorBL);
        else
            GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

        // Bottom Left
        if (hasColor)
            Misc.setColor(colorBL);
        GL11.glTexCoord2f(tc.x, tc.y);
        GL11.glVertex2f(x, y);

        // Bottom Right
        if (hasColor)
            Misc.setColor(colorBR);
        GL11.glTexCoord2f(tc.w, tc.y);
        GL11.glVertex2f(x + w, y);

        // Top Left
        if (hasColor)
            Misc.setColor(colorTL);
        GL11.glTexCoord2f(tc.x, tc.h);
        GL11.glVertex2f(x, y + h);

        // Top Right
        if (hasColor)
            Misc.setColor(colorTR);
        GL11.glTexCoord2f(tc.w, tc.h);
        GL11.glVertex2f(x + w, y + h);

        GL11.glEnd();

        if (monocolor || hasColor)
            GL11.glColor4f(1f, 1f, 1f, 1f); // reset color
    }

    public void render() {
        this.render(new Rect(0, 0, 1, 1));
    }

    // does not handle enables and disables
    public void render(SpriteAPI s) {
        this.render(new Rect(0, 0, s.getTextureWidth(), s.getTextureHeight()));
    }
}
