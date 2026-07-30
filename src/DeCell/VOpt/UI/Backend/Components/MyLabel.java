package DeCell.VOpt.UI.Backend.Components;

import DeCell.VOpt.UI.Backend.UIContainer;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;

import java.awt.*;

public class MyLabel extends UIContainer<MyLabel, UIComponentAPI> {
    private LabelAPI label;

    public LabelAPI getLabel() {
        return label;
    }

    public MyLabel(LabelAPI _label, MyTooltip _parent) {
        super(_parent.u);
        this.parent = _parent;
        this.label = _label;
        this.label.getPosition().inTL(0, 0);
    }

    //#region label stuff

    public MyLabel setHighlight(int start, int end) {
        this.label.setHighlight(start, end);
        return this;
    }

    public MyLabel highlightFirst(String substring) {
        this.label.highlightFirst(substring);
        return this;
    }

    public MyLabel highlightLast(String substring) {
        this.label.highlightLast(substring);
        return this;
    }

    public MyLabel setHighlight(String... substrings) {
        this.label.setHighlight(substrings);
        return this;
    }

    public MyLabel unhighlightIndex(int index) {
        this.label.unhighlightIndex(index);
        return this;
    }

    public MyLabel setHighlightColor(Color color) {
        this.label.setHighlightColor(color);
        return this;
    }

    public MyLabel setHighlightColors(Color... colors) {
        this.label.setHighlightColors(colors);
        return this;
    }

    public MyLabel setAlignment(Alignment mid) {
        this.label.setAlignment(mid);
        return this;
    }

    public MyLabel setText(String text) {
        float w = label.computeTextWidth(text);
        float h = label.computeTextHeight(text);
        this.setSize(w, h);
        this.label.getPosition().setSize(w, h);
        this.label.setText(text);
        return this;
    }

    //#endregion

    public static class Builder {

        private final String text;
        private Color color = null;
        private final MyTooltip parent;

        public Builder(String _text, MyTooltip _parent) {
            this.text = _text;
            this.parent = _parent;
        }

        public Builder(String _text, float w, float h, MyPanel _parent) {
            this(_text, new MyTooltip.Builder(w, h, _parent).build());
        }

        public Builder withColor(Color _c) {
            this.color = _c;
            return this;
        }

        public MyLabel build() {
            // no one EVER will use the padding so i dont care
            MyLabel lbl = null;

            if (color != null)
                lbl = new MyLabel(parent.addPara("", color, 0), parent);
            if (lbl == null)
                lbl = new MyLabel(parent.addPara("", 0), parent);
            lbl.setText(text);

            return lbl;
        }
    }
}

