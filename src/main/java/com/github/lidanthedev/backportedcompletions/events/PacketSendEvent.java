package com.github.lidanthedev.backportedcompletions.events;

import java.util.Optional;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PacketSendEvent extends PacketEvent {
    private final GenericFutureListener<? extends Future<? super Void>>[] returnListener;

    PacketSendEvent(final GenericFutureListener<? extends Future<? super Void>>[] returnListener, final Packet packet) {
        super(packet);

        this.returnListener = returnListener != null ? returnListener.clone() : null;
    }

    public Optional<GenericFutureListener<? extends Future<? super Void>>[]> getReturnListener() {
        if (this.returnListener == null) {
            return Optional.empty();
        }

        return Optional.of(this.returnListener.clone());
    }

    @Cancelable
    public static class Pre extends PacketSendEvent {
        public Pre(final Packet packet, final GenericFutureListener<? extends Future<? super Void>>[] returnListener) {
            super(returnListener, packet);
        }
    }

    public static class Post extends PacketSendEvent {
        public Post(final Packet packet, final GenericFutureListener<? extends Future<? super Void>>[] returnListener) {
            super(returnListener, packet);
        }
    }
}
