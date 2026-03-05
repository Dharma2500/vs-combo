package com.vs.vscombo.core.gui;

import com.vs.vscombo.feature.tabs.MacrosTab;
import net.minecraft.client.gui.screen.Screen;
import java.util.HashMap;
import java.util.Map;

public class TabManager {
    
    private final VSMainWindow parent;
    private final Map<String, IVSTab> tabs = new HashMap<>();
    private IVSTab activeTab;
    
    private int sidebarX, sidebarY;

    public TabManager(VSMainWindow parent) {
        this.parent = parent;
        registerTab("macros1", new MacrosTab());
    }
    
    public void registerTab(String id, IVSTab tab) {
        tabs.put(id, tab);
        if (activeTab == null) activeTab = tab;
    }

    public void init(int x, int y, int w, int h) {
        this.sidebarX = x;
        this.sidebarY = y;
        if (activeTab != null) activeTab.init(parent, x + 120, y + 25, w - 130, h - 35);
    }

    public void switchTab(String id) {
        IVSTab tab = tabs.get(id);
        if (tab != null) {
            if (activeTab != null) activeTab.onHide();
            activeTab = tab;
            activeTab.onShow();
            activeTab.init(parent, sidebarX + 120, sidebarY + 25, 
                    (int)(parent.width * 0.25f) - 130, (int)(parent.height * 0.25f) - 35);
        }
    }
    
    public IVSTab getActiveTab() { return activeTab; }
}
