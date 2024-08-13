package net.quepierts.simpleanimator.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.quepierts.simpleanimator.api.event.ISAEventBus;
import net.quepierts.simpleanimator.core.event.SAEventBusImpl;
import net.quepierts.simpleanimator.core.network.INetwork;
import net.quepierts.simpleanimator.core.proxy.ClientProxy;
import net.quepierts.simpleanimator.core.proxy.CommonProxy;
import org.slf4j.Logger;

public abstract class SimpleAnimator {
	public static final ISAEventBus EVENT_BUS = new SAEventBusImpl();
	public static final String MOD_ID = "simple_animator";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static CommonProxy proxy;
	private static INetwork network;
	private static boolean client;

	public static void init(boolean isClient, Runnable common, Runnable client, INetwork inetwork) {
		network = inetwork;
		proxy = isClient ? new ClientProxy(common, client) : new CommonProxy(common);
		proxy.setup();

		SimpleAnimator.client = isClient;
	}

	public static CommonProxy getProxy() {
		return proxy;
	}

	@Environment(EnvType.CLIENT)
	public static ClientProxy getClient() {
		return (ClientProxy) proxy;
	}

	public static INetwork getNetwork() {
		return network;
	}

	public static boolean isClient() {
		return client;
	}
}
