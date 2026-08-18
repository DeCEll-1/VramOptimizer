package DeCell.VOpt.Reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;

import static DeCell.VOpt.VOpt.LogDbg;


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

    public static final Class<?> linkOptionArrayClass;
    public static final MethodHandle filesExistsHandle;
    public static final MethodHandle fieldGetModifiersHandle;

    public static final Class<?> fileIOClass;
    public static final MethodHandle fileIOConstructorHandle;
    public static final MethodHandle fileIOExistsHandle;

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

            linkOptionArrayClass = Class.forName("[Ljava.nio.file.LinkOption;", false, Class.class.getClassLoader());

            filesExistsHandle = lookup.findStatic(
                    filesClass,
                    "exists",
                    MethodType.methodType(boolean.class, pathClass, linkOptionArrayClass)
            );

            fieldGetModifiersHandle = lookup.findVirtual(fieldClass, "getModifiers", MethodType.methodType(int.class));

            fileIOClass = Class.forName("java.io.File", false, Class.class.getClassLoader());

            // Constructors use 'void.class' as their return type descriptor in MethodTypes
            fileIOConstructorHandle = lookup.findConstructor(
                    fileIOClass,
                    MethodType.methodType(void.class, String.class)
            );

            fileIOExistsHandle = lookup.findVirtual(
                    fileIOClass,
                    "exists",
                    MethodType.methodType(boolean.class)
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

    public static byte[] readAllBytes(String filePath) {
        try {
            Object pathInstance = pathOfStringHandle.invoke(filePath, new String[0]);

            return (byte[]) filesReadAllBytesHandle.invoke(pathInstance);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to read bytes from file path: " + filePath, t);
        }
    }

    public static String readAllText(String filePath) {
        try {
            Object pathInstance = pathOfStringHandle.invoke(filePath, new String[0]);

            return (String) filesReadStringHandle.invoke(pathInstance);
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

    private static VarHandle textureWidthHandle = null;
    private static VarHandle textureHeightHandle = null;

    public static void extractTextureDimensionsHandles(Object textureInstance) {
        LogDbg("/ extractTextureDimensionsHandles called with textureInstance: " + (textureInstance == null ? "null" : textureInstance.getClass().getName()));

        if (textureInstance == null) {
            LogDbg("textureInstance is null, returning early.");
            return;
        }

        if (textureObjectClass == null) {
            textureObjectClass = textureInstance.getClass();
            LogDbg("Initialized textureObjectClass to: " + textureObjectClass.getName());
        } else {
            LogDbg("textureObjectClass already initialized to: " + textureObjectClass.getName());
        }

        try {
            Class<?> clazz = textureObjectClass;
            LogDbg("Target class for dimensions extraction: " + clazz.getName());

            Object[] fields = (Object[]) getDeclaredFieldsHandle.invoke(clazz);
            LogDbg("Total declared fields retrieved via reflection handle: " + (fields != null ? fields.length : 0));

            for (Object field : fields) {
                LogDbg("--- Processing Field object: " + field + " ---");

                String fieldName = getFieldName(field);
                LogDbg("Field name retrieved: '" + fieldName + "'");

                if (fieldName == null) {
                    LogDbg("Field name is null, skipping field.");
                    continue;
                }

                int modifiers = (int) fieldGetModifiersHandle.invoke(field);
                LogDbg("Field modifiers integer value: " + modifiers);

                boolean isStatic = (boolean) modifierIsStatic.invoke(modifiers);
                LogDbg("Is field static? " + isStatic);

                if (isStatic) {
                    continue;
                }

                Class<?> fieldType = (Class<?>) getFieldTypeHandle.invoke(field);
                LogDbg("Field type resolved to: " + (fieldType != null ? fieldType.getName() : "null"));

                if (fieldType != int.class) {
                    LogDbg("Skipping field because type (" + fieldType + ") is not int.class");
                    continue;
                }

                VarHandle vh = getVarHandle(clazz, fieldName, int.class);
                LogDbg("VarHandle generated successfully for field: " + fieldName + ", VarHandle: " + vh);

                int value = (int) vh.get(textureInstance);
                LogDbg("Extracted integer value for field '" + fieldName + "': " + value);


                if (value == 32) {
                    LogDbg("Matched target dimension value 32 on field: " + fieldName);
                    if (textureWidthHandle == null) {
                        textureWidthHandle = vh;
                        LogDbg("Assigned textureWidthHandle to field: " + fieldName);
                    } else if (textureHeightHandle == null) {
                        textureHeightHandle = vh;
                        LogDbg("Assigned textureHeightHandle to field: " + fieldName);
                        LogDbg("Both textureWidthHandle and textureHeightHandle have been successfully found. Breaking out of loop.");
                        break;
                    } else {
                        LogDbg("Both width and height handles are already populated.");
                    }
                } else {
                    LogDbg("Value " + value + " did not match target dimension constant (32).");
                }

                LogDbg("Current state -> textureWidthHandle: " + textureWidthHandle + ", textureHeightHandle: " + textureHeightHandle);
            }
            LogDbg("Dimensions field iteration finished. Final handles -> widthHandle: " + textureWidthHandle + ", heightHandle: " + textureHeightHandle);

        } catch (Throwable t) {
            LogDbg("Exception caught during dimensions handle extraction: " + t.getClass().getName() + " - " + t.getMessage());
            throw new RuntimeException("Failed to locate texture dimension VarHandles by value", t);
        }
    }

    public static int getTextureWidth(Object textureInstance) {
        if (textureWidthHandle == null) {
            extractTextureDimensionsHandles(textureInstance);
        }
        try {
            return (int) textureWidthHandle.get(textureInstance);
        } catch (Throwable t) {
            return -1;
        }
    }

    public static int getTextureHeight(Object textureInstance) {
        if (textureHeightHandle == null) {
            extractTextureDimensionsHandles(textureInstance);
        }
        try {
            return (int) textureHeightHandle.get(textureInstance);
        } catch (Throwable t) {
            return -1;
        }
    }

    public static void setTextureWidth(Object textureInstance, int width) {
        if (textureWidthHandle == null) {
            extractTextureDimensionsHandles(textureInstance);
        }
        try {
            textureWidthHandle.set(textureInstance, width);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to set texture width via VarHandle", t);
        }
    }

    public static void setTextureHeight(Object textureInstance, int height) {
        if (textureHeightHandle == null) {
            extractTextureDimensionsHandles(textureInstance);
        }
        try {
            textureHeightHandle.set(textureInstance, height);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to set texture height via VarHandle", t);
        }
    }

    private static VarHandle spriteFieldHandle = null;

    public static com.fs.graphics.Sprite extractSprite(com.fs.starfarer.api.graphics.SpriteAPI spriteAPI) {
        if (spriteAPI == null) return null;
        try {
            Class<?> clazz = spriteAPI.getClass();

            // If the handle isn't cached yet, scan for it once
            if (spriteFieldHandle == null) {
                Object[] fields = clazz.getDeclaredFields();

                for (Object field : fields) {
                    Class<?> fieldType = (Class<?>) Reflections.getFieldTypeHandle.invoke(field);

                    if (fieldType == com.fs.graphics.Sprite.class) {
                        String fieldName = Reflections.getFieldName(field);
                        spriteFieldHandle = Reflections.getVarHandle(clazz, fieldName, com.fs.graphics.Sprite.class);
                        break;
                    }
                }
            }

            if (spriteFieldHandle != null) {
                return (com.fs.graphics.Sprite) spriteFieldHandle.get(spriteAPI);
            }

        } catch (Throwable t) {
            throw new RuntimeException("Failed to extract Sprite from SpriteAPI implementation via cached reflection", t);
        }
        return null;
    }

    private static VarHandle textureFloatHandle1 = null;
    private static VarHandle textureFloatHandle2 = null;

    public static void extractTextureFloatHandles(Object textureInstance) {
        LogDbg("/ extractTextureFloatHandles called with textureInstance: " + (textureInstance == null ? "null" : textureInstance.getClass().getName()));

        if (textureInstance == null) {
            LogDbg("textureInstance is null, returning early.");
            return;
        }

        if (textureObjectClass == null) {
            textureObjectClass = textureInstance.getClass();
            LogDbg("Initialized textureObjectClass to: " + textureObjectClass.getName());
        } else {
            LogDbg("textureObjectClass already initialized to: " + textureObjectClass.getName());
        }

        try {
            Class<?> clazz = textureObjectClass;
            LogDbg("Target class for field extraction: " + clazz.getName());

            Object[] fields = clazz.getDeclaredFields();
            LogDbg("Total declared fields found: " + (fields != null ? fields.length : 0));

            for (Object field : fields) {
                LogDbg("--- Processing Field object: " + field + "---");
                Class<?> fieldType = (Class<?>) getFieldTypeHandle.invoke(field);
                LogDbg("Field type resolved to: " + (fieldType != null ? fieldType.getName() : "null"));

                if (fieldType != float.class) {
                    LogDbg("Skipping field because type (" + fieldType + ") is not float.class");
                    continue;
                }

                String fieldName = getFieldName(field);
                LogDbg("Field name retrieved: '" + fieldName + "'");

                VarHandle vh = getVarHandle(clazz, fieldName, float.class);
                LogDbg("VarHandle generated successfully for field: " + fieldName + ", VarHandle: " + vh);

                float value = (float) vh.get(textureInstance);
                LogDbg("Extracted float value for field '" + fieldName + "': " + value);

                if (value == 0.71875f) {
                    LogDbg("Matched target value 0.71875f on field: " + fieldName);
                    if (textureFloatHandle1 == null) {
                        textureFloatHandle1 = vh;
                        LogDbg("Assigned textureFloatHandle1 to field: " + fieldName);
                    } else {
                        LogDbg("textureFloatHandle1 was already assigned (current: " + textureFloatHandle1 + "), skipping assignment.");
                    }
                } else if (value == 0.625f) {
                    LogDbg("Matched target value 0.625f on field: " + fieldName);
                    if (textureFloatHandle2 == null) {
                        textureFloatHandle2 = vh;
                        LogDbg("Assigned textureFloatHandle2 to field: " + fieldName);
                    } else {
                        LogDbg("textureFloatHandle2 was already assigned (current: " + textureFloatHandle2 + "), skipping assignment.");
                    }
                } else {
                    LogDbg("Value " + value + " did not match target constants (0.71875f or 0.625f).");
                }

                LogDbg("Current state -> textureFloatHandle1: " + textureFloatHandle1 + ", textureFloatHandle2: " + textureFloatHandle2);

                if (textureFloatHandle1 != null && textureFloatHandle2 != null) {
                    LogDbg("Both textureFloatHandle1 and textureFloatHandle2 have been successfully found. Breaking out of loop early.");
                    break;
                }
            }

            LogDbg("Field iteration finished. Final handles -> handle1: " + textureFloatHandle1 + ", handle2: " + textureFloatHandle2);
        } catch (Throwable t) {
            LogDbg("Exception caught during dimensions handle extraction: " + t.getClass().getName() + " - " + t.getMessage());

            throw new RuntimeException("Failed to locate texture float VarHandles by value", t);
        }
    }

    public static void setTextureFloat1(Object textureInstance, float value) {
        if (textureFloatHandle1 == null) {
            extractTextureFloatHandles(textureInstance);
        }
        try {
            textureFloatHandle1.set(textureInstance, value);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to set texture float 1 via VarHandle", t);
        }
    }

    public static void setTextureFloat2(Object textureInstance, float value) {
        if (textureFloatHandle2 == null) {
            extractTextureFloatHandles(textureInstance);
        }
        try {
            textureFloatHandle2.set(textureInstance, value);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to set texture float 2 via VarHandle", t);
        }
    }

    public static Object createFile(String pathname) {
        try {
            return fileIOConstructorHandle.invoke(pathname);
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }
    }

    public static boolean runFileExists(Object fileObject) {
        try {
            return (boolean) fileIOExistsHandle.invoke(fileObject);
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }
    }
}