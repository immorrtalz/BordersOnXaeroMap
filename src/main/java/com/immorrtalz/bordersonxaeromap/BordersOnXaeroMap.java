package com.immorrtalz.bordersonxaeromap;

import com.immorrtalz.bordersonxaeromap.events.data.C2SJoinedWorldEventData;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(BordersOnXaeroMap.MODID)
public class BordersOnXaeroMap
{
	public static final String MODID = "bordersonxaeromap";
	public static final Logger LOGGER = LogUtils.getLogger();
	// private static IEventBus modEventBus; // might be needed later, idk

	// private static ZoneRepository repository;

	public BordersOnXaeroMap(IEventBus modEventBus, Dist dist)
	{
		// BordersOnXaeroMap.modEventBus = modEventBus;

		if (dist.isClient()) BordersOnXaeroMapClient.init(modEventBus);
		else BordersOnXaeroMapServer.init(modEventBus);

		modEventBus.addListener(BordersOnXaeroMap::register);
	}

	/* public static void initializeRepository()
	{
		if (repository != null) return;
		Path savePath = FMLPaths.CONFIGDIR.get().resolve("bordersonxaeromap.json");
		// repository = new ZoneRepository(savePath);
		LOGGER.info("Initialized local zones storage at {}", savePath);
	} */

	/* public static ZoneRepository getRepository() { return repository; } */

	public static void register(final RegisterPayloadHandlersEvent event)
	{
		final PayloadRegistrar registrar = event.registrar("1");

		registrar.playBidirectional(
			C2SJoinedWorldEventData.TYPE,
			C2SJoinedWorldEventData.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				ClientPayloadHandler::handleDataOnMain,
				ServerPayloadHandler::handleDataOnMain
			)
		);
	}

	public class ClientPayloadHandler
	{
		public static void handleDataOnMain(final C2SJoinedWorldEventData data, final IPayloadContext context)
		{
			String playerUuid = data.playerUuid();
			BordersOnXaeroMap.LOGGER.info("Message from the server: `{}`", playerUuid);
		}
	}

	public class ServerPayloadHandler
	{
		public static void handleDataOnMain(final C2SJoinedWorldEventData data, final IPayloadContext context)
		{
			String playerUuid = data.playerUuid();
			BordersOnXaeroMap.LOGGER.info("Message from the client: `{}`", playerUuid);
		}
	}
}