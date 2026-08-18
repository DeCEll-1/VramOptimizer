package DeCell.VOpt.Reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;

public class ReflectionUtils {

    public static Object getFieldFromName(Class<?> c, String name) throws Throwable {
        for (Object field : (Object[]) ReflectionHandles.getDeclaredFieldsHandle.invoke(c))
            if (Objects.equals(name, (String) ReflectionHandles.getFieldNameHandle.invoke(field)))
                return field;
        return null;
    }

    public static VarHandle getVarHandle(Class<?> targetClass, String fieldName, Class<?> fieldType) throws Throwable {
        MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(targetClass, MethodHandles.lookup());
        return privateLookup.findVarHandle(targetClass, fieldName, fieldType);
    }

    public static String getFieldName(Object field) {
        if (field == null) {
            return null;
        }
        try {
            return (String) ReflectionHandles.getFieldNameHandle.invoke(field);
        } catch (Throwable t) {
            return null;
        }
    }

    public static Object findConstructor(Class<?> targetClass, Class<?>... parameterTypes) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }

        Class<?>[] paramTypes = (parameterTypes != null) ? parameterTypes : new Class<?>[0];

        try {
            Object[] constructors = (Object[]) ReflectionHandles.getDeclaredConstructorsHandle.invoke(targetClass);

            for (Object constructor : constructors) {
                Class<?>[] currentParamTypes = (Class<?>[]) ReflectionHandles.getConstructorParameterTypesHandle.invoke(constructor);

                if (currentParamTypes.length != paramTypes.length) {
                    continue;
                }

                boolean match = true;
                for (int i = 0; i < currentParamTypes.length; i++) {
                    if (currentParamTypes[i] != paramTypes[i]) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    ReflectionHandles.setConstructorAccessibleHandle.invoke(constructor, true);
                    return constructor;
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException("Failed to scan or access constructors for " + targetClass.getName(), t);
        }

        return null;
    }

    public static Object newInstance(Object constructor, Object... args) {
        if (constructor == null) {
            throw new IllegalArgumentException("Constructor cannot be null");
        }
        Object[] methodArgs = (args != null) ? args : new Object[0];

        try {
            return ReflectionHandles.constructorNewInstanceHandle.invoke(constructor, methodArgs);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to blindly instantiate object via constructor", t);
        }
    }

    public static Object newInstance(Class<?> clazz, Object... args) {
        if (clazz == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }

        Object[] methodArgs = (args != null) ? args : new Object[0];
        Class<?>[] parameterTypes = new Class<?>[methodArgs.length];

        for (int i = 0; i < methodArgs.length; i++) {
            if (methodArgs[i] == null) {
                parameterTypes[i] = Object.class;
            } else {
                Class<?> argClass = methodArgs[i].getClass();

                if (argClass == Integer.class) parameterTypes[i] = int.class;
                else if (argClass == Boolean.class) parameterTypes[i] = boolean.class;
                else if (argClass == Long.class) parameterTypes[i] = long.class;
                else if (argClass == Float.class) parameterTypes[i] = float.class;
                else if (argClass == Double.class) parameterTypes[i] = double.class;
                else if (argClass == Byte.class) parameterTypes[i] = byte.class;
                else if (argClass == Character.class) parameterTypes[i] = char.class;
                else if (argClass == Short.class) parameterTypes[i] = short.class;
                else parameterTypes[i] = argClass;
            }
        }

        return newInstance(clazz, parameterTypes, methodArgs);
    }

    public static Object newInstance(Class<?> clazz, Class<?>[] parameterTypes, Object... args) {
        if (clazz == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }

        Object constructor = findConstructor(clazz, parameterTypes);
        if (constructor == null) {
            throw new RuntimeException("No constructor found for " + clazz.getName() +
                    " matching parameters: " + Arrays.toString(parameterTypes));
        }

        return newInstance(constructor, args);
    }

    public static Object findDeclaredConstructor(Class<?> targetClass, Class<?>... parameterTypes) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }

        Class<?>[] paramTypes = (parameterTypes != null) ? parameterTypes : new Class<?>[0];

        try {
            // Fetch all declared constructors using your handle
            Object[] constructors = (Object[]) ReflectionHandles.getDeclaredConstructorsHandle.invoke(targetClass);

            for (Object constructor : constructors) {
                Class<?>[] currentParamTypes = (Class<?>[]) ReflectionHandles.getConstructorParameterTypesHandle.invoke(constructor);

                if (currentParamTypes.length != paramTypes.length) {
                    continue;
                }

                boolean match = true;
                for (int i = 0; i < currentParamTypes.length; i++) {
                    if (currentParamTypes[i] != paramTypes[i]) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    // Force accessibility to bypass security/sandbox restrictions
                    ReflectionHandles.setConstructorAccessibleHandle.invoke(constructor, true);
                    return constructor;
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException("Failed to scan or access declared constructors for " + targetClass.getName(), t);
        }

        return null;
    }

    public static Object newInstanceDeclared(Class<?> clazz, Object... args) {
        if (clazz == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }

        Object[] methodArgs = (args != null) ? args : new Object[0];
        Class<?>[] parameterTypes = new Class<?>[methodArgs.length];

        for (int i = 0; i < methodArgs.length; i++) {
            if (methodArgs[i] == null) {
                parameterTypes[i] = Object.class;
            } else {
                Class<?> argClass = methodArgs[i].getClass();

                if (argClass == Integer.class) parameterTypes[i] = int.class;
                else if (argClass == Boolean.class) parameterTypes[i] = boolean.class;
                else if (argClass == Long.class) parameterTypes[i] = long.class;
                else if (argClass == Float.class) parameterTypes[i] = float.class;
                else if (argClass == Double.class) parameterTypes[i] = double.class;
                else if (argClass == Byte.class) parameterTypes[i] = byte.class;
                else if (argClass == Character.class) parameterTypes[i] = char.class;
                else if (argClass == Short.class) parameterTypes[i] = short.class;
                else parameterTypes[i] = argClass;
            }
        }

        return newInstanceDeclared(clazz, parameterTypes, methodArgs);
    }

    public static Object newInstanceDeclared(Class<?> clazz, Class<?>[] parameterTypes, Object... args) {
        if (clazz == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }

        Object constructor = findDeclaredConstructor(clazz, parameterTypes);
        if (constructor == null) {
            throw new RuntimeException("No declared constructor found for " + clazz.getName() +
                    " matching parameters: " + Arrays.toString(parameterTypes));
        }

        return newInstance(constructor, args);
    }

    public static Object invokeMethod(String methodName, Object targetOrClass) {
        if (targetOrClass == null || methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Target object/Class and method name cannot be null or empty");
        }

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

            Object method = ReflectionHandles.getMethodHandle.invoke(clazz, methodName, new Class<?>[0]);
            ReflectionHandles.setMethodAccessable.invoke(method, true);
            return ReflectionHandles.invokeMethodHandle.invoke(method, instance, new Object[0]);

        } catch (Throwable t) {
            String className = (targetOrClass instanceof Class<?>)
                    ? ((Class<?>) targetOrClass).getName()
                    : targetOrClass.getClass().getName();

            throw new RuntimeException("Failed to invoke method '" + methodName +
                    "' on " + className, t);
        }
    }

    public static Object invokeDeclaredMethod(String methodName, Object targetOrClass) {
        if (targetOrClass == null || methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Target object/Class and method name cannot be null or empty");
        }

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

            Object method = null;
            Class<?> currentClass = clazz;
            while (currentClass != null && currentClass != Object.class) {
                try {
                    method = ReflectionHandles.getDeclaredMethodHandle.invoke(
                            currentClass,
                            methodName,
                            new Class<?>[0]
                    );
                    break;
                } catch (NoSuchMethodException e) {
                    currentClass = currentClass.getSuperclass();
                }
            }

            if (method == null) {
                throw new NoSuchMethodException("Declared method '" + methodName + "' not found in " + clazz.getName() + " hierarchy.");
            }

            ReflectionHandles.setMethodAccessable.invoke(method, true);
            return ReflectionHandles.invokeMethodHandle.invoke(method, instance, new Object[0]);

        } catch (Throwable t) {
            String className = (targetOrClass instanceof Class<?>)
                    ? ((Class<?>) targetOrClass).getName()
                    : targetOrClass.getClass().getName();

            throw new RuntimeException("Failed to invoke declared method '" + methodName +
                    "' on " + className, t);
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

            Object method = ReflectionHandles.getMethodHandle.invoke(clazz, methodName, paramTypes);
            ReflectionHandles.setMethodAccessable.invoke(method, true);
            return ReflectionHandles.invokeMethodHandle.invoke(method, instance, methodArgs);

        } catch (Throwable t) {
            String className = (targetOrClass instanceof Class<?>)
                    ? ((Class<?>) targetOrClass).getName()
                    : targetOrClass.getClass().getName();

            throw new RuntimeException("Failed to invoke method '" + methodName +
                    "' on " + className + " with the specified parameters.", t);
        }
    }

    public static Object invokeDeclaredMethod(String methodName, Object targetOrClass, Class<?>[] parameterTypes, Object... args) {
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

            Object method = null;
            Class<?> currentClass = clazz;
            while (currentClass != null && currentClass != Object.class) {
                try {
                    method = ReflectionHandles.getDeclaredMethodHandle.invoke(
                            currentClass,
                            methodName,
                            paramTypes
                    );
                    break;
                } catch (NoSuchMethodException e) {
                    currentClass = currentClass.getSuperclass();
                }
            }

            if (method == null) {
                throw new NoSuchMethodException("Declared method '" + methodName + "' not found in " + clazz.getName() + " hierarchy.");
            }

            ReflectionHandles.setMethodAccessable.invoke(method, true);
            return ReflectionHandles.invokeMethodHandle.invoke(method, instance, methodArgs);

        } catch (Throwable t) {
            String className = (targetOrClass instanceof Class<?>)
                    ? ((Class<?>) targetOrClass).getName()
                    : targetOrClass.getClass().getName();

            throw new RuntimeException("Failed to invoke declared method '" + methodName +
                    "' on " + className + " with the specified parameters.", t);
        }
    }

    public static Object createInstanceWithArgs(Class<?> clazz, Class<?>[] parameterTypes, Object... args) {
        if (parameterTypes == null) parameterTypes = new Class<?>[0];
        if (args == null) args = new Object[0];

        try {
            Object[] constructors = (Object[]) ReflectionHandles.getDeclaredConstructorsHandle.invoke(clazz);

            for (Object constructor : constructors) {
                Class<?>[] paramTypes = (Class<?>[]) ReflectionHandles.getConstructorParameterTypesHandle.invoke(constructor);

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
                    ReflectionHandles.setConstructorAccessibleHandle.invoke(constructor, true);

                    Object[] newArray = Arrays.copyOf(args, args.length + 1);
                    newArray[0] = constructor;
                    System.arraycopy(args, 0, newArray, 1, args.length);
                    return ReflectionHandles.constructorNewInstanceHandle.invokeWithArguments(newArray);
                }
            }

            throw new NoSuchMethodException("No constructor found for " + clazz.getName() +
                    " with specified parameter types.");

        } catch (Throwable t) {
            throw new RuntimeException("Failed to instantiate " + clazz.getName() + " with arguments via MethodHandles", t);
        }
    }

    public static byte[] readAllBytes(String filePath) {
        try {
            Object pathInstance = NioFileHandles.pathOfStringHandle.invoke(filePath, new String[0]);
            return (byte[]) NioFileHandles.filesReadAllBytesHandle.invoke(pathInstance);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to read bytes from file path: " + filePath, t);
        }
    }

    public static String readAllText(String filePath) {
        try {
            Object pathInstance = NioFileHandles.pathOfStringHandle.invoke(filePath, new String[0]);
            return (String) NioFileHandles.filesReadStringHandle.invoke(pathInstance);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to read text from file path: " + filePath, t);
        }
    }

    public static boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        try {
            return runFileExists(createFile(filePath));
        } catch (Throwable t) {
            throw new RuntimeException("Failed to check if file path exists: " + filePath, t);
        }
    }

    public static Object createFile(String pathname) {
        try {
            return FileIoHandles.fileIOConstructorHandle.invoke(pathname);
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }
    }

    public static boolean runFileExists(Object fileObject) {
        try {
            return (boolean) FileIoHandles.fileIOExistsHandle.invoke(fileObject);
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }
    }
}