package DeCell.VOpt.UI.Backend.Components;

import DeCell.VOpt.UI.Backend.UIContainer;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.ui.ButtonAPI.*;
import com.fs.starfarer.api.ui.TooltipMakerAPI.*;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.*;

import java.awt.*;
import java.util.Set;

public class MyTooltip extends UIContainer<MyTooltip, TooltipMakerAPI> {

    public MyTooltip(float w, float h, boolean withScroller, MyPanel _parent) {
        super(_parent.u.createUIElement(w, h, withScroller));
        _parent.addUIElement(this);
        this.parent = _parent;
    }

    public MyTooltip(float w, float h, boolean withScroller, CustomPanelAPI parent) {
        super(parent.createUIElement(w, h, withScroller));
        parent.addUIElement(u);
    }

    public static class Builder {
        private final float width;
        private final float height;
        private boolean withScroller = false;
        private final MyPanel parent;

        public Builder(float width, float height, MyPanel parent) {
            this.width = width;
            this.height = height;
            this.parent = parent;
        }

        public Builder setWithScroller(boolean withScroller) {
            this.withScroller = withScroller;
            return this;
        }

        public MyTooltip build() {
            return new MyTooltip(width, height, withScroller, parent);
        }

    }

    //#region Text & Typography
    public LabelAPI addTitle(String text) {
        return u.addTitle(text);
    }

    public LabelAPI addTitle(String text, Color color) {
        return u.addTitle(text, color);
    }

    public LabelAPI addPara(String format, float pad, Color hl, String... highlights) {
        return u.addPara(format, pad, hl, highlights);
    }

    public LabelAPI addPara(String str, float pad) {
        return u.addPara(str, pad);
    }

    public LabelAPI addPara(String str, Color color, float pad) {
        return u.addPara(str, color, pad);
    }

    public LabelAPI addPara(String format, float pad, Color color, Color hl, String... highlights) {
        return u.addPara(format, pad, color, hl, highlights);
    }

    public LabelAPI addPara(String format, float pad, Color[] hl, String... highlights) {
        return u.addPara(format, pad, hl, highlights);
    }

    public LabelAPI addSectionHeading(String str, Alignment align, float pad) {
        return u.addSectionHeading(str, align, pad);
    }

    public LabelAPI addSectionHeading(String str, Color textColor, Color bgColor, Alignment align, float pad) {
        return u.addSectionHeading(str, textColor, bgColor, align, pad);
    }

    public LabelAPI addSectionHeading(String str, Color textColor, Color bgColor, Alignment align, float width, float pad) {
        return u.addSectionHeading(str, textColor, bgColor, align, width, pad);
    }

    public LabelAPI addParaWithMarkup(String str, float pad) {
        return u.addParaWithMarkup(str, pad);
    }

    public LabelAPI addParaWithMarkup(String str, Color color, float pad) {
        return u.addParaWithMarkup(str, color, pad);
    }

    public LabelAPI addParaWithMarkup(String str, float pad, String... tokens) {
        return u.addParaWithMarkup(str, pad, tokens);
    }

    public LabelAPI addParaWithMarkup(String str, Color color, float pad, String... tokens) {
        return u.addParaWithMarkup(str, color, pad, tokens);
    }
    //#endregion

    //#region Grid Layouts
    public Object addToGrid(int x, int y, String label, String value) {
        return u.addToGrid(x, y, label, value);
    }

    public Object addToGrid(int x, int y, String label, String value, Color valueColor) {
        return u.addToGrid(x, y, label, value, valueColor);
    }

    public void addGrid(float pad) {
        u.addGrid(pad);
    }
    //#endregion

    //#region Statistics & Fleet Modifiers
    public void addStatModGrid(float width, float valueWidth, float valuePad, float pad, MutableStat stat) {
        u.addStatModGrid(width, valueWidth, valuePad, pad, stat);
    }

    public void addStatModGrid(float width, float valueWidth, float valuePad, float pad, StatBonus stat) {
        u.addStatModGrid(width, valueWidth, valuePad, pad, stat);
    }

    public void addStatGridForShips(float width, float valueWidth, float valuePad, float pad, CampaignFleetAPI fleet, int maxNum, boolean ascending, FleetMemberValueGetter getter) {
        u.addStatGridForShips(width, valueWidth, valuePad, pad, fleet, maxNum, ascending, getter);
    }

    public void addStatModGrid(float width, float valueWidth, float valuePad, float pad, MutableStat stat, StatModValueGetter getter) {
        u.addStatModGrid(width, valueWidth, valuePad, pad, stat, getter);
    }

    public void addStatModGrid(float width, float valueWidth, float valuePad, float pad, StatBonus stat, StatModValueGetter getter) {
        u.addStatModGrid(width, valueWidth, valuePad, pad, stat, getter);
    }

