package DeCell.VOpt.Reflection;

import java.lang.invoke.VarHandle;

public class TextureUtils {

    private static Class<?> textureObjectClass = null;
    private static VarHandle textureObjectIDHandle = null;
    private static VarHandle textureWidthHandle = null;
    private static VarHandle textureHeightHandle = null;
    private static VarHandle spriteFieldHandle = null;
    private static VarHandle textureFloatHandle1 = null;
    private static VarHandle textureFloatHandle2 = null;

    private static void LogDbg(String message) {
        // Replace or hook into your logging implementation
        // System.out.println(message);
    }

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

            Object dummyInstance = ReflectionUtils.newInstance(clazz, 0, markerValue);
            Object[] fields = (Object[]) ReflectionHandles.getDeclaredFieldsHandle.invoke(clazz);

            if (fields.length == 0) throw new RuntimeException("No fields found to scan.");

            for (Object field : fields) {
                String fieldName = ReflectionUtils.getFieldName(field);

                try {
                    VarHandle vh = ReflectionUtils.getVarHandle(clazz, fieldName, int.class);
                    int currentValue = (int) vh.get(dummyInstance);

                    if (currentValue == markerValue) {
                        textureObjectIDHandle = vh;
                        break;
                    }
                } catch (ReflectiveOperationException e) {
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
            Object[] methods = (Object[]) ReflectionHandles.getDeclaredFieldsHandle.invoke(clazz); // or getMethods if preferred

            for (Object method : methods) {
                int paramCount = (int) ReflectionHandles.getParameterCount.invoke(method);
                if (paramCount != 0) continue;

                Class<?> returnType = (Class<?>) ReflectionHandles.getReturnTypeHandle.invoke(method);
                if (returnType != void.class) continue;

                boolean isSynthetic = (boolean) ReflectionHandles.methodIsSynthetic.invoke(method);
                boolean isBridge = (boolean) ReflectionHandles.methodIsBridge.invoke(method);
                if (isSynthetic || isBridge) continue;

                ReflectionHandles.setMethodAccessable.invoke(method, true);
                ReflectionHandles.invokeMethodHandle.invoke(method, textureInstance, new Object[0]);
                return;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Failed to find or execute the cross-platform void method", t);
        }
    }

    public static void extractTextureDimensionsHandles(Object textureInstance) {
        if (textureInstance == null) return;

        if (textureObjectClass == null) {
            textureObjectClass = textureInstance.getClass();
        }

        try {
            Class<?> clazz = textureObjectClass;
            Object[] fields = (Object[]) ReflectionHandles.getDeclaredFieldsHandle.invoke(clazz);

            for (Object field : fields) {
                String fieldName = ReflectionUtils.getFieldName(field);
                if (fieldName == null) continue;

                int modifiers = (int) ReflectionHandles.fieldGetModifiersHandle.invoke(field);
                boolean isStatic = (boolean) ModifierHandles.modifierIsStatic.invoke(modifiers);
                if (isStatic) continue;

                Class<?> fieldType = (Class<?>) ReflectionHandles.getFieldTypeHandle.invoke(field);
                if (fieldType != int.class) continue;

                VarHandle vh = ReflectionUtils.getVarHandle(clazz, fieldName, int.class);
                int value = (int) vh.get(textureInstance);

                if (value == 32) {
                    if (textureWidthHandle == null) {
                        textureWidthHandle = vh;
                    } else if (textureHeightHandle == null) {
                        textureHeightHandle = vh;
                        break;
                    }
                }
            }
        } catch (Throwable t) {
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

    public static com.fs.graphics.Sprite extractSprite(com.fs.starfarer.api.graphics.SpriteAPI spriteAPI) {
        if (spriteAPI == null) return null;
        try {
            Class<?> clazz = spriteAPI.getClass();

            if (spriteFieldHandle == null) {
                Object[] fields = (Object[]) ReflectionHandles.getDeclaredFieldsHandle.invoke(clazz);

                for (Object field : fields) {
                    Class<?> fieldType = (Class<?>) ReflectionHandles.getFieldTypeHandle.invoke(field);

                    if (fieldType == com.fs.graphics.Sprite.class) {
                        String fieldName = ReflectionUtils.getFieldName(field);
                        spriteFieldHandle = ReflectionUtils.getVarHandle(clazz, fieldName, com.fs.graphics.Sprite.class);
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

    public static void extractTextureFloatHandles(Object textureInstance) {
        if (textureInstance == null) return;

        if (textureObjectClass == null) {
            textureObjectClass = textureInstance.getClass();
        }

        try {
            Class<?> clazz = textureObjectClass;
            Object[] fields = (Object[]) ReflectionHandles.getDeclaredFieldsHandle.invoke(clazz);

            for (Object field : fields) {
                Class<?> fieldType = (Class<?>) ReflectionHandles.getFieldTypeHandle.invoke(field);
                if (fieldType != float.class) continue;

                String fieldName = ReflectionUtils.getFieldName(field);
                VarHandle vh = ReflectionUtils.getVarHandle(clazz, fieldName, float.class);
                float value = (float) vh.get(textureInstance);

                if (value == 0.71875f) {
                    if (textureFloatHandle1 == null) textureFloatHandle1 = vh;
                } else if (value == 0.625f) {
                    if (textureFloatHandle2 == null) textureFloatHandle2 = vh;
                }

                if (textureFloatHandle1 != null && textureFloatHandle2 != null) {
                    break;
                }
            }
        } catch (Throwable t) {
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
}