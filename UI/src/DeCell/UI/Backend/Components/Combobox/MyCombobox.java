package DeCell.UI.Backend.Components.Combobox;

import DeCell.UI.Backend.BaseBuilder;
import DeCell.UI.Backend.Components.MyButton;
import DeCell.UI.Backend.Components.MyPanel;
import DeCell.UI.Backend.Renderable.RenderableHandlerPlugin;
import DeCell.UI.Backend.UIContainer;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CutStyle;
import com.fs.starfarer.api.ui.UIComponentAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static DeCell.UI.Backend.DataPair.pair;

public class MyCombobox extends UIContainer<MyCombobox, UIComponentAPI> {

    private MyButton button;
    private MyPanel container;
    private boolean listOpen = false;

    public MyCombobox(MyPanel _parent, MyButton _btn) {
        super(_parent.u);
        _parent.addElement(this);
        this.button = _btn;
        this.parent = _parent;
        button.addOnMouseDown(this::click);
    }

    public MyCombobox setOnChange(BiConsumer<MyCombobox, ComboboxElement> onChange) {
        this.onChange = onChange;
        return this;
    }

    private void click(MyButton b) {
        if (!listOpen)
            openList();
        else
            closeList();
    }

    private List<ComboboxElement> elements = new ArrayList<>();
    protected BiConsumer<MyCombobox, ComboboxElement> onChange;

    public MyCombobox addItem(ComboboxElement e) {
        elements.add(e);
        return this;
    }

    private void openList() {
        listOpen = true;
        float w = Math.max(elements.stream().mapToInt(s -> s.text.length() * 7).max().orElse((int) button.w()), parent.w());
        float h = elements.size() * 26;

        container = new MyPanel.Builder(w, h)
                .setPlugin(new RenderableHandlerPlugin())
                .build(this.<MyPanel>getParent())
                .inBL(0, -h).update();

        float itemHeight = 26;
        float paddedHeight = itemHeight + 2; // 2 pixel padding

        for (int i = 0; i < elements.size(); i++) {
            ComboboxElement element = elements.get(i);


            CutStyle style = i == elements.size() - 1 ? CutStyle.BOTTOM : CutStyle.NONE;
            float topPadding = i * paddedHeight + 2;
            new MyButton.Builder(element.text, w, itemHeight, container)
                    .setStyle(Alignment.MID, style).build()
                    .inTL(new Vector2f(0, topPadding))
                    .setCustomData(element.data)
                    .addToInternalData(pair("index", i)) // you know java this pair thing would be a lot better
                    // İF YOU HAD GLOBAL STATİCS
                    .addOnMouseDown(b ->
                            setIndex(b.getFromInternal("index"))
                    );
        }
    }

    public MyCombobox setIndex(int index) {
        if (index < 0 || index >= elements.size()) {
            return this;
        }
        ComboboxElement element = elements.get(index);
        if (button != null && button.u != null) {
            button.setText(element.text);
            button.setCustomData(element.data);
        }
        if (onChange != null) {
            onChange.accept(this, element);
        }
        if (listOpen) {
            closeList();
        }
        return this;
    }

    private void closeList() {
        listOpen = false;
        container.markForDeletion();
    }

    public static class Builder extends BaseBuilder<Builder> {

        public static MyCombobox build(float w, float h, String btnBuilder, MyPanel p) {
            MyPanel parent = new MyPanel.Builder(w, h).build(p);

            MyButton button = new MyButton.Builder(btnBuilder, w, h, parent).setStyle(Alignment.MID, CutStyle.TOP).build().inTL(0, 0);
            return new MyCombobox(parent, button);
        }
    }

}
