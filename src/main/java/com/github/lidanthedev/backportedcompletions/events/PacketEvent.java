package com.github.lidanthedev.backportedcompletions.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class PacketEvent extends Event {
    private Packet packet;
}

