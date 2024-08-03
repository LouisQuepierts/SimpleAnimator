package net.quepierts.simpleanimator.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.forge.network.ForgeNetworkImpl;
import net.quepierts.simpleanimator.forge.proxy.ForgeClientProxy;
import net.quepierts.simpleanimator.forge.proxy.ForgeCommonProxy;

@Mod(SimpleAnimator.MOD_ID)
public class SimpleAnimatorForge {
    public SimpleAnimatorForge() {
        SimpleAnimator.init(
                FMLEnvironment.dist.isClient(),
                ForgeCommonProxy::setup,
                ForgeClientProxy::setup,
                new ForgeNetworkImpl()
        );
    }
}