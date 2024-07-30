package net.quepierts.simple_animator.core;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.quepierts.simple_animator.core.proxy.ClientProxy;
import net.quepierts.simple_animator.core.proxy.CommonProxy;
import org.slf4j.Logger;

@Mod(SimpleAnimator.MODID)
public class SimpleAnimator {
    public static final String MODID = "simple_animator";

    public static final Logger LOGGER = LogUtils.getLogger();

    private static SimpleAnimator instance;

    private CommonProxy proxy;


    public static SimpleAnimator getInstance() {
        return instance;
    }

    public SimpleAnimator() {
        instance = this;

        proxy = FMLEnvironment.dist.isClient() ? new ClientProxy() : new CommonProxy();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        proxy.setup(bus);
    }

    public CommonProxy getProxy() {
        return proxy;
    }

    public ClientProxy getClient() {
        if (!FMLEnvironment.dist.isClient())
            throw new RuntimeException("Client proxy cannot access in server!");
        return (ClientProxy) proxy;
    }
}