package com.github.lidanthedev.backportedcompletions;

import com.github.lidanthedev.backportedcompletions.listeners.MyEventHandlerClass;
import com.github.lidanthedev.backportedcompletions.config.ModConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "backportedcompletions", useMetadata=true)
public class BackportedCompletions {
    private static final Logger log = LogManager.getLogger(BackportedCompletions.class);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModConfig.load(event.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        log.info("Loaded Backported Completions");
        MinecraftForge.EVENT_BUS.register(new MyEventHandlerClass());
        MinecraftForge.EVENT_BUS.register(this);
    }
}
