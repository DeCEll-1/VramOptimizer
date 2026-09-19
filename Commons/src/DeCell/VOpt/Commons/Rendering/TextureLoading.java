package DeCell.VOpt.Commons.Rendering;

import com.fs.starfarer.api.*;
import org.lwjgl.*;
import org.lwjgl.opengl.*;

import java.nio.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL42.*;

public class TextureLoading {
    private static final int textureType = GL_COMPRESSED_RGBA_BPTC_UNORM;

    static final int BlockSize = 4;
    static final int BytesPerBlock = 16; // BC7 (BPTC) is 16 bytes per 4x4 block

    /// &nbsp;0 success <br>
    /// -1 header could not be parsed <br>
    /// -2 dds magic number was not found in first four bytes <br>
    /// +* opengl error codes <br>
    public static int UploadDDSTexture(int textureID, byte[] fileBytes) {
        if (!IsDDSFile(fileBytes))
            return -2;

        glDeleteTextures(textureID);

        // Clear pre-existing OpenGL errors
        while (glGetError() != GL_NO_ERROR) ;


        final boolean nomips = Global.getSettings().getBoolean("VOpt_no_mipmap");

        glBindTexture(GL_TEXTURE_2D, textureID);

        ByteBuffer wrapBuffer = ByteBuffer.wrap(fileBytes);
        wrapBuffer.order(ByteOrder.LITTLE_ENDIAN);
        DDSInfo info = new DDSInfo(wrapBuffer);

        if (!info.isValid) {
            return -1;
        }

        int mipLevelsToLoad = nomips ? 1 : info.mipCount;

        int dataSize = fileBytes.length - info.headerSize;
        if (nomips) {
            dataSize = GetPayloadByteAmount(info.width, info.height);
        }
        ByteBuffer dataBuffer = BufferUtils.createByteBuffer(dataSize);
        dataBuffer.put(fileBytes, info.headerSize, dataSize);
        dataBuffer.clear();

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);

        if (nomips) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        } else {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, info.mipCount - 1);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        }

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL_FALSE);

        int currentOffset = 0;

        for (int i = 0; i < mipLevelsToLoad; i++) {
            int mipWidth = Math.max(1, info.width >> i);
            int mipHeight = Math.max(1, info.height >> i);

            int payloadByteAmount = GetPayloadByteAmount(mipWidth, mipHeight);

            if (currentOffset + payloadByteAmount > dataSize) {
                break; // Buffer overflow guard
            }

            dataBuffer.position(currentOffset);
            dataBuffer.limit(currentOffset + payloadByteAmount);
            ByteBuffer levelBuffer = dataBuffer.slice();
            levelBuffer.order(ByteOrder.nativeOrder());

            // Upload BC7 Compressed Level
            GL13.glCompressedTexImage2D(
                    GL_TEXTURE_2D,
                    i,
                    GL42.GL_COMPRESSED_RGBA_BPTC_UNORM,
                    mipWidth,
                    mipHeight,
                    0,
                    levelBuffer
            );

            int glErr = glGetError();
            if (glErr != GL_NO_ERROR) {
                return glErr;
            }

            currentOffset += payloadByteAmount;
        }

        return GL_NO_ERROR;
    }

    private static int GetPayloadByteAmount(int mipWidth, int mipHeight) {
        int paddedWidth = (mipWidth + BlockSize - 1) / BlockSize * BlockSize;
        int paddedHeight = (mipHeight + BlockSize - 1) / BlockSize * BlockSize;

        // Compute blocks: minimum 1 block (4x4 pixels) even if dimension < 4
        int blockRows = paddedHeight / BlockSize;
        int blockColumns = paddedWidth / BlockSize;
        int payloadByteAmount = blockRows * blockColumns * BytesPerBlock;
        return payloadByteAmount;
    }

    private static int GetNextMultipleOf4(int n) {return (int) Math.max(4, (Math.ceil((n) / 4d) * 4));}

    public static boolean IsDDSFile(byte[] fileBytes) {
        // Check if the array is null or too short to contain the 4-byte signature
        if (fileBytes == null || fileBytes.length < 4) {
            return false;
        }

        // DDS magic number: 'D', 'D', 'S', ' ' (0x44, 0x44, 0x53, 0x20)
        return fileBytes[0] == 0x44 &&
                fileBytes[1] == 0x44 &&
                fileBytes[2] == 0x53 &&
                fileBytes[3] == 0x20;
    }
}
