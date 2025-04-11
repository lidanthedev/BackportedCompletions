package com.github.lidanthedev.backportedcompletions.listeners;

import com.github.lidanthedev.backportedcompletions.events.PacketReceiveEvent;
import com.google.common.collect.ObjectArrays;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyEventHandlerClass {
    private static final Logger log = LogManager.getLogger(MyEventHandlerClass.class);

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        log.info("chat message: {}", event.message.getFormattedText());
    }
}
