package DeCell.VOpt.UI.Backend.Components.Gears;

import DeCell.VOpt.ElapsingInterval;
import DeCell.VOpt.Misc;
import Kryz.Tweening.EasingFunctions;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UpDownArrow {
    private final float intervalMin;
    private final float intervalMax;
    private final float initialDelay;

    public UpDownArrow(float _intervalMin, float _intervalMax, float _initialDelay) {
        this.intervalMin = _intervalMin;
        this.intervalMax = _intervalMax;
        this.initialDelay = _initialDelay;
        keyPressInterval = new ElapsingInterval(intervalMin, intervalMax);
    }

    private final ElapsingInterval keyPressInterval;
    private float holdTime = 0;

    private final List<Consumer<ButtonType>> listeners = new ArrayList<>();

    public void addUpDownListener(Consumer<ButtonType> listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    public void advance(List<InputEventAPI> events) {
        float deltaTime = Global.getCombatEngine().getElapsedInLastFrame();
        boolean hasPressedActiveKey = false;
        boolean upPressed = false;

        for (InputEventAPI event : events) {
            if (event.isConsumed() || !event.isKeyboardEvent()) {
                continue;
            }

            int keyCode = event.getEventValue();
            boolean isUpKey = (keyCode == Keyboard.KEY_UP);
            boolean isDownKey = (keyCode == Keyboard.KEY_DOWN);

            if (isUpKey || isDownKey) {
                if (event.isKeyDownEvent()) {
                    handleArrowKeys(isUpKey);
                    holdTime = 0f;
                    keyPressInterval.setElapsed(0f);
                    event.consume();
                }

                hasPressedActiveKey = true;
                if (isUpKey) upPressed = true;
            }
        }

        if (!hasPressedActiveKey) {
            upPressed = Keyboard.isKeyDown(Keyboard.KEY_UP);
            boolean downPressed = Keyboard.isKeyDown(Keyboard.KEY_DOWN);
            hasPressedActiveKey = upPressed || downPressed;
        }

        if (hasPressedActiveKey) {
            holdTime += deltaTime;

            if (holdTime >= initialDelay) {
                keyPressInterval.advance(deltaTime);

                float dynamicInterval = intervalMax - Misc.clamp(EasingFunctions.Linear(holdTime * 0.1f), 0, intervalMax - 0.05f);
                keyPressInterval.setInterval(intervalMin, dynamicInterval);

                if (keyPressInterval.isElapsed()) {
                    handleArrowKeys(upPressed);
                }
            }
        } else {
            keyPressInterval.setInterval(intervalMin, intervalMax);
            keyPressInterval.setElapsed(0f);
            holdTime = 0f;
        }
    }

    private void handleArrowKeys(boolean upPressed) {
        for (Consumer<ButtonType> listener : listeners) {
            listener.accept(upPressed ? ButtonType.UP : ButtonType.DOWN);
        }
    }


    public enum ButtonType {
        UP,
        DOWN
    }
}
