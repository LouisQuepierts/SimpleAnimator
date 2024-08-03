package net.quepierts.simpleanimator.fabric.proxy;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.quepierts.simpleanimator.core.command.AnimateCommand;
import net.quepierts.simpleanimator.core.command.InteractCommand;

public class FabricClientProxy {
    public static void setup() {
        CommandRegistrationCallback.EVENT.register(FabricClientProxy::onRegisterCommand);

    }

    private static void onRegisterCommand(CommandDispatcher<CommandSourceStack> dispatcher, boolean b) {
        AnimateCommand.register(dispatcher);
        InteractCommand.register(dispatcher);
    }
}
