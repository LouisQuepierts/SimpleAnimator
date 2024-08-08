package net.quepierts.simpleanimator.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.quepierts.simpleanimator.neoforge.network.NeoForgeNetworkImpl;
import net.quepierts.simpleanimator.neoforge.proxy.NeoForgeClientProxy;
import net.quepierts.simpleanimator.neoforge.proxy.NeoForgeCommonProxy;
import net.quepierts.simpleanimator.core.SimpleAnimator;

@Mod(SimpleAnimator.MOD_ID)
public final class SimpleAnimatorNeoForge {
    public SimpleAnimatorNeoForge() {
        // Run our common setup.
        SimpleAnimator.init(
                FMLEnvironment.dist.isClient(),
                NeoForgeCommonProxy::setup,
                NeoForgeClientProxy::setup,
                new NeoForgeNetworkImpl()
        );
    }
}
