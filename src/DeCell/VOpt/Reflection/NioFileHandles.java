package DeCell.VOpt.Reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class NioFileHandles {
    public static final Class<?> filesClass;
    public static final Class<?> pathClass;
    public static final Class<?> uriClass;
    public static final Class<?> byteArrayClass;
    public static final Class<?> linkOptionArrayClass;

    public static final MethodHandle pathOfUriHandle;
    public static final MethodHandle filesReadStringHandle;
    public static final MethodHandle uriCreateHandle;
    public static final MethodHandle filesDeleteHandle;
    public static final MethodHandle filesDeleteIfExistsHandle;
    public static final MethodHandle filesReadAllBytesHandle;
    public static final MethodHandle pathOfStringHandle;
    public static final MethodHandle filesExistsHandle;

    static {
        try {
            filesClass = Class.forName("java.nio.file.Files", false, Class.class.getClassLoader());
            pathClass = Class.forName("java.nio.file.Path", false, Class.class.getClassLoader());
            uriClass = Class.forName("java.net.URI", false, Class.class.getClassLoader());
            byteArrayClass = Class.forName("[B", false, Class.class.getClassLoader());
            linkOptionArrayClass = Class.forName("[Ljava.nio.file.LinkOption;", false, Class.class.getClassLoader());

            pathOfUriHandle = ReflectionHandles.LOOKUP.findStatic(pathClass, "of", MethodType.methodType(pathClass, uriClass));
            filesReadStringHandle = ReflectionHandles.LOOKUP.findStatic(filesClass, "readString", MethodType.methodType(String.class, pathClass));
            uriCreateHandle = ReflectionHandles.LOOKUP.findStatic(uriClass, "create", MethodType.methodType(uriClass, String.class));
            filesDeleteHandle = ReflectionHandles.LOOKUP.findStatic(filesClass, "delete", MethodType.methodType(void.class, pathClass));
            filesDeleteIfExistsHandle = ReflectionHandles.LOOKUP.findStatic(filesClass, "deleteIfExists", MethodType.methodType(boolean.class, pathClass));
            filesReadAllBytesHandle = ReflectionHandles.LOOKUP.findStatic(filesClass, "readAllBytes", MethodType.methodType(byteArrayClass, pathClass));
            pathOfStringHandle = ReflectionHandles.LOOKUP.findStatic(pathClass, "of", MethodType.methodType(pathClass, String.class, String[].class));
            filesExistsHandle = ReflectionHandles.LOOKUP.findStatic(filesClass, "exists", MethodType.methodType(boolean.class, pathClass, linkOptionArrayClass));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}