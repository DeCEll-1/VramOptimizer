package DeCell.UI;

import DeCell.UI.Backend.Components.MyPanel;
import DeCell.UI.Backend.Plugins.PanelPlugin;
import DeCell.UI.Backend.UIContainer;
import DeCell.UI.Backend.UIElement;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WrapperPanelPlugin extends PanelPlugin {
    public List<UIElement<?, ?>> ActiveUIElements = new ArrayList<>();
    private final List<UIElement<?, ?>> UIElements = new ArrayList<>();

    private Consumer<MyPanel> init;

    public WrapperPanelPlugin setInit(Consumer<MyPanel> i) {
        this.init = i;
        return this;
    }

    @Override
    public void init(UIContainer<?, CustomPanelAPI> _p) {
        MyPanel parent = new MyPanel(_p.u).addTo(UIElements).setIgnoreEvents(true);
        init.accept(parent);
    }

    @Override
    public void advance(float amount) {
        for (UIElement<?, ?> element : ActiveUIElements) {
            element.advance(amount);
        }

        if (!UIElements.isEmpty()) {
            ActiveUIElements.addAll(UIElements);
            UIElements.clear();
        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        // need to recreate as the game destroys mouse inputs for stuff like buttons after consuming them
        InputEventAPICreator.discoverEventClass(events);
        List<InputEventAPI> zaza = InputEventAPICreator.createImmediateEvents();
        for (UIElement<?, ?> element : ActiveUIElements) {
            element.processInput(zaza);
        }
    }
}
