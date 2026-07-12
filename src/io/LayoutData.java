package io;

import java.util.List;

import model.BackgroundMap;
import model.DrawLine;
import model.LayoutItem;
import model.ProjectInfo;
import model.RoomObject;
import model.TextBoxItem;

public class LayoutData {

    private ProjectInfo projectInfo;

    private List<LayoutItem> items;
    
    private List<RoomObject> customRoomObjects;
    
    private List<DrawLine> drawLines;

    private BackgroundMap backgroundMap;

    private List<TextBoxItem> textBoxes;

    public LayoutData(ProjectInfo projectInfo,
             List<LayoutItem> items,
             List<RoomObject> customRoomObjects,
             List<DrawLine> drawLines,
             BackgroundMap backgroundMap,
             List<TextBoxItem> textBoxes) {
        this.projectInfo = projectInfo;
        this.items = items;
        this.customRoomObjects = customRoomObjects;
        this.drawLines = drawLines;
        this.backgroundMap = backgroundMap;
        this.textBoxes = textBoxes;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }

    public List<LayoutItem> getItems() {
        return items;
    }
    
    public List<RoomObject> getCustomRoomObjects() {
        return customRoomObjects;
    }

    public List<DrawLine> getDrawLines() {
        return drawLines;
    }

    public BackgroundMap getBackgroundMap() {
        return backgroundMap;
    }

    public List<TextBoxItem> getTextBoxes() {
        return textBoxes;
    }
    
}
