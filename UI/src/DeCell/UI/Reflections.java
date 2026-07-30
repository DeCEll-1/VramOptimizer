package DeCell.UI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;


public class Reflections {

    //region reflection
    public static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    public static final Class<?> methodClass;
    public static final MethodHandle getMethodHandle;
    public static final MethodHandle invokeMethodHandle;
    public static final MethodHandle setMethodAccessable;
    static {
        try {
            methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
            getMethodHandle = lookup.findVirtual(Class.class, "getMethod", MethodType.methodType(methodClass, String.class, Class[].class));
            invokeMethodHandle = lookup.findVirtual(methodClass, "invoke", MethodType.methodType(Object.class, Object.class, Object[].class));
            setMethodAccessable = lookup.findVirtual(methodClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//endregion

    public static Object invokeMethod(String methodName, Object target) {
        if (target == null || methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Target object and method name cannot be null or empty");
        }

        try {
            Class<?> clazz = target.getClass();

            // Get the Method object using getMethod (no parameters)
            Object method = getMethodHandle.invoke(clazz, methodName, new Class<?>[0]);

            // Make it accessible in case it's private/protected
            setMethodAccessable.invoke(method, true);

            // Invoke the method on the target
            return invokeMethodHandle.invoke(method, target, new Object[0]);

        } catch (Throwable t) {
            throw new RuntimeException("Failed to invoke method '" + methodName +
                    "' on " + target.getClass().getName(), t);
        }
    }

    public static Object invokeMethod(String methodName, Object targetOrClass, Class<?>[] parameterTypes, Object... args) {
        if (methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Method name cannot be null or empty");
        }
        if (targetOrClass == null) {
            throw new IllegalArgumentException("Target object or target Class cannot be null");
        }

        Class<?>[] paramTypes = (parameterTypes != null) ? parameterTypes : new Class<?>[0];
        Object[] methodArgs = (args != null) ? args : new Object[0];

        try {
            Class<?> clazz;
            Object instance;

            if (targetOrClass instanceof Class<?>) {
                clazz = (Class<?>) targetOrClass;
                instance = null;
            } else {
                clazz = targetOrClass.getClass();
                instance = targetOrClass;
            }

            Object method = getMethodHandle.invoke(clazz, methodName, paramTypes);

            setMethodAccessable.invoke(method, true);

            return invokeMethodHandle.invoke(method, instance, methodArgs);

        } catch (Throwable t) {
            String className = (targetOrClass instanceof Class<?>)
                    ? ((Class<?>) targetOrClass).getName()
                    : targetOrClass.getClass().getName();

            throw new RuntimeException("Failed to invoke method '" + methodName +
                    "' on " + className + " with the specified parameters.", t);
        }
    }
}