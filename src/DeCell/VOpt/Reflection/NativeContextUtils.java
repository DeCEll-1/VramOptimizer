package DeCell.VOpt.Reflection;

import org.lwjgl.opengl.Display;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;


public class NativeContextUtils {

    private static MethodHandle getHandleByteBuffer = null;
    private static MethodHandle winGetHGLRCHandle = null;
    private static MethodHandle linuxGetGLXContextHandle = null;

    public static void getRawNativeContextHandle() {
        try {
            // 1. Fetch Display.getDrawable() -> returns a Drawable interface object

            Class<?> displayClass = Display.class;
            Object displayDrawable = ReflectionUtils.invokeMethod("getDrawable", displayClass);
            if (displayDrawable == null) {
                throw new IllegalStateException("Display drawable is null. Is the display created?");
            }

            // 2. Create new SharedDrawable(displayDrawable)
            Class<?> sharedDrawableClass = Class.forName("org.lwjgl.opengl.SharedDrawable");
            Object sharedDrawable = ReflectionUtils.newInstance(sharedDrawableClass, new Class<?>[]{
                    Class.forName("org.lwjgl.opengl.Drawable")
            }, displayDrawable);

            // 3. Call getContext() on the shared drawable instance
            Object context = ReflectionUtils.invokeMethod("getContext", sharedDrawable);
            if (context == null) {
                throw new IllegalStateException("Failed to retrieve OpenGL context from SharedDrawable.");
            }

            // 4. Call getHandle() on the context instance to get the ByteBuffer handle
            ByteBuffer handleBuffer = (ByteBuffer) ReflectionUtils.invokeDeclaredMethod("getHandle", context);

            // 5. Check platform via LWJGLUtil.getPlatform()
            Class<?> lwjglUtilClass = Class.forName("org.lwjgl.LWJGLUtil");
            int platform = (int) ReflectionUtils.invokeMethod("getPlatform", lwjglUtilClass);

            // 6. Route to platform-specific context implementation via reflection
            if (platform == 3) {// Windows -> WindowsContextImplementation.getHGLRC(ByteBuffer)
                Class<?> winImplClass = Class.forName("org.lwjgl.opengl.WindowsContextImplementation");
                Object winImpl = ReflectionUtils.newInstanceDeclared(winImplClass);
                long hdc = (long) ReflectionUtils.invokeDeclaredMethod("getHDC", winImpl, new Class<?>[]{ByteBuffer.class}, handleBuffer);
                long hglrc = (long) ReflectionUtils.invokeDeclaredMethod("getHGLRC", winImpl, new Class<?>[]{ByteBuffer.class}, handleBuffer);

                System.out.println("HDC: 0x" + Long.toHexString(hdc) + " | HGLRC: 0x" + Long.toHexString(hglrc));
            } else {
                throw new UnsupportedOperationException("Platform not supported: " + platform);
            }

        } catch (Throwable t) {
            throw new RuntimeException("Failed to extract raw native context handle via reflection", t);
        }
    }
}