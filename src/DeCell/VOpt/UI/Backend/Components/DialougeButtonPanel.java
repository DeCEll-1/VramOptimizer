package DeCell.VOpt.UI.Backend.Components;

import DeCell.VOpt.TriConsumer;
import DeCell.VOpt.UI.Backend.Components.Charlie.CharlieElement;
import DeCell.VOpt.UI.Backend.Components.Charlie.Openable;
import DeCell.VOpt.UI.Backend.Components.Charlie.OpenableListener;
import DeCell.VOpt.UI.Backend.Plugins.LambdaUIPanelPlugin;
import DeCell.VOpt.UI.Backend.Plugins.MultiPluginHandler;
import DeCell.VOpt.UI.Backend.Renderable.BorderRenderable;
import DeCell.VOpt.UI.Backend.Renderable.RenderableHandlerPlugin;
import DeCell.VOpt.UI.Backend.UIContainer;
import DeCell.VOpt.UI.Backend.UIElement;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.Consumer;

import static DeCell.VOpt.Misc.getCurrentTab;

public class DialougeButtonPanel extends UIContainer<DialougeButtonPanel, CustomPanelAPI> implements Openable {

    private MyButton button;
    private MyPanel container;
    private boolean isOpen = false;

    public DialougeButtonPanel(float w, float h, MyButton _button, CharlieElement _parent) {
        super(_parent.u.createCustomPanel(w, h, null));
        _parent.addOpenable(this);
        this.parent = _parent;
        this.button = _button;

        this.name = "DialougeButtonPanel_" + this.name;
        this.button.name = "DialougeButtonPanel_button_" + this.button.name;
        this.parent.addElement(this);
        // we can set this things parents name as its parent will never be a pre existing panel
        // and nothing will touch it
        this.parent.name = "DialougeButtonPanel_parent_" + _parent.name;
        setIgnoreEvents(true);

        this.button.addOnMouseDown(this::click);
    }

    public DialougeButtonPanel(float w, float h, MyButton _button, MyPanel _parent) {
        super(_parent.u.createCustomPanel(w, h, null));
        _parent.addComponent(this.u);
        _parent.addElement(this);
        this.parent = _parent;
        this.button = _button;

        // Update names to match the convention of the first constructor
        this.name = "DialougeButtonPanel_" + this.name;
        this.button.name = "DialougeButtonPanel_button_" + _button.name;
        this.parent.name = "DialougeButtonPanel_parent_" + _parent.name;

        this.button.addOnMouseDown(this::click);
    }


    private void createContainer() {
        activeUIElements.clear();
        this.container = new MyPanel.Builder(this.w(), this.h())
                .setPlugin(new MultiPluginHandler() // main window
                        .add(new RenderableHandlerPlugin()
                                .addBelow(BorderRenderable.createBorder2())).add(new LambdaUIPanelPlugin()
                                .onProcessInput(events -> {
                                            for (InputEventAPI e : events) {
                                                if (e.isKeyDownEvent() && e.getEventValue() == Keyboard.KEY_ESCAPE) {
                                                    this.click();
                                                    e.consume();
                                                }
                                            }
                                            events.forEach(InputEventAPI::consume);
                                        }
                                )
                        )
                )
                .build(this.u).setConsumeEvents(false).inBL(0, 0).addTo(UIElements);
        this.container.setParent(this);


        if (this.showCloseButton)
            new MyButton.Builder("X", 24, 24, container).build().inTR(0, 0);
    }

    public DialougeButtonPanel(CustomPanelAPI underlying) {
        super(underlying);
    }

    protected TriConsumer<MyPanel, DialougeButtonPanel, List<UIElement<?, ?>>> onUIOpen;
    protected Consumer<DialougeButtonPanel> onUIClose;

    @Override
    public void markForDeletion() {
        super.markForDeletion();
    }

    private void click(MyButton b) {
        click();
    }

    private void click() {

        if (isOpen) {
            container.markForDeletion();
            if (onUIClose != null)
                onUIClose.accept(this);
        } else {
            createContainer();
            if (onUIOpen != null)
                onUIOpen.accept(container, this, UIElements);
        }
        isOpen = !isOpen;
        if (listener != null)
            listener.onOpenStateChanged(isOpen);
    }

    public DialougeButtonPanel setOnUIOpen(TriConsumer<MyPanel, DialougeButtonPanel, List<UIElement<?, ?>>> onClick) {
        this.onUIOpen = onClick;
        return this;
    }

    public DialougeButtonPanel setOnUIClose(Consumer<DialougeButtonPanel> onClick) {
        this.onUIClose = onClick;
        return this;
    }

    private boolean showCloseButton = true;

    public DialougeButtonPanel showCloseButton(boolean show) {
        this.showCloseButton = show;
        return this;
    }

    public DialougeButtonPanel setCloseButton(MyButton btn) {
        btn.addOnMouseDown(this::click);
        return this;
    }

    private OpenableListener listener;

    @Override
    public void setOnOpenClose(OpenableListener _listener) {
        this.listener = _listener;
    }

    public static class Builder {
        private final float w;
        private final float h;
        private final MyButton button;
        private boolean charlie = false;

        public Builder(float width, float height, MyButton button) {
            this.w = width;
            this.h = height;
            this.button = button;
        }

        public Builder(float width, float height, MyButton.Builder button) {
            this.w = width; // TODO: probably implement more *.Builder ones
            this.h = height;
            this.button = button.build();
        }

        public Builder withCharlie() {
            charlie = true;
            return this;
        }

        public DialougeButtonPanel build(List<UIElement<?, ?>> UIElements) {
            UIPanelAPI currentTab = (UIPanelAPI) getCurrentTab();
            MyPanel popupContainer = new MyPanel.Builder(currentTab.getPosition().getWidth(), currentTab.getPosition().getHeight()).build(currentTab)
                    .addTo(UIElements);
            if (charlie) {
                CharlieElement charlie = new CharlieElement(popupContainer);
                return new DialougeButtonPanel(w, h, button, charlie).inMid();
            }

            return new DialougeButtonPanel(w, h, button, popupContainer);
        }
    }
}
