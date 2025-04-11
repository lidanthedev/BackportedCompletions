package com.github.lidanthedev.backportedcompletions.events;

import io.netty.channel.ChannelHandlerContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class PacketReceiveEvent extends PacketEvent {
    private final INetHandler packetListener;
    private final ChannelHandlerContext channelHandlerContext;

    PacketReceiveEvent(
            final INetHandler packetListener, final ChannelHandlerContext channelHandlerContext, final Packet packet) {
        super(packet);

        this.packetListener = packetListener;
        this.channelHandlerContext = channelHandlerContext;
    }

    @Cancelable
    public static class Pre extends PacketReceiveEvent {
        public Pre(
                final INetHandler packetListener,
                final Packet packet,
                final ChannelHandlerContext channelHandlerContext) {
            super(packetListener, channelHandlerContext, packet);
        }
    }

    public static class Post extends PacketReceiveEvent {
        public Post(
                final INetHandler packetListener,
                final Packet packet,
                final ChannelHandlerContext channelHandlerContext) {
            super(packetListener, channelHandlerContext, packet);
        }
    }
}
