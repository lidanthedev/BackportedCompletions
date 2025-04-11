package com.github.lidanthedev.backportedcompletions.mixin;

import com.github.lidanthedev.backportedcompletions.events.PacketReceiveEvent;
import com.github.lidanthedev.backportedcompletions.events.PacketSendEvent;
import com.github.lidanthedev.backportedcompletions.util.EventUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Future;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Shadow
    private INetHandler packetListener;

    @Inject(method = "dispatchPacket", at = @At("HEAD"), cancellable = true)
    private void sendPacketPre(
            final Packet dispatchPacket1,
            final GenericFutureListener<? extends Future<? super Void>>[] dispatchPacket2,
            final CallbackInfo info) {
        final PacketSendEvent.Pre event =
                EventUtils.postEventSave(new PacketSendEvent.Pre(dispatchPacket1, dispatchPacket2));
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "dispatchPacket", at = @At("RETURN"))
    private void sendPacketPost(
            final Packet dispatchPacket1,
            final GenericFutureListener<? extends Future<? super Void>>[] dispatchPacket2,
            final CallbackInfo info) {
        EventUtils.postEventSave(new PacketSendEvent.Post(dispatchPacket1, dispatchPacket2));
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void receivePacketPre(
            final ChannelHandlerContext channelRead1, final Packet channelRead2, final CallbackInfo info) {
        final Event event =
                EventUtils.postEventSave(new PacketReceiveEvent.Pre(this.packetListener, channelRead2, channelRead1));

        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "channelRead0", at = @At("RETURN"))
    private void receivePacketPost(
            final ChannelHandlerContext channelRead1, final Packet channelRead2, final CallbackInfo info) {
        EventUtils.postEventSave(new PacketReceiveEvent.Post(this.packetListener, channelRead2, channelRead1));
    }
}
