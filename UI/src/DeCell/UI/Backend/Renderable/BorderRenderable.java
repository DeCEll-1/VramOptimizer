package DeCell.UI.Backend.Renderable;

import DeCell.UI.Backend.Rect;
import DeCell.UI.Backend.UIContainer;
import DeCell.UI.Backend.UIElement;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;

import static org.lwjgl.opengl.GL11.*;

public class BorderRenderable extends PluginRenderable {

    private SpriteAPI borderSprite;
    private Rect cacheDstTopLeft, cacheDstTopRight, cacheDstBottomLeft, cacheDstBottomRight;
    private Rect cacheDstTopEdge, cacheDstBottomEdge, cacheDstLeftEdge, cacheDstRightEdge;
    private Rect cacheDstInside;

    private Rect cacheSrcTopLeft, cacheSrcTopRight, cacheSrcBottomLeft, cacheSrcBottomRight;
    private Rect cacheSrcTopEdge, cacheSrcBottomEdge, cacheSrcLeftEdge, cacheSrcRightEdge;
    private Rect cacheSrcInsideBorder;
    private boolean needsLayoutUpdate = true;

    // the texture thickness widths
    private float leftSlice, rightSlice, topSlice, bottomSlice;

    // the actual border width
    private float leftThickness, rightThickness, topThickness, bottomThickness;

    private float padding = 0;
    private boolean renderInside = false;
    private SpriteAPI renderInsideTexture;

    public BorderRenderable(SpriteAPI borderSprite) {
        this.borderSprite = borderSprite;
    }

    public BorderRenderable(SpriteAPI borderSprite, float left, float right, float top, float bottom) {
        this.borderSprite = borderSprite;
        this.leftSlice = left;
        this.rightSlice = right;
        this.topSlice = top;
        this.bottomSlice = bottom;
    }

    public BorderRenderable setSlices(float slice) {
        this.leftSlice = this.rightSlice = this.topSlice = this.bottomSlice = slice;
        return this;
    }

    public BorderRenderable setSlices(float left, float top, float right, float bottom) {
        this.leftSlice = left;
        this.topSlice = top;
        this.rightSlice = right;
        this.bottomSlice = bottom;
        return this;
    }

    public BorderRenderable setThickness(float thickness) {
        leftThickness = rightThickness = topThickness = bottomThickness = thickness;
        return this;
    }

    public BorderRenderable setThickness(float left, float top, float right, float bottom) {
        this.leftThickness = left;
        this.topThickness = top;
        this.rightThickness = right;
        this.bottomThickness = bottom;
        return this;
    }

    public BorderRenderable setRenderInside(boolean s) {
        this.renderInside = s;
        return this;
    }

    public BorderRenderable setRenderInsideTexture(SpriteAPI tex) {
        this.renderInsideTexture = tex;
        return this;
    }

    public BorderRenderable setPadding(float s) {
        this.padding = s;
        return this;
    }

    public void invalidateLayout() {
        this.needsLayoutUpdate = true;
    }

    @Override
    public void init(UIContainer<? extends UIElement<?, CustomPanelAPI>, CustomPanelAPI> parent) {
        super.init(parent);
    }

    @Override
    public void update(UIContainer<? extends UIElement<?, CustomPanelAPI>, CustomPanelAPI> parent) {
        super.update(parent);
    }

    @Override
    public void renderBelow(float alpha) {
        if (zone == null || borderSprite == null) return;

        if (needsLayoutUpdate) {
            updateCachedRects();
        }

        glEnable(GL_TEXTURE_2D);
        borderSprite.bindTexture();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        cacheDstTopLeft.render(cacheSrcTopLeft);
        cacheDstTopRight.render(cacheSrcTopRight);
        cacheDstBottomLeft.render(cacheSrcBottomLeft);
        cacheDstBottomRight.render(cacheSrcBottomRight);

        cacheDstTopEdge.render(cacheSrcTopEdge);
        cacheDstBottomEdge.render(cacheSrcBottomEdge);
        cacheDstLeftEdge.render(cacheSrcLeftEdge);
        cacheDstRightEdge.render(cacheSrcRightEdge);

        if (renderInside) {
            if (renderInsideTexture != null) {
                renderInsideTexture.bindTexture();

                float maxU = cacheDstInside.w / renderInsideTexture.getWidth();
                float maxV = cacheDstInside.h / renderInsideTexture.getHeight();

                cacheDstInside.render(new Rect(0, 0, maxU, maxV));
            } else {
                cacheDstInside.render(cacheSrcInsideBorder);
            }
        }

        glDisable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
    }

