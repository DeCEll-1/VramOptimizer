package DeCell.UI.Backend.Components.ColorPicker;

import DeCell.UI.Backend.BaseBuilder;
import DeCell.UI.Backend.Components.MyPanel;
import DeCell.UI.Backend.Components.MyTextBox;
import DeCell.UI.Backend.Components.NumericUpDown;
import DeCell.UI.Backend.Components.Slider;
import DeCell.UI.Backend.PointF;
import DeCell.UI.Backend.Rect;
import DeCell.UI.Backend.Renderable.MonoColorRenderable;
import DeCell.UI.Backend.Renderable.QuadColorRenderable;
import DeCell.UI.Backend.Renderable.RenderableHandlerPlugin;
import DeCell.UI.Backend.Renderable.ShaderRenderable;
import DeCell.UI.Backend.UIContainer;
import com.fs.starfarer.api.ui.UIComponentAPI;

import java.awt.Color;
import java.util.function.Consumer;

public class ColorPickerV2 extends UIContainer<ColorPickerV2, UIComponentAPI> {
    //#region constants
    public final static float xPadding = 10f;
    public final static float yPadding = 2f;

    public final static Rect colorPickerPanelRect = new Rect(0, 0, 230, 230);
    public final static Rect colorPickerSliderRect =
            new Rect(0, colorPickerPanelRect.h + yPadding, colorPickerPanelRect.w, Slider.defaultHeight);

    public final static Rect previewPanelRect
            = new Rect(
            0, colorPickerSliderRect.y + colorPickerSliderRect.h + yPadding,
            colorPickerPanelRect.w, Slider.defaultHeight
    );

    public final static Rect NudRect =
            new Rect( // colorPickerPanelRect.w - (previewPanelRect.w + xPadding)
                    0, previewPanelRect.y + previewPanelRect.h + yPadding,
                    60, MyTextBox.defaultHeight
            );
    public final static Rect SliderRect = // put the sliders next to the nuds
            new Rect(
                    0, 0,
                    colorPickerPanelRect.w - NudRect.w - xPadding, Slider.defaultHeight
            );

    public final static Rect sizeRect = new Rect(
            0,
            0,
            colorPickerPanelRect.w,
            colorPickerPanelRect.h + yPadding +
                    colorPickerSliderRect.h + yPadding +
                    previewPanelRect.h + yPadding +
                    ((NudRect.h + yPadding) * 3) - yPadding
    );

    // this fucking sucks

    public final static Color defaultColor = new Color(0xFFFFFF);
    //#endregion

    private final MyPanel container;
    // TODO: put a cursor on the color picker panel
    private final MyPanel colorPickerPanel;
    private final Slider colorPickerSlider;
    private final MyPanel colorPreview;

    private final NumericUpDown xNud;
    private final Slider xSlider;
    private final NumericUpDown yNud;
    private final Slider ySlider;
    private final NumericUpDown zNud;
    private final Slider zSlider;

    private ColorPickerAdapter adapter;

    public ColorPickerV2(
            MyPanel _container,
            MyPanel _colorPickerPanel,
            Slider _colorPickerSlider,
            MyPanel _colorPreview,
            NumericUpDown _xNud, Slider _xSlider,
            NumericUpDown _yNud, Slider _ySlider,
            NumericUpDown _zNud, Slider _zSlider
    ) {
        super(_container.u);
        _container.addElement(this);
        this.container = _container;
        this.parent = container;
        this.colorPickerPanel = _colorPickerPanel;
        this.colorPickerSlider = _colorPickerSlider;
        this.colorPreview = _colorPreview;

        this.xNud = _xNud;
        this.xSlider = _xSlider;

        this.yNud = _yNud;
        this.ySlider = _ySlider;

        this.zNud = _zNud;
        this.zSlider = _zSlider;

        xNud.setOnChange(this::updateSliders);
        yNud.setOnChange(this::updateSliders);
        zNud.setOnChange(this::updateSliders);

        xSlider.setOnChange(this::updateNuds);
        ySlider.setOnChange(this::updateNuds);
        zSlider.setOnChange(this::updateNuds);

        colorPickerPanel.addOnHover(s -> {
            if (s.isDragging())
                updateFromPanelAndSlider();
        });
        colorPickerSlider.setOnChange(s -> updateFromPanelAndSlider());
    }

    private void updateFromPanelAndSlider() {
        PointF mousePos = colorPickerPanel.rect().getRelativeMousePositionNormalised();
        if (colorPickerPanel.rect().containsMouse() && colorPickerPanel.isDragging())
            adapter.updateFromPanel(mousePos.x, mousePos.y);
        adapter.updateFromSlider(colorPickerSlider.getValue());
        syncUiFromAdapter();
    }

