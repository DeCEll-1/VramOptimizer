package DeCell.VOpt.UI.Backend.Components.ColorPicker.Adapters;

import DeCell.VOpt.UI.Backend.Components.ColorPicker.ColorPickerAdapter;
import DeCell.VOpt.UI.Backend.Components.MyPanel;
import DeCell.VOpt.UI.Backend.Components.Slider;
import DeCell.VOpt.UI.Backend.Renderable.QuadColorRenderable;
import DeCell.VOpt.VFX.Shader;
import org.lwjgl.util.vector.Vector3f;

import java.awt.*;

public class RgbColorPickerAdapter extends ColorPickerAdapter {

    public RgbColorPickerAdapter() {
        // Setup structural min, max, and decimals for standard RGB fields
        this.xMin = 0.0;
        this.xMax = 255.0;
        this.xDecimalPlaces = 0;
        this.yMin = 0.0;
        this.yMax = 255.0;
        this.yDecimalPlaces = 0;
        this.zMin = 0.0;
        this.zMax = 255.0;
        this.zDecimalPlaces = 0;

        // Initialize state to standard default opaque white (1.0 normalized)
        this.currentX = 1.0;
        this.currentY = 1.0;
        this.currentZ = 1.0;
    }

    @Override
    public double getNormalisedX() {
        return Math.max(0.0, Math.min(1.0, currentX));
    }

    @Override
    public double getNormalisedY() {
        return Math.max(0.0, Math.min(1.0, currentY));
    }

    @Override
    public double getNormalisedZ() {
        return Math.max(0.0, Math.min(1.0, currentZ));
    }

    @Override
    public Color getColor() {
        // Quantize normalized floating points back into standard 0-255 channels
        int r = (int) Math.round(getNormalisedX() * 255.0);
        int g = (int) Math.round(getNormalisedY() * 255.0);
        int b = (int) Math.round(getNormalisedZ() * 255.0);
        return new Color(r, g, b);
    }

    @Override
    public void setColor(Color color) {
        if (color == null) return;
        // Strip incoming absolute channels down to 0-1 metrics
        this.currentX = color.getRed() / 255.0;
        this.currentY = color.getGreen() / 255.0;
        this.currentZ = color.getBlue() / 255.0;
    }

    @Override
    public void updateFromPanel(float px, float py) {
        // px, py represent the 2D panel vector points, sx represents the master slider
        this.currentX = px;
        this.currentY = py;
    }

    @Override
    public void updateFromSlider(float sx) {
        this.currentZ = sx;
    }

    @Override
    public void updateValues(float x, float y, float z) {
        // Direct assignment from individual UI components (already normalized 0-1)
        this.currentX = x;
        this.currentY = y;
        this.currentZ = z;
    }

    @Override
    public void updateSlidersVisuals(Slider xSlider, Slider ySlider, Slider zSlider) {
        // Get the single, real-time quantized RGB integers of our current total state
        int r = (int) Math.round(getNormalisedX() * 255.0);
        int g = (int) Math.round(getNormalisedY() * 255.0);
        int b = (int) Math.round(getNormalisedZ() * 255.0);

        // Update each slider's gradient based on what the other two channels are locked at
        applyGradientToSlider(xSlider, new Color(0, g, b), new Color(255, g, b)); // X = Red gradient
        applyGradientToSlider(ySlider, new Color(r, 0, b), new Color(r, 255, b)); // Y = Green gradient
        applyGradientToSlider(zSlider, new Color(r, g, 0), new Color(r, g, 255)); // Z = Blue gradient
    }

    /**
     * Replicates your legacy QuadColorRenderable color matching pipeline.
     */
    private void applyGradientToSlider(Slider slider, Color leftColor, Color rightColor) {
        if (slider == null || slider.getRenderHandler() == null || slider.getRenderHandler().getRenderBelows().isEmpty()) {
            return;
        }

        // Fetch the background QuadColorRenderable sitting at index 0 under the slider handle layer
        QuadColorRenderable sliderBackground = (QuadColorRenderable) slider.getRenderHandler().getRenderBelows().get(0);

        // Apply left-anchored vertex colors
        sliderBackground.CTL(leftColor);
        sliderBackground.CBL(leftColor);

        // Apply right-anchored vertex colors
        sliderBackground.CTR(rightColor);
        sliderBackground.CBR(rightColor);

        // Push buffer updates down to GL matrix context
        sliderBackground.updateColors();
    }

    @Override
    public void updateInputPanelAndSlider(MyPanel colorPickerPanel, Slider colorPickerSlider) {
        colorPickerSlider.setSliderValue((float) getNormalisedZ());

        int r = (int) Math.round(getNormalisedX() * 255.0);
        int g = (int) Math.round(getNormalisedY() * 255.0);
        int b = (int) Math.round(getNormalisedZ() * 255.0);

        applyGradientToSlider(colorPickerSlider, new Color(r, g, 0), new Color(r, g, 255)); // Z = Blue gradient
    }

    @Override
    public Shader getPanelShader() {
        return Shader.fromFile("data/shaders/cpg/main.vert", "data/shaders/cpg/colorPickerShaders/rgb.frag").init();
    }

    @Override
    public void updateColorPickerShaderUniforms(Shader shader) {
        shader.getUniformManager().setVector3("currColor", new Vector3f((float) currentX, (float) currentY, (float) currentZ));
    }
}