package net.quepierts.simpleanimator.fabric.proxy;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.command.AnimateCommand;
import net.quepierts.simpleanimator.core.command.InteractCommand;
import net.quepierts.simpleanimator.core.network.NetworkPackets;
import net.quepierts.simpleanimator.core.proxy.CommonProxy;
import net.quepierts.simpleanimator.fabric.resource.AnimationReloadListener;

public class FabricCommonProxy {
    private static CommonProxy proxy;
    public static void setup() {
        FabricCommonProxy.proxy = SimpleAnimator.getProxy();

        NetworkPackets.register();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new AnimationReloadListener());

        ServerPlayConnectionEvents.JOIN.register(FabricCommonProxy::onJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(FabricCommonProxy::onDisconnect);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(FabricCommonProxy::onDatapackReloaded);
        CommandRegistrationCallback.EVENT.register(FabricCommonProxy::onCommandRegister);
    }

    private static void onCommandRegister(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        AnimateCommand.register(dispatcher);
        InteractCommand.register(dispatcher);
    }

    private static void onJoin(ServerGamePacketListenerImpl serverGamePacketListener, PacketSender packetSender, MinecraftServer minecraftServer) {
        ServerPlayer player = serverGamePacketListener.getPlayer();
        proxy.getAnimationManager().sync(player);
        proxy.getAnimatorManager().sync(player);
    }

    private static void onDisconnect(ServerGamePacketListenerImpl serverGamePacketListener, MinecraftServer minecraftServer) {
        SimpleAnimator.getProxy().getAnimatorManager().remove(serverGamePacketListener.getPlayer().getUUID());
    }

    private static void onDatapackReloaded(MinecraftServer minecraftServer, CloseableResourceManager closeableResourceManager, boolean success) {
        if (success) {
            SimpleAnimator.getProxy().getAnimationManager().sync(minecraftServer.getPlayerList());
        }
    }
}
