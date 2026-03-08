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
    
    // ========== СТАТИЧЕСКИЕ ПЕРЕМЕННЫЕ ==========
    private static VSMainWindow instance;
    private static boolean isOpen = false;
    
    // ========== КОНСТАНТЫ ИНТЕРФЕЙСА ==========
    private static final float WIDTH_RATIO = 0.55f;      // 55% ширины экрана
    private static final float HEIGHT_RATIO = 0.60f;     // 60% высоты экрана
    private static final int BG_ALPHA = 200;              // Прозрачность фона
    private static final int PANEL_COLOR = 0xFF1A1A1A;    // Цвет панели
    private static final int TEXT_COLOR = 0xFFE0E0E0;     // Цвет текста
    private static final int SIDEBAR_WIDTH = 120;         // Ширина боковой панели
    private static final int BOTTOM_SECTION_HEIGHT = 60;  // Высота нижней секции
    
    // ========== ПЕРЕМЕННЫЕ ИНТЕРФЕЙСА ==========
    private TabManager tabManager;
    private int panelX, panelY, panelW, panelH;           // Позиция и размер панели
    private int contentX, contentY, contentW, contentH;   // Позиция и размер контента
    private static boolean suppressNextChar = false;      // Подавление следующего символа
    
    // ========== КНОПКИ И ПОЛЯ ВВОДА ==========
    private final List<Button> tabButtons = new ArrayList<>();
    private TextFieldWidget delayField;
    private TextFieldWidget loopField;
    private Button stopButton;
    private Button executeButton;
    private Button clearButton;
    
    // ========== НАСТРОЙКИ ==========
    private int currentDelay = 50;
    private int currentLoop = 1;
    private boolean isBlocksTabActive = false;            // Флаг: активна ли вкладка Blocks

    // ========== КОНСТРУКТОР ==========
    private VSMainWindow() {
        super(new StringTextComponent("Vitaly_Sokolov Universe"));
        this.tabManager = new TabManager(this);
    }

    // ========== ПУБЛИЧНЫЙ МЕТОД: ОТКРЫТЬ/ЗАКРЫТЬ ОКНО ==========
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

    // ========== ИНИЦИАЛИЗАЦИЯ ==========
    @Override
    protected void init() {
        // Рассчитываем размеры панели
        panelW = (int)(this.width * WIDTH_RATIO);
        panelH = (int)(this.height * HEIGHT_RATIO);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
        
        // Рассчитываем размеры области контента
        contentX = panelX + SIDEBAR_WIDTH;
        contentY = panelY + 25;
        contentW = panelW - SIDEBAR_WIDTH - 10;
        contentH = panelH - 60 - BOTTOM_SECTION_HEIGHT;
        
        // Проверяем, активна ли вкладка Blocks
        isBlocksTabActive = tabManager.getActiveTab() instanceof BlocksTab;
        
        // Инициализируем менеджер вкладок
        this.tabManager.init(panelX, panelY, panelW, panelH);
        
        // Создаём кнопки боковой панели
        int buttonY = panelY + 25;
        int buttonSpacing = 25;
        this.addButton(new Button(panelX + 5, buttonY, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#1"), btn -> switchTab("macros1")));
        this.addButton(new Button(panelX + 5, buttonY + buttonSpacing, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#2"), btn -> switchTab("macros2")));
        this.addButton(new Button(panelX + 5, buttonY + buttonSpacing * 2, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#3"), btn -> switchTab("macros3")));
        this.addButton(new Button(panelX + 5, buttonY + buttonSpacing * 3, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#4"), btn -> switchTab("macros4")));
        this.addButton(new Button(panelX + 5, buttonY + buttonSpacing * 4, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Macros#5"), btn -> switchTab("macros5")));
        this.addButton(new Button(panelX + 5, buttonY + buttonSpacing * 5, SIDEBAR_WIDTH - 10, 20,
            new StringTextComponent("Blocks"), btn -> switchTab("blocks")));
        
        // Инициализируем нижнюю секцию и кнопки вкладок
        initBottomSection();
        initTabButtons();
        syncFieldsWithTab();
    }
    
    // ========== ПЕРЕКЛЮЧЕНИЕ ВКЛАДКИ ==========
    private void switchTab(String id) {
        tabManager.switchTab(id);
        isBlocksTabActive = tabManager.getActiveTab() instanceof BlocksTab;
        syncFieldsWithTab();
        
        // Пересоздаём кнопки для вкладки Blocks
        if (isBlocksTabActive && tabManager.getActiveTab() instanceof BlocksTab) {
            initTabButtons();
        }
    }
    
    // ========== СИНХРОНИЗАЦИЯ ПОЛЕЙ С ВКЛАДКОЙ ==========
    private void syncFieldsWithTab() {
        if (tabManager.getActiveTab() instanceof MacrosTab) {
            MacrosTab tab = (MacrosTab) tabManager.getActiveTab();
            currentDelay = tab.getDelay();
            currentLoop = tab.getLoopCount();
            if (delayField != null) delayField.setText(String.valueOf(currentDelay));
            if (loopField != null) loopField.setText(String.valueOf(currentLoop));
        }
    }
    
    // ========== ИНИЦИАЛИЗАЦИЯ НИЖНЕЙ СЕКЦИИ ==========
    private void initBottomSection() {
        int bottomY = panelY + panelH - BOTTOM_SECTION_HEIGHT + 10;
        
        // Скрываем нижнюю панель для вкладки Blocks
        if (isBlocksTabActive) {
            return;
        }
        
        // Кнопка Clear
        clearButton = new Button(panelX + 5, bottomY, 60, 20,
            new StringTextComponent("Clear"), btn -> clearChat());
        this.addButton(clearButton);
        
        // Поле Delay
        this.font.drawString(new MatrixStack(), "Delay:", 
            (float)(panelX + 75), (float)(bottomY + 5), 0xFFAAAAAA);
        
        delayField = new TextFieldWidget(this.font, panelX + 120, bottomY, 50, 18,
            new StringTextComponent("Delay"));
        delayField.setText(String.valueOf(currentDelay));
        delayField.setTextColor(0xFFFFFF);
        delayField.setMaxStringLength(5);
        this.children.add(delayField);
        
        // Поле Loop
        this.font.drawString(new MatrixStack(), "Loop:", 
            (float)(panelX + 180), (float)(bottomY + 5), 0xFFAAAAAA);
        
        loopField = new TextFieldWidget(this.font, panelX + 230, bottomY, 50, 18,
            new StringTextComponent("Loop"));
        loopField.setText(String.valueOf(currentLoop));
        loopField.setTextColor(0xFFFFFF);
        loopField.setMaxStringLength(4);
        this.children.add(loopField);
        
        // Кнопка Stop
        stopButton = new Button(panelX + 290, bottomY, 60, 20,
            new StringTextComponent("Stop"), btn -> stopMacro());
        stopButton.active = true;
        this.addButton(stopButton);
        
        // Кнопка Execute
        executeButton = new Button(panelX + panelW - 85, bottomY, 75, 20,
            new StringTextComponent("Execute"), btn -> executeMacro());
        executeButton.active = true;
        this.addButton(executeButton);
    }
    
    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========
    private void clearChat() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.ingameGUI != null && mc.ingameGUI.getChatGUI() != null) {
            mc.ingameGUI.getChatGUI().clearChatMessages(true);
        }
    }
    
    private void stopMacro() {
        if (tabManager.getActiveTab() instanceof MacrosTab) {
            ((MacrosTab) tabManager.getActiveTab()).stopExecution();
        }
    }
    
    private void executeMacro() {
        if (!(tabManager.getActiveTab() instanceof MacrosTab)) return;
        MacrosTab tab = (MacrosTab) tabManager.getActiveTab();
        
        try {
            currentDelay = Integer.parseInt(delayField.getText());
            if (currentDelay < 0) currentDelay = 0;
            if (currentDelay > 10000) currentDelay = 10000;
        } catch (NumberFormatException e) { currentDelay = 50; }
        
        try {
            currentLoop = Integer.parseInt(loopField.getText());
            if (currentLoop < 1) currentLoop = 1;
            if (currentLoop > 1000) currentLoop = 1000;
        } catch (NumberFormatException e) { currentLoop = 1; }
        
        tab.updateSettings(currentDelay, currentLoop);
        tab.executeWithSettings(currentDelay, currentLoop);
    }
    
    // ========== ИНИЦИАЛИЗАЦИЯ КНОПОК ВКЛАДКИ ==========
    private void initTabButtons() {
        // Удаляем старые кнопки
        for (Button btn : tabButtons) {
            this.children.remove(btn);
            this.buttons.remove(btn);
        }
        tabButtons.clear();
        
        // Добавляем кнопки для вкладки Blocks
        if (isBlocksTabActive && tabManager.getActiveTab() instanceof BlocksTab) {
            List<Button> blockButtons = tabManager.getActiveTab().getButtons(
                contentX, contentY, contentW, contentH);
            for (Button btn : blockButtons) {
                this.addButton(btn);
                tabButtons.add(btn);
            }
        }
    }

    // ========== ОТРИСОВКА ==========
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Фон
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        AbstractGui.fill(matrixStack, 0, 0, this.width, this.height, (BG_ALPHA << 24) | 0x000000);
        
        // Панель
        AbstractGui.fill(matrixStack, panelX, panelY, panelX + panelW, panelY + panelH, PANEL_COLOR);
        
        // Границы панели
        AbstractGui.fill(matrixStack, panelX, panelY, panelX + panelW, panelY + 1, 0xFF555555);
        AbstractGui.fill(matrixStack, panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, 0xFF555555);
        
        // Разделитель нижней секции (только для Macros вкладок)
        if (!isBlocksTabActive) {
            int bottomSectionY = panelY + panelH - BOTTOM_SECTION_HEIGHT;
            AbstractGui.fill(matrixStack, panelX, bottomSectionY, panelX + panelW, bottomSectionY + 1, 0xFF555555);
        }
        
        // Текст автора
        this.font.drawString(matrixStack, "Created by Vitaly_Sokolov", 
                (float)(panelX + 10), (float)(panelY + 8), TEXT_COLOR);
        
        // Рендер активной вкладки
        if (this.tabManager.getActiveTab() != null) {
            this.tabManager.getActiveTab().render(matrixStack, mouseX, mouseY, partialTicks, 
                    contentX, contentY, contentW, contentH);
        }
        
        // Поля ввода (только для Macros вкладок)
        if (!isBlocksTabActive) {
            if (delayField != null) delayField.render(matrixStack, mouseX, mouseY, partialTicks);
            if (loopField != null) loopField.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    // ========== ОБРАБОТКА НАЖАТИЙ КЛАВИШ ==========
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (suppressNextChar && keyCode == GLFW.GLFW_KEY_X) {
            suppressNextChar = false;
            return true;
        }
        
        // Поля ввода (только для Macros вкладок)
        if (!isBlocksTabActive) {
            if (delayField != null && delayField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (loopField != null && loopField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        
        // Активная вкладка
        if (this.tabManager.getActiveTab() != null) {
            if (this.tabManager.getActiveTab().keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    // ========== ОБРАБОТКА ВВОДА ТЕКСТА ==========
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (suppressNextChar) {
            suppressNextChar = false;
            return true;
        }
        
        // Поля ввода (только для Macros вкладок)
        if (!isBlocksTabActive) {
            if (delayField != null && delayField.isFocused()) {
                if (codePoint >= '0' && codePoint <= '9') {
                    return delayField.charTyped(codePoint, modifiers);
                }
                return true;
            }
            if (loopField != null && loopField.isFocused()) {
                if (codePoint >= '0' && codePoint <= '9') {
                    return loopField.charTyped(codePoint, modifiers);
                }
                return true;
            }
        }
        
        // Активная вкладка
        if (this.tabManager.getActiveTab() != null) {
            if (this.tabManager.getActiveTab().charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }
    
    // ========== ОБРАБОТКА КЛИКОВ МЫШИ ==========
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Поля ввода (только для Macros вкладок)
        if (!isBlocksTabActive) {
            if (delayField != null && delayField.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            if (loopField != null && loopField.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        
        // Активная вкладка
        if (this.tabManager.getActiveTab() != null) {
            if (this.tabManager.getActiveTab().mouseClicked(mouseX, mouseY, button, 
                    contentX, contentY, contentW, contentH)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    // ========== ЗАКРЫТИЕ ОКНА ==========
    @Override
    public void onClose() {
        isOpen = false;
        if (delayField != null) this.children.remove(delayField);
        if (loopField != null) this.children.remove(loopField);
        super.onClose();
    }
    
    // ========== ПУБЛИЧНЫЕ МЕТОДЫ ==========
    public void reinitTabButtons() {
        initTabButtons();
    }
    
    public int getContentX() { return contentX; }
    public int getContentY() { return contentY; }
    public int getContentW() { return contentW; }
    public int getContentH() { return contentH; }
}
