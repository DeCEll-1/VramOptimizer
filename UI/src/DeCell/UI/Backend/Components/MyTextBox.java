package DeCell.UI.Backend.Components;

import DeCell.UI.Backend.BaseBuilder;
import DeCell.UI.Backend.UIContainer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.TextFieldAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.function.*;
import java.util.regex.Pattern;

import static DeCell.UI.Reflections.invokeMethod;


public class MyTextBox extends UIContainer<MyTextBox, UIComponentAPI> {
    //#region constants
    public static final float defaultHeight = 28f;
    public static final String defaultFont = "graphics/fonts/insignia21LTaa.fnt";
    private boolean ignoreNextTextChangeTrigger = false;
    //#endregion
    private TextFieldAPI textField;

    public TextFieldAPI getTExtField() {
        return textField;
    }

    public MyTextBox(float w, float pad, MyTooltip _parent) {
        super(_parent.u);
        this.parent = _parent;
        parent.addElement(this);
        textField = _parent.addTextField(w, pad);
    }

    public MyTextBox(float w, String font, float pad, MyTooltip _parent) {
        super(_parent.u);
        this.parent = _parent;
        parent.addElement(this);
        textField = _parent.addTextField(w, font, pad);
    }

    public MyTextBox(float w, float h, String font, float pad, MyTooltip _parent) {
        super(_parent.u);
        this.parent = _parent;
        parent.addElement(this);
        textField = _parent.addTextField(w, h, font, pad);
    }

    protected boolean hadFocusLastFrame = false;
    protected String textLastFrame = "";

    @Override
    public void processInput(List<InputEventAPI> events) {
        super.processInput(events);

        boolean currentlyHavesFocus = this.textField.hasFocus();

        if (currentlyHavesFocus && !hadFocusLastFrame)
            // we captured focus
            if (onFocusEnter != null)
                onFocusEnter.accept(this);

        if (!currentlyHavesFocus && hadFocusLastFrame)
            // we lost focus
            if (onFocusExit != null)
                onFocusExit.accept(this);

        hadFocusLastFrame = currentlyHavesFocus;

        String currentText = this.textField.getText();

        if (Objects.equals(currentText, textLastFrame))
            return;

        if (currentText.isEmpty())
            currentText = "";

        boolean isValid =
                (validationPattern == null || validationPattern.matcher(currentText).matches())
                        && (validator == null || validator.test(currentText));

        if (!isValid) {
            revertText();
            revertText();
            return;
        }

        textLastFrame = currentText;

        if (onTextChange != null && !this.ignoreNextTextChangeTrigger) {
            onTextChange.accept(this);
        }
        this.ignoreNextTextChangeTrigger = false;
    }

    private void revertText() {
        this.textField.setText(textLastFrame);
        this.setText(textLastFrame, false);
        Global.getSoundPlayer().playUISound("ui_typer_buzz", 1, 2);
    }

    private Consumer<MyTextBox> onFocusEnter;
    private Consumer<MyTextBox> onFocusExit;
    private Consumer<MyTextBox> onTextChange;
    private Predicate<String> validator = text -> true;

    protected Pattern validationPattern = null;

    public MyTextBox setOnFocusEnter(Consumer<MyTextBox> onFocusEnter) {
        this.onFocusEnter = onFocusEnter;
        return this;
    }

    public MyTextBox setOnFocusExit(Consumer<MyTextBox> onFocusExit) {
        this.onFocusExit = onFocusExit;
        return this;
    }

    public MyTextBox setOnTextChange(Consumer<MyTextBox> onTextChange) {
        this.onTextChange = onTextChange;
        return this;
    }

    public MyTextBox setTextValidator(Predicate<String> validator) {
        this.validator = validator;
        return this;
    }

    public MyTextBox setValidationRegex(String regex) {
        if (regex == null || regex.isEmpty())
            this.validationPattern = null;
        else
            this.validationPattern = Pattern.compile(regex);
        return this;
    }

