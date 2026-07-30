package DeCell.UI.Backend.Components;

import DeCell.UI.Backend.BaseBuilder;
import DeCell.UI.Backend.UIContainer;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CutStyle;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class MyButton extends UIContainer<MyButton, UIComponentAPI> {

    private ButtonAPI btn;

    public ButtonAPI getButton() {
        return btn;
    }

    private MyButton(String text, float width, float height, float pad, MyTooltip _parent) {
        super(_parent.u);
        _parent.addElementToFirst(this);
        this.parent = _parent;
        btn = _parent.addButton(text, null, width, height, pad);
    }

    private MyButton(String text, Color base, Color bg,
                     float width, float height, float pad, MyTooltip _parent) {
        super(_parent.u);
        _parent.addElementToFirst(this);
        this.parent = _parent;
        btn = _parent.addButton(text, null, base, bg, width, height, pad);
    }

    private MyButton(String text, Color base, Color bg,
                     Alignment align, CutStyle style,
                     float width, float height, float pad, MyTooltip _parent) {
        super(_parent.u);
        _parent.addElementToFirst(this);
        this.parent = _parent;
        btn = _parent.addButton(text, null, base, bg, align, style, width, height, pad);
    }

    private MyButton(String text, Alignment align, CutStyle style,
                     float width, float height, float pad, MyTooltip parent) {
        this(text, Misc.getButtonTextColor(), Misc.getDarkPlayerColor(),
                align, style, width, height, pad, parent);
    }

    public boolean wasClickedLastFrame() {
        return wasClickedLastFrame;
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
    }

    //#region button functions

    public MyButton setShortcut(int key, boolean putLast) {
        btn.setShortcut(key, putLast);
        return this;
    }

    public MyButton setEnabled(boolean enabled) {
        btn.setEnabled(enabled);
        return this;
    }

    public MyButton setButtonPressedSound(String buttonPressedSound) {
        btn.setButtonPressedSound(buttonPressedSound);
        return this;
    }

    public MyButton setMouseOverSound(String mouseOverSound) {
        btn.setMouseOverSound(mouseOverSound);
        return this;
    }

    public MyButton setButtonDisabledPressedSound(String buttonDisabledPressedSound) {
        btn.setButtonDisabledPressedSound(buttonDisabledPressedSound);
        return this;
    }

    public MyButton setChecked(boolean checked) {
        btn.setChecked(checked);
        return this;
    }

    public MyButton highlight() {
        btn.highlight();
        return this;
    }

    public MyButton unhighlight() {
        btn.unhighlight();
        return this;
    }

    public MyButton setHighlightBrightness(float highlightBrightness) {
        btn.setHighlightBrightness(highlightBrightness);
        return this;
    }

    public MyButton setQuickMode(boolean quickMode) {
        btn.setQuickMode(quickMode);
        return this;
    }

    public MyButton setClickable(boolean clickable) {
        btn.setClickable(clickable);
        return this;
    }

    public MyButton setGlowBrightness(float glowBrightness) {
        btn.setGlowBrightness(glowBrightness);
        return this;
    }

    public MyButton setText(String text) {
        btn.setText(text);
        return this;
    }

    public MyButton setSkipPlayingPressedSoundOnce(boolean skipPlayingPressedSoundOnce) {
        btn.setSkipPlayingPressedSoundOnce(skipPlayingPressedSoundOnce);
        return this;
    }

    public MyButton setHighlightBounceDown(boolean b) {
        btn.setHighlightBounceDown(b);
        return this;
    }

    public MyButton setShowTooltipWhileInactive(boolean showTooltipWhileInactive) {
        btn.setShowTooltipWhileInactive(showTooltipWhileInactive);
        return this;
    }

    public MyButton setRightClicksOkWhenDisabled(boolean rightClicksOkWhenDisabled) {
        btn.setRightClicksOkWhenDisabled(rightClicksOkWhenDisabled);
        return this;
    }

    public MyButton setFlashBrightness(float flashBrightness) {
        btn.setFlashBrightness(flashBrightness);
        return this;
    }

    public MyButton flash(boolean withSound, float in, float out) {
        btn.flash(withSound, in, out);
        return this;
    }

    public MyButton flash(boolean withSound) {
        btn.flash(withSound);
        return this;
    }

    public MyButton flash() {
        btn.flash();
        return this;
    }

    public MyButton setPerformActionWhenDisabled(boolean performActionWhenDisabled) {
        btn.setPerformActionWhenDisabled(performActionWhenDisabled);
        return this;
    }

    public MyButton setCustomData(Object customData) {
        btn.setCustomData(customData);
        return this;
    }
    //#endregion

    public static class Builder extends BaseBuilder<Builder> {
        private final String text;
        private float w;
        private float h;
        private MyTooltip parent = null;
        private float pad = 0f;
        private Color baseColor = null;
        private Color bgColor = null;
        private Alignment alignment = null;
        private CutStyle cutStyle = null;

        public Builder(String text, float width, float height, MyTooltip parent) {
            this.text = text;
            this.w = width;
            this.h = height;
            this.parent = parent;
        }

        public Builder(String text, float width, float height, MyPanel parent) {
            this.text = text;
            this.w = width;
            this.h = height;
            this.parent = new MyTooltip.Builder(width, height, parent).build();
        }

        public Builder(String text, MyTooltip parent) {
            this(text, 0, 0, parent);
        }

        public Builder(String text, MyPanel parent) {
            this(text, 0, 0, parent);
        }

        public Builder setParent(MyPanel panel) {
            this.parent = new MyTooltip.Builder(this.w, this.h, panel).build();
            return this;
        }

        public boolean havesParent() {
            return parent != null;
        }

        public Builder setParent(MyTooltip panel) {
            this.parent = panel;
            return this;
        }

        public Builder setPad(float pad) {
            this.pad = pad;
            return this;
        }

        public Builder setShape(float w, float h) {
            this.w = w;
            this.h = h;
            return this;
        }

        public Builder setColors(Color baseColor, Color bgColor) {
            this.baseColor = baseColor;
            this.bgColor = bgColor;
            return this;
        }

        public Builder setStyle(Alignment alignment, CutStyle cutStyle) {
            this.alignment = alignment;
            this.cutStyle = cutStyle;
            return this;
        }

        public MyButton build() {
            MyButton btn = null;
            if (baseColor != null && bgColor != null && alignment != null && cutStyle != null && btn == null) {
                btn = new MyButton(text, baseColor, bgColor, alignment, cutStyle, w, h, pad, parent);
            }

            if (baseColor != null && bgColor != null && btn == null) {
                btn = new MyButton(text, baseColor, bgColor, w, h, pad, parent);
            }

            if (alignment != null && cutStyle != null && btn == null) {
                btn = new MyButton(text, alignment, cutStyle, w, h, pad, parent);
            }

            if (btn == null)
                btn = new MyButton(text, w, h, pad, parent);
            btn.getButton().getPosition().inTR(0, 0);
            btn.name = "Button_" + btn.name; // we are doing this here cuz doing it above would suck lowkey
            btn.parent.name = "Button_parent_" + btn.name;
            return btn;
        }

    }
}
