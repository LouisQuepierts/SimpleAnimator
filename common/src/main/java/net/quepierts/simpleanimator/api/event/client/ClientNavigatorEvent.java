package net.quepierts.simpleanimator.api.event.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.api.event.ICancelable;
import net.quepierts.simpleanimator.api.event.SAEvent;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public abstract class ClientNavigatorEvent extends SAEvent {
    protected ClientNavigatorEvent() {}

    public static class Start extends ClientNavigatorEvent implements ICancelable {
        private final Player target;
        private final float forward;
        private final float left;

        public Start(Player target, float forward, float left) {
            this.target = target;
            this.forward = forward;
            this.left = left;
        }

        public Player getTarget() {
            return target;
        }

        public float getForward() {
            return forward;
        }

        public float getLeft() {
            return left;
        }
    }

    public static class End extends ClientNavigatorEvent {
        private final boolean finished;

        public End(boolean finished) {
            this.finished = finished;
        }

        public boolean isFinished() {
            return finished;
        }
    }
}
