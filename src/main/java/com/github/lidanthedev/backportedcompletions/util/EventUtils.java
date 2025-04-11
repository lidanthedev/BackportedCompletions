package com.github.lidanthedev.backportedcompletions.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

@UtilityClass
@Log4j2
public class EventUtils {
    public <T extends Event> T postEventSave(final T event) {
        try {
            MinecraftForge.EVENT_BUS.post(event);
        } catch (final Exception e) {
            log.debug("Failed to post event", e);
        }

        return event;
    }

    public void registerEvents(final Object... events) {
        for (final Object event : events) {
            MinecraftForge.EVENT_BUS.register(event);
        }
    }

    public void unregisterEvents(final Object... events) {
        for (final Object event : events) {
            MinecraftForge.EVENT_BUS.unregister(event);
        }
    }
}
