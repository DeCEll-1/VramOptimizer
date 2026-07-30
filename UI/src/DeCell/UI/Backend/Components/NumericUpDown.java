package DeCell.UI.Backend.Components;

import DeCell.UI.Backend.BaseBuilder;
import DeCell.UI.Backend.Components.Gears.Scroll;
import DeCell.UI.Backend.Components.Gears.UpDownArrow;
import DeCell.UI.Backend.UIContainer;
import DeCell.UI.DeCellUI;
import DeCell.UI.Misc;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CutStyle;
import com.fs.starfarer.api.ui.UIComponentAPI;

import java.util.List;
import java.util.function.Consumer;

import static DeCell.UI.Backend.DataPair.pair;
import static DeCell.UI.DeCellUI.Patterns.decimalWithMaxDecimalPlaces;


public class NumericUpDown extends UIContainer<NumericUpDown, UIComponentAPI> {

    private final MyTextBox tb;
    private final MyButton up;
    private final MyButton down;
    private final UpDownArrow upDownArrowGear;
    // TODO: allow users to hold down left click on the buttons for changing the value
    // TODO: add sounds for inputs

    public NumericUpDown(MyPanel _container, MyTextBox _tb, MyButton _up, MyButton _down) {
        super(_container.u);
        _container.addElement(this);
        this.name = "NumericUpDown_" + this.name;
        this.parent = _container;
        this.tb = _tb;
        this.tb.name = "NumericUpDown_tb_" + this.tb.name;
        this.up = _up;
        this.up.name = "NumericUpDown_up_" + this.up.name;
        this.down = _down;
        this.down.name = "NumericUpDown_down_" + this.down.name;

        tb.setValidationRegex(DeCellUI.Patterns.DECIMAL_ONLY);
        tb.setTextValidator((t) -> {
            double newVal = getValueForString(t);
            if (newVal < minValue || newVal > maxValue)
                return false;
            return true;
        });

        tb.setOnTextChange((text) -> {
            if (onChange != null) onChange.accept(this);
        });

        this.setValue(0, false);

        up.addOnMouseDown(this::buttonUpdate).addToInternalData(pair("type", UpDownArrow.ButtonType.UP));
        down.addOnMouseDown(this::buttonUpdate).addToInternalData(pair("type", UpDownArrow.ButtonType.DOWN));


        Scroll scrollGear = new Scroll();
        tb.addOnHover(scrollGear::onHover);
        up.addOnHover(scrollGear::onHover);
        down.addOnHover(scrollGear::onHover);
        scrollGear.addScrollListener(this::onScrollEnd);

        this.upDownArrowGear = new UpDownArrow(intervalMin, intervalMax, initialDelay);
        upDownArrowGear.addUpDownListener(s -> {
            switch (s) {
                case UP -> up();
                case DOWN -> down();
            }
        });

        setAmountOfDecimalPlaces(2);
    }

    private void onScrollEnd(Float val) {
        if (val > 0)
            up();
        else
            down();
    }

    private void buttonUpdate(MyButton button) {
        UpDownArrow.ButtonType type = button.getFromInternal("type");

        switch (type) {
            case UP -> up();
            case DOWN -> down();
        }
    }

    private void up() {
        this.setValue(this.getValue() + stepSize, true);
    }

    private void down() {
        this.setValue(this.getValue() - stepSize, true);
    }


    //#region arrow keys
    private static final float initialDelay = 0.4f;
    private static final float intervalMin = 0;
    private static final float intervalMax = 0.4f;

    @Override
    public void processInput(List<InputEventAPI> events) {
        if (!tb.hasFocus()) {
            return;
        }

        upDownArrowGear.advance(events);
    }

    private void handleArrowKeys(boolean upPressed) {
        if (upPressed) up();
        else down();
    }
    //#endregion

    //#region getter setters
    private float stepSize = 1f;

    public NumericUpDown setStepSize(float _stepSize) {
        this.stepSize = _stepSize;
        return this;
    }

    public MyTextBox getTextBox() {
        return this.tb;
    }

    private double maxValue = Double.MAX_VALUE;
    private double minValue = -Double.MAX_VALUE; // MIN_VALUE isnt actually the minimum value, thanks java

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public NumericUpDown setMinMax(double min, double max) {
        this.minValue = min;
        this.maxValue = max;
        int maxChars = Math.max(Double.toString(this.maxValue).length(), Double.toString(this.minValue).length());
        this.tb.setMaxChars(maxChars);
        this.tb.setLimitByStringWidth(false);
        return this;
    }
    //#endregion

    //#region value handling
    private int amountOfDecimalPlaces;

    public NumericUpDown setAmountOfDecimalPlaces(int zaza) {
        this.amountOfDecimalPlaces = zaza;
        this.tb.setValidationRegex(decimalWithMaxDecimalPlaces(amountOfDecimalPlaces));
        return this;
    }

    public NumericUpDown setValue(double val, boolean triggerOnChange) {
        val = Misc.clamp(val, minValue, maxValue);
        String text = String.format("%." + amountOfDecimalPlaces + "f", val);
        this.tb.setText(text, false);

        if (onChange != null && triggerOnChange) {
            onChange.accept(this);
        }
        return this;
    }

    public NumericUpDown setValueNormalised(double val) {
        setValue(val * (maxValue - minValue) + minValue, false);
        return this;
    }

    public double getValue() {
        return getValueForString(this.tb.getText());
    }

    public double getValueNormalised() {
        return (getValue() - minValue) / (maxValue - minValue);
    }

    private double getValueForString(String text) {
        if (text == null || text.isEmpty())
            text = "0";
        return Double.parseDouble(text);
    }
    //#endregion

    private Consumer<NumericUpDown> onChange = null;

    public NumericUpDown setOnChange(Consumer<NumericUpDown> listener) {
        this.onChange = listener;
        return this;
    }

    // this wwill be a regular ass winforms numeric up down, it must have 2 buttons for up down
    // properties such as:
    // min, max, step size, end floating points, value changed event, if the button is getting hold keep increase/decreasing it
    // custom regex match, shift arrow keys bla bla allat jazz
    // requred components sis a text box and a button
    public static class Builder extends BaseBuilder<Builder> {

        //#region constants
        public final static float xPadding = 0f;
        public final static float yPadding = 2f;

        public final static float buttonWidth = 16;
        public final static float buttonHeight = MyTextBox.defaultHeight / 2f;


        //#endregion

        // width and height includes the buttons
        public NumericUpDown build(float w, MyPanel parent) {
            final float tbWidth = w - (xPadding + buttonWidth);
            final float tbHeight = MyTextBox.defaultHeight;

            MyPanel container = new MyPanel.Builder(w, tbHeight).build(parent);

            MyTextBox tb = new MyTextBox.Builder(tbWidth, tbHeight, container)
                    .build().inTL(0, 0);

            MyButton upButton = new MyButton.Builder("", buttonWidth, buttonHeight, container)
                    .setStyle(Alignment.MID, CutStyle.TOP)
                    .build().inTR(0, 0);

            MyButton downButton = new MyButton.Builder("", buttonWidth, buttonHeight, container)
                    .setStyle(Alignment.MID, CutStyle.BOTTOM)
                    .build().inBR(0, 0);

            return new NumericUpDown(container, tb, upButton, downButton);
        }

    }
}