    public MyTextBox setValidationRegex(Pattern regex) {
        this.validationPattern = regex;
        return this;
    }


    //#region tb functions


    public boolean isMinimalMode() {
        return (boolean) invokeMethod("isMinimalMode", this.u);
    }

    public MyTextBox setMinimalMode(boolean isMinimal) {
        invokeMethod("setMinimalMode", this.u, new Class[]{boolean.class}, isMinimal);
        return this;
    }

    public MyTextBox setPad(float pad) {
        this.textField.setPad(pad);
        return this;
    }

    public MyTextBox setMidAlignment() {
        this.textField.setMidAlignment();
        return this;
    }

    public MyTextBox setColor(Color color) {
        this.textField.setColor(color);
        return this;
    }

    public MyTextBox setBgColor(Color bgColor) {
        this.textField.setBgColor(bgColor);
        return this;
    }

    public String getText() {
        return this.textField.getText();
    }

    public MyTextBox setText(String string, boolean triggerOnChange) {
        this.textField.setText(string);
        this.ignoreNextTextChangeTrigger = !triggerOnChange;
        return this;
    }

    public MyTextBox setLimitByStringWidth(boolean limitByStringWidth) {
        this.textField.setLimitByStringWidth(limitByStringWidth);
        return this;
    }

    public MyTextBox setMaxChars(int maxChars) {
        this.textField.setMaxChars(maxChars);
        return this;
    }

    public MyTextBox deleteAll() {
        this.textField.deleteAll();
        return this;
    }

    public MyTextBox deleteAll(boolean withSound) {
        this.textField.deleteAll(withSound);
        return this;
    }

    public MyTextBox deleteLastWord() {
        this.textField.deleteLastWord();
        return this;
    }

    public MyTextBox grabFocus() {
        this.textField.grabFocus();
        return this;
    }

    public MyTextBox grabFocus(boolean playSound) {
        this.textField.grabFocus(playSound);
        return this;
    }

    public MyTextBox setUndoOnEscape(boolean undoOnEscape) {
        this.textField.setUndoOnEscape(undoOnEscape);
        return this;
    }

    public MyTextBox setHandleCtrlV(boolean handleCtrlV) {
        this.textField.setHandleCtrlV(handleCtrlV);
        return this;
    }

    public MyTextBox setBorderColor(Color borderColor) {
        this.textField.setBorderColor(borderColor);
        return this;
    }

    public MyTextBox setVerticalCursor(boolean verticalCursor) {
        this.textField.setVerticalCursor(verticalCursor);
        return this;
    }

    public MyTextBox hideCursor() {
        this.textField.hideCursor();
        return this;
    }

    public MyTextBox showCursor() {
        this.textField.showCursor();
        return this;
    }

    public boolean hasFocus() {
        return textField.hasFocus();
    }

    //#endregion

    // the textbox will need:
    // on text change
    public static class Builder extends BaseBuilder<Builder> {


        private final MyTooltip parent;
        private final float w;
        private final float h;
        private float pad = 0;
        private String font = defaultFont;

        // Width is required, so we pass it into the constructor
        public Builder(float w, float h, MyPanel parent) {
            this.parent = new MyTooltip.Builder(w, h, parent).build();
            this.h = h;
            this.w = w;
        }

        public Builder(float w, MyPanel parent) {
            this(w, defaultHeight, parent);
        }

        public Builder setPad(float pad) {
            this.pad = pad;
            return this;
        }

        public Builder setFont(String font) {
            this.font = font;
            return this;
        }

        public MyTextBox build() {
            MyTextBox tb = new MyTextBox(w, h, font, pad, parent);
            tb.textField.getPosition().inTL(0, 0);
            tb.name = "TextBox_" + tb.name;
            // since the parent is a tooltip they dont get names by default
            tb.parent.name = "TextBox_parent_" + tb.parent.name;
            return tb;
        }

    }
}
