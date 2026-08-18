package DeCell.VOpt.Reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class FileIoHandles {
    public static final Class<?> fileIOClass;
    public static final MethodHandle fileIOConstructorHandle;
    public static final MethodHandle fileIOExistsHandle;

    static {
        try {
            fileIOClass = Class.forName("java.io.File", false, Class.class.getClassLoader());
            fileIOConstructorHandle = ReflectionHandles.LOOKUP.findConstructor(fileIOClass, MethodType.methodType(void.class, String.class));
            fileIOExistsHandle = ReflectionHandles.LOOKUP.findVirtual(fileIOClass, "exists", MethodType.methodType(boolean.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}