    private void updateCachedRects() {
        if (zone == null || borderSprite == null) return;

        float x = zone.x;
        float y = zone.y;
        float w = zone.w;
        float h = zone.h;

        float texS = borderSprite.getTextureWidth();
        float texT = borderSprite.getTextureHeight();
        float texW = borderSprite.getWidth() / texT;
        float texH = borderSprite.getHeight() / texT;

        float borderTopTextureBorder = texT - (topSlice / texH);
        float borderRightTextureBorder = texS - (rightSlice / texW);
        float borderBottomTextureBorder = bottomSlice / texH;
        float borderLeftTextureBorder = leftSlice / texW;

        cacheDstTopLeft = new Rect(x + padding, y + h - topThickness - padding, leftThickness, topThickness);
        cacheDstTopRight = new Rect(x + w - rightThickness - padding, y + h - topThickness - padding, rightThickness, topThickness);
        cacheDstBottomLeft = new Rect(x + padding, y + padding, leftThickness, bottomThickness);
        cacheDstBottomRight = new Rect(x + w - rightThickness - padding, y + padding, rightThickness, bottomThickness);

        float edgeW = w - leftThickness - rightThickness - (2 * padding);
        float edgeH = h - topThickness - bottomThickness - (2 * padding);

        cacheDstTopEdge = new Rect(x + leftThickness + padding, y + h - topThickness - padding, edgeW, topThickness);
        cacheDstBottomEdge = new Rect(x + leftThickness + padding, y + padding, edgeW, bottomThickness);
        cacheDstLeftEdge = new Rect(x + padding, y + bottomThickness + padding, leftThickness, edgeH);
        cacheDstRightEdge = new Rect(x + w - rightThickness - padding, y + bottomThickness + padding, rightThickness, edgeH);
        cacheDstInside = new Rect(x + leftThickness + padding, y + bottomThickness + padding, edgeW, edgeH);

        cacheSrcTopLeft = new Rect(0, borderTopTextureBorder, borderLeftTextureBorder, texT);
        cacheSrcTopRight = new Rect(borderRightTextureBorder, borderTopTextureBorder, texS, texT);
        cacheSrcBottomLeft = new Rect(0, 0, borderLeftTextureBorder, borderBottomTextureBorder);
        cacheSrcBottomRight = new Rect(borderRightTextureBorder, 0, texS, borderBottomTextureBorder);

        cacheSrcTopEdge = new Rect(borderLeftTextureBorder, borderTopTextureBorder, borderRightTextureBorder, texT);
        cacheSrcBottomEdge = new Rect(borderLeftTextureBorder, 0, borderRightTextureBorder, borderBottomTextureBorder);
        cacheSrcLeftEdge = new Rect(0, borderBottomTextureBorder, borderLeftTextureBorder, borderTopTextureBorder);
        cacheSrcRightEdge = new Rect(borderRightTextureBorder, borderBottomTextureBorder, texS, borderTopTextureBorder);
        cacheSrcInsideBorder = new Rect(borderLeftTextureBorder, borderBottomTextureBorder, borderRightTextureBorder, borderTopTextureBorder);

        needsLayoutUpdate = false;
    }

    public static BorderRenderable createBorder2() {
        return new BorderRenderable(Global.getSettings().getSprite("cpg", "border2"))
                .setSlices(32)
                .setRenderInsideTexture(Global.getSettings().getSprite("cpg", "border2Inside"))
                .setThickness(16)
                .setPadding(-16).setRenderInside(true);
    }
}