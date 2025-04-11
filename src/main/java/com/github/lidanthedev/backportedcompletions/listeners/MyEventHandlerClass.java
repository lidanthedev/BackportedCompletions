package com.github.lidanthedev.backportedcompletions.listeners;

import com.github.lidanthedev.backportedcompletions.events.PacketReceiveEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyEventHandlerClass {
    private static final Logger log = LogManager.getLogger(MyEventHandlerClass.class);

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        log.info("chat message: {}", event.message.getFormattedText());
    }

    @SubscribeEvent
    public void onPacketReceive(final PacketReceiveEvent event) {
        log.info("Received packet: {}", event.getPacket().getClass().getSimpleName());
        // Handle the packet here
    }
}
