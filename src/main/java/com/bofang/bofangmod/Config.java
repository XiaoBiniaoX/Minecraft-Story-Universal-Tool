package com.bofang.bofangmod;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    public static Path getConfigPath() {
        return Paths.get("").resolve("config").resolve("binbulid");
    }
    
    public static Path getTextPath() {
        return getConfigPath().resolve("text");
    }
}