package net.quepierts.simpleanimator.core;

import com.mojang.logging.LogUtils;
import net.quepierts.simpleanimator.core.network.INetwork;
import net.quepierts.simpleanimator.core.proxy.ClientProxy;
import net.quepierts.simpleanimator.core.proxy.CommonProxy;
import org.slf4j.Logger;

public abstract class SimpleAnimator {
	public static final String MOD_ID = "simple_animator";
	public static final Logger LOGGER = LogUtils.getLogger();

	private static CommonProxy proxy;
	private static INetwork network;

	public static void init(boolean isClient, Runnable common, Runnable client, INetwork inetwork) {
		network = inetwork;
		proxy = isClient ? new ClientProxy(common, client) : new CommonProxy(common);
		proxy.setup();
	}

	public static CommonProxy getProxy() {
		return proxy;
	}

	public static ClientProxy getClient() {
		return (ClientProxy) proxy;
	}

	public static INetwork getNetwork() {
		return network;
	}

}
