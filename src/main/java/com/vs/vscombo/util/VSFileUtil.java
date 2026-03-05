package com.vs.vscombo.util;

import com.vs.vscombo.VSBaseMod;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class VSFileUtil {
    
    public static void saveString(File file, String content) {
        try {
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
                fw.write(content);
            }
        } catch (IOException e) {
            VSBaseMod.LOGGER.error("Failed to save VS data: {}", file.getName(), e);
        }
    }
    
    public static String loadString(File file) {
        if (!file.exists()) return null;
        try (FileReader fr = new FileReader(file, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(fr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        } catch (IOException e) {
            VSBaseMod.LOGGER.error("Failed to load VS data: {}", file.getName(), e);
            return null;
        }
    }
}