    public void addStatModGrid(float width, float valueWidth, float valuePad, float pad, StatBonus stat, boolean showNonMods, StatModValueGetter getter) {
        u.addStatModGrid(width, valueWidth, valuePad, pad, stat, showNonMods, getter);
    }

    public void addStatModGrid(float width, float valueWidth, float valuePad, float pad, MutableStat stat, boolean showNonMods, StatModValueGetter getter) {
        u.addStatModGrid(width, valueWidth, valuePad, pad, stat, showNonMods, getter);
    }
    //#endregion

    //#region Images & Icons
    public void addImage(String spriteName, float pad) {
        u.addImage(spriteName, pad);
    }

    public void addImage(String spriteName, float width, float pad) {
        u.addImage(spriteName, width, pad);
    }

    public void addImage(String spriteName, float width, float height, float pad) {
        u.addImage(spriteName, width, height, pad);
    }

    public void addImages(float width, float height, float pad, float imagePad, String... spriteNames) {
        u.addImages(width, height, pad, imagePad, spriteNames);
    }

    public UIPanelAPI addImageWithText(float pad) {
        return u.addImageWithText(pad);
    }

    public void addIcons(CommodityOnMarketAPI com, int num, IconRenderMode mode) {
        u.addIcons(com, num, mode);
    }

    public void addIcons(CommoditySpecAPI com, int num, IconRenderMode mode) {
        u.addIcons(com, num, mode);
    }

    public void addIconGroup(float pad) {
        u.addIconGroup(pad);
    }

    public void addIconGroup(float rowHeight, float pad) {
        u.addIconGroup(rowHeight, pad);
    }

    public void addIconGroupAndCenter(float pad) {
        u.addIconGroupAndCenter(pad);
    }

    public void addIconGroup(float rowHeight, int rows, float pad) {
        u.addIconGroup(rowHeight, rows, pad);
    }
    //#endregion

    //#region Tables & Rows
    public Object addRow(Object... data) {
        return u.addRow(data);
    }

    public Object addRowWithGlow(Object... data) {
        return u.addRowWithGlow(data);
    }

    public void addTable(String emptyText, int andMore, float pad) {
        u.addTable(emptyText, andMore, pad);
    }
    //#endregion

    //#region Tooltips
    public void addTooltipToAddedRow(TooltipCreator tc, TooltipLocation loc) {
        u.addTooltipToAddedRow(tc, loc);
    }

    public void addTooltipToAddedRow(TooltipCreator tc, TooltipLocation loc, boolean recreateEveryFrame) {
        u.addTooltipToAddedRow(tc, loc, recreateEveryFrame);
    }

    public void addTooltipToPrevious(TooltipCreator tc, TooltipLocation loc) {
        u.addTooltipToPrevious(tc, loc);
    }

    public void addTooltipToPrevious(TooltipCreator tc, TooltipLocation loc, boolean recreateEveryFrame) {
        u.addTooltipToPrevious(tc, loc, recreateEveryFrame);
    }

    public void addTableHeaderTooltip(int colIndex, TooltipCreator tc) {
        u.addTableHeaderTooltip(colIndex, tc);
    }

    public void addTableHeaderTooltip(int colIndex, String text) {
        u.addTableHeaderTooltip(colIndex, text);
    }

    public void addTooltipTo(TooltipCreator tc, UIComponentAPI to, TooltipLocation loc) {
        u.addTooltipTo(tc, to, loc);
    }

    public void addTooltipTo(TooltipCreator tc, UIComponentAPI to, TooltipLocation loc, boolean recreateEveryFrame) {
        u.addTooltipTo(tc, to, loc, recreateEveryFrame);
    }
    //#endregion

    //#region Interactive Elements (Buttons, Inputs, Checkboxes)
    public ButtonAPI addButton(String text, Object data, float width, float height, float pad) {
        return u.addButton(text, data, width, height, pad);
    }

    public ButtonAPI addButton(String text, Object data, Color base, Color bg, float width, float height, float pad) {
        return u.addButton(text, data, base, bg, width, height, pad);
    }

    public ButtonAPI addButton(String text, Object data, Color base, Color bg, Alignment align, CutStyle style, float width, float height, float pad) {
        return u.addButton(text, data, base, bg, align, style, width, height, pad);
    }

    public ButtonAPI addAreaCheckbox(String text, Object data, Color base, Color bg, Color bright, float width, float height, float pad) {
        return u.addAreaCheckbox(text, data, base, bg, bright, width, height, pad);
    }

    public ButtonAPI addAreaCheckbox(String text, Object data, Color base, Color bg, Color bright, float width, float height, float pad, boolean leftAlign) {
        return u.addAreaCheckbox(text, data, base, bg, bright, width, height, pad, leftAlign);
    }

    public TextFieldAPI addTextField(float width, float pad) {
        return u.addTextField(width, pad);
    }

