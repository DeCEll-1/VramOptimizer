package DeCell.VOpt.Commons.Rendering;

import com.fs.starfarer.api.*;
import org.lwjgl.*;
import org.lwjgl.opengl.*;

import java.nio.*;
import java.util.*;

import static org.lwjgl.opengl.EXTTextureCompressionS3TC.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL42.*;

public class Textures {

    @Deprecated
    public static void Init() {
        throw new UnsupportedOperationException("This method is deprecated and no longer supported. Update Fast Rendering");
    }

    @Deprecated
    public static void BeforeTextureUpload(int width, int height, int textureID, int textureType) {
        throw new UnsupportedOperationException("This method is deprecated and no longer supported. Update Fast Rendering");
    }

    @Deprecated
    public static void AfterTextureUpload(int width, int height, int textureID, int textureType) {
        throw new UnsupportedOperationException("This method is deprecated and no longer supported. Update Fast Rendering");
    }


    /**
     * Updates a subregion of a 2D texture, automatically handling both block-compressed (BC)
     * and uncompressed texture formats.
     *
     * @param target    the texture target (e.g., {@code GL11.GL_TEXTURE_2D})
     * @param textureId the OpenGL texture ID used to query the compression state
     * @param level     the level-of-detail number (base level is 0)
     * @param xoffset   texel offset in the x direction within the texture array
     * @param yoffset   texel offset in the y direction within the texture array
     * @param width     width of the texture subimage to update
     * @param height    height of the texture subimage to update
     * @param format    the format of the pixel data, or the compressed internal format if block-compressed
     * @param type      the data type of the pixel data (unused and ignored if the texture is compressed)
     * @param data      a ByteBuffer containing the subimage data; must be formatted as compressed bytes
     *                  if the texture is compressed, or raw pixel data otherwise
     */
    public static void glTexSubImage2DBCUC(int target, int textureId, int level, int xoffset,
                                           int yoffset, int width, int height, int format,
                                           int type, ByteBuffer data) {
        boolean isCompressed = isTextureCompressed(target, textureId, level);

        if (isCompressed) {
            // glCompressedTexSubImage2D uses 'format' as the compressed internal format
            // and does not require a pixel 'type' parameter.
            glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, data);
        } else {
            glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, data);
        }
    }

    //region glGetTexImage

    /**
     * Extracts pixel data or compressed blocks from an existing GPU texture into a direct
     * {@link ByteBuffer}, supporting both block-compressed (BC) and uncompressed formats.
     *
     * @param target    the texture target (e.g., {@code GL11.GL_TEXTURE_2D})
     * @param textureId the OpenGL texture ID to read from
     * @param level     the level-of-detail number of the texture array (base level is 0)
     * @param format    the pixel format (e.g., {@code GL11.GL_RGBA}) or compressed internal format
     * @param width     width of the texture image level to extract
     * @param height    height of the texture image level to extract
     * @return a newly allocated, flipped {@link ByteBuffer} containing the downloaded texture data from the GPU
     */
    public static ByteBuffer glGetTexImageBCUC(int target, int textureId, int level, int format, int width, int height) {
        // Bind the source texture to read from it
        glBindTexture(target, textureId);

        ByteBuffer buffer;

        if (isTextureCompressed(target, textureId, level)) {
            // Decompress via FBO (returns a buffer of width * height * 4 bytes)
            buffer = GetRGBAPixelsWithFBO(textureId, width, height);
            // Ensure position is at 0 ready for reading
            buffer.rewind();
        } else {
            int bytesPerPixel = getBytesPerPixel(format);
            int imageSize = width * height * bytesPerPixel;
            buffer = BufferUtils.createByteBuffer(imageSize);

            glGetTexImage(target, level, format, GL_UNSIGNED_BYTE, buffer);

            // Reset position to 0 so caller can read from the buffer
            buffer.rewind();
        }

        return buffer;
    }

    static int fboID = -1;
    static int fboTextureID = -1;
    static int fboWidth = -1;
    static int fboHeight = -1;

    private static void EnsureFBOExists(int w, int h) {
        if (fboWidth >= w && fboHeight >= h && fboID != -1)
            return;


        if (fboID != -1) {
            GL30.glDeleteFramebuffers(fboID);
            GL11.glDeleteTextures(fboTextureID);
        }

        fboWidth = w;
        fboHeight = h;


        // create the texture that the FBO will write unto
        fboTextureID = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fboTextureID);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);


        fboID = glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, fboTextureID, 0);

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE)
            throw new RuntimeException("Failed to create FBO, status code: " + status);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    private static ByteBuffer GetRGBAPixelsWithFBO(int sourceTextureId, int width, int height) {
        EnsureFBOExists(width, height);

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1); // these CANNOT be saved with push attrib

        // 1. Save state (attribs, matrices, viewport)
        GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT | GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);

        GL11.glMatrixMode(GL11.GL_PROJECTION); // set the matrix mode to projection
        GL11.glPushMatrix(); // push the current matrix onto the stack
        GL11.glLoadIdentity(); // reset the current matrix
        GL11.glOrtho(0, width, 0, height, -1, 1); // change the projection

        GL11.glMatrixMode(GL11.GL_MODELVIEW); // set the matrix mode to model
        GL11.glPushMatrix(); // push the current matrix onto the stack
        GL11.glLoadIdentity(); // reset the matrix

        // fbo
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
        GL11.glViewport(0, 0, width, height);

        // property settings
        int previousShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        GL20.glUseProgram(0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, sourceTextureId);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        // render
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex2f(0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex2f(width, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex2f(width, height);
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex2f(0.0f, height);
        GL11.glEnd();

        // read the fbo
        ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);

        // restore
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
//        int mainWindowW = (int) Global.getSettings().getScreenWidth();
//        int mainWindowH = (int) Global.getSettings().getScreenHeight();
//        glViewport(0, 0, mainWindowW, mainWindowH); // glPushAttrib GL11.GL_VIEWPORT_BIT should restore this

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix(); // pull back from the matrix stack
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopAttrib();

        GL20.glUseProgram(previousShader);
        return pixels;
    }

    private static int getBytesPerPixel(int format) {
        switch (format) {
            case GL_RED:
                return 1;
            case GL_RG:
                return 2;
            case GL_RGB:
                return 3;
            case GL_RGBA:
                return 4;
            default:
                return 4; // Default fallback assumption
        }
    }

    private static int getBaseFormatFromInternal(int internalFormat) {
        switch (internalFormat) {
            // Uncompressed Formats
            case GL_RGB8:
            case GL_RGB:
            case GL_RGB16F:
            case GL_RGB32F:
                return GL_RGB;
            case GL_RG8:
            case GL_RG16F:
            case GL_RG32F:
                return GL_RG;
            case GL_R8:
            case GL_R16F:
            case GL_R32F:
                return GL_RED;
            case GL_RGBA8:
            case GL_RGBA:
            case GL_RGBA16F:
            case GL_RGBA32F:
            case GL_SRGB8_ALPHA8:
                return GL_RGBA;

            // Block-Compressed Formats (BC1 - BC7 / S3TC / BPTC)
            // Return the format itself since compressed GL functions require the internal format token
            case GL_COMPRESSED_RGB_S3TC_DXT1_EXT: // GL_COMPRESSED_RGB_S3TC_DXT1_EXT
            case GL_COMPRESSED_RGBA_S3TC_DXT1_EXT: // GL_COMPRESSED_RGBA_S3TC_DXT1_EXT
            case GL_COMPRESSED_RGBA_S3TC_DXT3_EXT: // GL_COMPRESSED_RGBA_S3TC_DXT3_EXT
            case GL_COMPRESSED_RGBA_S3TC_DXT5_EXT: // GL_COMPRESSED_RGBA_S3TC_DXT5_EXT
            case GL_COMPRESSED_RGBA_BPTC_UNORM: // GL_COMPRESSED_RGBA_BPTC_UNORM
            case GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM: // GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM
            case GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT: // GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT
            case GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT: // GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT
                return internalFormat;

            default:
                // Fallback: if it's an unrecognized format, return it directly
                // rather than defaulting to GL_RGBA which causes driver errors on compressed data.
                return internalFormat;
        }
    }

    //endregion

    //region texture compression checker
    private static final Map<Integer, Boolean> textureCompressedOrNotMap = new HashMap<>();

    public static boolean isTextureCompressed(int target, int textureId, int level) {
        return textureCompressedOrNotMap.computeIfAbsent(textureId, id -> {
            int previousTextureId = glGetInteger(GL_TEXTURE_BINDING_2D);
            glBindTexture(target, textureId);

            int internalFormat = glGetTexLevelParameteri(target, level, GL_TEXTURE_INTERNAL_FORMAT);
            boolean compressed = isCompressedFormat(internalFormat);

            glBindTexture(target, previousTextureId);
            return compressed;
        });
    }

    public static boolean isCompressedFormat(int internalFormat) {
        switch (internalFormat) {
            // DXT / BC Formats (Common DDS)
            case GL_COMPRESSED_RGB_S3TC_DXT1_EXT:
            case GL_COMPRESSED_RGBA_S3TC_DXT1_EXT:
            case GL_COMPRESSED_RGBA_S3TC_DXT3_EXT:
            case GL_COMPRESSED_RGBA_S3TC_DXT5_EXT:
                // BC7 / BPTC Formats
            case GL_COMPRESSED_RGBA_BPTC_UNORM:
            case GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM:
            case GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT:
            case GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT:
                return true;
            default:
                return false;
        }
    }
    //endregion
}
