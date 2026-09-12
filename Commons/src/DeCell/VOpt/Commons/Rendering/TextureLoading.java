package DeCell.VOpt.Commons.Rendering;

import org.lwjgl.opengl.*;

import java.nio.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL42.*;

public class TextureLoading {
    private static final int textureType = GL_COMPRESSED_RGBA_BPTC_UNORM;

    /// &nbsp;0 success <br>
    /// -1 header could not be parsed <br>
    /// -2 dds magic number was not found in first four bytes <br>
    /// +* opengl error codes <br>
    public static int UploadDDSTexture(int textureID, byte[] fileBytes) {
        if (!IsDDSFile(fileBytes))
            return -2;
        while (glGetError() != GL_NO_ERROR) ; // clear older errors
        System.out.println(textureID);

        glBindTexture(GL_TEXTURE_2D, textureID);

        ByteBuffer wrapBuffer = ByteBuffer.wrap(fileBytes);
        wrapBuffer.order(ByteOrder.LITTLE_ENDIAN);
        DDSInfo info = new DDSInfo(wrapBuffer);

        assert info.width % 4 == 0 : "original DDS texture width MUST be divisable by 4" +
                "\n" + info.toString();
        assert info.height % 4 == 0 : "original DDS texture height MUST be divisable by 4" +
                "\n" + info.toString();

        if (!info.isValid) {
            return -1;
        }

        int dataSize = fileBytes.length - info.headerSize;

        ByteBuffer dataBuffer = ByteBuffer.allocateDirect(dataSize);
        dataBuffer.order(ByteOrder.nativeOrder());
        dataBuffer.put(fileBytes, info.headerSize, dataSize);
        dataBuffer.flip();

//        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 4); // this only effects performance for upload
//        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

        int calculatedPayloadSize = 0;
        int currentOffset = 0;
        for (int i = 0; i < info.mipCount; i++) {

            int mipWidth = (info.width >> i);
            int mipHeight = (info.height >> i);

            int paddedWidth = (mipWidth + 3) / 4 * 4;
            int paddedHeight = (mipHeight + 3) / 4 * 4;
            int mipSize = (paddedWidth / 4) * (paddedHeight / 4) * 16;
            calculatedPayloadSize += mipSize;

            dataBuffer.position(currentOffset);
            dataBuffer.limit(currentOffset + mipSize);
            ByteBuffer levelBuffer = dataBuffer.slice();
            levelBuffer.order(ByteOrder.nativeOrder());

            // Pass loop index 'i' as the mipmap level parameter
            GL13.glCompressedTexImage2D(GL_TEXTURE_2D, i, textureType, mipWidth, mipHeight, 0, levelBuffer);

            int glErr = glGetError();
            if (glErr != GL_NO_ERROR) {
                return glErr;
            }

            currentOffset += mipSize;
            if (currentOffset >= info.payloadSize) break;
        }

        assert calculatedPayloadSize == info.payloadSize :
                "DDS payload size mismatch! Calculated: " + calculatedPayloadSize +
                        ", Expected in header/file: " + info.payloadSize;

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, info.mipCount - 1);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, 0);

        int err = glGetError();
        return err;
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
