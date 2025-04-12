package com.github.lidanthedev.backportedcompletions.config;

import lombok.Data;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

@Data
public class ModConfig {
    public static boolean matchStartOnly = false;
    public static boolean caseSensitive = false;

    private static Configuration config;

    public static void load(File configDir) {
        File configFile = new File(configDir, "backported_completions.cfg");
        config = new Configuration(configFile);

        try {
            config.load();

            matchStartOnly = config.getBoolean("matchStartOnly", Configuration.CATEGORY_GENERAL, false,
                    "Only match suggestions that start with your input instead of anywhere in the string.");

            caseSensitive = config.getBoolean("caseSensitive", Configuration.CATEGORY_GENERAL, false,
                    "Treat suggestions as case-sensitive (e.g., 'Tp' won't match 'tp').");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
