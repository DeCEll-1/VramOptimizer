package DeCell.VOpt.Commons.Rendering;

import com.fs.starfarer.api.Global;
import org.json.JSONObject;

public class TextureConfigData {
    private static TextureConfigData instance = null;


    public boolean forceLinearFiltering = true;
    public boolean forceBaseLevelZero = true;
    public boolean enableAnisotropy = false;
    public float maxAnisotropyOverride = 0.0f;
    public boolean forceRGBASwizzle = false;
    public int unpackAlignment = 4;

    public static TextureConfigData loadConfig() {
        if (instance != null) return instance;

        instance = new TextureConfigData();
        try {
            JSONObject json = Global.getSettings().loadJSON("data/config/texture_config.json");
            Global.getLogger(TextureConfigData.class).info("data/config/texture_config.json: " + json.toString());


            instance.forceLinearFiltering = json.optBoolean("forceLinearFiltering", true);
            instance.forceBaseLevelZero = json.optBoolean("forceBaseLevelZero", true);
            instance.enableAnisotropy = json.optBoolean("enableAnisotropy", false);
            instance.maxAnisotropyOverride = (float) json.optDouble("maxAnisotropyOverride", 0.0f);
            instance.forceRGBASwizzle = json.optBoolean("forceRGBASwizzle", false);
            instance.unpackAlignment = json.optInt("unpackAlignment", 4);


        } catch (Exception e) {
            Global.getLogger(TextureConfigData.class).info("Failed to load texture_config.json, using default values: " + e.getMessage());
        }
        return instance;
    }
}
