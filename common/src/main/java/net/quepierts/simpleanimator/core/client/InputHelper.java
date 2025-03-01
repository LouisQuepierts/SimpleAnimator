package net.quepierts.simpleanimator.core.client;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;

public class InputHelper {
    public static void cancel(ClientInput input) {
        input.forwardImpulse = 0;
        input.leftImpulse = 0;
        input.keyPresses = Input.EMPTY;
    }

    public static void idle(ClientInput input) {
        input.forwardImpulse = 0;
        input.leftImpulse = 0;
        input.keyPresses = new Input(
                false,
                false,
                false,
                false,
                input.keyPresses.jump(),
                input.keyPresses.shift(),
                input.keyPresses.sprint()
        );
    }
}
