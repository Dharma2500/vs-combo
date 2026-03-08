package com.vs.vscombo.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import com.vs.vscombo.feature.tabs.MacrosTab;
import com.vs.vscombo.feature.tabs.BlocksTab;

import java.util.ArrayList;
import java.util.List;

public class VSMainWindow extends Screen {
    
    private static VSMainWindow instance;
    private static boolean isOpen = false;
    
    private static final float WIDTH_RATIO = 0.55f;
    private static final float HEIGHT_RATIO = 0.60f;
    private static final int BG_ALPHA = 200;
    private static final int PANEL_COLOR = 0xFF1A1A1A;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int SIDEBAR_WIDTH = 120;
    private static final int BOTTOM_SECTION_HEIGHT = 60;
    
    private TabManager tabManager;
    private int panelX, panelY, panelW, panelH;
    private int contentX, contentY, contentW, contentH;
    private static boolean suppressNextChar = false;
    
    private final List<Button> tabButtons = new ArrayList<>();
    private TextFieldWidget delayField;
    private TextFieldWidget loopField;
    private Button stopButton;
    private Button executeButton;
    private Button clearButton;
    
    private int currentDelay = 50;
    private int currentLoop = 1;
    private boolean isBlocksTabActive = false;

    private VSMainWindow() {
        super(new StringTextComponent("Vitaly_Sokolov Universe"));
        this.tabManager = new TabManager(this);
    }

    public static void toggle() {
        Minecraft mc = Minecraft.getInstance();
        if (isOpen && mc.currentScreen instanceof VSMainWindow) {
            mc.displayGuiScreen(null);
            isOpen = false;
        } else if (!isOpen) {
            instance = new VSMainWindow();
            mc.displayGuiScreen(instance);
            isOpen = true;
            suppressNextChar = true;
        }
    }

    @Override
    protected void init() {
        panelW = (int)(this.width * WIDTH_RATIO);
        panelH = (int)(this.height * HEIGHT_RATIO);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
        
        contentX = panelX + SIDEBAR_WIDTH;
        contentY = panelY + 25;
        contentW = panelW - SIDEBAR_WIDTH - 10;
        contentH = panelH - 60 - BOTTOM_SECTION_HEIGHT;
        
        // Проверяем, активна ли вкладка Blocks
        isBlocksTabActive = tabManager.getActiveTab() instanceof BlocksTab;
        
        this.tabManager.init(panelX, panelY, panelW, panelH);
        
        // Sidebar buttons
        int buttonY = panelY
