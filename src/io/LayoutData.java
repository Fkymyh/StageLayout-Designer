package io;

import java.util.List;

import model.DrawLine;
import model.LayoutItem;
import model.ProjectInfo;
import model.RoomObject;

public class LayoutData {

    private ProjectInfo projectInfo;

    private List<LayoutItem> items;
    
    private List<RoomObject> customRoomObjects;
    
    private List<DrawLine> drawLines;

    public LayoutData(ProjectInfo projectInfo, List<LayoutItem> items,
    		 List<RoomObject> customRoomObjects,List<DrawLine> drawLines) {
        this.projectInfo = projectInfo;
        this.items = items;
        this.customRoomObjects = customRoomObjects;
        this.drawLines = drawLines;
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
    
}
