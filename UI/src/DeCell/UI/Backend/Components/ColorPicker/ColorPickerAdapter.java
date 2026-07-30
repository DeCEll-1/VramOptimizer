package DeCell.UI.Backend.Components.ColorPicker;

import DeCell.UI.Backend.Components.MyPanel;
import DeCell.UI.Backend.Components.Slider;
import DeCell.VFXCommons.Shader;

import java.awt.*;

public abstract class ColorPickerAdapter {
    protected double xMin, xMax;
    protected int xDecimalPlaces;
    protected double yMin, yMax;
    protected int yDecimalPlaces;
    protected double zMin, zMax;
    protected int zDecimalPlaces;

    protected double currentX;
    protected double currentY;
    protected double currentZ;

    // 0-1
    public abstract double getNormalisedX();

    public abstract double getNormalisedY();

    public abstract double getNormalisedZ();

    //#region getters

    public int getzDecimalPlaces() {
        return zDecimalPlaces;
    }

    public double getzMax() {
        return zMax;
    }

    public double getzMin() {
        return zMin;
    }

    public int getyDecimalPlaces() {
        return yDecimalPlaces;
    }

    public double getyMax() {
        return yMax;
    }

    public double getyMin() {
        return yMin;
    }

    public int getxDecimalPlaces() {
        return xDecimalPlaces;
    }

    public double getxMax() {
        return xMax;
    }

    public double getxMin() {
        return xMin;
    }
    //#endregion

    public abstract void updateSlidersVisuals(Slider xSlider, Slider ySlider, Slider zSlider);

    // get rgb color
    public abstract Color getColor();

    public abstract void setColor(Color color);

    // px & py are panels 0-1 normalised click coordinates and sx is sliders normalised x
    public abstract void updateFromPanel(float px, float py);

    public abstract void updateFromSlider(float sx);

    // takes in normalised values (0-1)
    public abstract void updateValues(float x, float y, float z);

    // will update the slider and the panel view
    public abstract void updateInputPanelAndSlider(MyPanel colorPickerPanel, Slider colorPickerSlider);

    public abstract Shader getPanelShader();

    public abstract void updateColorPickerShaderUniforms(Shader shader);
}
