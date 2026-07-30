package DeCell.UI.Backend;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static DeCell.UI.Backend.DataPair.pair;

public abstract class UIElement<T extends UIElement<T, U>, U extends UIComponentAPI> {
    public final U u; // underlying
    public String name = UUID.randomUUID().toString(); // solely for debugging

    protected final List<Consumer<T>> onMouseEnterListeners = new ArrayList<>();
    protected final List<Consumer<T>> onMouseExitListeners = new ArrayList<>();

    protected final List<BiConsumer<T, List<InputEventAPI>>> onHoverListeners = new ArrayList<>();
    protected final List<BiConsumer<T, List<InputEventAPI>>> onMouseDownListeners = new ArrayList<>();
    protected final List<BiConsumer<T, List<InputEventAPI>>> onMouseUpListeners = new ArrayList<>();

    protected boolean wasClickedLastFrame = false;
    protected boolean isDragging = false;

    protected boolean wasHovered = false;
    protected boolean markedForDeletion = false;

    protected UIContainer<?, ? extends UIPanelAPI> parent;
    protected boolean ignoreEvents = false;
    protected boolean consumeEvents = true;
    protected Map<String, Object> internalData = new HashMap<>();

    public UIElement(U underlying) {
        this.u = underlying;
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    private <P extends UIContainer<?, ?>> P parent() {
        return (P) parent;
    }

    //#region getter setters

    public T setIgnoreEvents(boolean ignore) {
        this.ignoreEvents = ignore;
        return this.self();
    }

    public T setConsumeEvents(boolean consumeEvents) {
        this.consumeEvents = consumeEvents;

        return this.self();
    }

    public <P extends UIContainer<?, ?>> P getParent() {
        return this.parent();
    }

    public T setParent(UIContainer<?, ? extends UIPanelAPI> parent) {
        this.parent = parent;
        return this.self();
    }

    public void markForDeletion() {
        this.markedForDeletion = true;
    }

    public boolean isMarkedForDeletion() {
        return this.markedForDeletion;
    }

    public boolean isDragging() {
        return isDragging;
    }

    public T setDragging(boolean dragging) {
        this.isDragging = dragging;
        return this.self();
    }

//#endregion

    public T addToInternalData(String s, Object data) {
        addToInternalData(pair(s, data));
        return this.self();
    }

    public T addToInternalData(DataPair<?>... entries) {
        for (DataPair<?> entry : entries) {
            if (entry != null) {
                if (internalData.get(entry.key) != null)
                    internalData.remove(entry.key);
                internalData.put(entry.key, entry.value);
            }
        }
        return this.self();
    }

    /**
     * @throws ClassCastException if the retrieved object is not assignable to Z
     */
    @SuppressWarnings("unchecked")
    public <Z> Z getFromInternal(String s) {
        return (Z) this.internalData.get(s);
    }

    public void processInput(List<InputEventAPI> events) {
        if (u == null || ignoreEvents) return;
        boolean isMouseOver = this.rect().containsMouse();

        // 1. Fire entry and exit hooks based on positional state tracking
        if (isMouseOver && !wasHovered) {
            for (Consumer<T> listener : onMouseEnterListeners) listener.accept(self());
        } else if (!isMouseOver && wasHovered) {
            for (Consumer<T> listener : onMouseExitListeners) listener.accept(self());
        }

        // 2. Iterate through events queue to process active inputs safely
        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;

            boolean eventInside = this.getPosition().containsEvent(event);

            // Handle hover stream hooks per-event (crucial for frame-perfect scroll deltas)
            if (isMouseOver && !onHoverListeners.isEmpty()) {
                for (BiConsumer<T, List<InputEventAPI>> listener : onHoverListeners) {
                    listener.accept(self(), events);
                }
            }

            // Mouse button down tracking
            if (event.isLMBDownEvent() && eventInside) {
                isDragging = true;
                if (consumeEvents) {
                    event.consume();
                }
                for (BiConsumer<T, List<InputEventAPI>> listener : onMouseDownListeners) {
                    listener.accept(self(), events);
                }
            }

            // Fixes the broken drag loop tracking safely via clean event queuing instead of global pollers
            if (event.isLMBUpEvent() && isDragging) {
                isDragging = false;
                if (consumeEvents) {
                    event.consume();
                }
                for (BiConsumer<T, List<InputEventAPI>> listener : onMouseUpListeners) {
                    listener.accept(self(), events);
                }
            }
        }

        wasHovered = isMouseOver;
    }

    public void advance(float amount) {
        if (this.isMarkedForDeletion() && this.parent != null) {
            this.parent.u.removeComponent(this.u);
        }
    }

    public T update() {
        return this.self();
    }

    public T addTo(List<UIElement<?, ?>> l) {
        l.add(this);
        return this.self();
    }

// --- Fluent Subscription API ---

    // --- Mouse Enter / Exit (Always 1-Argument) ---
    public T addOnMouseEnter(Consumer<T> listener) {
        if (listener != null) this.onMouseEnterListeners.add(listener); return this.self();
    }

    public T addOnMouseExit(Consumer<T> listener) {
        if (listener != null) this.onMouseExitListeners.add(listener); return this.self();
    }

    // --- On Hover Overloads ---
    public T addOnHover(Consumer<T> listener) {
        if (listener != null) this.onHoverListeners.add((element, event) -> listener.accept(element));
        return this.self();
    }

    public T addOnHover(BiConsumer<T, List<InputEventAPI>> listener) {
        if (listener != null) this.onHoverListeners.add(listener);
        return this.self();
    }

