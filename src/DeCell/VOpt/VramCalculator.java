package DeCell.VOpt;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL11.*;

public class VramCalculator {
    public static long calculateDDSVRAMUsage(int width, int height, String compressionFormat) {
        if (compressionFormat == null) {
            return (long) width * height * 4; // Default safe fallback to RGBA8
        }

        String format = compressionFormat.toUpperCase().trim();

        // Calculate block grid for compressed formats (4x4 blocks, minimum 1 block)
        long blockWidth = Math.max(1, (width + 3) / 4);
        long blockHeight = Math.max(1, (height + 3) / 4);
        long totalBlocks = blockWidth * blockHeight;

        // Match format against known compression and uncompressed strings
        return switch (format) {
            // --- Uncompressed & Float Formats ---
            case "RGB", "RGB8", "B8G8R8" -> (long) width * height * 3;
            case "RGBA", "RGBA8", "BGRA8", "ARGB8" -> (long) width * height * 4;
            case "RGB565", "ARGB4444", "RGBA4444" -> (long) width * height * 2;
            case "RGB16" -> (long) width * height * 6;  // 2 bytes per channel * 3 channels
            case "R32F" -> (long) width * height * 4;   // 32-bit float single channel

            // --- Depth and Stencil Formats ---
            case "DEPTH24_STENCIL8" -> (long) width * height * 4; // 32 bits total (24-bit depth + 8-bit stencil)
            case "STENCIL_INDEX8" -> (long) width * height;      // 8 bits (1 byte) per pixel

            // --- DXT1 / BC1 (4 bytes per 4x4 block -> 0.5 bpp) ---
            case "DXT1", "BC1", "BC1_UNORM" -> totalBlocks * 4;

            // --- DXT2 / DXT3 / BC2 (16 bytes per 4x4 block -> 4 bpp) ---
            case "DXT2", "DXT3", "BC2", "BC2_UNORM" -> totalBlocks * 16;

            // --- DXT4 / DXT5 / BC3 (16 bytes per 4x4 block -> 4 bpp) ---
            case "DXT4", "DXT5", "BC3", "BC3_UNORM" -> totalBlocks * 16;

            // --- BC4 / ATI1 (8 bytes per 4x4 block -> Red only) ---
            case "BC4", "BC4_UNORM", "ATI1" -> totalBlocks * 8;

            // --- BC5 / ATI2 (16 bytes per 4x4 block -> RG channels) ---
            case "BC5", "BC5_UNORM", "ATI2" -> totalBlocks * 16;

            // --- BC7 / BPTC (16 bytes per 4x4 block -> High quality modern compression) ---
            case "BC7", "BC7_UNORM", "COMPRESSED_RGBA_BPTC_UNORM" -> totalBlocks * 16;

            // Default fallback for unrecognized formats (treat as RGBA8 uncompressed)
            default -> (long) width * height * 4;
        };
    }

    public static long getTotalTextureVRAM() {
        long totalVRAMBytes = 0;
        int consecutiveFailures = 0;
        final int MAX_CONSECUTIVE_FAILURES = 100;

        int id = 1;
        while (consecutiveFailures < MAX_CONSECUTIVE_FAILURES) {
            if (GL11.glIsTexture(id)) {
                // Found a valid texture handle -> reset failure counter
                consecutiveFailures = 0;

                // Bind to inspect parameters
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

                // Fetch metadata for mip level 0
                int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
                int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
                int internalFormat = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_INTERNAL_FORMAT);

                // Avoid processing 0x0 or deleted/uninitialized textures
                if (width > 0 && height > 0) {
                    String formatName = translateGLFormatToString(internalFormat);
                    totalVRAMBytes += calculateDDSVRAMUsage(width, height, formatName);
                }
            } else {
                // Increment failure count for consecutive misses
                consecutiveFailures++;
            }

            // Increment ID for next iteration
            id++;
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return totalVRAMBytes;
    }

    private static String translateGLFormatToString(int internalFormat) {
        // Maps standard OpenGL internal format integers back to string tokens matching your formats
        return switch (internalFormat) {
            case GL11.GL_RGBA8 -> "RGBA8";
            case GL30.GL_DEPTH24_STENCIL8 -> "DEPTH24_STENCIL8";
            case GL11.GL_RGBA -> "RGBA";
            case GL42.GL_COMPRESSED_RGBA_BPTC_UNORM -> "BC7";
            case GL30.GL_R32F -> "R32F";
            case GL11.GL_RGB8 -> "RGB8";
            case GL11.GL_RGB16 -> "RGB16";
            case GL30.GL_STENCIL_INDEX8 -> "STENCIL_INDEX8";
            case 0x83F1 -> "DXT1"; // GL_COMPRESSED_RGBA_S3TC_DXT1_EXT
            case 0x83F3 -> "DXT5"; // GL_COMPRESSED_RGBA_S3TC_DXT5_EXT
            default -> "RGBA8";   // Default fallback uncompressed
        };
    }
}
