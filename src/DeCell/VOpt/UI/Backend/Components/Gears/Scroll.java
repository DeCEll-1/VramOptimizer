package DeCell.VOpt.UI.Backend.Components.Gears;

import DeCell.VOpt.UI.Backend.UIElement;
import com.fs.starfarer.api.input.InputEventAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Scroll {
    private static final float scrollDelta = 2f;
    private static final float maxAccelerationMultiplier = 8f;
    private static final long accelerationThresholdMS = 100;

    private long lastScrollEventNanos = 0;
    private long lastScrollTimeMs = 0; // Tracks the absolute system time of the last scroll action

    private final List<Consumer<Float>> listeners = new ArrayList<>();

    public void addScrollListener(Consumer<Float> listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    public void onHover(UIElement<?, ?> el, List<InputEventAPI> events) {
        if (!el.rect().containsMouse()) {
            return;
        }

        for (InputEventAPI event : events) {
            if (event.isConsumed() || !event.isMouseScrollEvent())
                continue;

            int wheelDelta = event.getEventValue();
            if (wheelDelta == 0)
                continue;

            event.consume();

            long currentTimeMs = System.currentTimeMillis();
            long timeSinceLastScroll = currentTimeMs - lastScrollTimeMs;
            lastScrollTimeMs = currentTimeMs;

            float currentScrollDelta = scrollDelta;

            if (timeSinceLastScroll < accelerationThresholdMS && timeSinceLastScroll > 0) {
                float speedFactor = (float) accelerationThresholdMS / timeSinceLastScroll;
                float multiplier = Math.min(speedFactor, maxAccelerationMultiplier);
                currentScrollDelta *= multiplier;
            }

            if (wheelDelta < 0)
                currentScrollDelta = -currentScrollDelta;

            for (Consumer<Float> listener : listeners)
                listener.accept(currentScrollDelta);
        }
    }

}
