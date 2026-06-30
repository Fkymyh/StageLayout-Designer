package io;

import java.util.List;

import model.LayoutItem;
import model.ProjectInfo;

public class LayoutData {

    private ProjectInfo projectInfo;

    private List<LayoutItem> items;

    public LayoutData(ProjectInfo projectInfo, List<LayoutItem> items) {
        this.projectInfo = projectInfo;
        this.items = items;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }

    public List<LayoutItem> getItems() {
        return items;
    }
}
