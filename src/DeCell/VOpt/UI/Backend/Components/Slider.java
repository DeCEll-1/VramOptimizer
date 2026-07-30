package DeCell.VOpt.UI.Backend.Components;

import DeCell.VOpt.Misc;
import DeCell.VOpt.UI.Backend.BaseBuilder;
import DeCell.VOpt.UI.Backend.Components.Gears.Scroll;
import DeCell.VOpt.UI.Backend.Renderable.PluginRenderable;
import DeCell.VOpt.UI.Backend.Renderable.RenderableHandlerPlugin;
import DeCell.VOpt.UI.Backend.UIContainer;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.function.Consumer;

public class Slider extends UIContainer<Slider, UIComponentAPI> {


    public static final float handleHeight = 22;
    public static final float handleWidth = 18;
    public static final float halfHandleWidth = handleWidth - (handleWidth / 4f);
    // TODO: make this height adjustable
    public static final float defaultHeight = 16;

    // for the button to be centered
    public static final float handleTLPadding = -(handleHeight - defaultHeight) / 2;

    public final MyPanel slider;
    public final MyButton handle;
    private final RenderableHandlerPlugin renderableHandlerPlugin;
    private float handleRelativeX = 0;

    public Slider(MyPanel sldr, MyButton hndl, RenderableHandlerPlugin renderPlugin) {
        super(sldr.u);
        setIgnoreEvents(true);
        this.name = "Slider_" + this.name;
        this.slider = sldr;
        this.slider.name = "Slider_slider_" + this.slider.name;
        handle = hndl;
        this.handle.name = "Slider_handle_" + this.handle.name;
        slider.addElement(this);
        Scroll scrollGear = new Scroll();
        slider.addOnHover(scrollGear::onHover);
        scrollGear.addScrollListener(s -> {
            handleRelativeX += s;
            updateHandle();
        });
        slider.addOnMouseDown(this::onSliderClick);

        handle.inTL(0 - halfHandleWidth, handleTLPadding);

        renderableHandlerPlugin = renderPlugin;
    }


    public Slider addToSliderBackground(PluginRenderable backgroundRenderable) {
        renderableHandlerPlugin.addBelow(backgroundRenderable);
        return this;
    }

    public RenderableHandlerPlugin getRenderHandler() {
        return renderableHandlerPlugin;
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        super.processInput(events);
        // java is so good!!!!! it allows protected fields and whatnot to be accessable in the same package!!!
        // very good languag design
        // ;kms
        if (!handle.isDragging())
            return;
        // the button is currently getting held down
        float parentX = slider.x();

        float mouseX = Mouse.getX() + halfHandleWidth;

        handleRelativeX = Mouse.getX() - slider.x();

        updateHandle();
    }

    public void updateHandle() {
        updateHandle(true);
    }

    public void updateHandle(boolean triggerOnChange) {
        handleRelativeX = Misc.clamp(handleRelativeX, 0, slider.w());
        handle.inTL(handleRelativeX - halfHandleWidth, handleTLPadding);
        if (onChange != null && triggerOnChange)
            onChange.accept(this);
    }

    private Consumer<Slider> onChange;

    public Slider setOnChange(Consumer<Slider> onChange) {
        this.onChange = onChange;
        return this;
    }

    // from 0-1, 0 at the start of the slider and 1 at the end of the slider
    public float getValue() {
        return Misc.clamp(handleRelativeX / slider.w(), 0, 1);
    }

    // zaza must be from 0-1
    public Slider setSliderValue(float zaza) {
        this.handleRelativeX = slider.w() * zaza;
        updateHandle(false);
        return this;
    }
    //#endregion

    //#region panel

    private void onSliderClick(MyPanel __) {
        float parentX = slider.x();

        float mouseX = Mouse.getX() + halfHandleWidth;

        handleRelativeX = Mouse.getX() - slider.x();
        handle.setDragging(true);
        updateHandle(true);
    }

    //#endregion


    // so the slider will need:
    // a panel for rendering (aka the slider section)
    // a handle
    // onChange method that will give from 0-1
    public static class Builder extends BaseBuilder<Builder> {

        //        private final float h;
        private final RenderableHandlerPlugin renderableHandlerPlugin = new RenderableHandlerPlugin();
        private final MyPanel slider;
        private final MyButton.Builder handle;

        public Builder(float w, MyPanel parent) {

            this.slider = new MyPanel.Builder(w, defaultHeight).
                    setPlugin(renderableHandlerPlugin).build(parent);

            this.handle = new MyButton.Builder("", handleWidth, handleHeight, slider);
        }

        public Slider build() {
            return new Slider(slider, handle.build(), renderableHandlerPlugin);
        }
    }
}
