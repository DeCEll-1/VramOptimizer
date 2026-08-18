package DeCell.VOpt.Reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class ModifierHandles {
    public static final Class<?> modifierClass;
    public static final MethodHandle modifierIsPublic;
    public static final MethodHandle modifierIsStatic;

    static {
        try {
            modifierClass = Class.forName("java.lang.reflect.Modifier", false, Class.class.getClassLoader());
            modifierIsPublic = ReflectionHandles.LOOKUP.findStatic(modifierClass, "isPublic", MethodType.methodType(boolean.class, int.class));
            modifierIsStatic = ReflectionHandles.LOOKUP.findStatic(modifierClass, "isStatic", MethodType.methodType(boolean.class, int.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
