package DeCell.UI.Backend.Components.Dialogues;

import DeCell.UI.Backend.Components.ColorPicker.Adapters.RgbColorPickerAdapter;
import DeCell.UI.Backend.Components.ColorPicker.ColorPickerV2;
import DeCell.UI.Backend.Components.DialougeButtonPanel;
import DeCell.UI.Backend.Components.MyButton;
import DeCell.UI.Backend.DataPair;
import DeCell.UI.Backend.UIElement;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class ColorPickerV2Dialogue implements Dialogueable<ColorPickerV2, Color> {
    @Override
    public void popup(MyButton btn, List<UIElement<?, ?>> UIElements, Color col, Consumer<ColorPickerV2> onClose, DataPair... externalData) {
        new DialougeButtonPanel
                .Builder(ColorPickerV2.sizeRect.w, ColorPickerV2.sizeRect.h,
                btn
        ).withCharlie().build(UIElements)
                .showCloseButton(false)
                .addToInternalData(externalData)
                .addToInternalData("externalData", externalData) // need to set these accessable by the inner element as well
                .addToInternalData("on_close", onClose)
                .addToInternalData("col", col)
                .setOnUIOpen(
                        (_panel, _dialogue, __UIElements) ->
                        {
                            Color c = _dialogue.<Color>getFromInternal("col");
                            ColorPickerV2 picker = new ColorPickerV2.Builder().build(_panel).setAdapter(new RgbColorPickerAdapter())
                                    .inMid()
                                    .setColor(c)
                                    .addToInternalData("dialogue", _dialogue)
                                    .addToInternalData(_dialogue.getFromInternal("externalData"))
                                    .setOnChange(s -> {
                                        s.<DialougeButtonPanel>getFromInternal("dialogue")
                                                .addToInternalData("out", s);
                                    });
                            _dialogue.addToInternalData("out", picker);
                        }
                ).setOnUIClose(_dialogue ->
                        _dialogue.<Consumer<ColorPickerV2>>getFromInternal("on_close").accept(_dialogue.getFromInternal("out"))
                );
    }
}
