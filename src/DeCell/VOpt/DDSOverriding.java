package DeCell.VOpt;

import DeCell.VOpt.Commons.Rendering.*;
import com.fs.graphics.Sprite;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static DeCell.VOpt.Reflection.ReflectionUtils.*;
import static DeCell.VOpt.Reflection.TextureUtils.*;
import static org.lwjgl.opengl.GL11.*;

public class DDSOverriding {
    private static String starsectorDirectory;
    private static String ddsCacheDirectory;

    private static final Map<String, String> replacedTextures = new ConcurrentHashMap<>();

    public static void HandleDDS() throws JSONException {
        updatePaths();

        // FR already does what this thing is for
        if (VOpt.frEnabled)
            VOpt.Log("FR found, skipping texture object field overriding");
        else
            UpdateHandles();

        List<ModSpecAPI> mods = Global.getSettings().getModManager().getEnabledModsCopy();
        mods.sort(Comparator.comparing(ModSpecAPI::getName));
        VOpt.Log("Starting DDS texture replacement");

        try {
            if (fileExists(ddsCacheDirectory + "starsector-core/dds_metadata.json")) {

                String DDSMetadata = readAllText(ddsCacheDirectory + "starsector-core/dds_metadata.json");

                List<FileMetadata> starsectorFiles = parseFileList(DDSMetadata); // handle starsector specifically

                for (FileMetadata starsectorFile : starsectorFiles) {
                    replaceFileInVram(starsectorFile);
                }
            } else
                VOpt.LogWarn("No metadata found for starsector, be sure to generate it");
        } catch (Exception zaza) {
            VOpt.LogErr("Error while trying to load metadata for starsector:");
            zaza.printStackTrace(System.out);
        }

        for (ModSpecAPI mod : mods) {
            // we want individual mods to be able to supply their own dds files
            // so we will check if they have a metadata already
            String DDSMetadata = "null";
            String modFolderName = mod.getDirName();
            if (Objects.equals(modFolderName, "VramOptimizer"))
                continue;


            try {
                // since wwe have the cache in the mod folder we need reflection
                if (fileExists(ddsCacheDirectory + modFolderName + "/dds_metadata.json")) {
                    DDSMetadata = readAllText(ddsCacheDirectory + modFolderName + "/dds_metadata.json");
                    VOpt.Log("Processing " + modFolderName);
                } else {
                    VOpt.LogWarn("No metadata found for " + modFolderName + ", be sure to generate it if you feel like it");
                }
            } catch (Exception zaza) {
                VOpt.LogErr("Error while trying to load metadata for: " + modFolderName);
                zaza.printStackTrace(System.out);
            }

            if (Objects.equals(DDSMetadata, "null"))
                continue; // no metadata so skip it

            List<FileMetadata> list = parseFileList(DDSMetadata);
            for (FileMetadata fileMetadata : list) {
                replaceFileInVram(fileMetadata);
            }
        }

        VOpt.Log("Ending DDS texture replacement");
    }

    private static void UpdateHandles() {
        SpriteAPI handleFinderSprite = Global.getSettings().getSprite("graphics/asteroids/asteroid1.png");
        Sprite tex = extractSprite(handleFinderSprite); // we use that specific texture to get the handles for the padding amounts
        extractTextureDimensionsHandles(tex.getTexture());
        extractTextureFloatHandles(tex.getTexture());
    }

    private static void updatePaths() {
        starsectorDirectory = System.getProperty("user.dir").replaceAll("\\\\", "/");

        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            starsectorDirectory = starsectorDirectory.replace("/starsector-core", "");
        } else if (os.contains("mac")) { // i cant test these so we just gamble
            // it probly wont work but i dont have a mac
            starsectorDirectory = starsectorDirectory.replace("/Contents/Resources/Java", "");
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            // linux has the mods next to the ss jar so
        } else {
            System.out.println("Operating System: Unknown / Other (" + os + ")");
        }

        ddsCacheDirectory = starsectorDirectory + "/mods/DDSCache/";
    }

    private static void replaceFileInVram(FileMetadata fileMetadata) {
        String path = (fileMetadata.RelativeImagePath).replace("\\", "/");
        SpriteAPI currLoadedImage = Global.getSettings().getSprite(path);
        int texID = currLoadedImage.getTextureId();

        if (texID == 0) {
//            VOpt.LogErr("texture id found 0 for path: " + path);
            return;
        }

        if (!VOpt.frEnabled) {
            // FR already does this so no need to bother with it
            currLoadedImage.setTexWidth(1f); // since our textures dont have padding theres no need to float them
            currLoadedImage.setTexHeight(1f);

            currLoadedImage.setTexWidth(fileMetadata.Width);
            currLoadedImage.setTexHeight(fileMetadata.Height);

            Sprite tex = extractSprite(currLoadedImage);
            setTextureFloat1(tex.getTexture(), 1);
            setTextureFloat2(tex.getTexture(), 1);

            setTextureWidth(tex.getTexture(), fileMetadata.Width);
            setTextureHeight(tex.getTexture(), fileMetadata.Height);
        }

        // since SettingsAPI does not have any way to load binary files, ill have to do it manually, using reflection
        // yippee

        // who needs unsigned bytes?
        byte[] bytes = null;
        try {
            bytes = readAllBytes((starsectorDirectory + fileMetadata.DDSFilePath).replaceAll("\\\\", "/"));
        } catch (Exception bruh) {
            VOpt.LogErr("Error while trying to load metadata:");
            VOpt.LogErr(fileMetadata.toString());
            throw bruh;
        }

        if (VOpt.isVerbose) {
            String newTextureSource = fileMetadata.ModID + "::" + fileMetadata.RelativeImagePath + " (" + fileMetadata.DDSFilePath + ")";

            replacedTextures.compute(fileMetadata.RelativeImagePath, (key, oldSource) -> {
                if (oldSource != null) {
                    VOpt.LogDbgVrbs("Overriding texture '" + key + "' previously provided by '" + oldSource + "' with new source: '" + newTextureSource + "'");
                } else {
                    VOpt.LogDbgVrbs("Registering texture replacement for '" + key + "' with source: '" + newTextureSource + "'");
                }
                return newTextureSource;
            });
        }
        uploadDDSTexture(texID, fileMetadata.Width, fileMetadata.Height, bytes, path);
    }

    private static List<FileMetadata> parseFileList(String jsonString) throws JSONException {
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

    private static void uploadDDSTexture(int textureId, int width, int height, byte[] ddsBytes, String path) {
        int error = TextureLoading.UploadDDSTexture(textureId, ddsBytes);
        if (error != GL_NO_ERROR)
            VOpt.LogErr("Got error " + error + " while trying to update regular texture with dds texture");
    }
}