    public ColorPickerV2 setAdapter(ColorPickerAdapter newAdapter) {
        this.adapter = newAdapter;

        xNud.setMinMax(adapter.getxMin(), adapter.getxMax());
        xNud.setAmountOfDecimalPlaces(adapter.getxDecimalPlaces());
        yNud.setMinMax(adapter.getyMin(), adapter.getyMax());
        yNud.setAmountOfDecimalPlaces(adapter.getyDecimalPlaces());
        zNud.setMinMax(adapter.getzMin(), adapter.getzMax());
        zNud.setAmountOfDecimalPlaces(adapter.getzDecimalPlaces());
        colorPickerPanel.<RenderableHandlerPlugin>getPlugin()
                .<ShaderRenderable>getRenderBelow(0)
                .setShader(adapter.getPanelShader())// WORK
                .setBeforeRender(adapter::updateColorPickerShaderUniforms);

        syncUiFromAdapter();

        return this;
    }

    private void syncUiFromAdapter() {
        adapter.updateSlidersVisuals(xSlider, ySlider, zSlider);
        adapter.updateInputPanelAndSlider(colorPickerPanel, colorPickerSlider);
        updateElements();
    }

    private void updateNuds(Slider __) {
        adapter.updateValues(
                xSlider.getValue(),
                ySlider.getValue(),
                zSlider.getValue()
        );
        syncUiFromAdapter();
    }

    private void updateSliders(NumericUpDown __) {
        adapter.updateValues(
                (float) xNud.getValueNormalised(),
                (float) yNud.getValueNormalised(),
                (float) zNud.getValueNormalised()
        );
        syncUiFromAdapter();
    }

    private boolean updating = false;

    private void updateElements() {
        if (updating) return;
        updating = true; // TODO: this triggers twice , fix it sometime
        xSlider.setSliderValue((float) adapter.getNormalisedX());
        ySlider.setSliderValue((float) adapter.getNormalisedY());
        zSlider.setSliderValue((float) adapter.getNormalisedZ());
        xNud.setValueNormalised(adapter.getNormalisedX());
        yNud.setValueNormalised(adapter.getNormalisedY());
        zNud.setValueNormalised(adapter.getNormalisedZ());
        colorPreview.<RenderableHandlerPlugin>getPlugin().<MonoColorRenderable>getRenderBelow(0)
                .setColor(adapter.getColor()).updateColors();

        if (onChange != null)
            onChange.accept(this);

        updating = false;
    }


    public Color getColor() {
        return adapter.getColor();
    }

    public ColorPickerV2 setColor(Color col) {
        adapter.setColor(col);
        syncUiFromAdapter();
        return this;
    }

    private Consumer<ColorPickerV2> onChange = null;

    public ColorPickerV2 setOnChange(Consumer<ColorPickerV2> onChange) {
        this.onChange = onChange;
        return this;
    }


    public static class Builder extends BaseBuilder<Builder> {

        public ColorPickerV2 build(MyPanel parent) {

            MyPanel container = new MyPanel.Builder(
                    sizeRect.w,
                    sizeRect.h
            ).build(parent);

            MyPanel colorPickerPanel = new MyPanel.Builder(colorPickerPanelRect.w, colorPickerPanelRect.h)
                    .setPlugin(new RenderableHandlerPlugin().addBelow(new ShaderRenderable(null)))
                    .build(container).inTL(0, 0);

            Slider colorPickerSlider = new Slider.Builder(colorPickerSliderRect.w, container)
                    .build()
                    .inTL(colorPickerSliderRect.x, colorPickerSliderRect.y)
                    .addToSliderBackground(new QuadColorRenderable(Color.white));

            MyPanel colorPreview = new MyPanel.Builder(
                    previewPanelRect.w,
                    previewPanelRect.h
            ).setPlugin(new RenderableHandlerPlugin().addBelow(new MonoColorRenderable(Color.red))).build(container)
                    .inTL(previewPanelRect.x, previewPanelRect.y);

            NumericUpDown xNud = new NumericUpDown.Builder().build(NudRect.w, container)
                    .inTL(NudRect.x, NudRect.y);

            Slider xSlider = new Slider.Builder(SliderRect.w, container)
                    .build()
                    .rightOfMid(xNud.u, xPadding)
                    .addToSliderBackground(new QuadColorRenderable(Color.white));

            NumericUpDown yNud = new NumericUpDown.Builder().build(NudRect.w, container)
                    .belowMid(xNud.u, yPadding);

            Slider ySlider = new Slider.Builder(SliderRect.w, container)
                    .build()
                    .rightOfMid(yNud.u, xPadding)
                    .addToSliderBackground(new QuadColorRenderable(Color.white));

            NumericUpDown zNud = new NumericUpDown.Builder().build(NudRect.w, container)
                    .belowMid(yNud.u, yPadding);

            Slider zSlider = new Slider.Builder(SliderRect.w, container)
                    .build()
                    .rightOfMid(zNud.u, xPadding)
                    .addToSliderBackground(new QuadColorRenderable(Color.white));


            return new ColorPickerV2(
                    container,
                    colorPickerPanel,
                    colorPickerSlider,
                    colorPreview,
                    xNud, xSlider,
                    yNud, ySlider,
                    zNud, zSlider
            );
        }
    }
}
