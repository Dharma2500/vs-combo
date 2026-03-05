public void executeWithSettings() {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null) {
        VSBaseMod.LOGGER.warn("Cannot execute: player is null");
        return;
    }
    if (mc.getConnection() == null) {
        VSBaseMod.LOGGER.warn("Cannot execute: connection is null");
        return;
    }
    
    int delay = VSMainWindow.lineDelay;
    int timer = VSMainWindow.executionTimer;
    
    new Thread(() -> {
        try {
            long startTime = System.currentTimeMillis();
            int sentCount = 0;
            
            for (String line : lines) {
                if (timer > 0) {
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    if (elapsed >= timer) {
                        VSBaseMod.LOGGER.info("Timer reached {} seconds, stopping execution", timer);
                        break;
                    }
                }
                
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                
                String cmd = trimmed;
                if (!cmd.startsWith("/")) {
                    cmd = "/" + cmd;
                }
                
                // FIX: final variable for lambda
                final String finalCmd = cmd;
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.sendChatMessage(finalCmd);
                    }
                });
                sentCount++;
                
                Thread.sleep(delay);
            }
            
            VSBaseMod.LOGGER.info("Executed {} commands from Macros#{}", sentCount, tabId);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            VSBaseMod.LOGGER.error("Macro execution interrupted");
        }
    }, "MacroExecutor-" + tabId).start();
}
