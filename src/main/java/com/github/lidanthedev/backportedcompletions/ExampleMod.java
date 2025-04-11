package com.github.lidanthedev.backportedcompletions;

import com.github.lidanthedev.backportedcompletions.listeners.MyEventHandlerClass;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "backportedcompletions", useMetadata=true)
public class ExampleMod {
    private static final Logger log = LogManager.getLogger(ExampleMod.class);

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        log.info("This is a test mod for backported completions.");
        MinecraftForge.EVENT_BUS.register(new MyEventHandlerClass());
        MinecraftForge.EVENT_BUS.register(this);
    }
}
