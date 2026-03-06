package com.vs.vscombo.core.gui;

import com.vs.vscombo.feature.tabs.BlocksTab;
import com.vs.vscombo.feature.tabs.MacrosTab;
import net.minecraft.client.gui.screen.Screen;
import java.util.HashMap;
import java.util.Map;

public class TabManager {
    
    private final VSMainWindow parent;
    private final Map<String, IVSTab> tabs = new HashMap<>();
    private IVSTab activeTab;
    
    private int sidebarX, sidebarY, contentX, contentY, contentW, contentH;

    public TabManager(VSMainWindow parent) {
        this.parent = parent;
        registerTab("macros1", new MacrosTab("1"));
        registerTab("macros2", new MacrosTab("2"));
        registerTab("macros3", new MacrosTab("3"));
        registerTab("macros4", new MacrosTab("4"));
        registerTab("macros5", new MacrosTab("5"));
        registerTab("blocks", new BlocksTab()); // FIX: Новая вкладка
    }
    
    public void registerTab(String id, IVSTab tab) {
        tabs.put(id, tab);
        if (activeTab == null) activeTab = tab;
    }

    public void init(int x, int y, int w, int h) {
        this.sidebarX = x;
        this.sidebarY = y;
        this.contentX = x + 120;
        this.contentY = y + 25;
        this.contentW = w - 130;
        this.contentH = h - 60 - 30;
        if (activeTab != null) activeTab.init(parent, contentX, contentY, contentW, contentH);
    }

    public void switchTab(String id) {
        IVSTab tab = tabs.get(id);
        if (tab != null) {
            if (activeTab != null) activeTab.onHide();
            activeTab = tab;
            activeTab.onShow();
            activeTab.init(parent, contentX, contentY, contentW, contentH);
            parent.reinitTabButtons();
        }
    }
    
    public IVSTab getActiveTab() { return activeTab; }
}
