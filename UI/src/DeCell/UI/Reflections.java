package DeCell.UI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;


public class Reflections {

    //region reflection
    public static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    public static final Class<?> methodClass;
    public static final MethodHandle getMethodHandle;
    public static final MethodHandle invokeMethodHandle;
    public static final MethodHandle setMethodAccessable;
    public static final Class<?> constructorArrayClass;

    public static final MethodHandle setConstructorAccessibleHandle;

    public static final MethodHandle getDeclaredConstructorsHandle;

    public static final MethodHandle getConstructorParameterTypesHandle;

    public static final Class<?> constructorClass;

    public static final MethodHandle constructorNewInstanceHandle;


    static {
        try {
            methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
            getMethodHandle = lookup.findVirtual(Class.class, "getMethod", MethodType.methodType(methodClass, String.class, Class[].class));
            invokeMethodHandle = lookup.findVirtual(methodClass, "invoke", MethodType.methodType(Object.class, Object.class, Object[].class));
            setMethodAccessable = lookup.findVirtual(methodClass, "setAccessible", MethodType.methodType(void.class, boolean.class));

            constructorArrayClass = Class.forName("[Ljava.lang.reflect.Constructor;", false, Class.class.getClassLoader());
            constructorClass = Class.forName("java.lang.reflect.Constructor", false, Class.class.getClassLoader());

            getDeclaredConstructorsHandle = lookup.findVirtual(Class.class, "getDeclaredConstructors", MethodType.methodType(constructorArrayClass));

            getConstructorParameterTypesHandle = lookup.findVirtual(constructorClass, "getParameterTypes", MethodType.methodType(Class[].class));

            setConstructorAccessibleHandle = lookup.findVirtual(constructorClass, "setAccessible", MethodType.methodType(void.class, boolean.class));

            constructorNewInstanceHandle = lookup.findVirtual(constructorClass, "newInstance", MethodType.methodType(Object.class, Object[].class));

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

    public static Object createInstanceWithArgs(Class<?> clazz, Class<?>[] parameterTypes, Object... args) {
        if (parameterTypes == null) parameterTypes = new Class<?>[0];
        if (args == null) args = new Object[0];

        try {
            Object[] constructors = (Object[]) getDeclaredConstructorsHandle.invoke(clazz);

            for (Object constructor : constructors) {
                Class<?>[] paramTypes = (Class<?>[]) getConstructorParameterTypesHandle.invoke(constructor);

                if (paramTypes.length != parameterTypes.length) {
                    continue;
                }

                boolean match = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    if (paramTypes[i] != parameterTypes[i]) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    setConstructorAccessibleHandle.invoke(constructor, true);

                    Object[] newArray = Arrays.copyOf(args, args.length + 1);
                    newArray[0] = constructor;
                    System.arraycopy(args, 0, newArray, 1, args.length);
                    return constructorNewInstanceHandle.invokeWithArguments(newArray);
                }
            }

            throw new NoSuchMethodException("No constructor found for " + clazz.getName() +
                    " with specified parameter types.");

        } catch (Throwable t) {
            throw new RuntimeException("Failed to instantiate " + clazz.getName() + " with arguments via MethodHandles", t);
        }
    }
}