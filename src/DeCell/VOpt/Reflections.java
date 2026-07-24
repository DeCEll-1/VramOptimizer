package DeCell.VOpt;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;


public class Reflections {

    //region reflection
    public static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    public static final Class<?> fieldClass;
    public static final Class<?> fieldArrayClass;
    public static final Class<?> methodClass;
    public static final Class<?> typeClass;
    public static final Class<?> typeArrayClass;
    public static final Class<?> parameterizedTypeClass;
    public static final Class<?> constructorClass;
    public static final Class<?> constructorArrayClass;
    public static final MethodHandle getFieldTypeHandle;
    public static final MethodHandle setFieldHandle;
    public static final MethodHandle getFieldHandle;
    public static final MethodHandle getFieldNameHandle;
    public static final MethodHandle setFieldAccessibleHandle;
    public static final MethodHandle getParameterCount;
    public static final MethodHandle getMethodNameHandle;
    public static final MethodHandle getMethodHandle;
    public static final MethodHandle invokeMethodHandle;
    public static final MethodHandle setMethodAccessable;
    public static final MethodHandle getModifiersHandle;
    public static final MethodHandle getParameterTypesHandle;
    public static final MethodHandle getReturnTypeHandle;
    public static final MethodHandle getDeclaringClass;
    public static final MethodHandle getGenericTypeHandle;
    public static final MethodHandle getTypeNameHandle;
    public static final MethodHandle getActualTypeArgumentsHandle;
    public static final MethodHandle setConstructorAccessibleHandle;
    public static final MethodHandle getDeclaredConstructorsHandle;
    public static final MethodHandle getDeclaredFieldsHandle;
    public static final MethodHandle getConstructorParameterTypesHandle;
    public static final MethodHandle constructorGetDeclaringClass;
    public static final MethodHandle constructorNewInstanceHandle;
    public static final Class<?> modifierClass;
    public static final MethodHandle modifierIsPublic;
    public static final MethodHandle modifierIsStatic;
    public static final MethodHandle methodIsSynthetic;

    public static final MethodHandle methodIsBridge;

    public static final Class<?> filesClass;
    public static final Class<?> pathClass;
    public static final MethodHandle filesReadStringHandle;
    public static final Class<?> uriClass;
    public static final MethodHandle pathOfUriHandle;

    public static final MethodHandle uriCreateHandle;
    public static final MethodHandle filesDeleteHandle;
    public static final MethodHandle filesDeleteIfExistsHandle;

    public static final Class<?> byteArrayClass;
    public static final MethodHandle filesReadAllBytesHandle;
    public static final MethodHandle pathOfStringHandle;

