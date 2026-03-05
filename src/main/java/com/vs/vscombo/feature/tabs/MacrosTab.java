// В методе executeWithSettings(), внутри цикла for:

String cmd = trimmed;
if (!cmd.startsWith("/")) {
    cmd = "/" + cmd;
}

// FIX: Создаём final-копию для лямбды
final String finalCmd = cmd;
mc.execute(() -> {
    if (mc.player != null) {
        mc.player.sendChatMessage(finalCmd);
    }
});
