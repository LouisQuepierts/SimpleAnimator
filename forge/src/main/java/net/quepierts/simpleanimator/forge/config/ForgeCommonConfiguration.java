package net.quepierts.simpleanimator.forge.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.quepierts.simpleanimator.core.config.CommonConfiguration;

public class ForgeCommonConfiguration extends CommonConfiguration {
    private static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.BooleanValue SHOULD_SYNC_ANIMATION;
    public static void register(ModLoadingContext context, IEventBus bus) {
        context.registerConfig(ModConfig.Type.COMMON, SPEC);
        bus.addListener(ForgeCommonConfiguration::onLoadConfig);
    }

    private static void onLoadConfig(ModConfigEvent event) {
        CommonConfiguration.syncAnimationToClient = SHOULD_SYNC_ANIMATION.get();
    }

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        SHOULD_SYNC_ANIMATION = builder
                .comment("Whether animations on server will sync to clients")
                .define("shouldSyncAnimation", false);

        SPEC = builder.build();
    }
}
