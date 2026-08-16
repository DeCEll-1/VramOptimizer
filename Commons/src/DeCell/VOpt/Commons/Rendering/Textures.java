package DeCell.VOpt.Commons.Rendering;

import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL33;

public class Textures {

    private static TextureConfigData config = TextureConfigData.loadConfig();

    // ran before any textures are loaded (excluding launcher)
    public static void Init() {
        if (config.unpackAlignment > 0) {
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, config.unpackAlignment);
        }
    }

    // ran after glBindTexture
    public static void BeforeTextureUpload(int width, int height, int textureID, String texturePath, int textureType) {

    }

    // ran after glBindTexture
    public static void AfterTextureUpload(int width, int height, int textureID, String texturePath, int textureType) {
        if (config.forceBaseLevelZero) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
        }

        if (config.forceLinearFiltering) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }

        if (config.enableAnisotropy) {
            float maxAnisotropy = config.maxAnisotropyOverride > 0 ?
                    config.maxAnisotropyOverride :
                    GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy);
        }

        if (config.forceRGBASwizzle) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_R, GL11.GL_RED);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_G, GL11.GL_GREEN);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_B, GL11.GL_BLUE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_A, GL11.GL_ALPHA);
        }


    }
}
