package DeCell.UI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventClass;
import com.fs.starfarer.api.input.InputEventType;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InputEventAPICreator {
    private static boolean wasLMBDownLastFrame = false;
    private static boolean wasRMBDownLastFrame = false;
    private static int lastMouseX = 0;
    private static int lastMouseY = 0;
    //#region keys since intellij doesnt allow hiding this
    private static int[] keys = new int[]{ // "useless" ones will be commented
//            Keyboard.KEY_NONE,
            Keyboard.KEY_ESCAPE,
//            Keyboard.KEY_1,
//            Keyboard.KEY_2,
//            Keyboard.KEY_3,
//            Keyboard.KEY_4,
//            Keyboard.KEY_5,
//            Keyboard.KEY_6,
//            Keyboard.KEY_7,
//            Keyboard.KEY_8,
//            Keyboard.KEY_9,
//            Keyboard.KEY_0,
//            Keyboard.KEY_MINUS,
//            Keyboard.KEY_EQUALS,
//            Keyboard.KEY_BACK,
//            Keyboard.KEY_TAB,
//            Keyboard.KEY_Q,
//            Keyboard.KEY_W,
//            Keyboard.KEY_E,
//            Keyboard.KEY_R,
//            Keyboard.KEY_T,
//            Keyboard.KEY_Y,
//            Keyboard.KEY_U,
//            Keyboard.KEY_I,
//            Keyboard.KEY_O,
//            Keyboard.KEY_P,
//            Keyboard.KEY_LBRACKET, // ah yes my beloved keys ö and ç and ü and ğ and ş and ı
//            Keyboard.KEY_RBRACKET,
            Keyboard.KEY_RETURN,
//            Keyboard.KEY_LCONTROL,
//            Keyboard.KEY_A,
//            Keyboard.KEY_S,
//            Keyboard.KEY_D,
//            Keyboard.KEY_F,
//            Keyboard.KEY_G,
//            Keyboard.KEY_H,
//            Keyboard.KEY_J,
//            Keyboard.KEY_K,
//            Keyboard.KEY_L,
//            Keyboard.KEY_SEMICOLON,
//            Keyboard.KEY_APOSTROPHE,
//            Keyboard.KEY_GRAVE,
//            Keyboard.KEY_LSHIFT,
//            Keyboard.KEY_BACKSLASH,
//            Keyboard.KEY_Z,
//            Keyboard.KEY_X,
//            Keyboard.KEY_C,
//            Keyboard.KEY_V,
//            Keyboard.KEY_B,
//            Keyboard.KEY_N,
//            Keyboard.KEY_M,
//            Keyboard.KEY_COMMA,
//            Keyboard.KEY_PERIOD,
//            Keyboard.KEY_SLASH,
//            Keyboard.KEY_RSHIFT,
//            Keyboard.KEY_MULTIPLY,
//            Keyboard.KEY_LMENU,
//            Keyboard.KEY_SPACE,
//            Keyboard.KEY_CAPITAL,
//            Keyboard.KEY_F1,
//            Keyboard.KEY_F2,
//            Keyboard.KEY_F3,
//            Keyboard.KEY_F4,
//            Keyboard.KEY_F5,
//            Keyboard.KEY_F6,
//            Keyboard.KEY_F7,
//            Keyboard.KEY_F8,
//            Keyboard.KEY_F9,
//            Keyboard.KEY_F10,
//            Keyboard.KEY_NUMLOCK,
//            Keyboard.KEY_SCROLL,
//            Keyboard.KEY_NUMPAD7,
//            Keyboard.KEY_NUMPAD8,
//            Keyboard.KEY_NUMPAD9,
//            Keyboard.KEY_SUBTRACT,
//            Keyboard.KEY_NUMPAD4,
//            Keyboard.KEY_NUMPAD5,
//            Keyboard.KEY_NUMPAD6,
//            Keyboard.KEY_ADD,
//            Keyboard.KEY_NUMPAD1,
//            Keyboard.KEY_NUMPAD2,
//            Keyboard.KEY_NUMPAD3,
//            Keyboard.KEY_NUMPAD0,
//            Keyboard.KEY_DECIMAL,
//            Keyboard.KEY_F11,
//            Keyboard.KEY_F12,
//            Keyboard.KEY_F13,
//            Keyboard.KEY_F14,
//            Keyboard.KEY_F15,
//            Keyboard.KEY_F16,
//            Keyboard.KEY_F17,
//            Keyboard.KEY_F18,
//            Keyboard.KEY_KANA,
//            Keyboard.KEY_F19,
//            Keyboard.KEY_CONVERT,
//            Keyboard.KEY_NOCONVERT,
//            Keyboard.KEY_YEN,
//            Keyboard.KEY_NUMPADEQUALS,
//            Keyboard.KEY_CIRCUMFLEX,
//            Keyboard.KEY_AT,
//            Keyboard.KEY_COLON,
//            Keyboard.KEY_UNDERLINE,
//            Keyboard.KEY_KANJI,
//            Keyboard.KEY_STOP,
//            Keyboard.KEY_AX,
//            Keyboard.KEY_UNLABELED,
//            Keyboard.KEY_NUMPADENTER,
//            Keyboard.KEY_RCONTROL,
//            Keyboard.KEY_SECTION,
//            Keyboard.KEY_NUMPADCOMMA,
//            Keyboard.KEY_DIVIDE,
//            Keyboard.KEY_SYSRQ,
//            Keyboard.KEY_RMENU,
//            Keyboard.KEY_FUNCTION,
//            Keyboard.KEY_PAUSE,
//            Keyboard.KEY_HOME,
            Keyboard.KEY_UP,
//            Keyboard.KEY_PRIOR,
//            Keyboard.KEY_LEFT,
//            Keyboard.KEY_RIGHT,
//            Keyboard.KEY_END,
            Keyboard.KEY_DOWN,
//            Keyboard.KEY_NEXT,
//            Keyboard.KEY_INSERT,
//            Keyboard.KEY_DELETE,
//            Keyboard.KEY_CLEAR,
//            Keyboard.KEY_LMETA,
//            Keyboard.KEY_RMETA,
//            Keyboard.KEY_APPS,
//            Keyboard.KEY_POWER,
//            Keyboard.KEY_SLEEP
    };
    //#endregion
    private static final Set<Integer> keysDownLastFrame = new HashSet<>();

    public static List<InputEventAPI> createImmediateEvents() {
        List<InputEventAPI> immediateList = new ArrayList<>();

        int currentX = Mouse.getX();
        int currentY = Mouse.getY();
        boolean isLMBDown = Mouse.isButtonDown(0);
        boolean isRMBDown = Mouse.isButtonDown(1);
        int dWheel = Mouse.getDWheel();

        if (currentX != lastMouseX || currentY != lastMouseY) {
            InputEventAPI event = createEvent(
                    InputEventClass.MOUSE_EVENT, InputEventType.MOUSE_MOVE, currentX, currentY, -1, '\0'
            );
            if (event != null) immediateList.add(event);
        }

        if (isLMBDown && !wasLMBDownLastFrame) {
            InputEventAPI event = createEvent(
                    InputEventClass.MOUSE_EVENT, InputEventType.MOUSE_DOWN, currentX, currentY, 0, '\0'
            );
            if (event != null) immediateList.add(event);
        } else if (!isLMBDown && wasLMBDownLastFrame) {
            InputEventAPI event = createEvent(
                    InputEventClass.MOUSE_EVENT, InputEventType.MOUSE_UP, currentX, currentY, 0, '\0'
            );
            if (event != null) immediateList.add(event);
        }

        if (isRMBDown && !wasRMBDownLastFrame) {
            InputEventAPI event = createEvent(
                    InputEventClass.MOUSE_EVENT, InputEventType.MOUSE_DOWN, currentX, currentY, 1, '\0'
            );
            if (event != null) immediateList.add(event);
        } else if (!isRMBDown && wasRMBDownLastFrame) {
            InputEventAPI event = createEvent(
                    InputEventClass.MOUSE_EVENT, InputEventType.MOUSE_UP, currentX, currentY, 1, '\0'
            );
            if (event != null) immediateList.add(event);
        }

        if (dWheel != 0) {
            InputEventAPI event = createEvent(
                    InputEventClass.MOUSE_EVENT, InputEventType.MOUSE_SCROLL, currentX, currentY, dWheel, '\0'
            );
            if (event != null) immediateList.add(event);
        }

        for (int keyCode : keys) {
            boolean isKeyDownNow = Keyboard.isKeyDown(keyCode);
            boolean wasKeyDownLastFrame = keysDownLastFrame.contains(keyCode);

            if (isKeyDownNow && !wasKeyDownLastFrame) {
                // Key was just pressed down this frame
                keysDownLastFrame.add(keyCode);

                // For immediate mode, we pass '\0' as the character, or convert common ones if needed
                InputEventAPI event = createEvent(
                        InputEventClass.KEYBOARD_EVENT,
                        InputEventType.KEY_DOWN,
                        currentX,
                        currentY,
                        keyCode,
                        '\0'
                );
                if (event != null) immediateList.add(event);

            } else if (!isKeyDownNow && wasKeyDownLastFrame) {
                // Key was just released this frame
                keysDownLastFrame.remove(keyCode);

                InputEventAPI event = createEvent(
                        InputEventClass.KEYBOARD_EVENT,
                        InputEventType.KEY_UP,
                        currentX,
                        currentY,
                        keyCode,
                        '\0'
                );
                if (event != null) immediateList.add(event);
            }
        }

        wasLMBDownLastFrame = isLMBDown;
        wasRMBDownLastFrame = isRMBDown;
        lastMouseX = currentX;
        lastMouseY = currentY;
        return immediateList;
    }


    private static Class<?> eventImplementationClass;
    private static final Class<?>[] CONSTRUCTOR_SIGNATURE;

    static {
        // Pre-define the constructor signature layout to optimize runtime checks
        CONSTRUCTOR_SIGNATURE = new Class<?>[]{
                InputEventClass.class,
                InputEventType.class,
                int.class,
                int.class,
                int.class,
                char.class
        };
    }
    public static void discoverEventClass(List<InputEventAPI> events) {
        if (eventImplementationClass != null || events == null || events.isEmpty()) {
            return;
        }

        for (InputEventAPI event : events) {
            if (event != null) {
                eventImplementationClass = event.getClass();
                return;
            }
        }
    }

    public static InputEventAPI createEvent(InputEventClass eventClass, InputEventType eventType, int x, int y, int value, char eventChar) {
        if (eventImplementationClass == null) {
            return null;
        }

        try {
            Object instance = Reflections.createInstanceWithArgs(
                    eventImplementationClass,
                    CONSTRUCTOR_SIGNATURE,
                    eventClass,
                    eventType,
                    x,
                    y,
                    value,
                    eventChar
            );

            return (InputEventAPI) instance;

        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}
