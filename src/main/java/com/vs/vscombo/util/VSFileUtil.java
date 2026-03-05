package com.vs.vscombo.util;

import com.vs.vscombo.VSBaseMod;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class VSFileUtil {
    
    public static void saveString(File file, String content) {
        try {
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            try (OutputStream os = new FileOutputStream(file);
                 Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                writer.write(content);
            }
        } catch (IOException e) {
            VSBaseMod.LOGGER.error("Failed to save VS  {}", file.getName(), e);
        }
    }
    
    public static String loadString(File file) {
        if (!file.exists()) return null;
        try (InputStream is = new FileInputStream(file);
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(reader)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        } catch (IOException e) {
            VSBaseMod.LOGGER.error("Failed to load VS  {}", file.getName(), e);
            return null;
        }
    }
}
