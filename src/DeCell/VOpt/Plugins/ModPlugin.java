package DeCell.VOpt.Plugins;

import DeCell.VOpt.FileMetadata;
import DeCell.VOpt.Reflections;
import DeCell.VOpt.VOpt;
import com.fs.graphics.Sprite;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static DeCell.VOpt.VramCalculator.getTotalTextureVRAM;
import static org.lwjgl.opengl.GL11.*;

public class ModPlugin extends BaseModPlugin {
    private static String starsectorDirectory;

    @Override
    public void onApplicationLoad() throws Exception {

        starsectorDirectory = ModPlugin.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getFile().replace("starsector-core/../", "").replaceFirst("/", "");
        starsectorDirectory = starsectorDirectory.substring(0, starsectorDirectory.indexOf("/mods"));

        SpriteAPI handleFinderSprite = Global.getSettings().getSprite("graphics/asteroids/asteroid1.png");
        Sprite tex = Reflections.extractSprite(handleFinderSprite);
        Reflections.extractTextureDimensionsHandles(tex.getTexture());
        Reflections.extractTextureFloatHandles(tex.getTexture());


        List<ModSpecAPI> mods = Global.getSettings().getModManager().getEnabledModsCopy();
        VOpt.Log("VRAM usage before dds replacement: " + (getTotalTextureVRAM() / 1024 / 1024) + "MB");

        for (ModSpecAPI mod : mods) {
            // we want individual mods to be able to supply their own dds files
            // so we will check if they have a metadata already
            String DDSMetadata = "null";

            try {
                DDSMetadata = Global.getSettings().loadText("DDSCache/" + mod.getDirName() + "/dds_metadata.json", mod.getId());
            } catch (Exception ignored) {
                try { // fallback
                    DDSMetadata = Global.getSettings().loadText("DDSCache/" + mod.getDirName() + "/dds_metadata.json", "VramOptimizer");
                } catch (Exception ignored1) {
                }
            }

            if (Objects.equals(DDSMetadata, "null"))
                continue; // no metadata so skip it

            List<FileMetadata> list = parseFileList(DDSMetadata);
            for (FileMetadata fileMetadata : list) {
                if (Objects.equals(fileMetadata.ModID, mod.getId())) {
                    replaceFileInVram(fileMetadata);
                }
            }
        }

        try { // fallback
            String DDSMetadata = Global.getSettings().loadText("DDSCache/starsector-core/dds_metadata.json", "VramOptimizer");

            List<FileMetadata> starsectorFiles = parseFileList(DDSMetadata); // handle starsector specifically

            for (FileMetadata starsectorFile : starsectorFiles) {
                replaceFileInVram(starsectorFile);
            }
        } catch (Exception ignored1) {
        }
        VOpt.Log("VRAM usage after dds replacement: " + (getTotalTextureVRAM() / 1024 / 1024) + "MB");
    }

    public static void replaceFileInVram(FileMetadata fileMetadata) {
        String path = (fileMetadata.RelativeImagePath).replace("\\", "/");
        SpriteAPI currLoadedImage = Global.getSettings().getSprite(path);
        int texID = currLoadedImage.getTextureId();

        if (texID == 0) {
//            VOpt.LogErr("texture id found 0 for path: " + path);
            return;
        }

        currLoadedImage.setTexWidth(1f); // since our textures dont have padding theres no need to float them
        currLoadedImage.setTexHeight(1f);

        currLoadedImage.setTexWidth(fileMetadata.Width);
        currLoadedImage.setTexHeight(fileMetadata.Height);

        Sprite tex = Reflections.extractSprite(currLoadedImage);
        Reflections.setTextureFloat1(tex.getTexture(), 1);
        Reflections.setTextureFloat2(tex.getTexture(), 1);

        Reflections.setTextureWidth(tex.getTexture(), fileMetadata.Width);
        Reflections.setTextureHeight(tex.getTexture(), fileMetadata.Height);



        // since SettingsAPI does not have any way to load binary files, ill have to do it manually, using reflection
        // yippee

        // who needs unsigned bytes?
        byte[] bytes = Reflections.readAllBytes((starsectorDirectory + fileMetadata.DDSFilePath).replaceAll("\\\\", "/"));

        uploadDDSTexture(texID, fileMetadata.Width, fileMetadata.Height, bytes);
    }

    public static List<FileMetadata> parseFileList(String jsonString) throws JSONException {
        List<FileMetadata> metadataList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonString);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            FileMetadata metadata = new FileMetadata();

            metadata.ModID = obj.getString("ModID");
            metadata.ModFolderName = obj.getString("ModFolderName");
            metadata.RelativeImagePath = obj.getString("RelativeImagePath");

            String imageTypeStr = obj.optString("ImageType", "None");
            metadata.ImageType = FileMetadata.ImageFileType.valueOf(imageTypeStr);

            metadata.ImageCreationDate = OffsetDateTime.parse(obj.getString("ImageCreationDate"));
            metadata.ImageEditDateDate = OffsetDateTime.parse(obj.getString("ImageEditDateDate"));

            metadata.DDSFilePath = obj.getString("DDSFilePath");
            metadata.DDSCreationDate = OffsetDateTime.parse(obj.getString("DDSCreationDate"));
            metadata.DDSEditDate = OffsetDateTime.parse(obj.getString("DDSEditDate"));

            metadata.CompressionFormat = obj.getString("CompressionFormat");
            metadata.Width = obj.getInt("Width");
            metadata.Height = obj.getInt("Height");

            metadataList.add(metadata);
        }

        return metadataList;
    }

    public static void uploadDDSTexture(int textureId, int width, int height, byte[] ddsBytes) {
        glGetError(); // clear older errors
        // 1. Bind the existing texture ID so modifications apply to it
        GL11.glBindTexture(GL_TEXTURE_2D, textureId);

        // 128 (Standard Header) + 20 (DX10 Header) = 148 bytes total
        int headerLength = 148;
        int imageSize = ddsBytes.length - headerLength;

        // 3. Allocate a direct native ByteBuffer for the compressed payload
        ByteBuffer dataBuffer = ByteBuffer.allocateDirect(imageSize);
        dataBuffer.order(ByteOrder.nativeOrder());
        dataBuffer.put(ddsBytes, headerLength, imageSize);
        dataBuffer.flip(); // Set position to 0, limit to imageSize

        // 4. Call OpenGL using your specified parameters:
        // target         = GL_TEXTURE_2D (0x0DE1)
        // level          = 0 (Base image level)
        // width          = metadata.Width
        // height         = metadata.Height
        // border         = 0 (Must always be 0)
        // data           = direct ByteBuffer containing the compressed payload bytes

        GL13.glCompressedTexImage2D(GL_TEXTURE_2D, 0, GL42.GL_COMPRESSED_RGBA_BPTC_UNORM, width, height, 0, dataBuffer);

        int error = glGetError();
        if (error != GL_NO_ERROR)
            VOpt.LogErr("Got error " + error + " while trying to update regular texture with dds texture");
    }


}