    // --- On Mouse Down Overloads ---
    public T addOnMouseDown(Consumer<T> listener) {
        if (listener != null) this.onMouseDownListeners.add((element, event) -> listener.accept(element));
        return this.self();
    }

    public T addOnMouseDown(BiConsumer<T, List<InputEventAPI>> listener) {
        if (listener != null) this.onMouseDownListeners.add(listener);
        return this.self();
    }

    // --- On Mouse Up Overloads ---
    public T addOnMouseUp(Consumer<T> listener) {
        if (listener != null) this.onMouseUpListeners.add((element, event) -> listener.accept(element));
        return this.self();
    }

    public T addOnMouseUp(BiConsumer<T, List<InputEventAPI>> listener) {
        if (listener != null) this.onMouseUpListeners.add(listener);
        return this.self();
    }

//    // --- Unsubscribe API (Optional Safety Feature) ---
//
//    public T removeOnMouseEnter(Consumer<T> listener) {this.onMouseEnterListeners.remove(listener); return this.self();}
//
//    public T removeOnHover(Consumer<T> listener) {this.onHoverListeners.remove(listener); return this.self();}
//
//    public T removeOnMouseExit(Consumer<T> listener) {this.onMouseExitListeners.remove(listener); return this.self();}
//
//    public T removeOnMouseDown(Consumer<T> listener) {this.onMouseDownListeners.remove(listener); return this.self();}
//
//    public T removeOnMouseUp(Consumer<T> listener) {this.onMouseUpListeners.remove(listener); return this.self();}
    // this SUCKS, i am not removing events, but this way of removing events suck as java lambdas suck
    // see: https://share.gemini.google/NEe1tAE4Ui22 on ideas on how to fix it if i *ever* need to fix it for god knows why


    public PositionAPI getPosition() {return u.getPosition();}

    public Rect rect() {return new Rect(x(), y(), w(), h());}

    public float x() {return u.getPosition().getX();}

    public float y() {return u.getPosition().getY();}

    public float w() {return u.getPosition().getWidth();}

    public float h() {return u.getPosition().getHeight();}

    public T setLocation(float x, float y) {getPosition().setLocation(x, y); return this.self();}

    public T setSize(float width, float height) {getPosition().setSize(width, height); return this.self();}

    public T setXAlignOffset(float xAlignOffset) {getPosition().setXAlignOffset(xAlignOffset); return this.self();}

    public T setYAlignOffset(float yAlignOffset) {getPosition().setYAlignOffset(yAlignOffset); return this.self();}

    //#region --- Parent Container Positioning ---
    public T inTL(float xPad, float yPad) {getPosition().inTL(xPad, yPad); return this.self();}

    public T inTMid(float yPad) {getPosition().inTMid(yPad); return this.self();}

    public T inTR(float xPad, float yPad) {getPosition().inTR(xPad, yPad); return this.self();}

    public T inRMid(float xPad) {getPosition().inRMid(xPad); return this.self();}

    public T inMid() {getPosition().inMid(); return this.self();}

    public T inBR(float xPad, float yPad) {getPosition().inBR(xPad, yPad); return this.self();}

    public T inBMid(float yPad) {getPosition().inBMid(yPad); return this.self();}

    public T inBL(float xPad, float yPad) {getPosition().inBL(xPad, yPad); return this.self();}

    public T inLMid(float xPad) {getPosition().inLMid(xPad); return this.self();}

    public T inTL(Vector2f pad) {getPosition().inTL(pad.x, pad.y); return this.self();}

    public T inTR(Vector2f pad) {getPosition().inTR(pad.x, pad.y); return this.self();}

    public T inBR(Vector2f pad) {getPosition().inBR(pad.x, pad.y); return this.self();}

    public T inBL(Vector2f pad) {getPosition().inBL(pad.x, pad.y); return this.self();}
//#endregion

    //#region --- Sibling Relative Positioning ---
    public T leftOfTop(UIComponentAPI sibling, float xPad) {getPosition().leftOfTop(sibling, xPad); return this.self();}

    public T leftOfMid(UIComponentAPI sibling, float xPad) {getPosition().leftOfMid(sibling, xPad); return this.self();}

    public T leftOfBottom(UIComponentAPI sibling, float xPad) {getPosition().leftOfBottom(sibling, xPad); return this.self();}

    public T rightOfTop(UIComponentAPI sibling, float xPad) {getPosition().rightOfTop(sibling, xPad); return this.self();}

    public T rightOfMid(UIComponentAPI sibling, float xPad) {getPosition().rightOfMid(sibling, xPad); return this.self();}

    public T rightOfBottom(UIComponentAPI sibling, float xPad) {getPosition().rightOfBottom(sibling, xPad); return this.self();}

    public T aboveLeft(UIComponentAPI sibling, float yPad) {getPosition().aboveLeft(sibling, yPad); return this.self();}

    public T aboveMid(UIComponentAPI sibling, float yPad) {getPosition().aboveMid(sibling, yPad); return this.self();}

    public T aboveRight(UIComponentAPI sibling, float yPad) {getPosition().aboveRight(sibling, yPad); return this.self();}

    public T belowLeft(UIComponentAPI sibling, float yPad) {getPosition().belowLeft(sibling, yPad); return this.self();}

    public T belowMid(UIComponentAPI sibling, float yPad) {getPosition().belowMid(sibling, yPad); return this.self();}

    public T belowRight(UIComponentAPI sibling, float yPad) {getPosition().belowRight(sibling, yPad); return this.self();}
//#endregion

    @Override
    public String toString() {
        return this.name;
    }
}
