package DeCell.UI.Backend.Plugins;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import java.util.List;
import java.util.function.Consumer;

public class LambdaUIPanelPlugin extends PanelPlugin {

    private Consumer<PositionAPI> onPositionChanged = null;
    private Consumer<Float> onRenderBelow = null;
    private Consumer<Float> onRender = null;
    private Consumer<Float> onAdvance = null;
    private Consumer<List<InputEventAPI>> onProcessInput = null;
    private Consumer<Object> onButtonPressed = null;

    // ====================== SETTERS (Fluent) ======================

    public LambdaUIPanelPlugin onPositionChanged(Consumer<PositionAPI> func) {
        this.onPositionChanged = func;
        return this;
    }

    public LambdaUIPanelPlugin onRenderBelow(Consumer<Float> func) {
        this.onRenderBelow = func;
        return this;
    }

    public LambdaUIPanelPlugin onRender(Consumer<Float> func) {
        this.onRender = func;
        return this;
    }

    public LambdaUIPanelPlugin onAdvance(Consumer<Float> func) {
        this.onAdvance = func;
        return this;
    }

    public LambdaUIPanelPlugin onProcessInput(Consumer<List<InputEventAPI>> func) {
        this.onProcessInput = func;
        return this;
    }

    public LambdaUIPanelPlugin onButtonPressed(Consumer<Object> func) {
        this.onButtonPressed = func;
        return this;
    }

    @Override
    public void positionChanged(PositionAPI position) {
        if (onPositionChanged != null) {
            onPositionChanged.accept(position);
        }
    }

    @Override
    public void renderBelow(float alphaMult) {
        if (onRenderBelow != null) {
            onRenderBelow.accept(alphaMult);
        }
    }

    @Override
    public void render(float alphaMult) {
        if (onRender != null) {
            onRender.accept(alphaMult);
        }
    }

    @Override
    public void advance(float amount) {
        if (onAdvance != null) {
            onAdvance.accept(amount);
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        if (onProcessInput != null) {
            onProcessInput.accept(events);
        }
    }

    @Override
    public void buttonPressed(Object buttonId) {
        if (onButtonPressed != null) {
            onButtonPressed.accept(buttonId);
        }
    }
}