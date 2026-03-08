package com.vs.vscombo.core.gui;

import com.vs.vscombo.feature.tabs.MacrosTab;
import com.vs.vscombo.feature.tabs.BlocksTab;

import java.util.HashMap;
import java.util.Map;

public class TabManager {
    
    private final Map<String, IVSTab> tabs = new HashMap<>();
    private IVSTab activeTab;
    private VSMainWindow mainWindow;

    public TabManager(VSMainWindow mainWindow) {
        this.mainWindow = mainWindow;
        initializeTabs();
    }

    private void initializeTabs() {
        // Macros вкладки
        tabs.put("macros1", new MacrosTab("macros1"));
        tabs.put("macros2", new MacrosTab("macros2"));
        tabs.put("macros3", new MacrosTab("macros3"));
        tabs.put("macros4", new MacrosTab("macros4"));
        tabs.put("macros5", new MacrosTab("macros5"));
        
        // Вкладка Blocks
        tabs.put("blocks", new BlocksTab());
        
        // Активная вкладка по умолчанию
        activeTab = tabs.get("macros1");
    }

    public void init(int panelX, int panelY, int panelW, int panelH) {
        for (IVSTab tab : tabs.values()) {
            tab.init(mainWindow, panelX, panelY, panelW, panelH);
        }
    }

    public void switchTab(String tabId) {
        if (tabs.containsKey(tabId)) {
            if (activeTab != null) {
                activeTab.onHide();
            }
            activeTab = tabs.get(tabId);
            activeTab.onShow();
        }
    }

    public IVSTab getActiveTab() {
        return activeTab;
    }
}
