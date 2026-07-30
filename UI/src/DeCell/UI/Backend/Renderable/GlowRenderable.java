package DeCell.UI.Backend.Renderable;

import DeCell.UI.Backend.UIContainer;
import DeCell.UI.Backend.UIElement;
import Kryz.Tweening.EasingFunctions;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class GlowRenderable extends PluginRenderable {
    public Color c;
    private boolean needsUpdate = false;
    private boolean isHovering = false;
    private float currentProgress = 0f; // Goes from 0.0 (hidden) to 1.0 (fully glowing)
    private static final float durationSeconds = 0.5f; // How fast the glow fades in/out

    public GlowRenderable(Color _c) {
        this.c = _c;
    }

    @Override
    public void init(UIContainer<? extends UIElement<?, CustomPanelAPI>, CustomPanelAPI> parent) {
        super.init(parent);
        parent.addOnMouseEnter(this::onMouseEnter);
        parent.addOnMouseExit(this::onMouseExit);
    }

    private void onMouseExit(UIElement<? extends UIElement<?, CustomPanelAPI>, CustomPanelAPI> uiElement) {
        isHovering = false;
    }

    private void onMouseEnter(UIElement<? extends UIElement<?, CustomPanelAPI>, CustomPanelAPI> uiElement) {
        isHovering = true;
    }

    @Override
    public void renderBelow(float alpha) {
        super.render(alpha);

        if (zone == null)
            return;

        // 1. Update the linear progress timer based on Starsector's frame delta time
        // Note: Replace 'alpha' with your actual frame delta time (e.g., Global.getCombatEngine().getElapsedInLastFrame())
        // if 'alpha' represents UI transparency rather than delta time. Assuming 'alpha' is delta time here:
        float deltaTime = Global.getCombatEngine().getElapsedInLastFrame();

        if (isHovering) {
            currentProgress += deltaTime / durationSeconds;
            if (currentProgress > 1f) currentProgress = 1f;
        } else {
            currentProgress -= deltaTime / durationSeconds;
            if (currentProgress < 0f) currentProgress = 0f;
        }

        // 2. Short-circuit if the glow is completely faded out
        if (currentProgress <= 0f)
            return;

        // 3. Apply your custom Easing function to the progress
        // OutCubic or OutQuad gives a snappy fade-in and smooth finish
        float easedAlpha = EasingFunctions.OutCubic(currentProgress);

        // 4. Factor the eased value into the glow color's native alpha channel
        int finalAlpha = Math.round(c.getAlpha() * easedAlpha);
        Color shiftedColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), finalAlpha);

        if (!zone.hasColor() || needsUpdate) {
            zone.monoColor(shiftedColor);
            this.needsUpdate = false;
        } else {
            // Constantly apply the updated alpha color to the zone
            zone.monoColor(shiftedColor);
        }

        // 5. Open GL Rendering
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        zone.render();

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public GlowRenderable updateColor() {
        needsUpdate = true;
        return this;
    }
}
