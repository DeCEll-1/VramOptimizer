package DeCell.UI;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.state.AppDriver;
import com.fs.state.AppState;
import org.lwjgl.opengl.GL11;


import java.awt.Color;
import java.util.Random;

public class Misc {
    public static void setColor(Color c) {
        GL11.glColor4f(
                c.getRed() / 255f,
                c.getGreen() / 255f,
                c.getBlue() / 255f,
                c.getAlpha() / 255f
        );
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public static Color getRandomColor(int alpha) {
        return new Color(
                (new Random().nextInt() & 0x00FFFFFF) | (0x55 << 24)
                , true);
    }

    public static Color getRandomColor() {
        return getRandomColor(0x55);
    }

    public static Color negativeColor(Color c) {
        return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
    }

    public static Color getBrightPlayerColor() {
        return Global.getSector().getPlayerFaction().getBrightUIColor();
    }

    public static Color getBasePlayerColor() {
        return Global.getSector().getPlayerFaction().getBaseUIColor();
    }

    public static Color getDarkPlayerColor() {
        return Global.getSector().getPlayerFaction().getDarkUIColor();
    }

    public static Color getTextColor() {
        return Global.getSettings().getColor("standardTextColor");
    }

    public static Color getButtonTextColor() {
        return Global.getSettings().getColor("buttonText");
    }


    public static Color[] getColorsFromFloatMatrix(float[][] val) {
        float[][] zaza = val;
        Color[] colors = new Color[zaza.length];

        for (int i = 0; i < zaza.length; i++)
            colors[i] = new Color((int) zaza[i][0], (int) zaza[i][1], (int) zaza[i][2]);
        return colors;
    }

    public static float[][] getFloatMatrixFromColors(Color[] colors) {
        if (colors == null) return null;

        float[][] val = new float[colors.length][3];

        for (int i = 0; i < colors.length; i++) {
            if (colors[i] != null) {
                val[i][0] = colors[i].getRed();   // R
                val[i][1] = colors[i].getGreen(); // G
                val[i][2] = colors[i].getBlue();  // B
            }
        }
        return val;
    }

    public static Color floatArrayToColor(float[] colorArray) {
        if (colorArray == null || colorArray.length < 3) {
            throw new IllegalArgumentException("Array must contain at least 3 elements (RGB).");
        }

        int r = Math.max(0, Math.min(255, Math.round(colorArray[0])));
        int g = Math.max(0, Math.min(255, Math.round(colorArray[1])));
        int b = Math.max(0, Math.min(255, Math.round(colorArray[2])));

        int a = 255;
        if (colorArray.length >= 4) {
            a = Math.max(0, Math.min(255, Math.round(colorArray[3])));
        }

        return new Color(r, g, b, a);
    }

    public static float[] colorToFloatArray(Color color, boolean includeAlpha) {
        if (color == null) {
            throw new IllegalArgumentException("Color object cannot be null.");
        }

        float[] colorArray = new float[includeAlpha ? 4 : 3];

        colorArray[0] = color.getRed();
        colorArray[1] = color.getGreen();
        colorArray[2] = color.getBlue();

        if (includeAlpha) {
            colorArray[3] = color.getAlpha();
        }

        return colorArray;
    }

    public static void enableClamp() {
        GL11.glTexParameteri(3553, 10242, 33071);
        GL11.glTexParameteri(3553, 10243, 33071);
    }

    public static void disableClamp() {
        GL11.glTexParameteri(3553, 10242, 10497);
        GL11.glTexParameteri(3553, 10243, 10497);
    }

    public static void textureWrapRepeat() {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
    }

    public static boolean isSkin(ShipAPI ship) {
        return ship.getHullSpec().getRestoredToHullId().equals(ship.getHullSpec().getBaseHullId());
    }

    public static com.fs.starfarer.coreui.refit.U getRefitPanel() {
        return (com.fs.starfarer.coreui.refit.U) Reflections.invokeMethod("getRefitPanel", getCurrentTab());
    }

    public static Object getCurrentTab() {
        CampaignUIAPI campaignUI = Global.getSector().getCampaignUI();
        InteractionDialogAPI dialog = campaignUI.getCurrentInteractionDialog();

        Object core = dialog == null
                ? Reflections.invokeMethod("getCore", campaignUI)
                : Reflections.invokeMethod("getCoreUI", dialog);
        return Reflections.invokeMethod("getCurrentTab", core);
    }

    public static Object getScreenPanel() {
        AppState state = AppDriver.getInstance().getCurrentState();
        return Reflections.invokeMethod("getScreenPanel", state);
    }
}
