package net.quepierts.simpleanimator.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.quepierts.simpleanimator.api.animation.Animator;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.packet.AnimatorPlayPacket;
import net.quepierts.simpleanimator.core.network.packet.AnimatorStopPacket;

import java.util.UUID;

public class AnimateCommand {
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_ANIMATION = (context, builder) -> SharedSuggestionProvider.suggestResource(SimpleAnimator.getProxy().getAnimationManager().getAnimationNames(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("animate")
                        .requires(stack -> stack.hasPermission(2))
                        .then(Commands.literal("play").then(Commands.argument("animation", ResourceLocationArgument.id()).suggests(SUGGEST_ANIMATION).executes(AnimateCommand::play)))
                        .then(Commands.literal("stop").executes(AnimateCommand::stop))
        );
    }

    private static int stop(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            UUID uuid = player.getUUID();
            Animator animator = SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(uuid);

            if (animator.isRunning()) {
                SimpleAnimator.getNetwork().sendToAllPlayers(new AnimatorStopPacket(uuid), player);
            }
        }
        return 1;
    }

    private static int play(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
                return 0;

            ResourceLocation location = ResourceLocationArgument.getId(context, "animation");
            UUID uuid = player.getUUID();
            SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(uuid).play(location);
            SimpleAnimator.getNetwork().sendToAllPlayers(new AnimatorPlayPacket(uuid, location), player);
            return 1;
        }
        return 0;
    }
}
