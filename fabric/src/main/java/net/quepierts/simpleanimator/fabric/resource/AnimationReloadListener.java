package net.quepierts.simpleanimator.fabric.resource;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AnimationReloadListener implements IdentifiableResourceReloadListener {
    private final ResourceLocation ID = new ResourceLocation(SimpleAnimator.MOD_ID, "animation");
    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        return SimpleAnimator.getProxy().getAnimationManager().reload(preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2);
    }
}
