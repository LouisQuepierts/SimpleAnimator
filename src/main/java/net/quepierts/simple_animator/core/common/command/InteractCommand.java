package net.quepierts.simple_animator.core.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.common.PlayerUtils;
import net.quepierts.simple_animator.core.common.animation.InteractionManager;
import net.quepierts.simple_animator.core.network.ModNetwork;
import net.quepierts.simple_animator.core.network.packet.InteractAcceptPacket;
import net.quepierts.simple_animator.core.network.packet.InteractInvitePacket;

public class InteractCommand {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_INTERACTION = (context, builder) -> {
        return SharedSuggestionProvider.suggestResource(SimpleAnimator.getInstance().getProxy().getAnimationManager().getInteractionNames(), builder);
    };;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("interact")
                .then(Commands.literal("accept").then(Commands.argument("requester", EntityArgument.player()).executes(InteractCommand::accept)))
                .then(Commands.literal("invite").then(Commands.argument("target", EntityArgument.player()).then(Commands.argument("interaction", ResourceLocationArgument.id()).suggests(SUGGEST_INTERACTION).executes(InteractCommand::invite)))));
    }

    private static int invite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            source.sendFailure(Component.translatable("animator.commands.failed.invalid_source"));
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");

        if (player == target) {
            source.sendFailure(Component.translatable("animator.commands.failed.same_player"));
            return 0;
        }

        ResourceLocation location = ResourceLocationArgument.getId(context, "interaction");

        if (!PlayerUtils.inSameDimension(player, target) || player.distanceToSqr(target) > 1024) {
            return 0;
        }

        ModNetwork.sendToAllPlayers(new InteractInvitePacket(player.getUUID(), target.getUUID(), location), player);
        return 1;
    }

    private static int accept(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            source.sendFailure(Component.translatable("animator.commands.failed.invalid_source"));
            return 0;
        }

        ServerPlayer player = source.getPlayer();
        ServerPlayer requester = EntityArgument.getPlayer(context, "requester");

        if (player == requester) {
            source.sendFailure(Component.translatable("animator.commands.failed.same_player"));
            return 0;
        }

        InteractionManager.Request request = SimpleAnimator.getInstance().getProxy().getInteractionManager().get(requester.getUUID());

        if (request == null || !request.target().equals(player.getUUID())) {
            source.sendFailure(Component.translatable("animator.commands.failed.nonexistent_request"));
            return 0;
        }

        ModNetwork.sendToAllPlayers(new InteractAcceptPacket(requester.getUUID(), player.getUUID()), player);
        return 1;
    }
}