    static {
        try {
            fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
            fieldArrayClass = Class.forName("[Ljava.lang.reflect.Field;", false, Class.class.getClassLoader());
            methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
            typeClass = Class.forName("java.lang.reflect.Type", false, Class.class.getClassLoader());
            typeArrayClass = Class.forName("[Ljava.lang.reflect.Type;", false, Class.class.getClassLoader());
            parameterizedTypeClass = Class.forName("java.lang.reflect.ParameterizedType", false, Class.class.getClassLoader());
            constructorClass = Class.forName("java.lang.reflect.Constructor", false, Class.class.getClassLoader());
            constructorArrayClass = Class.forName("[Ljava.lang.reflect.Constructor;", false, Class.class.getClassLoader());

            setFieldHandle = lookup.findVirtual(fieldClass, "set", MethodType.methodType(void.class, Object.class, Object.class));
            getFieldHandle = lookup.findVirtual(fieldClass, "get", MethodType.methodType(Object.class, Object.class));
            getFieldNameHandle = lookup.findVirtual(fieldClass, "getName", MethodType.methodType(String.class));
            getFieldTypeHandle = lookup.findVirtual(fieldClass, "getType", MethodType.methodType(Class.class));
            setFieldAccessibleHandle = lookup.findVirtual(fieldClass, "setAccessible", MethodType.methodType(void.class, boolean.class));


            getMethodNameHandle = lookup.findVirtual(methodClass, "getName", MethodType.methodType(String.class));
            getMethodHandle = lookup.findVirtual(Class.class, "getMethod", MethodType.methodType(methodClass, String.class, Class[].class));

            invokeMethodHandle = lookup.findVirtual(methodClass, "invoke", MethodType.methodType(Object.class, Object.class, Object[].class));
            setMethodAccessable = lookup.findVirtual(methodClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
            getModifiersHandle = lookup.findVirtual(methodClass, "getModifiers", MethodType.methodType(int.class));

            getParameterTypesHandle = lookup.findVirtual(methodClass, "getParameterTypes", MethodType.methodType(Class[].class));
            getReturnTypeHandle = lookup.findVirtual(methodClass, "getReturnType", MethodType.methodType(Class.class));
            getParameterCount = lookup.findVirtual(methodClass, "getParameterCount", MethodType.methodType(int.class));

            getDeclaringClass = lookup.findVirtual(methodClass, "getDeclaringClass", MethodType.methodType(Class.class));

            getGenericTypeHandle = lookup.findVirtual(fieldClass, "getGenericType", MethodType.methodType(typeClass));
            getTypeNameHandle = lookup.findVirtual(typeClass, "getTypeName", MethodType.methodType(String.class));
            getActualTypeArgumentsHandle = lookup.findVirtual(parameterizedTypeClass, "getActualTypeArguments", MethodType.methodType(typeArrayClass));

            setConstructorAccessibleHandle = lookup.findVirtual(constructorClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
            getConstructorParameterTypesHandle = lookup.findVirtual(constructorClass, "getParameterTypes", MethodType.methodType(Class[].class));
            constructorNewInstanceHandle = lookup.findVirtual(constructorClass, "newInstance", MethodType.methodType(Object.class, Object[].class));

            constructorGetDeclaringClass = lookup.findVirtual(constructorClass, "getDeclaringClass", MethodType.methodType(Class.class));

            getDeclaredConstructorsHandle = lookup.findVirtual(Class.class, "getDeclaredConstructors", MethodType.methodType(constructorArrayClass));
            getDeclaredFieldsHandle = lookup.findVirtual(Class.class, "getDeclaredFields", MethodType.methodType(fieldArrayClass));

            modifierClass = Class.forName("java.lang.reflect.Modifier", false, Class.class.getClassLoader());

            modifierIsPublic = lookup.findStatic(modifierClass, "isPublic", MethodType.methodType(boolean.class, int.class));
            modifierIsStatic = lookup.findStatic(modifierClass, "isStatic", MethodType.methodType(boolean.class, int.class));

            methodIsSynthetic = lookup.findVirtual(methodClass, "isSynthetic", MethodType.methodType(boolean.class));
            methodIsBridge = lookup.findVirtual(methodClass, "isBridge", MethodType.methodType(boolean.class));

            filesClass = Class.forName("java.nio.file.Files", false, Class.class.getClassLoader());
            pathClass = Class.forName("java.nio.file.Path", false, Class.class.getClassLoader());
            uriClass = Class.forName("java.net.URI", false, Class.class.getClassLoader());

            pathOfUriHandle = lookup.findStatic(pathClass, "of", MethodType.methodType(pathClass, uriClass));

            // Files.readString(Path) -> String
            filesReadStringHandle = lookup.findStatic(filesClass, "readString", MethodType.methodType(String.class, pathClass));

            uriCreateHandle = lookup.findStatic(
                    uriClass,
                    "create",
                    MethodType.methodType(uriClass, String.class)
            );

            filesDeleteHandle = lookup.findStatic(
                    filesClass,
                    "delete",
                    MethodType.methodType(void.class, pathClass)
            );

            // Files.deleteIfExists(Path) -> boolean
            filesDeleteIfExistsHandle = lookup.findStatic(
                    filesClass,
                    "deleteIfExists",
                    MethodType.methodType(boolean.class, pathClass)
            );

            byteArrayClass = Class.forName("[B", false, Class.class.getClassLoader());

            // Files.readAllBytes(Path) -> byte[]
            filesReadAllBytesHandle = lookup.findStatic(
                    filesClass,
                    "readAllBytes",
                    MethodType.methodType(byteArrayClass, pathClass)
            );

            // Path.of(String, String[]) -> Path
            pathOfStringHandle = lookup.findStatic(
                    pathClass,
                    "of",
                    MethodType.methodType(pathClass, String.class, String[].class)
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//endregion

    public static Object getFieldFromName(Class<?> c, String name) throws Throwable {
        for (Object field : c.getDeclaredFields())
            if (Objects.equals(name, (String) Reflections.getFieldNameHandle.invoke(field)))
                return field;
        return null;
    }

    public static VarHandle getVarHandle(Class<?> targetClass, String fieldName, Class<?> fieldType) throws Throwable {
        // Create a private lookup with full access to the target class
        MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(targetClass, MethodHandles.lookup());

        // Find and return the VarHandle
        return privateLookup.findVarHandle(targetClass, fieldName, fieldType);
    }

    public static String getFieldName(Object field) {
        if (field == null) {
            return null;
        }
        try {
            return (String) Reflections.getFieldNameHandle.invoke(field);
        } catch (Throwable t) {
            // Fallback or log if necessary, otherwise fail gracefully under sandbox restrictions
            return null;
        }
    }

    public static Object findConstructor(Class<?> targetClass, Class<?>... parameterTypes) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }

        Class<?>[] paramTypes = (parameterTypes != null) ? parameterTypes : new Class<?>[0];

        try {
            // 1. Fetch all declared constructors blindly as an Object[] using your pre-built handle
            Object[] constructors = (Object[]) getDeclaredConstructorsHandle.invoke(targetClass);

            for (Object constructor : constructors) {
                // 2. Extract the parameter types using your pre-built handle
                Class<?>[] currentParamTypes = (Class<?>[]) getConstructorParameterTypesHandle.invoke(constructor);

                // Compare length first
                if (currentParamTypes.length != paramTypes.length) {
                    continue;
                }

                // Compare exact types
                boolean match = true;
                for (int i = 0; i < currentParamTypes.length; i++) {
                    if (currentParamTypes[i] != paramTypes[i]) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    // 3. Target found! Force accessibility blindly using your pre-built handle
                    setConstructorAccessibleHandle.invoke(constructor, true);
                    return constructor;
                }
            }

        } catch (Throwable t) {
            throw new RuntimeException("Failed to scan or access constructors for " + targetClass.getName(), t);
        }

        return null; // Return null if no matching constructor exists
    }

    public static Object newInstance(Object constructor, Object... args) {
        if (constructor == null) {
            throw new IllegalArgumentException("Constructor cannot be null");
        }
        Object[] methodArgs = (args != null) ? args : new Object[0];

        try {
            // Use your pre-configured constructorNewInstanceHandle:
            // MethodType.methodType(Object.class, Object[].class)
            // It takes the constructor object as the first parameter, and the args array as the second.
            return constructorNewInstanceHandle.invoke(constructor, methodArgs);
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

        // 1. Resolve the constructor dynamically using your signature-blind method
        Object constructor = findConstructor(clazz, parameterTypes);
        if (constructor == null) {
            throw new RuntimeException("No constructor found for " + clazz.getName() +
                    " matching parameters: " + Arrays.toString(parameterTypes));
        }

        // 2. Safely execute it using your existing array-based utility structure
        return newInstance(constructor, args);
    }

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

    private static Class<?> textureObjectClass = null;
    private static VarHandle textureObjectIDHandle = null;

    public static int extractTextureID(Object textureInstance) {
        if (textureInstance == null) return -1;
        if (textureObjectClass == null) {
            textureObjectClass = textureInstance.getClass();
            createTextureObjectIDHandle();
        }

        if (textureObjectIDHandle == null)
            return -1;

        try {
            return (int) textureObjectIDHandle.get(textureInstance);
        } catch (Throwable t) {
            return -1;
        }
    }

    private static void createTextureObjectIDHandle() {
        try {
            Class<?> clazz = textureObjectClass;

            int markerValue = 8675309;

            Object dummyInstance = newInstance(clazz, 0, markerValue);

            Object[] fields = clazz.getDeclaredFields();

            if (fields.length == 0) throw new RuntimeException("No fields found to scan.");


            for (Object field : fields) {
                String fieldName = getFieldName(field);

                try {
                    VarHandle vh = getVarHandle(clazz, fieldName, int.class);

                    int currentValue = (int) vh.get(dummyInstance);

                    if (currentValue == markerValue) {
                        textureObjectIDHandle = vh;
                        break;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    continue;
                }
            }

            if (textureObjectIDHandle == null) {
                throw new NoSuchFieldException("Could not pinpoint the texture ID field via marker scanning.");
            }

        } catch (Throwable t) {
            throw new RuntimeException("Failed cross-platform VarHandle signature scan", t);
        }
    }

    public static void bindTexture(Object textureInstance) {
        if (textureInstance == null) return;

        try {
            Class<?> clazz = textureInstance.getClass();

            Object[] methods = clazz.getDeclaredMethods();

            // 2. Scan for the specific signature
            for (Object method : methods) {

                // Check parameters: Must accept 0 parameters
                int paramCount = (int) Reflections.getParameterCount.invoke(method);
                if (paramCount != 0) {
                    continue;
                }

                // Check return type: Must be void.class
                Class<?> returnType = (Class<?>) Reflections.getReturnTypeHandle.invoke(method);
                if (returnType != void.class) {
                    continue;
                }

                // Skip compiler-generated synthetic or bridge methods
                boolean isSynthetic = (boolean) Reflections.methodIsSynthetic.invoke(method);
                boolean isBridge = (boolean) Reflections.methodIsBridge.invoke(method);
                if (isSynthetic || isBridge) {
                    continue;
                }

                // 3. Match found. Bypass accessibility restrictions and execute.
                Reflections.setMethodAccessable.invoke(method, true);
                Reflections.invokeMethodHandle.invoke(method, textureInstance, new Object[0]);

                return; // Exit after successful execution
            }

        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Failed to find or execute the cross-platform void method", t);
        }
    }

    public static byte[] readAllBytes(Object pathInstance) {
        if (pathInstance == null) {
            throw new IllegalArgumentException("Path instance cannot be null");
        }
        try {
            return (byte[]) filesReadAllBytesHandle.invoke(pathInstance);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to read all bytes from path", t);
        }
    }

    public static byte[] readAllBytes(String filePath) {
        try {
            Object pathInstance = pathOfStringHandle.invoke(filePath, new String[0]);

            return (byte[]) filesReadAllBytesHandle.invoke(pathInstance);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to read bytes from file path: " + filePath, t);
        }
    }
}