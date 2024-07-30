package net.quepierts.simple_animator.common.command;

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
import net.quepierts.simple_animator.SimpleAnimator;
import net.quepierts.simple_animator.network.ModNetwork;
import net.quepierts.simple_animator.network.packet.InteractInvitePacket;

public class InteractCommand {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_INTERACTION = (context, builder) -> {
        return SharedSuggestionProvider.suggestResource(SimpleAnimator.getInstance().getProxy().getAnimationManager().getInteractionNames(), builder);
    };;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("interact")
                .then(Commands.literal("accept").then(Commands.argument("requester", EntityArgument.player()).executes(InteractCommand::accept)))
                .then(Commands.literal("invite").then(Commands.argument("target", EntityArgument.player())).then(Commands.argument("interaction", ResourceLocationArgument.id()).suggests(SUGGEST_INTERACTION).executes(InteractCommand::invite))));
    }

    private static int invite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            source.sendFailure(Component.translatable("animator.commands.failed.invalid_source"));
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        ResourceLocation location = ResourceLocationArgument.getId(context, "interaction");

        if (player.level() != target.level() || player.distanceToSqr(target) > 64 * 64) {
            return 0;
        }

        ModNetwork.sendToPlayer(new InteractInvitePacket(player.getUUID(), location), target);
        return 1;
    }

    private static int accept(CommandContext<CommandSourceStack> context) {
        return 1;
    }
}
