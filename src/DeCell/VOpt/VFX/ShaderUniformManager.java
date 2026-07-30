package DeCell.VOpt.VFX;

import DeCell.VOpt.VOpt;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Hashtable;

public class ShaderUniformManager {
    private final int programHandle;

    // Cache for uniform locations
    private final Hashtable<String, Integer> uniformCache = new Hashtable<>();
    private static final FloatBuffer floatArrayBuffer = BufferUtils.createFloatBuffer(512); // 16kb
    private static final IntBuffer intArrayBuffer = BufferUtils.createIntBuffer(512); // 16kb

    public ShaderUniformManager(int shaderProgramHandle) {
        this.programHandle = shaderProgramHandle;
    }

    private int getUniformLocation(String name) {
        Integer loc = uniformCache.get(name);
        if (loc == null || loc == -1) {
            loc = GL20.glGetUniformLocation(programHandle, name);
            if (loc == -1) {
                VOpt.LogWarn("Warning: Uniform '" + name + "' not found or not used in shader.");
            }
            uniformCache.put(name, loc);
        }
        return loc;
    }

    // ====================== Matrix ======================
    public ShaderUniformManager setMatrix4(String name, Matrix4f matrix) {
        int loc = getUniformLocation(name);
        if (loc == -1) return this;

        // LWJGL 2 Matrix4f -> FloatBuffer
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.store(buffer);
        buffer.flip();

        GL20.glUniformMatrix4(loc, false, buffer);   // false = do NOT transpose (column-major)
        return this;
    }

    // ====================== Vectors ======================
    public ShaderUniformManager setVector4(String name, Vector4f vector) {
        int loc = getUniformLocation(name);
        if (loc == -1) return this;
        GL20.glUniform4f(loc, vector.x, vector.y, vector.z, vector.w);
        return this;
    }

    public ShaderUniformManager setVector3(String name, Vector3f vector) {
        int loc = getUniformLocation(name);
        if (loc == -1) return this;
        GL20.glUniform3f(loc, vector.x, vector.y, vector.z);
        return this;
    }

    public ShaderUniformManager setVector2(String name, Vector2f vector) {
        int loc = getUniformLocation(name);
        if (loc == -1) return this;
        GL20.glUniform2f(loc, vector.x, vector.y);
        return this;
    }

    public ShaderUniformManager setVector4(String name, float[] vector) {
        int loc = getUniformLocation(name);
        if (loc == -1 || vector == null || vector.length < 4) return this;
        GL20.glUniform4f(loc, vector[0], vector[1], vector[2], vector[3]);
        return this;
    }

    public ShaderUniformManager setVector3(String name, float[] vector) {
        int loc = getUniformLocation(name);
        if (loc == -1 || vector == null || vector.length < 3) return this;
        GL20.glUniform3f(loc, vector[0], vector[1], vector[2]);
        return this;
    }

    public ShaderUniformManager setVector2(String name, float[] vector) {
        int loc = getUniformLocation(name);
        if (loc == -1 || vector == null || vector.length < 2) return this;
        GL20.glUniform2f(loc, vector[0], vector[1]);
        return this;
    }

    // ====================== Primitives ======================
    public ShaderUniformManager setFloat(String name, float value) {
        int loc = getUniformLocation(name);
        if (loc == -1) return this;
        GL20.glUniform1f(loc, value);
        return this;
    }

    public ShaderUniformManager setInt(String name, int value) {
        int loc = getUniformLocation(name);
        if (loc == -1) return this;
        GL20.glUniform1i(loc, value);
        return this;
    }

    // ====================== Float Array (Fixed) ======================
    public ShaderUniformManager setFloatArray(String name, float[] values) {
        int loc = getUniformLocation(name);
        if (loc == -1) return this;

        // Clear and fill buffer
        floatArrayBuffer.clear();
        floatArrayBuffer.put(values);
        floatArrayBuffer.flip();

        GL20.glUniform1(loc, floatArrayBuffer);
        return this;
    }

    public ShaderUniformManager setVector3Array(String name, Vector3f[] values) {
        int loc = getUniformLocation(name);
        if (loc == -1) return this;

        // Clear and fill buffer
        floatArrayBuffer.clear();
        for (Vector3f vector : values) {
            floatArrayBuffer.put(vector.x);
            floatArrayBuffer.put(vector.y);
            floatArrayBuffer.put(vector.z);
        }
        floatArrayBuffer.flip();

        GL20.glUniform3(loc, floatArrayBuffer);
        return this;
    }

    public ShaderUniformManager setVector4Array(String name, Vector4f[] values) {
        int loc = getUniformLocation(name);
        if (loc == -1) return this;

        // Clear and fill buffer
        floatArrayBuffer.clear();
        for (Vector4f vector : values) {
            floatArrayBuffer.put(vector.x);
            floatArrayBuffer.put(vector.y);
            floatArrayBuffer.put(vector.z);
            floatArrayBuffer.put(vector.w);
        }
        floatArrayBuffer.flip();

        GL20.glUniform4(loc, floatArrayBuffer);
        return this;
    }

    // ====================== Int Array ======================
    public ShaderUniformManager setIntArray(String name, int[] values) {
        int loc = getUniformLocation(name);
        if (loc == -1) return this;

        intArrayBuffer.clear();
        intArrayBuffer.put(values);
        intArrayBuffer.flip();

        GL20.glUniform1(loc, intArrayBuffer);
        return this;
    }

    // ====================== Textures ======================
    public ShaderUniformManager setTexture(String name, int texID, int textureUnit) {
        // textureUnit example: GL13.GL_TEXTURE0, GL13.GL_TEXTURE1, etc.

        GL13.glActiveTexture(textureUnit);
//        tex.bindTexture();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);

        int loc = getUniformLocation(name);
        if (loc != -1) {
            int unitIndex = textureUnit - GL13.GL_TEXTURE0;
            GL20.glUniform1i(loc, unitIndex);
        }
        return this;
    }

    // ====================== Colors ======================
    public ShaderUniformManager setColor4(String name, Color color) {  // or your own Color4
        setVector4(name, new Vector4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
        return this;
    }

    public ShaderUniformManager setColor4Array(String name, Color[] colors) {
        int loc = getUniformLocation(name);
        if (loc == -1) return this;

        // Clear and fill buffer
        floatArrayBuffer.clear();
        for (Color vector : colors) {
            floatArrayBuffer.put(vector.getRed());
            floatArrayBuffer.put(vector.getGreen());
            floatArrayBuffer.put(vector.getBlue());
            floatArrayBuffer.put(vector.getAlpha());
        }
        floatArrayBuffer.flip();

        GL20.glUniform4(loc, floatArrayBuffer);
        return this;
    }

    public ShaderUniformManager setColor3(String name, Color color) {
        float[] comps = color.getColorComponents(null);
        setVector3(name, comps);
        return this;
    }

    public ShaderUniformManager setColor3Array(String name, Color[] colors) {
        int loc = getUniformLocation(name);
        if (loc == -1) return this;

        floatArrayBuffer.clear();
        for (Color col : colors) {
            floatArrayBuffer.put(col.getRed() / 255f);
            floatArrayBuffer.put(col.getGreen() / 255f);
            floatArrayBuffer.put(col.getBlue() / 255f);
        }
        floatArrayBuffer.flip();

        GL20.glUniform3(loc, floatArrayBuffer);
        return this;
    }


}
