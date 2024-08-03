package net.quepierts.simpleanimator.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.fabric.network.FabricNetworkImpl;
import net.quepierts.simpleanimator.fabric.proxy.FabricClientProxy;
import net.quepierts.simpleanimator.fabric.proxy.FabricCommonProxy;

public class SimpleAnimatorFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SimpleAnimator.init(
                FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT,
                FabricClientProxy::setup,
                FabricCommonProxy::setup,
                new FabricNetworkImpl()
        );
    }
}