    public TextFieldAPI addTextField(float width, String font, float pad) {
        return u.addTextField(width, font, pad);
    }

    public TextFieldAPI addTextField(float width, float height, String font, float pad) {
        return u.addTextField(width, height, font, pad);
    }

    public ButtonAPI addCheckbox(float width, float height, String text, Object data, UICheckboxSize size, float pad) {
        return u.addCheckbox(width, height, text, data, size, pad);
    }

    public ButtonAPI addCheckbox(float width, float height, String text, Object data, String font, Color textColor, UICheckboxSize size, float pad) {
        return u.addCheckbox(width, height, text, data, font, textColor, size, pad);
    }

    @Deprecated
    public ButtonAPI addCheckbox(float width, float height, String text, UICheckboxSize size, float pad) {
        return u.addCheckbox(width, height, text, size, pad);
    }

    @Deprecated
    public ButtonAPI addCheckbox(float width, float height, String text, String font, Color textColor, UICheckboxSize size, float pad) {
        return u.addCheckbox(width, height, text, font, textColor, size, pad);
    }
    //#endregion

    //#region Custom Components & Layout Spacers
    public UIComponentAPI addCustom(UIComponentAPI comp, float pad) {
        return u.addCustom(comp, pad);
    }

    public UIComponentAPI addSpacer(float height) {
        return u.addSpacer(height);
    }

    public UIComponentAPI addLabelledValue(String label, String value, Color labelColor, Color valueColor, float width, float pad) {
        return u.addLabelledValue(label, value, labelColor, valueColor, width, pad);
    }

    public UIComponentAPI addCustomDoNotSetPosition(UIComponentAPI comp) {
        return u.addCustomDoNotSetPosition(comp);
    }
    //#endregion

    //#region Specialized Campaign Panels (Ships, Progression, Factions)
    public void addShipList(int cols, int rows, float iconSize, Color baseColor, java.util.List<FleetMemberAPI> ships, float pad) {
        u.addShipList(cols, rows, iconSize, baseColor, ships, pad);
    }

    public void addStoryPointUseInfo(float pad, float bonusXPFraction, boolean withNoSPNotification) {
        u.addStoryPointUseInfo(pad, bonusXPFraction, withNoSPNotification);
    }

    public void addStoryPointUseInfo(float pad, int numPoints, float bonusXPFraction, boolean withNoSPNotification) {
        u.addStoryPointUseInfo(pad, numPoints, bonusXPFraction, withNoSPNotification);
    }

    public void addPlaythroughDataPanel(float width, float height) {
        u.addPlaythroughDataPanel(width, height);
    }

    public void addRelationshipBar(PersonAPI person, float pad) {
        u.addRelationshipBar(person, pad);
    }

    public void addRelationshipBar(PersonAPI person, float width, float pad) {
        u.addRelationshipBar(person, width, pad);
    }

    public void addRelationshipBar(FactionAPI faction, float pad) {
        u.addRelationshipBar(faction, pad);
    }

    public void addRelationshipBar(FactionAPI faction, float width, float pad) {
        u.addRelationshipBar(faction, width, pad);
    }

    public void addRelationshipBar(float value, float pad) {
        u.addRelationshipBar(value, pad);
    }

    public void addRelationshipBar(float value, float width, float pad) {
        u.addRelationshipBar(value, width, pad);
    }

    public void addImportanceIndicator(PersonImportance importance, float width, float pad) {
        u.addImportanceIndicator(importance, width, pad);
    }

    public UIComponentAPI addSkillPanel(PersonAPI person, float pad) {
        return u.addSkillPanel(person, pad);
    }

    public UIComponentAPI addSkillPanelOneColumn(PersonAPI person, float pad) {
        return u.addSkillPanelOneColumn(person, pad);
    }

    public UIComponentAPI addSkillPanel(PersonAPI person, boolean admin, float pad) {
        return u.addSkillPanel(person, admin, pad);
    }

    public UIComponentAPI addSkillPanelOneColumn(PersonAPI person, boolean admin, float pad) {
        return u.addSkillPanelOneColumn(person, admin, pad);
    }

    public EventProgressBarAPI addEventProgressBar(BaseEventIntel intel, float pad) {
        return u.addEventProgressBar(intel, pad);
    }

    public UIComponentAPI addEventStageMarker(EventStageDisplayData data) {
        return u.addEventStageMarker(data);
    }

    public UIComponentAPI addEventProgressMarker(BaseEventIntel intel) {
        return u.addEventProgressMarker(intel);
    }

    public UIPanelAPI addSectorMap(float w, float h, StarSystemAPI system, float pad) {
        return u.addSectorMap(w, h, system, pad);
    }

    public void addCodexEntries(String title, Set<String> entryIds, boolean sort, float pad) {
        u.addCodexEntries(title, entryIds, sort, pad);
    }
    //#endregion
}
