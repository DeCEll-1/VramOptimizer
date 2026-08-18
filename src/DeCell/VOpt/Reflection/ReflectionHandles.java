package DeCell.VOpt.Reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ReflectionHandles {
    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static final Class<?> fieldClass;
    public static final Class<?> fieldArrayClass;
    public static final Class<?> methodClass;
    public static final Class<?> typeClass;
    public static final Class<?> typeArrayClass;
    public static final Class<?> parameterizedTypeClass;
    public static final Class<?> constructorClass;
    public static final Class<?> constructorArrayClass;
    public static final Class<?> classArrayClass;

    public static final MethodHandle getFieldTypeHandle;
    public static final MethodHandle setFieldHandle;
    public static final MethodHandle getFieldHandle;
    public static final MethodHandle getFieldNameHandle;
    public static final MethodHandle setFieldAccessibleHandle;
    public static final MethodHandle fieldGetModifiersHandle;
    public static final MethodHandle getParameterCount;
    public static final MethodHandle getMethodNameHandle;
    public static final MethodHandle getMethodHandle;
    public static final MethodHandle getDeclaredMethodHandle;
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
    public static final MethodHandle methodIsSynthetic;
    public static final MethodHandle methodIsBridge;

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
            classArrayClass = Class.forName("[Ljava.lang.Class;", false, Class.class.getClassLoader());

            setFieldHandle = LOOKUP.findVirtual(fieldClass, "set", MethodType.methodType(void.class, Object.class, Object.class));
            getFieldHandle = LOOKUP.findVirtual(fieldClass, "get", MethodType.methodType(Object.class, Object.class));
            getFieldNameHandle = LOOKUP.findVirtual(fieldClass, "getName", MethodType.methodType(String.class));
            getFieldTypeHandle = LOOKUP.findVirtual(fieldClass, "getType", MethodType.methodType(Class.class));
            setFieldAccessibleHandle = LOOKUP.findVirtual(fieldClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
            fieldGetModifiersHandle = LOOKUP.findVirtual(fieldClass, "getModifiers", MethodType.methodType(int.class));

            getMethodNameHandle = LOOKUP.findVirtual(methodClass, "getName", MethodType.methodType(String.class));
            getMethodHandle = LOOKUP.findVirtual(Class.class, "getMethod", MethodType.methodType(methodClass, String.class, Class[].class));
            getDeclaredMethodHandle = LOOKUP.findVirtual(Class.class, "getDeclaredMethod", MethodType.methodType(methodClass, String.class, classArrayClass));
            invokeMethodHandle = LOOKUP.findVirtual(methodClass, "invoke", MethodType.methodType(Object.class, Object.class, Object[].class));
            setMethodAccessable = LOOKUP.findVirtual(methodClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
            getModifiersHandle = LOOKUP.findVirtual(methodClass, "getModifiers", MethodType.methodType(int.class));
            getParameterTypesHandle = LOOKUP.findVirtual(methodClass, "getParameterTypes", MethodType.methodType(Class[].class));
            getReturnTypeHandle = LOOKUP.findVirtual(methodClass, "getReturnType", MethodType.methodType(Class.class));
            getParameterCount = LOOKUP.findVirtual(methodClass, "getParameterCount", MethodType.methodType(int.class));
            getDeclaringClass = LOOKUP.findVirtual(methodClass, "getDeclaringClass", MethodType.methodType(Class.class));
            methodIsSynthetic = LOOKUP.findVirtual(methodClass, "isSynthetic", MethodType.methodType(boolean.class));
            methodIsBridge = LOOKUP.findVirtual(methodClass, "isBridge", MethodType.methodType(boolean.class));

            getGenericTypeHandle = LOOKUP.findVirtual(fieldClass, "getGenericType", MethodType.methodType(typeClass));
            getTypeNameHandle = LOOKUP.findVirtual(typeClass, "getTypeName", MethodType.methodType(String.class));
            getActualTypeArgumentsHandle = LOOKUP.findVirtual(parameterizedTypeClass, "getActualTypeArguments", MethodType.methodType(typeArrayClass));

            setConstructorAccessibleHandle = LOOKUP.findVirtual(constructorClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
            getConstructorParameterTypesHandle = LOOKUP.findVirtual(constructorClass, "getParameterTypes", MethodType.methodType(Class[].class));
            constructorNewInstanceHandle = LOOKUP.findVirtual(constructorClass, "newInstance", MethodType.methodType(Object.class, Object[].class));
            constructorGetDeclaringClass = LOOKUP.findVirtual(constructorClass, "getDeclaringClass", MethodType.methodType(Class.class));

            getDeclaredConstructorsHandle = LOOKUP.findVirtual(Class.class, "getDeclaredConstructors", MethodType.methodType(constructorArrayClass));
            getDeclaredFieldsHandle = LOOKUP.findVirtual(Class.class, "getDeclaredFields", MethodType.methodType(fieldArrayClass));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}